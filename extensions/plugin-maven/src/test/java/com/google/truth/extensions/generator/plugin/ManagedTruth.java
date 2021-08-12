package com.google.truth.extensions.generator.plugin;

import com.google.truth.extensions.shaded.java.io.FileSubject;

import java.io.File;
import static com.google.truth.extensions.shaded.java.io.FileSubject.*;
import com.google.common.truth.Truth;
import com.google.truth.extensions.generator.plugin.GeneratorMojoSubject;
import static com.google.truth.extensions.generator.plugin.GeneratorMojoSubject.*;

/**
 * Single point of access for all managed Subjects.
 */
public class ManagedTruth {

	/**
	 * Entry point for {@link File} assertions.
	 */
	public static FileSubject assertThat(java.io.File actual) {
		return Truth.assertAbout(files()).that(actual);
	}

	/**
	 * Convenience entry point for {@link File} assertions when being mixed with
	 * other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static FileSubject assertTruth(java.io.File actual) {
		return assertThat(actual);
	}

	/**
	 * Entry point for {@link GeneratorMojo} assertions.
	 */
	public static GeneratorMojoSubject assertThat(
			com.google.common.truth.extension.generator.plugin.GeneratorMojo actual) {
		return Truth.assertAbout(generatorMojoes()).that(actual);
	}

	/**
	 * Convenience entry point for {@link GeneratorMojo} assertions when being mixed
	 * with other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static GeneratorMojoSubject assertTruth(
			com.google.common.truth.extension.generator.plugin.GeneratorMojo actual) {
		return assertThat(actual);
	}
}
