package com.google.common.truth.extension.generator;

import com.google.common.truth.Truth;
import com.google.common.truth.extension.generator.SourceClassSets.PackageAndClasses;
import com.google.common.truth.extension.generator.internal.*;
import com.google.common.truth.extension.generator.internal.SubjectMethodGenerator.ClassOrGenerated;
import com.google.common.truth.extension.generator.internal.model.*;
import com.google.common.truth.extension.generator.shaded.org.jboss.forge.roaster.model.source.JavaClassSourceSubject;
import com.google.common.truth.extension.generator.testModel.*;
import com.google.common.truth.extension.generator.testModel.MyEmployee.State;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import static com.google.common.truth.extension.generator.PackageAndClassesSubject.packageAndClasseses;
import static com.google.common.truth.extension.generator.SourceClassSetsSubject.sourceClassSetses;
import static com.google.common.truth.extension.generator.TestModelUtilsSubject.testModelUtilses;
import static com.google.common.truth.extension.generator.TruthGeneratorAPISubject.truthGeneratorAPIs;
import static com.google.common.truth.extension.generator.internal.ClassOrGeneratedSubject.classOrGenerateds;
import static com.google.common.truth.extension.generator.internal.OverallEntryPointSubject.overallEntryPoints;
import static com.google.common.truth.extension.generator.internal.SkeletonGeneratorAPISubject.skeletonGeneratorAPIs;
import static com.google.common.truth.extension.generator.internal.SkeletonGeneratorSubject.skeletonGenerators;
import static com.google.common.truth.extension.generator.internal.SourceCheckingSubject.sourceCheckings;
import static com.google.common.truth.extension.generator.internal.SubjectMethodGeneratorSubject.subjectMethodGenerators;
import static com.google.common.truth.extension.generator.internal.TruthGeneratorSubject.truthGenerators;
import static com.google.common.truth.extension.generator.internal.UtilsSubject.utilses;
import static com.google.common.truth.extension.generator.internal.model.AClassSubject.aClasses;
import static com.google.common.truth.extension.generator.internal.model.ManagedClassSetSubject.managedClassSets;
import static com.google.common.truth.extension.generator.internal.model.MiddleClassSubject.middleClasses;
import static com.google.common.truth.extension.generator.internal.model.ParentClassSubject.parentClasses;
import static com.google.common.truth.extension.generator.internal.model.ThreeSystemSubject.threeSystems;
import static com.google.common.truth.extension.generator.shaded.org.jboss.forge.roaster.model.source.JavaClassSourceSubject.javaClassSources;
import static com.google.common.truth.extension.generator.testModel.IdCardSubject.idCards;
import static com.google.common.truth.extension.generator.testModel.MyEmployeeSubject.myEmployees;
import static com.google.common.truth.extension.generator.testModel.PersonSubject.persons;
import static com.google.common.truth.extension.generator.testModel.ProjectSubject.projects;
import static com.google.common.truth.extension.generator.testModel.StateSubject.states;

/**
 * Single point of access for all managed Subjects.
 */
public class ManagedTruthChicken {

	/**
	 * Entry point for {@link SourceClassSets} assertions.
	 */
	public static SourceClassSetsSubject assertThat(
			com.google.common.truth.extension.generator.SourceClassSets actual) {
		return Truth.assertAbout(sourceClassSetses()).that(actual);
	}

	/**
	 * Convenience entry point for {@link SourceClassSets} assertions when being
	 * mixed with other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static SourceClassSetsSubject assertTruth(
			com.google.common.truth.extension.generator.SourceClassSets actual) {
		return assertThat(actual);
	}

	/**
	 * Entry point for {@link ManagedClassSet} assertions.
	 */
	public static ManagedClassSetSubject assertThat(
			com.google.common.truth.extension.generator.internal.model.ManagedClassSet actual) {
		return Truth.assertAbout(managedClassSets()).that(actual);
	}

