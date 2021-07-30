package com.google.common.truth.extension.generator;

import com.google.common.collect.Lists;
import com.google.common.truth.Subject;
import org.apache.commons.lang3.ClassUtils;
import org.jboss.forge.roaster.model.source.Import;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.reflections.ReflectionUtils.*;

public class TestGenerator {

    private final Map<String, Class> subjects;

    public TestGenerator() {
        Reflections reflections = new Reflections("com.google.common.truth", "io.confluent");
        Set<Class<? extends Subject>> subTypes = reflections.getSubTypesOf(Subject.class);

        Map<String, Class> maps = new HashMap<>();
        subTypes.stream().forEach(x -> maps.put(x.getSimpleName(), x));
        this.subjects = maps;
    }


    public void addTests(JavaClassSource parent, Class<?> classUnderTest) {
        Set<Field> allFields = ReflectionUtils.getAllFields(classUnderTest, x -> true);

        Set<Method> getters = ReflectionUtils.getAllMethods(classUnderTest,
                withModifier(Modifier.PUBLIC), withPrefix("get"), withParametersCount(0));

        Set<Method> issers = ReflectionUtils.getAllMethods(classUnderTest,
                withModifier(Modifier.PUBLIC), withPrefix("is"), withParametersCount(0));

        getters.addAll(issers);

        //
        for (Method method : getters) {
            addPrimitiveTest(method, parent, classUnderTest);
        }

        try {
            TruthGenerator.writeToDisk(parent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void addPrimitiveTest(Method f, JavaClassSource generated, Class<?> classUnderTest) {
        Class<?> type = f.getReturnType();
        Class<?> aClass1 = ClassUtils.primitiveToWrapper(type);

        Optional<? extends Class> subjectForType = getSubjectForType(type);

        // no subject to chain
        // todo needs two passes - one to generate the custom classes, then one to use them in other classes
        // should generate all base classes first, then run the test creator pass afterwards
        if (subjectForType.isEmpty()) {
            System.out.println("Cant find subject for " + type);
            // todo log
            return;
        }

        Class subjectClass = subjectForType.get();

        // todo add versions with and with the get
        String prefix = (type.getSimpleName().contains("boolean")) ? "is" : "";
        MethodSource<JavaClassSource> has = generated.addMethod()
                .setName(prefix + f.getName())
                .setPublic();

        StringBuilder body = new StringBuilder("isNotNull();\n");
        String check = "return check(\"" + f.getName() + "\")";
        body.append(check);

        // todo use qualified names
        // todo add support truth8 extensions - optional etc
        // todo try generatin classes for DateTime pakages, like Instant and Duration
        // todo this is of course too aggresive
        List<String> specials = Lists.newArrayList("String", "BigDecimal", "Iterable", "List");

        boolean isCoveredByNonPrimitiveStandardSubjects = specials.contains(type.getSimpleName());
        boolean notPrimitive = !type.isPrimitive();
        if (notPrimitive && !isCoveredByNonPrimitiveStandardSubjects) {
            // need to get the Subject instance using about
            // return check("hasCommittedToPartition(%s)", tp).about(commitHistories()).that(commitHistory);
            String aboutName;
//            Set<Method> factoryPotentials = getMethods(subjectClass, x ->
//                    !x.getName().startsWith("assert") // the factory method won't be the assert methods
//                    && !x.getName().startsWith("lambda") // the factory method won't be the assert methods
//            );
//            if (factoryPotentials.isEmpty()) {
            aboutName = TruthGenerator.getFactoryName(type); // take a guess
//            } else {
//                Method method = factoryPotentials.stream().findFirst().get();
//                aboutName = method.getName();
//            }
            body.append(format(".about(%s())", aboutName));

            // import
            Optional<Class> factoryContainer = this.subjects.values().parallelStream()
                    .filter(classes -> Arrays.stream(classes.getMethods())
                            .anyMatch(methods -> methods.getName().equals(aboutName)))
                    .findFirst();
            if (factoryContainer.isPresent()) {
                Class container = factoryContainer.get();
                Import anImport = generated.addImport(container);
                String name = container.getCanonicalName() + "." + aboutName;
                anImport.setName(name) // todo better way to do static method import?
                        .setStatic(true);
            } else
                System.err.println(format("Can't find container for method %s", aboutName));
        }

//        String methodPrefix = (type.getSimpleName().contains("boolean")) ? "is" : "get";
//        body.append(".that(actual." + methodPrefix + capitalize(f.getName()) + "());");
        body.append(format(".that(actual.%s());", f.getName()));

        has.setBody(body.toString());

        has.setReturnType(subjectClass);
        generated.addImport(subjectClass);
    }

    private Optional<? extends Class> getSubjectForType(final Class<?> type) {
        String name;
        if (type.isPrimitive()) {
            Class<?> wrapped = ClassUtils.primitiveToWrapper(type);
            name = wrapped.getSimpleName();
        } else {
            name = type.getSimpleName();
        }
        Optional<? extends Class> subject = getSubjectFromString(name);

        if (subject.isEmpty()) {
            if (Iterable.class.isAssignableFrom(type)) {
                subject = getSubjectForType(Iterable.class);
            }
        }
        return subject;
    }

    private Optional<? extends Class> getSubjectFromString(final String name) {
        return Optional.ofNullable(this.subjects.get(name + "Subject"));
    }

}
