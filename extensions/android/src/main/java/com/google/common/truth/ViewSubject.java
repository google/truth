package com.google.common.truth;

import android.view.View;
import android.view.animation.Animation;

import javax.annotation.Nullable;

import static com.google.common.truth.Truth.assertThat;


/**
 * @author Kevin Leigh Crain
 */

/**
 * Todo: remove finals on method and make class abstract
 */
public class ViewSubject extends Subject<ViewSubject, View> {

    public ViewSubject(FailureStrategy failureStrategy, @Nullable View actual) {
        super(failureStrategy, actual);
    }

    public final void hasAlpha(float alpha) {
        float actualAlpha = actual().getAlpha();
        if(actual().getAlpha() != alpha) fail("has alpha: " + alpha + ", actual: " + actualAlpha);
    }

    public final void hasAnimation(Animation animation) {
        assertThat(actual().getAnimation())
                .isSameAs(animation);
    }

    public final void isVisible() {
        if(actual().getVisibility() != View.VISIBLE) fail("is not visible");
    }

    public final void isNotVisible() {
        if(actual().getVisibility() == View.VISIBLE) fail("is visible");
    }

    public static SubjectFactory<ViewSubject, View> views() {
        return FACTORY;
    }

    private static final SubjectFactory<ViewSubject, View> FACTORY = new SubjectFactory<ViewSubject, View>() {
        @Override
        public ViewSubject getSubject(FailureStrategy fs, View that) {
            return new ViewSubject(fs, that);
        }
    };
}