	/**
	 * Convenience entry point for {@link ManagedClassSet} assertions when being
	 * mixed with other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static ManagedClassSetSubject assertTruth(
			com.google.common.truth.extension.generator.internal.model.ManagedClassSet actual) {
		return assertThat(actual);
	}

	/**
	 * Entry point for {@link SkeletonGenerator} assertions.
	 */
	public static SkeletonGeneratorSubject assertThat(
			com.google.common.truth.extension.generator.internal.SkeletonGenerator actual) {
		return Truth.assertAbout(skeletonGenerators()).that(actual);
	}

	/**
	 * Convenience entry point for {@link SkeletonGenerator} assertions when being
	 * mixed with other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static SkeletonGeneratorSubject assertTruth(
			com.google.common.truth.extension.generator.internal.SkeletonGenerator actual) {
		return assertThat(actual);
	}

	/**
	 * Entry point for {@link SubjectMethodGenerator} assertions.
	 */
	public static SubjectMethodGeneratorSubject assertThat(
			com.google.common.truth.extension.generator.internal.SubjectMethodGenerator actual) {
		return Truth.assertAbout(subjectMethodGenerators()).that(actual);
	}

	/**
	 * Convenience entry point for {@link SubjectMethodGenerator} assertions when
	 * being mixed with other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static SubjectMethodGeneratorSubject assertTruth(
			com.google.common.truth.extension.generator.internal.SubjectMethodGenerator actual) {
		return assertThat(actual);
	}

	/**
	 * Entry point for {@link ClassOrGenerated} assertions.
	 */
	public static ClassOrGeneratedSubject assertThat(
			com.google.common.truth.extension.generator.internal.SubjectMethodGenerator.ClassOrGenerated actual) {
		return Truth.assertAbout(classOrGenerateds()).that(actual);
	}

	/**
	 * Convenience entry point for {@link ClassOrGenerated} assertions when being
	 * mixed with other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static ClassOrGeneratedSubject assertTruth(
			com.google.common.truth.extension.generator.internal.SubjectMethodGenerator.ClassOrGenerated actual) {
		return assertThat(actual);
	}

	/**
	 * Entry point for {@link PackageAndClasses} assertions.
	 */
	public static PackageAndClassesSubject assertThat(
			com.google.common.truth.extension.generator.SourceClassSets.PackageAndClasses actual) {
		return Truth.assertAbout(packageAndClasseses()).that(actual);
	}

	/**
	 * Convenience entry point for {@link PackageAndClasses} assertions when being
	 * mixed with other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static PackageAndClassesSubject assertTruth(
			com.google.common.truth.extension.generator.SourceClassSets.PackageAndClasses actual) {
		return assertThat(actual);
	}

	/**
	 * Entry point for {@link ThreeSystem} assertions.
	 */
	public static ThreeSystemSubject assertThat(
			com.google.common.truth.extension.generator.internal.model.ThreeSystem actual) {
		return Truth.assertAbout(threeSystems()).that(actual);
	}

	/**
	 * Convenience entry point for {@link ThreeSystem} assertions when being mixed
	 * with other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static ThreeSystemSubject assertTruth(
			com.google.common.truth.extension.generator.internal.model.ThreeSystem actual) {
		return assertThat(actual);
	}

	/**
	 * Entry point for {@link IdCard} assertions.
	 */
	public static IdCardSubject assertThat(com.google.common.truth.extension.generator.testModel.IdCard actual) {
		return Truth.assertAbout(idCards()).that(actual);
	}

	/**
	 * Convenience entry point for {@link IdCard} assertions when being mixed with
	 * other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static IdCardSubject assertTruth(com.google.common.truth.extension.generator.testModel.IdCard actual) {
		return assertThat(actual);
	}

	/**
	 * Entry point for {@link State} assertions.
	 */
	public static StateSubject assertThat(
			com.google.common.truth.extension.generator.testModel.MyEmployee.State actual) {
		return Truth.assertAbout(states()).that(actual);
	}

