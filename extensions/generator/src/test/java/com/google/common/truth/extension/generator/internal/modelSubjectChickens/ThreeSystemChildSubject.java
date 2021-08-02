package com.google.common.truth.extension.generator.internal.modelSubjectChickens;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Truth;
import com.google.common.truth.extension.generator.internal.model.ThreeSystem;

import javax.annotation.processing.Generated;

/**
 * Entry point for assertions for @{ThreeSystem}. Import the static accessor
 * methods from this class and use them. Combines the generated code from
 * {@ThreeSystemParentSubject}and the user code from {@ThreeSystemSubject}.
 * 
 * @see ThreeSystem
 * @see ThreeSystemSubject
 * @see ThreeSystemParentSubject
 */
@Generated("truth-generator")
public class ThreeSystemChildSubject extends ThreeSystemSubject {

	/**
	 * This constructor should not be used, instead see the parent's.
	 * 
	 * @see ThreeSystemSubject
	 */
	private ThreeSystemChildSubject(FailureMetadata failureMetadata,
                                  ThreeSystem actual) {
		super(failureMetadata, actual);
	}

	/**
	 * Entry point for {@link ThreeSystem} assertions.
	 */
	public static ThreeSystemSubject assertThat(
			ThreeSystem actual) {
		return Truth.assertAbout(threeSystems()).that(actual);
	}

	/**
	 * Convenience entry point for {@link ThreeSystem} assertions when being mixed
	 * with other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static ThreeSystemSubject assertTruth(
			ThreeSystem actual) {
		return assertThat(actual);
	}
}
