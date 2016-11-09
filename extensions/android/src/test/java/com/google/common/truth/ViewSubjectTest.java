package com.google.common.truth;

import android.view.View;
import android.view.animation.Animation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Random;

import static com.google.common.truth.AndroidTruth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Kevin Leigh Crain
 */
@RunWith(JUnit4.class)
public class ViewSubjectTest {

    @Mock
    View view;
    View viewSpy;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        viewSpy = spy(view);
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

    @Test
    public void testIsVisibleFailure_gone() {
        try {
            assertThat(whenReturn(viewSpy.getVisibility(), View.GONE))
                    .isVisible();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <view> is not visible");
        }
    }

    @Test
    public void testIsNotVisibleFailure() {
        try {
            assertThat(whenReturn(viewSpy.getVisibility(), View.VISIBLE))
                    .isNotVisible();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <view> is visible");
        }
    }

    private <T> View whenReturn(T methodCall, T value) {
        return when(methodCall)
                .thenReturn(value)
                .getMock();
    }
}