	/**
	 * Convenience entry point for {@link State} assertions when being mixed with
	 * other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static StateSubject assertTruth(
			com.google.common.truth.extension.generator.testModel.MyEmployee.State actual) {
		return assertThat(actual);
	}

	/**
	 * Entry point for {@link SkeletonGeneratorAPI} assertions.
	 */
	public static SkeletonGeneratorAPISubject assertThat(
			com.google.common.truth.extension.generator.internal.SkeletonGeneratorAPI actual) {
		return Truth.assertAbout(skeletonGeneratorAPIs()).that(actual);
	}

	/**
	 * Convenience entry point for {@link SkeletonGeneratorAPI} assertions when
	 * being mixed with other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static SkeletonGeneratorAPISubject assertTruth(
			com.google.common.truth.extension.generator.internal.SkeletonGeneratorAPI actual) {
		return assertThat(actual);
	}

	/**
	 * Entry point for {@link Person} assertions.
	 */
	public static PersonSubject assertThat(com.google.common.truth.extension.generator.testModel.Person actual) {
		return Truth.assertAbout(persons()).that(actual);
	}

	/**
	 * Convenience entry point for {@link Person} assertions when being mixed with
	 * other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static PersonSubject assertTruth(com.google.common.truth.extension.generator.testModel.Person actual) {
		return assertThat(actual);
	}

	/**
	 * Entry point for {@link MiddleClass} assertions.
	 */
	public static MiddleClassSubject assertThat(
			com.google.common.truth.extension.generator.internal.model.MiddleClass actual) {
		return Truth.assertAbout(middleClasses()).that(actual);
	}

	/**
	 * Convenience entry point for {@link MiddleClass} assertions when being mixed
	 * with other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static MiddleClassSubject assertTruth(
			com.google.common.truth.extension.generator.internal.model.MiddleClass actual) {
		return assertThat(actual);
	}

	/**
	 * Entry point for {@link Utils} assertions.
	 */
	public static UtilsSubject assertThat(com.google.common.truth.extension.generator.internal.Utils actual) {
		return Truth.assertAbout(utilses()).that(actual);
	}

	/**
	 * Convenience entry point for {@link Utils} assertions when being mixed with
	 * other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static UtilsSubject assertTruth(com.google.common.truth.extension.generator.internal.Utils actual) {
		return assertThat(actual);
	}

	/**
	 * Entry point for {@link SourceChecking} assertions.
	 */
	public static SourceCheckingSubject assertThat(
			com.google.common.truth.extension.generator.internal.SourceChecking actual) {
		return Truth.assertAbout(sourceCheckings()).that(actual);
	}

	/**
	 * Convenience entry point for {@link SourceChecking} assertions when being
	 * mixed with other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static SourceCheckingSubject assertTruth(
			com.google.common.truth.extension.generator.internal.SourceChecking actual) {
		return assertThat(actual);
	}

	/**
	 * Entry point for {@link AClass} assertions.
	 */
	public static AClassSubject assertThat(com.google.common.truth.extension.generator.internal.model.AClass actual) {
		return Truth.assertAbout(aClasses()).that(actual);
	}

	/**
	 * Convenience entry point for {@link AClass} assertions when being mixed with
	 * other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static AClassSubject assertTruth(com.google.common.truth.extension.generator.internal.model.AClass actual) {
		return assertThat(actual);
	}

	/**
	 * Entry point for {@link MyEmployee} assertions.
	 */
	public static MyEmployeeSubject assertThat(
			com.google.common.truth.extension.generator.testModel.MyEmployee actual) {
		return Truth.assertAbout(myEmployees()).that(actual);
	}

