package com.google.common.truth.extension.generator;

import com.google.common.base.Joiner;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;
import org.atteo.evo.inflector.English;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.Method;
import org.jboss.forge.roaster.model.source.Import;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaDocSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TruthGenerator {

    private final String basePackage;

    public TruthGenerator(String basePackage) {
        this.basePackage = basePackage;
    }

    private final List<ManagedClassSet> managedSubjects = new ArrayList<>();
    private final List<JavaClassSource> children = new ArrayList<>();

    /**
     * Takes a user maintained source file, and adds boiler plate that is missing. If aggressively skips parst if it
     * thinks the user has overridden something.
     */
    public String maintain(Class source, Class userAndGeneratedMix) {
        throw new IllegalStateException();
    }

    /**
     * Uses an optional three layer system to manage the Subjects.
     * <ol>
     * <li> The top layer extends Subject and stores the actual and factory.
     * <li> The second layer is the user's code - they extend the first layer, and add their custom assertion methods there.
     * <li> The third layer extends the user's class, and stores the generated entry points, so that users's get's access
     * to all three layers, by only importing the bottom layer.
     * </ol>
     * <p>
     * For any source class that doesn't have a user created middle class, an empty one will be generated, that the user
     * can copy into their source control. If it's used, a helpful message will be written to the console, prompting the
     * user to do so. These messages can be globally disabled.
     * <p>
     * This way there's no complexity with mixing generated and user written code, but at the cost of 2 extra classes
     * per source. While still allowing the user to leverage the full code generation system but maintaining their own extensions
     * with clear separation from the code generation.
     */
    public void threeLayerSystem(Class<?> source, Class<?> usersMiddleClass) throws FileNotFoundException {
        // make parent - boiler plate access
        ParentClass parent = createParent(source);

        String factoryMethodName = getFactoryName(source);

        // make child - client code entry point
        createChild(parent, usersMiddleClass.getName(), source, factoryMethodName);
    }

    /**
     * Create the place holder middle class, for optional copying into source code
     *
     * @see #threeLayerSystem(Class, Class)
     */
    public void threeLayerSystem(Class source) throws FileNotFoundException {
        ParentClass parent = createParent(source);

        // todo try to see if class already exists first, user may already have a written one and not know
        MiddleClass middle = createMiddlePlaceHolder(parent.generated, source);

        createChild(parent, middle.generated.getQualifiedName(), source, middle.factoryMethod.getName());
    }

    private MiddleClass createMiddlePlaceHolder(JavaClassSource parent, Class source) throws FileNotFoundException {
        JavaClassSource middle = Roaster.create(JavaClassSource.class);
        middle.setName(getSubjectName(source.getSimpleName()));
        middle.setPackage(parent.getPackage());
        middle.extendSuperType(parent);
        JavaDocSource<JavaClassSource> jd = middle.getJavaDoc();
        jd.setText("Optionally move this class into source control, and add your custom assertions here.");
        jd.addTagValue("@see", parent.getName());

        addConstructor(source, middle, false);

        MethodSource factory = addFactoryAccesor(source, middle, source.getSimpleName());

        writeToDisk(middle);
        return new MiddleClass(middle, factory);
    }

    class AClass {
        final JavaClassSource generated;

        AClass(final JavaClassSource generated) {
            this.generated = generated;
        }
    }

    class ParentClass extends AClass {
        ParentClass(JavaClassSource generated) {
            super(generated);
        }
    }

    class MiddleClass extends AClass {
        final MethodSource<JavaClassSource> factoryMethod;

        MiddleClass(JavaClassSource generated, MethodSource<JavaClassSource> factoryMethod) {
            super(generated);
            this.factoryMethod = factoryMethod;
        }
    }

    /**
     * Having collected together all the access points, creates one large class filled with access points to all of
     * them.
     * <p>
     * The overall access will throw an error if any middle classes don't correctly extend their parent.
     */
    public void createOverallAccessPoints() throws FileNotFoundException {
        JavaClassSource overallAccess = Roaster.create(JavaClassSource.class);
        overallAccess.setName("ManagedTruth");
        overallAccess.getJavaDoc()
                .setText("Single point of access for all managed Subjects.");
        overallAccess.setPublic()
                .setPackage(getManagedClassesBasePackage());

        // brute force
        for (JavaClassSource j : children) {
            List<MethodSource<JavaClassSource>> methods = j.getMethods();
            for (Method m : methods) {
                overallAccess.addMethod(m);
            }
            // this seems like overkill, but at least in the child style case, there's very few imports - even
            // none extra at all (aside from wild card vs specific methods).
            List<Import> imports = j.getImports();
            for (Import i : imports) {
                overallAccess.addImport(i);
            }
        }

        writeToDisk(overallAccess);
    }

    private String getManagedClassesBasePackage() {
        return basePackage;
    }

    private <T> ParentClass createParent(Class<T> source) throws FileNotFoundException {
        JavaClassSource parent = Roaster.create(JavaClassSource.class);
        String sourceName = source.getSimpleName();
        String parentName = getSubjectName(sourceName + "Parent");
        parent.setName(parentName);

        addPackageSuperAndAnnotation(parent, source);

        addClassJavaDoc(parent, sourceName);

        addActualField(source, parent);

        addConstructor(source, parent, true);

        TestCreator testCreator = new TestCreator();
        testCreator.addTests(parent, source);

        writeToDisk(parent);
        return new ParentClass(parent);
    }

    private <T> void addPackageSuperAndAnnotation(final JavaClassSource javaClass, final Class<T> source) {
        addPackageSuperAndAnnotation(javaClass, source.getPackage().getName(), getSubjectName(source.getSimpleName()));
    }

    private <T> JavaClassSource createChild(ParentClass parent,
                                            String usersMiddleClassName,
                                            Class<T> source,
                                            String factoryMethodName) throws FileNotFoundException {
        // todo if middle doesn't extend parent, warn

        JavaClassSource child = Roaster.create(JavaClassSource.class);
        child.setName(getSubjectName(source.getSimpleName() + "Child"));
        child.setPackage(parent.generated.getPackage());
        JavaDocSource<JavaClassSource> javaDoc = child.getJavaDoc();
        javaDoc.setText("Entry point for assertions for @{" + source.getSimpleName() + "}. Import the static accessor methods from this class and use them.\n" +
                "Combines the generated code from {@" + parent.generated.getName() + "}and the user code from {@" + usersMiddleClassName + "}.");
        javaDoc.addTagValue("@see", source.getName());
        javaDoc.addTagValue("@see", usersMiddleClassName);
        javaDoc.addTagValue("@see", parent.generated.getName());

        addAccessPoints(source, child, factoryMethodName, usersMiddleClassName);

        writeToDisk(child);
        children.add(child);
        return child;
    }

    private <T> void registerManagedClass(Class<T> sourceClass, JavaClassSource gengeratedClass) {
        managedSubjects.add(new ManagedClassSet(sourceClass, gengeratedClass));
    }

    class ManagedClassSet<T> {
        final Class<T> sourceClass;
        final JavaClassSource generatedClass;

        ManagedClassSet(final Class<T> sourceClass, final JavaClassSource generatedClass) {
            this.sourceClass = sourceClass;
            this.generatedClass = generatedClass;
        }
    }

    public <T> String generate(Class<T> source) throws FileNotFoundException {
        JavaClassSource javaClass = Roaster.create(JavaClassSource.class);

        JavaClassSource handWrittenExampleCode = Roaster.parse(JavaClassSource.class, handWritten);

        registerManagedClass(source, handWrittenExampleCode);

        javaClass = handWrittenExampleCode;

        String packageName = source.getPackage().getName();
        String sourceName = source.getSimpleName();
        String subjectClassName = getSubjectName(sourceName);


        addPackageSuperAndAnnotation(javaClass, packageName, subjectClassName);

        addClassJavaDoc(javaClass, sourceName);

        addActualField(source, javaClass);

        addConstructor(source, javaClass, true);

        MethodSource<JavaClassSource> factory = addFactoryAccesor(source, javaClass, sourceName);

        addAccessPoints(source, javaClass, factory.getName(), javaClass.getQualifiedName());

        // todo add static import for Truth.assertAbout somehow?
//        Import anImport = javaClass.addImport(Truth.class);
//        javaClass.addImport(anImport.setStatic(true));
//        javaClass.addImport(new Im)

        String classSource = writeToDisk(javaClass);

        return classSource;
    }

    private String getSubjectName(final String sourceName) {
        return sourceName + "Subject";
    }

    private <T> void addAccessPoints(Class<T> source, JavaClassSource javaClass,
                                     String factoryMethod,
                                     String factoryContainerQualifiedName) {
        MethodSource<JavaClassSource> assertThat = addAssertThat(source, javaClass, factoryMethod, factoryContainerQualifiedName);

        addAssertTruth(source, javaClass, assertThat);
    }

    private void addPackageSuperAndAnnotation(JavaClassSource javaClass, String packageName, String subjectClassName) {
        javaClass.setPackage(packageName);

        // extend
        javaClass.extendSuperType(Subject.class);

        // requires java 9
        // annotate generated
        // @javax.annotation.Generated(value="")
        // only in @since 1.9, so can't add it programmatically
        // AnnotationSource<JavaClassSource> generated = javaClass.addAnnotation(Generated.class);
        // Can't add it without the value param, see https://github.com/forge/roaster/issues/201
        // AnnotationSource<JavaClassSource> generated = javaClass.addAnnotation("javax.annotation.processing.Generated");
        // generated.addAnnotationValue("truth-generator"); https://github.com/forge/roaster/issues/201
    }

    private void addClassJavaDoc(JavaClassSource javaClass, String sourceName) {
        // class javadc
        JavaDocSource<JavaClassSource> classDocs = javaClass.getJavaDoc();
        if (classDocs.getFullText().isEmpty()) {
            classDocs.setText("Truth Subject for the {@link " + sourceName + "}." +
                    "\n\n" +
                    "Note that this class is generated / managed, and will change over time. So any changes you might " +
                    "make will be overwritten.");
            classDocs.addTagValue("@see", sourceName);
            classDocs.addTagValue("@see", getSubjectName(sourceName));
            classDocs.addTagValue("@see", getSubjectName(sourceName + "Child"));
        }
    }

    private String writeToDisk(JavaClassSource javaClass) throws FileNotFoundException {
        String classSource = javaClass.toString();
        try (PrintWriter out = new PrintWriter(getFileName(javaClass))) {
            out.println(classSource);
        }
        return classSource;
    }

    private <T> void addAssertTruth(Class<T> source, JavaClassSource javaClass, MethodSource<JavaClassSource> assertThat) {
        String name = "assertTruth";
        if (!containsMethodCalled(javaClass, name)) {
            // convenience entry point when being mixed with other "assertThat" assertion libraries
            MethodSource<JavaClassSource> assertTruth = javaClass.addMethod()
                    .setName(name)
                    .setPublic()
                    .setStatic(true)
                    .setReturnType(assertThat.getReturnType());
            assertTruth.addParameter(source, "actual");
            assertTruth.setBody("return " + assertThat.getName() + "(actual);");
            assertTruth.getJavaDoc().setText("Convenience entry point for {@link " + source.getSimpleName() + "} assertions when being " +
                    "mixed with other \"assertThat\" assertion libraries.")
                    .addTagValue("@see", "#assertThat");
        }
    }

    private <T> MethodSource<JavaClassSource> addAssertThat(Class<T> source,
                                                            JavaClassSource javaClass,
                                                            String factoryMethodName,
                                                            String factoryContainerQualifiedName) {
        String methodName = "assertThat";
        if (containsMethodCalled(javaClass, methodName)) {
            return getMethodCalled(javaClass, methodName);
        } else {
            // entry point
            MethodSource<JavaClassSource> assertThat = javaClass.addMethod()
                    .setName(methodName)
                    .setPublic()
                    .setStatic(true)
                    .setReturnType(factoryContainerQualifiedName);
            assertThat.addParameter(source, "actual");
            //         return assertAbout(things()).that(actual);
            // add explicit static reference for now - see below
            javaClass.addImport(factoryContainerQualifiedName + ".*")
                    .setStatic(true);
            String entryPointBody = "return Truth.assertAbout(" + factoryMethodName + "()).that(actual);";
            assertThat.setBody(entryPointBody);
            javaClass.addImport(Truth.class);
            assertThat.getJavaDoc().setText("Entry point for {@link " + source.getSimpleName() + "} assertions.");
            return assertThat;
        }
    }

    private MethodSource<JavaClassSource> getMethodCalled(JavaClassSource javaClass, String methodName) {
        return javaClass.getMethods().stream().filter(x -> x.getName().equals(methodName)).findFirst().get();
    }

    private <T> MethodSource<JavaClassSource> addFactoryAccesor(Class<T> source, JavaClassSource javaClass, String sourceName) {
        String factoryName = getFactoryName(source);
        if (containsMethodCalled(javaClass, factoryName)) {
            return getMethodCalled(javaClass, factoryName);
        } else {
            // factory accessor
            String returnType = getTypeWithGenerics(Subject.Factory.class, javaClass.getName(), sourceName);
            MethodSource<JavaClassSource> factory = javaClass.addMethod()
                    .setName(factoryName)
                    .setPublic()
                    .setStatic(true)
                    // todo replace with something other than the string method - I suppose it's not possible to do generics type safely
                    .setReturnType(returnType)
                    .setBody("return " + javaClass.getName() + "::new;");
            JavaDocSource<MethodSource<JavaClassSource>> factoryDocs = factory.getJavaDoc();
            factoryDocs.setText("Returns an assertion builder for a {@link " + sourceName + "} class.");
            return factory;
        }
    }

    private boolean containsMethodCalled(JavaClassSource javaClass, String factoryName) {
        return javaClass.getMethods().stream().anyMatch(x -> x.getName().equals(factoryName));
    }

    private <T> void addConstructor(Class<T> source, JavaClassSource javaClass, boolean setActual) {
        if (!javaClass.getMethods().stream().anyMatch(x -> x.isConstructor())) {
            // constructor
            MethodSource<JavaClassSource> constructor = javaClass.addMethod()
                    .setConstructor(true)
                    .setProtected();
            constructor.addParameter(FailureMetadata.class, "failureMetadata");
            constructor.addParameter(source, "actual");
            StringBuilder sb = new StringBuilder("super(failureMetadata, actual);\n");
            if (setActual)
                sb.append("this.actual = actual;");
            constructor.setBody(sb.toString());
        }
    }

    private <T> void addActualField(Class<T> source, JavaClassSource javaClass) {
        String fieldName = "actual";
        if (javaClass.getField(fieldName) == null) {
            // actual field
            javaClass.addField()
                    .setProtected()
                    .setType(source)
                    .setName(fieldName)
                    .setFinal(true);
        }
    }

    private String getTypeWithGenerics(Class<?> factoryClass, String... classes) {
        String genericsList = Joiner.on(", ").skipNulls().join(classes);
        String generics = new StringBuilder("<>").insert(1, genericsList).toString();
        return factoryClass.getSimpleName() + generics;
    }

    private String getFileName(JavaClassSource javaClass) {
        String directoryName = getDirectoryName(javaClass);
        File dir = new File(directoryName);
        if (!dir.exists()) {
            boolean mkdir = dir.mkdirs();
        }
        return directoryName + javaClass.getName() + ".java";
    }

    private String getDirectoryName(JavaClassSource javaClass) {
        String parent = Paths.get("").toAbsolutePath().toString();
        String packageName = javaClass.getPackage();
        String packageNameDir = packageName.replace(".", "/");
        return parent + "/target/generated-test-sources/" + packageNameDir + "/";
    }

    private <T> String getFactoryName(Class<T> source) {
        String simpleName = source.getSimpleName();
        String plural = English.plural(simpleName);
        String normal = toLowerCaseFirstLetter(plural);
        return normal;
    }

    private String toLowerCaseFirstLetter(String plural) {
        return plural.substring(0, 1).toLowerCase() + plural.substring(1);
    }

    String handWritten = "package io.confluent.csid.utils;\n" +
            "\n" +
            "import com.google.common.truth.Subject;\n" +
            "import com.google.common.truth.FailureMetadata;\n" +
            "import com.google.common.truth.Truth;\n" +
            "import io.confluent.parallelconsumer.model.CommitHistory;\n" +
            "import io.confluent.parallelconsumer.truth.CommitHistorySubject;\n" +
            "import org.apache.kafka.clients.consumer.OffsetAndMetadata;\n" +
            "import org.apache.kafka.common.TopicPartition;\n" +
            "\n" +
            "import java.util.List;\n" +
            "import java.util.Map;\n" +
            "import java.util.concurrent.CopyOnWriteArrayList;\n" +
            "import java.util.stream.Collectors;\n" +
            "\n" +
            "import static io.confluent.parallelconsumer.truth.CommitHistorySubject.commitHistories;\n" +
            "\n" +
//            "/**\n" +
//            " * Truth Subject for the {@link LongPollingMockConsumer} - extend this class,\n" +
//            " * with your own custom assertions.\n" +
//            " * \n" +
//            " * Note that the generated class will change over time, so your edits will be\n" +
//            " * overwritten. Or, you can copy the generated code into your project.\n" +
//            " * \n" +
//            " * @see LongPollingMockConsumer\n" +
            "/** my own docs\n" +
            " */\n" +
            "public class LongPollingMockConsumerSubject extends Subject {\n" +
            "\n" +
//            "\tprotected LongPollingMockConsumer actual;\n" +
//            "\n" +
//            "\tprotected LongPollingMockConsumerSubject(FailureMetadata failureMetadata,\n" +
//            "\t\t\tio.confluent.csid.utils.LongPollingMockConsumer actual) {\n" +
//            "\t\tsuper(failureMetadata, actual);\n" +
//            "\t\tthis.actual = actual;\n" +
//            "\t}\n" +
//            "\n" +
//            "\t/**\n" +
//            "\t * Returns an assertion builder for a {@link LongPollingMockConsumer} class.\n" +
//            "\t */\n" +
//            "\tpublic static Factory<LongPollingMockConsumerSubject, LongPollingMockConsumer> longPollingMockConsumers() {\n" +
//            "\t\treturn LongPollingMockConsumerSubject::new;\n" +
//            "\t}\n" +
//            "\n" +
//            "\t/**\n" +
//            "\t * Entry point for {@link LongPollingMockConsumer} assertions.\n" +
//            "\t */\n" +
//            "\tpublic static LongPollingMockConsumerSubject assertThat(io.confluent.csid.utils.LongPollingMockConsumer actual) {\n" +
//            "\t\treturn Truth.assertAbout(longPollingMockConsumers()).that(actual);\n" +
//            "\t}\n" +
//            "\n" +
//            "\t/**\n" +
//            "\t * Convenience entry point for {@link LongPollingMockConsumer} assertions when\n" +
//            "\t * being mixed with other \"assertThat\" assertion libraries.\n" +
//            "\t * \n" +
//            "\t * @see #assertThat\n" +
//            "\t */\n" +
//            "\tpublic static LongPollingMockConsumerSubject assertTruth(io.confluent.csid.utils.LongPollingMockConsumer actual) {\n" +
//            "\t\treturn assertThat(actual);\n" +
//            "\t}\n" +
            "\n" +
            "\tpublic CommitHistorySubject hasCommittedToPartition(TopicPartition tp) {\n" +
            "\t\tisNotNull();\n" +
            "\t\tCopyOnWriteArrayList<Map<TopicPartition, OffsetAndMetadata>> commits = actual.getCommitHistoryInt();\n" +
            "\t\tList<OffsetAndMetadata> collect = commits.stream()\n" +
            "\t\t\t\t.filter(x -> x.containsKey(tp))\n" +
            "\t\t\t\t.map(x -> x.get(tp))\n" +
            "\t\t\t\t.collect(Collectors.toList());\n" +
            "\t\tCommitHistory commitHistory = new CommitHistory(collect);\n" +
            "\t\treturn check(\"hasCommittedToPartition(%s)\", tp).about(commitHistories()).that(commitHistory);\n" +
            "\t}\n" +
            "\n" +
            "void somethingElse(){}\n" +
            "}\n";


}
