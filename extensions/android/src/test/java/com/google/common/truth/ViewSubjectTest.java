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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

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
     * Todo: Remove not really dynamic/flexible ugly, error prone assertion tests generator method {@link #generateSuccessAssertionTests()}
     *
     * RememberTo: find method in {@link View} - strip away assertions first word and use contains
     *             (think of exceptions like not etc..)
     *
     * RememberTo: generate a failure generation version
     * RememberTo: remove tight coupling between {@link ViewSubjectTest} doing next RememberTo
     * RememberTo: abstraction (DRY) for remainder of AssertJ porting of classes
     * RememberTo: consider using ML... dun dun dun, much much later ;)
     */
    @Test
    public void generateSuccessAssertionTests() {
        ViewSubject mock = new ViewSubject(new FailureStrategy() {
            @Override
            public void fail(String message) {
                super.fail(message);
            }
        }, null);

        Method[] methods = mock.getClass().getDeclaredMethods();

        List<String> subClassMethodNames = Lists.newArrayList(
                Lists.transform(Lists.newArrayList(mock.getClass().getSuperclass().getDeclaredMethods()),
                        method1 -> method1.getName()));

        HashMap<String, HashMap<String, String>> assertionParamMap = new HashMap<>();

        ArrayList<String> assertionMethodsNames = Lists.newArrayList();
        Lists.newArrayList(methods).stream()
                .filter(method -> !subClassMethodNames.contains(method.getName()) || !method.getName().contains("views"))
                .forEach(method -> {
                    String assertionMethodName = method.getName();

                    ArrayList<Parameter> parameters = Lists.newArrayList(method.getParameters());
                    if (parameters.size() > 0) {
                        Parameter parameter = parameters.get(0);
                        assertionParamMap.put(assertionMethodName, new HashMap<String, String>() {{
                            // Maybe integrate dependency to get parameter name: https://github.com/paul-hammant/paranamer
                            put(parameter.getName(), parameter.getParameterizedType().getTypeName());
                        }});
                    } else assertionParamMap.put(assertionMethodName, null);

                    assertionMethodsNames.add(assertionMethodName);
                });

        assertionMethodsNames.sort((str1, str2) -> ComparisonChain.start().
                compare(str1, str2, String.CASE_INSENSITIVE_ORDER).
                compare(str1, str2).
                result());

        assertionMethodsNames.forEach(assertionMethodName -> {
            String testName = String.valueOf(assertionMethodName.charAt(0)).toUpperCase()
                    + assertionMethodName.substring(1, assertionMethodName.length());

            String methodCall = "";
            if (assertionMethodName.contains("has")) {
                methodCall = assertionMethodName.replace("has", "get");
            } else {
                methodCall = assertionMethodName;
            } // move into features/tags

            String valueInstantiationLine = null;
            String value = null;

            HashMap<String, String> paramMap = assertionParamMap.get(assertionMethodName);

            if (paramMap != null) {

                String paramName = Lists.newArrayList(paramMap.keySet()).get(0);
                String paramType = paramMap.get(paramName);

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
                        "    }\n", testName, valueInstantiationLine, methodCall, assertionMethodName, paramName));
            } else {
                System.out.println(String.format("\n@Test\n" +
                        "    public void test%s() {\n" +
                        "        assertThat(whenReturn(viewSpy.%s(), value))\n" +
                        "                .%s();\n" +
                        "    }\n", testName, methodCall, assertionMethodName));
            }
        });

    }

    private <T> View whenReturn(T methodCall, T value) {
        return when(methodCall)
                .thenReturn(value)
                .getMock();
    }
}