	/**
	 * Convenience entry point for {@link MyEmployee} assertions when being mixed
	 * with other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static MyEmployeeSubject assertTruth(
			com.google.common.truth.extension.generator.testModel.MyEmployee actual) {
		return assertThat(actual);
	}

	/**
	 * Entry point for {@link Project} assertions.
	 */
	public static ProjectSubject assertThat(com.google.common.truth.extension.generator.testModel.Project actual) {
		return Truth.assertAbout(projects()).that(actual);
	}

	/**
	 * Convenience entry point for {@link Project} assertions when being mixed with
	 * other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static ProjectSubject assertTruth(com.google.common.truth.extension.generator.testModel.Project actual) {
		return assertThat(actual);
	}

	/**
	 * Entry point for {@link TestModelUtils} assertions.
	 */
	public static TestModelUtilsSubject assertThat(com.google.common.truth.extension.generator.TestModelUtils actual) {
		return Truth.assertAbout(testModelUtilses()).that(actual);
	}

	/**
	 * Convenience entry point for {@link TestModelUtils} assertions when being
	 * mixed with other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static TestModelUtilsSubject assertTruth(com.google.common.truth.extension.generator.TestModelUtils actual) {
		return assertThat(actual);
	}

	/**
	 * Entry point for {@link TruthGenerator} assertions.
	 */
	public static TruthGeneratorSubject assertThat(
			com.google.common.truth.extension.generator.internal.TruthGenerator actual) {
		return Truth.assertAbout(truthGenerators()).that(actual);
	}

	/**
	 * Convenience entry point for {@link TruthGenerator} assertions when being
	 * mixed with other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static TruthGeneratorSubject assertTruth(
			com.google.common.truth.extension.generator.internal.TruthGenerator actual) {
		return assertThat(actual);
	}

	/**
	 * Entry point for {@link ParentClass} assertions.
	 */
	public static ParentClassSubject assertThat(
			com.google.common.truth.extension.generator.internal.model.ParentClass actual) {
		return Truth.assertAbout(parentClasses()).that(actual);
	}

	/**
	 * Convenience entry point for {@link ParentClass} assertions when being mixed
	 * with other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static ParentClassSubject assertTruth(
			com.google.common.truth.extension.generator.internal.model.ParentClass actual) {
		return assertThat(actual);
	}

	/**
	 * Entry point for {@link TruthGeneratorAPI} assertions.
	 */
	public static TruthGeneratorAPISubject assertThat(
			com.google.common.truth.extension.generator.TruthGeneratorAPI actual) {
		return Truth.assertAbout(truthGeneratorAPIs()).that(actual);
	}

	/**
	 * Convenience entry point for {@link TruthGeneratorAPI} assertions when being
	 * mixed with other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static TruthGeneratorAPISubject assertTruth(
			com.google.common.truth.extension.generator.TruthGeneratorAPI actual) {
		return assertThat(actual);
	}

	/**
	 * Entry point for {@link OverallEntryPoint} assertions.
	 */
	public static OverallEntryPointSubject assertThat(
			com.google.common.truth.extension.generator.internal.OverallEntryPoint actual) {
		return Truth.assertAbout(overallEntryPoints()).that(actual);
	}

	/**
	 * Convenience entry point for {@link OverallEntryPoint} assertions when being
	 * mixed with other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static OverallEntryPointSubject assertTruth(
			com.google.common.truth.extension.generator.internal.OverallEntryPoint actual) {
		return assertThat(actual);
	}

	/**
	 * Entry point for {@link JavaClassSource} assertions.
	 */
	public static JavaClassSourceSubject assertThat(org.jboss.forge.roaster.model.source.JavaClassSource actual) {
		return Truth.assertAbout(javaClassSources()).that(actual);
	}

	/**
	 * Convenience entry point for {@link JavaClassSource} assertions when being
	 * mixed with other "assertThat" assertion libraries.
	 * 
	 * @see #assertThat
	 */
	public static JavaClassSourceSubject assertTruth(org.jboss.forge.roaster.model.source.JavaClassSource actual) {
		return assertThat(actual);
	}
}
