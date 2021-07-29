package com.google.common.truth.extension.generator;

import org.apache.commons.lang3.ClassUtils;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.capitalize;

public class TestCreator {

    public void addTests(JavaClassSource parent, Class<?> classUnderTest) {
        Set<Field> allFields = ReflectionUtils.getAllFields(classUnderTest, x -> true);
//        Set<Field> allFields = ReflectionUtils.getAllMethods(classUnderTest, x -> x.);
//

        for (Field f : allFields) {
            Class<?> type = f.getType();
            if (type.isPrimitive()) {
                addPrimitiveTest(f, parent, classUnderTest);
            } else {
                addCallToDelegateSubject(f, parent, classUnderTest);
            }
        }

        System.out.println(allFields.toString());
    }

    private void addPrimitiveTest(Field f, JavaClassSource parent, Class<?> classUnderTest) {
        Class<?> type = f.getType();
        Class<?> aClass1 = ClassUtils.primitiveToWrapper(type);

        if (aClass1.getSimpleName().contains("boolean"))
            return;

        MethodSource<JavaClassSource> has = parent.addMethod()
                .setName("has" + f.getName())
                .setPublic();


        has.setBody("return check(\"" + f.getName() + "\").that(actual.get" + capitalize(f.getName()) + "());");

        String retrnTypeName = aClass1.getSimpleName() + "Subject";
//        Class<?> aClass = null;
//        try {
//            aClass = ClassUtils.getClass(Truth.class.getClassLoader(), retrnTypeName);
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
        has.setReturnType(retrnTypeName);
        parent.addImport(retrnTypeName);

        //        check("atLeastOffset()").that(actual.highestCommit()).isAtLeast(needleCommit);
    }

    private void addCallToDelegateSubject(Field f, JavaClassSource parent, Class<?> classUnderTest) {

    }
}
