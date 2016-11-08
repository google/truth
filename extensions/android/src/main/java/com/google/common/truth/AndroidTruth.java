package com.google.common.truth;

import android.view.View;

import javax.annotation.Nullable;

import static com.google.common.truth.Truth.assertAbout;

/**
 * @author Kevin Leigh Crain
 */
public class AndroidTruth {

    public static ViewSubject assertThat(@Nullable View target) {
        return assertAbout(ViewSubject.views()).that(target);
    }

}
