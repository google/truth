package com.google.common.truth;

import android.view.View;
import android.view.animation.Animation;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.truth.AndroidTruth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * @author Kevin Leigh Crain
 */
@RunWith(JUnit4.class)
public class ViewSubjectTest {

    /**
     * {@link #generateSuccessAssertionTests()} ()}
     */
    private static final String DUMMY_STRING = "DUMMY_STRING";

    @Mock
    View view;
    View viewSpy;

    /**
     * {@link #generateSuccessAssertionTests()} ()}
     */
    private Random randomGenerator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        viewSpy = spy(view);

        randomGenerator = new Random(99);
    }

    @Test
    public void testHasAlpha() {
        float alpha = new Random(99).nextFloat();
        assertThat(whenReturn(viewSpy.getAlpha(), alpha))
                .hasAlpha(alpha);
    }

    @Test
    public void testHasAlphaFailure() {
        try {
            assertThat(whenReturn(viewSpy.getAlpha(), new Random(99).nextFloat()))
                    .hasAlpha(101);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <view> has alpha: 101.0, actual: " + viewSpy.getAlpha());
        }
    }

    @Test
    public void testHasAnimation() {
        Animation animation = mock(Animation.class);
        assertThat(whenReturn(viewSpy.getAnimation(), animation))
                .hasAnimation(animation);
    }

    @Test
    public void testHasAnimationFailure() {
        Animation animation = mock(Animation.class);
        Animation expect = mock(Animation.class);
        try {
            assertThat(whenReturn(viewSpy.getAnimation(), animation))
                    .hasAnimation(expect);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <Mock for Animation, hashCode: "
                    + animation.hashCode() + "> is the same instance as <Mock for Animation, hashCode: "
                    + expect.hashCode() + ">");
        }
    }

    @Test
    public void testIsVisible() {
        assertThat(whenReturn(viewSpy.getVisibility(), View.VISIBLE))
                .isVisible();
    }

    @Test
    public void testIsNotVisible() {
        assertThat(whenReturn(viewSpy.getVisibility(), View.INVISIBLE))
                .isNotVisible();
        assertThat(whenReturn(viewSpy.getVisibility(), View.GONE))
                .isNotVisible();
    }

    @Test
    public void testIsVisibleFailure_invisible() {
        try {
            assertThat(whenReturn(viewSpy.getVisibility(), View.INVISIBLE))
                    .isVisible();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <view> is not visible");
        }
    }


    /**
     * Todo: Remove not really dynamic/flexible ugly, error prone assertion tests generator method
     * <p>
     * RememberTo: find method in {@link View} - strip away assertions first word and use contains
     * (think of exceptions like not etc..)
     * <p>
     * RememberTo: generate a failure generation version
     * RememberTo: remove tight coupling between {@link ViewSubjectTest} doing next RememberTo
     * RememberTo: abstraction (DRY) for remainder of AssertJ porting of classes
     * RememberTo: consider using ML... dun dun dun, much much later ;)
     */
    @Test
    public void generateSuccessAssertionTests() {
        Function<Class<?>, List<String>> getClassDeclaredMethodNames = cls -> Lists.newArrayList(
                Lists.transform(Lists.newArrayList(cls.getSuperclass().getDeclaredMethods()),
                        method -> method.getName()));

        ViewSubject subject = new ViewSubject(new FailureStrategy() {
            @Override
            public void fail(String message) {
                super.fail(message);
            }
        }, null);

        Method[] declaredMethods = subject.getClass().getDeclaredMethods();
        List<String> superClassMethodNames = getClassDeclaredMethodNames.apply(subject.getClass());

        Predicate<? super Method> superClassMethodFilter = method -> !superClassMethodNames.contains(method.getName()) && !method.getName().contains("views");

        Function<? super Method, ? extends String> keyMethodNameMapper = method -> method.getName();
        Function<? super Method, ? extends String[]> valueParamNameTypeMapMapper = method -> {
            Parameter[] params = method.getParameters();
            /**
             * Todo: determine whether its worth the effort for a more dynamic approach of mapping more than one parameter
             */
            if (params.length > 0) {
                Parameter param = params[0];
                return new String[]{param.getName(), param.getType().getTypeName()};
            }
            return new String[0];
        };

        BiConsumer<? super String, ? super String[]> consumeMethodAndParamTypeMap =
                (methodName, paramValueAndType) -> {
                    String testName = String.valueOf(methodName.charAt(0)).toUpperCase()
                            + methodName.substring(1, methodName.length());

                    String methodCall = "";
                    if (methodName.contains("has")) {
                        methodCall = methodName.replace("has", "get");
                    } else {
                        methodCall = methodName;
                    } // move into features/tags

                    String valueInstantiationLine = null;
                    String value = null;

                    if (paramValueAndType.length > 0) {
                        String paramName = paramValueAndType[0];
                        String paramType = paramValueAndType[1];

                        if (paramType != null) {
                            switch (paramType) {
                                case "float":
                                    value = "randomGenerator.nextFloat()";
                                    break;
                                case "int":
                                    value = "randomGenerator.nextInt()";
                                    break;
                                case "boolean":
                                    value = "true";
                                    break;
                                case "java.lang.CharSequence":
                                    value = "DUMMY_STRING";
                                    break;
                                default:
                                    value = String.format("mock(%s.class)", paramType);
                                    break;
                            }
                        } // move into features/tags

                        paramName = "value";
                        valueInstantiationLine = String.format("%s %s = %s;", paramType, paramName, value);

                        System.out.println(String.format("\n@Test\n" +
                                "    public void test%s() {\n" +
                                "        %s\n" +
                                "        assertThat(whenReturn(viewSpy.%s(), value))\n" +
                                "                .%s(%s);\n" +
                                "    }\n", testName, valueInstantiationLine, methodCall, methodName, paramName));
                    } else {
                        System.out.println(String.format("\n@Test\n" +
                                "    public void test%s() {\n" +
                                "        assertThat(whenReturn(viewSpy.%s(), value))\n" +
                                "                .%s();\n" +
                                "    }\n", testName, methodCall, methodName));
                    }
                };

        Function<List<Method>, Map<String, String[]>> sortAndMapMethodParamValueAndType =
                methods -> methods.stream()
                        .filter(superClassMethodFilter)
                        .sorted((mX, mY) -> ComparisonChain.start().
                                compare(mX.getName(), mY.getName(), String.CASE_INSENSITIVE_ORDER).
                                compare(mX.getName(), mY.getName()).result())
                        .collect(Collectors.toMap(keyMethodNameMapper, valueParamNameTypeMapMapper,
                                (BinaryOperator<String[]>) (strings, strings2) -> new String[0]));

        sortAndMapMethodParamValueAndType
                .apply(Lists.newArrayList(declaredMethods))
                .forEach(consumeMethodAndParamTypeMap);

    }

    private <T> View whenReturn(T methodCall, T value) {
        return when(methodCall)
                .thenReturn(value)
                .getMock();
    }
}