package com.google.common.truth.extension.generator;

import com.google.common.base.Joiner;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;
import org.atteo.evo.inflector.English;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaDocSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;

public class TruthGenerator {

    public <T> String generate(final Class<T> source) throws FileNotFoundException {
        final JavaClassSource javaClass = Roaster.create(JavaClassSource.class);

        String packageName = source.getPackage().getName();
        String sourceName = source.getSimpleName();
        String subjectClassName = sourceName + "Subject";


        javaClass.setPackage(packageName).setName(subjectClassName);

        // extend
        javaClass.extendSuperType(Subject.class);

        // class javadc
        JavaDocSource<JavaClassSource> classDocs = javaClass.getJavaDoc();
        classDocs.setText("Truth Subject for the {@link " + sourceName + "} - extend this class, with your own custom " +
                "assertions.\n\n" +
                "Note that the generated class will change over time, so your edits will be overwritten. " +
                "Or, you can copy the generated code into your project.");
        classDocs.addTagValue("@see", sourceName);

        // requires java 9
        // annotate generated
        // @javax.annotation.Generated(value="")
        // only in @since 1.9, so can't add it programmatically
        // AnnotationSource<JavaClassSource> generated = javaClass.addAnnotation(Generated.class);
        // Can't add it without the value param, see https://github.com/forge/roaster/issues/201
        // AnnotationSource<JavaClassSource> generated = javaClass.addAnnotation("javax.annotation.processing.Generated");
        // generated.addAnnotationValue("truth-generator"); https://github.com/forge/roaster/issues/201

        // constructor
        MethodSource<JavaClassSource> constructor = javaClass.addMethod()
                .setConstructor(true)
                .setProtected();
        constructor.addParameter(FailureMetadata.class, "failureMetadata");
        constructor.addParameter(source, "actual");
        constructor.setBody("super(failureMetadata, actual);");


        // factory accessor
        String returnType = getTypeWithGenerics(Subject.Factory.class, javaClass.getName(), sourceName);
        MethodSource<JavaClassSource> factory = javaClass.addMethod()
                .setName(getFactoryName(source))
                .setPublic()
                .setStatic(true)
                // todo replace with something other than the string method - I suppose it's not possible to do generics type safely
                .setReturnType(returnType)
                .setBody("return " + javaClass.getName() + "::new;");
        JavaDocSource<MethodSource<JavaClassSource>> factoryDocs = factory.getJavaDoc();
        factoryDocs.setText("Returns an assertion builder for a {@link " + sourceName + "} class.");


        // entry point
        MethodSource<JavaClassSource> assertThat = javaClass.addMethod()
                .setName("assertThat")
                .setPublic()
                .setStatic(true)
                .setReturnType(javaClass.getEnclosingType());
        assertThat.addParameter(source, "actual");
        //         return assertAbout(things()).that(actual);
        // add explicit static reference for now - see below
        String entryPointBody = "return Truth.assertAbout(" + factory.getName() + "()).that(actual);";
        assertThat.setBody(entryPointBody);
        javaClass.addImport(Truth.class);
        assertThat.getJavaDoc().setText("Entry point for {@link " + sourceName + "} assertions.");


        // todo add static import for Truth.assertAbout somehow?
//        Import anImport = javaClass.addImport(Truth.class);
//        javaClass.addImport(anImport.setStatic(true));
//        javaClass.addImport(new Im)

        // output
        String classSource = javaClass.toString();
        try (PrintWriter out = new PrintWriter(getFileName(javaClass))) {
            out.println(classSource);
        }

        return classSource;
    }

    private String getTypeWithGenerics(final Class<?> factoryClass, String... classes) {
        String genericsList = Joiner.on(", ").skipNulls().join(classes);
        final String generics = new StringBuilder("<>").insert(1, genericsList).toString();
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

    private <T> String getFactoryName(final Class<T> source) {
        String simpleName = source.getSimpleName();
        String plural = English.plural(simpleName);
        String normal = toLowerCaseFirstLetter(plural);
        return normal;
    }

    private String toLowerCaseFirstLetter(final String plural) {
        return plural.substring(0, 1).toLowerCase() + plural.substring(1);
    }
}
