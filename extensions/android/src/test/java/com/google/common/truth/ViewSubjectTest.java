package com.google.common.truth;

import android.view.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Random;

import static com.google.common.truth.AndroidTruth.assertThat;
import static org.junit.Assert.fail;
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
        assertThat(withAlpha(alpha))
                .hasAlpha(alpha);
    }

    @Test
    public void testHasAlphaFailure() {
        try {
            assertThat(withAlpha(new Random(99).nextFloat()))
                    .hasAlpha(101);
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <view> has alpha: 101.0, actual: " + viewSpy.getAlpha());
        }
    }

    @Test
    public void testIsVisible() {
        assertThat(withVisibility(View.VISIBLE))
                .isVisible();
    }

    @Test
    public void testIsNotVisible() {
        assertThat(withVisibility(View.INVISIBLE))
                .isNotVisible();
        assertThat(withVisibility(View.GONE))
                .isNotVisible();
    }

    @Test
    public void testIsVisibleFailure_invisible() {
        try {
            assertThat(withVisibility(View.INVISIBLE))
                    .isVisible();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <view> is not visible");
        }
    }

    @Test
    public void testIsVisibleFailure_gone() {
        try {
            assertThat(withVisibility(View.GONE))
                    .isVisible();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <view> is not visible");
        }
    }

    @Test
    public void testIsNotVisibleFailure() {
        try {
            assertThat(withVisibility(View.VISIBLE))
                    .isNotVisible();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <view> is visible");
        }
    }

    private View withVisibility(int visibility) {
        when(viewSpy.getVisibility())
                .thenReturn(visibility);
        return viewSpy;
    }

    private View withAlpha(float alpha) {
        when(viewSpy.getAlpha())
                .thenReturn(alpha);
        return viewSpy;
    }
}