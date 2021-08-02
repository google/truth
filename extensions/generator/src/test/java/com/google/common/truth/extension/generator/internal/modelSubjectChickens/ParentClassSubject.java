package com.google.common.truth.extension.generator.internal.modelSubjectChickens;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.extension.generator.internal.model.ParentClass;
import com.google.common.truth.extension.generator.UserManagedTruth;

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
 * @see ParentClassParentSubject
 */
@UserManagedTruth(clazz = ParentClass.class)
@Generated("truth-generator")
public class ParentClassSubject extends ParentClassParentSubject {

	protected ParentClassSubject(FailureMetadata failureMetadata,
                               ParentClass actual) {
		super(failureMetadata, actual);
	}

	/**
	 * Returns an assertion builder for a {@link ParentClass} class.
	 */
	public static Factory<ParentClassSubject, ParentClass> parentClasses() {
		return ParentClassSubject::new;
	}
}
