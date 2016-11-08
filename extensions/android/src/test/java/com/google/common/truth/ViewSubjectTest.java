package com.google.common.truth;

import android.view.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.google.common.truth.AndroidTruth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * @author Kevin Leigh Crain
 */
@RunWith(JUnit4.class)
public class ViewSubjectTest {

    @Mock
    View view;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testIsVisible() {
        assertThat(createViewSpyWithVisibility(view, View.VISIBLE))
                .isVisible();
    }

    @Test
    public void testIsNotVisible() {
        assertThat(createViewSpyWithVisibility(view, View.INVISIBLE))
                .isNotVisible();
        assertThat(createViewSpyWithVisibility(view, View.GONE))
                .isNotVisible();
    }

    @Test
    public void testIsVisibleFailure_invisible() {
        try {
            assertThat(createViewSpyWithVisibility(view, View.INVISIBLE))
                    .isVisible();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <view> is not visible");
        }
    }

    @Test
    public void testIsVisibleFailure_gone() {
        try {
            assertThat(createViewSpyWithVisibility(view, View.GONE))
                    .isVisible();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <view> is not visible");
        }
    }

    @Test
    public void testIsNotVisibleFailure() {
        try {
            assertThat(createViewSpyWithVisibility(view, View.VISIBLE))
                    .isNotVisible();
            fail("Should have thrown.");
        } catch (AssertionError e) {
            Truth.assertThat(e).hasMessage("Not true that <view> is visible");
        }
    }

    private View createViewSpyWithVisibility(View view, int visibility) {
        View viewSpy = spy(view);
        when(viewSpy.getVisibility())
                .thenReturn(visibility);
        return viewSpy;
    }
}