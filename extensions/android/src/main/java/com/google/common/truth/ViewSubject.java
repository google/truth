package com.google.common.truth;

import android.view.View;

import javax.annotation.Nullable;

/**
 * @author Kevin Leigh Crain
 */
public class ViewSubject extends Subject<ViewSubject, View> {
    public ViewSubject(FailureStrategy failureStrategy, @Nullable View actual) {
        super(failureStrategy, actual);
    }
}
