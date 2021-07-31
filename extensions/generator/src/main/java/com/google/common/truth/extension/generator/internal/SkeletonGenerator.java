package com.google.common.truth.extension.generator.internal;

import com.google.common.base.Joiner;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;
import com.google.common.truth.extension.generator.internal.model.*;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.*;
import org.reflections.ReflectionUtils;

import com.google.common.flogger.FluentLogger;

import javax.annotation.processing.Generated;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.logging.Level;

import static com.google.common.truth.extension.generator.internal.Utils.getFactoryName;
import static com.google.common.truth.extension.generator.internal.Utils.writeToDisk;
import static java.lang.String.format;
import static java.util.Optional.empty;

/**
 * @author Antony Stubbs
 */
public class SkeletonGenerator implements SkeletonGeneratorAPI {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    @Override
    public String maintain(Class source, Class userAndGeneratedMix) {
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public Optional<Object> threeLayerSystem(Class<?> source, Class<?> usersMiddleClass) throws FileNotFoundException {
        if (checkSource(source))
            return empty();

        // make parent - boiler plate access
        ParentClass parent = createParent(source);

        String factoryMethodName = getFactoryName(source);

        // make child - client code entry point
        JavaClassSource child = createChild(parent, usersMiddleClass.getName(), source, factoryMethodName);

        MiddleClass middleClass = new MiddleClass(null, null, usersMiddleClass);

        return Optional.of(new ThreeSystem(source, parent, middleClass, child));
    }

    @Override
    public Optional<ThreeSystem> threeLayerSystem(Class<?> source, Optional<String> targetPackageName) {
        if (checkSource(source))
            return empty();

        ParentClass parent = createParent(source);

        // todo try to see if class already exists first, user may already have a written one and not know
        MiddleClass middle = createMiddlePlaceHolder(parent.generated, source);

        JavaClassSource child = createChild(parent, middle.generated.getQualifiedName(), source, middle.factoryMethod.getName());

        return Optional.of(new ThreeSystem(source, parent, middle, child));
    }

    private boolean checkSource(final Class<?> source) {
        if (source.isAnonymousClass()) {
            logger.at(Level.FINE).log("Skipping anonymous class %s", source);
            return true;
        }

        String simpleName = source.getSimpleName();
        if (simpleName.contains("Builder")) {
            logger.at(Level.FINE).log("Skipping builder class %s", source);
            return true;
        }

        if (isTestClass(source)) {
            logger.at(Level.FINE).log("Skipping a test class %s", source);
            return true;
        }

        return false;
    }

    /**
     * If any method is annotated with something with Test in it, then assume it's a test class
     */
    private boolean isTestClass(final Class<?> source) {
        boolean hasTestAnnotatedMethod = !ReflectionUtils.getMethods(source,
                x -> Arrays.stream(x.getAnnotations())
                        .anyMatch(y -> y.annotationType()
                                .getSimpleName().contains("Test"))).isEmpty();
        boolean nameEndsInTest = source.getSimpleName().endsWith("Test");
        return hasTestAnnotatedMethod || nameEndsInTest;
    }

    private MiddleClass createMiddlePlaceHolder(JavaClassSource parent, Class source) {
        JavaClassSource middle = Roaster.create(JavaClassSource.class);
        middle.setName(getSubjectName(source.getSimpleName()));
        middle.setPackage(parent.getPackage());
        middle.extendSuperType(parent);
        JavaDocSource<JavaClassSource> jd = middle.getJavaDoc();
        jd.setText("Optionally move this class into source control, and add your custom assertions here.\n\n" +
                "<p>If the system detects this class already exists, it won't attempt to generate a new one. Note that " +
                "if the base skeleton of this class ever changes, you won't automatically get it updated.");
        jd.addTagValue("@see", parent.getName());

        addConstructor(source, middle, false);

        MethodSource factory = addFactoryAccesor(source, middle, source.getSimpleName());

        addGeneratedMarker(middle);

        writeToDisk(middle);
        return new MiddleClass(middle, factory, null);
    }

    private <T> ParentClass createParent(Class<T> source) {
        JavaClassSource parent = Roaster.create(JavaClassSource.class);
        String sourceName = source.getSimpleName();
        String parentName = getSubjectName(sourceName + "Parent");
        parent.setName(parentName);

        addPackageSuperAndAnnotation(parent, source);

        addClassJavaDoc(parent, sourceName);

        addActualField(source, parent);

        addConstructor(source, parent, true);

        writeToDisk(parent);
        return new ParentClass(parent);
    }

    private <T> void addPackageSuperAndAnnotation(final JavaClassSource javaClass, final Class<T> source) {
        addPackageSuperAndAnnotation(javaClass, source.getPackage().getName(), getSubjectName(source.getSimpleName()));
    }

    private <T> JavaClassSource createChild(ParentClass parent,
                                            String usersMiddleClassName,
                                            Class<T> source,
                                            String factoryMethodName) {
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

        addGeneratedMarker(child);

        writeToDisk(child);
        return child;
    }

//    private <T> void registerManagedClass(Class<T> sourceClass, JavaClassSource gengeratedClass) {
//        managedSubjects.add(new ManagedClassSet(sourceClass, gengeratedClass));
//    }

    @Override
    public <T> String combinedSystem(Class<T> source) {
        JavaClassSource javaClass = Roaster.create(JavaClassSource.class);

//        JavaClassSource handWrittenExampleCode = Roaster.parse(JavaClassSource.class, handWritten);

//        registerManagedClass(source, handWrittenExampleCode);

//        javaClass = handWrittenExampleCode;

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

        addGeneratedMarker(javaClass);
    }

    private void addGeneratedMarker(final JavaClassSource javaClass) {
        // requires java 9
        // annotate generated
        // @javax.annotation.Generated(value="")
        // only in @since 1.9, so can't add it programmatically
        AnnotationSource<JavaClassSource> generated = javaClass.addAnnotation(Generated.class);
        generated.setStringValue("truth-generator");
        // Can't add it without the value param, see https://github.com/forge/roaster/issues/201
        // AnnotationSource<JavaClassSource> generated = javaClass.addAnnotation("javax.annotation.processing.Generated");
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

}
