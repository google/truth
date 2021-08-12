package com.google.truth.extensions.generator.plugin;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.extension.generator.UserManagedTruth;
import com.google.common.truth.extension.generator.plugin.GeneratorMojo;
import javax.annotation.processing.Generated;

/**
 * Optionally move this class into source control, and add your custom
 * assertions here.
 * 
 * <p>
 * If the system detects this class already exists, it won't attempt to generate
 * a new one. Note that if the base skeleton of this class ever changes, you
 * won't automatically get it updated.
 * 
 * @see GeneratorMojo
 * @see GeneratorMojoParentSubject
 */
@UserManagedTruth(clazz = GeneratorMojo.class)
@Generated("truth-generator")
public class GeneratorMojoSubject extends GeneratorMojoParentSubject {

	protected GeneratorMojoSubject(FailureMetadata failureMetadata,
			com.google.common.truth.extension.generator.plugin.GeneratorMojo actual) {
		super(failureMetadata, actual);
	}

	/**
	 * Returns an assertion builder for a {@link GeneratorMojo} class.
	 */
	public static Factory<GeneratorMojoSubject, GeneratorMojo> generatorMojoes() {
		return GeneratorMojoSubject::new;
	}
}
