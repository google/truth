package com.google.truth.extensions.generator.plugin;

import com.google.common.truth.extension.generator.plugin.GeneratorMojo;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Truth;
import javax.annotation.processing.Generated;

/**
 * Entry point for assertions for @{GeneratorMojo}. Import the static accessor
 * methods from this class and use them. Combines the generated code from
 * {@GeneratorMojoParentSubject}and the user code from {@GeneratorMojoSubject}.
 * 
 * @see com.google.common.truth.extension.generator.plugin.GeneratorMojo
 * @see GeneratorMojoSubject
 * @see GeneratorMojoParentSubject
 */
@Generated("truth-generator")
public class GeneratorMojoChildSubject extends GeneratorMojoSubject {

	/**
	 * This constructor should not be used, instead see the parent's.
	 * 
	 * @see GeneratorMojoSubject
	 */
	private GeneratorMojoChildSubject(FailureMetadata failureMetadata,
			com.google.common.truth.extension.generator.plugin.GeneratorMojo actual) {
		super(failureMetadata, actual);
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
