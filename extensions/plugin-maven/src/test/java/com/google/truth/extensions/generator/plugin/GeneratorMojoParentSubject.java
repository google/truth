package com.google.truth.extensions.generator.plugin;

import com.google.common.truth.Subject;
import javax.annotation.processing.Generated;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.BooleanSubject;
import static com.google.common.truth.Fact.*;
import java.util.Map;
import com.google.common.truth.MapSubject;
import com.google.common.truth.extension.generator.plugin.GeneratorMojo;
import org.apache.maven.plugin.logging.Log;
import com.google.common.truth.ObjectArraySubject;
import com.google.common.truth.StringSubject;
import java.io.File;
import static com.google.truth.extensions.shaded.java.io.FileSubject.files;
import com.google.truth.extensions.shaded.java.io.FileSubject;

/**
 * Truth Subject for the {@link GeneratorMojo}.
 * 
 * Note that this class is generated / managed, and will change over time. So
 * any changes you might make will be overwritten.
 * 
 * @see GeneratorMojo
 * @see GeneratorMojoSubject
 * @see GeneratorMojoChildSubject
 */
@Generated("truth-generator")
public class GeneratorMojoParentSubject extends Subject {

	protected final GeneratorMojo actual;

	protected GeneratorMojoParentSubject(FailureMetadata failureMetadata,
			com.google.common.truth.extension.generator.plugin.GeneratorMojo actual) {
		super(failureMetadata, actual);
		this.actual = actual;
	}

	/**
	 * Simple is or is not expectation for boolean fields.
	 */
	public void isRecursive() {
		if (actual.isRecursive()) {
			failWithActual(simpleFact("expected to be Recursive"));
		}
	}

	/**
	 * Simple is or is not expectation for boolean fields.
	 */
	public void isNotRecursive() {
		if (!actual.isRecursive()) {
			failWithActual(simpleFact("expected NOT to be Recursive"));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public BooleanSubject hasRecursive() {
		isNotNull();
		return check("isRecursive").that(actual.isRecursive());
	}

	/**
	 * Check Maps for containing a given key.
	 */
	public void hasPluginContextNotWithKey(java.lang.Object expected) {
		if (!actual.getPluginContext().containsKey(expected)) {
			failWithActual(fact("expected PluginContext NOT to have key", expected));
		}
	}

	/**
	 * Check Maps for containing a given key.
	 */
	public void hasPluginContextWithKey(java.lang.Object expected) {
		if (actual.getPluginContext().containsKey(expected)) {
			failWithActual(fact("expected PluginContext to have key", expected));
		}
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasPluginContextNotEqualTo(Map expected) {
		if (!(actual.getPluginContext().equals(expected))) {
			failWithActual(fact("expected PluginContext NOT to be equal to", expected));
		}
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasPluginContextEqualTo(java.util.Map expected) {
		if ((actual.getPluginContext().equals(expected))) {
			failWithActual(fact("expected PluginContext to be equal to", expected));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public MapSubject hasPluginContext() {
		isNotNull();
		return check("getPluginContext").that(actual.getPluginContext());
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasLogNotEqualTo(Log expected) {
		if (!(actual.getLog().equals(expected))) {
			failWithActual(fact("expected Log NOT to be equal to", expected));
		}
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasLogEqualTo(org.apache.maven.plugin.logging.Log expected) {
		if ((actual.getLog().equals(expected))) {
			failWithActual(fact("expected Log to be equal to", expected));
		}
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasClassesNotEqualTo(java.lang.String[] expected) {
		if (!(actual.getClasses().equals(expected))) {
			failWithActual(fact("expected Classes NOT to be equal to", expected));
		}
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasClassesEqualTo(java.lang.String[] expected) {
		if ((actual.getClasses().equals(expected))) {
			failWithActual(fact("expected Classes to be equal to", expected));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public ObjectArraySubject hasClasses() {
		isNotNull();
		return check("getClasses()").that(actual.getClasses());
	}

	/**
	 * Simple is or is not expectation for boolean fields.
	 */
	public void isSkip() {
		if (actual.isSkip()) {
			failWithActual(simpleFact("expected to be Skip"));
		}
	}

	/**
	 * Simple is or is not expectation for boolean fields.
	 */
	public void isNotSkip() {
		if (!actual.isSkip()) {
			failWithActual(simpleFact("expected NOT to be Skip"));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public BooleanSubject hasSkip() {
		isNotNull();
		return check("isSkip").that(actual.isSkip());
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasPackagesNotEqualTo(java.lang.String[] expected) {
		if (!(actual.getPackages().equals(expected))) {
			failWithActual(fact("expected Packages NOT to be equal to", expected));
		}
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasPackagesEqualTo(java.lang.String[] expected) {
		if ((actual.getPackages().equals(expected))) {
			failWithActual(fact("expected Packages to be equal to", expected));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public ObjectArraySubject hasPackages() {
		isNotNull();
		return check("getPackages").that(actual.getPackages());
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasIncludesNotEqualTo(java.lang.String[] expected) {
		if (!(actual.getIncludes().equals(expected))) {
			failWithActual(fact("expected Includes NOT to be equal to", expected));
		}
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasIncludesEqualTo(java.lang.String[] expected) {
		if ((actual.getIncludes().equals(expected))) {
			failWithActual(fact("expected Includes to be equal to", expected));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public ObjectArraySubject hasIncludes() {
		isNotNull();
		return check("getIncludes").that(actual.getIncludes());
	}

	/**
	 * Simple is or is not expectation for boolean fields.
	 */
	public void isCleanTargetDir() {
		if (actual.isCleanTargetDir()) {
			failWithActual(simpleFact("expected to be CleanTargetDir"));
		}
	}

	/**
	 * Simple is or is not expectation for boolean fields.
	 */
	public void isNotCleanTargetDir() {
		if (!actual.isCleanTargetDir()) {
			failWithActual(simpleFact("expected NOT to be CleanTargetDir"));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public BooleanSubject hasCleanTargetDir() {
		isNotNull();
		return check("isCleanTargetDir").that(actual.isCleanTargetDir());
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasExcludesNotEqualTo(java.lang.String[] expected) {
		if (!(actual.getExcludes().equals(expected))) {
			failWithActual(fact("expected Excludes NOT to be equal to", expected));
		}
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasExcludesEqualTo(java.lang.String[] expected) {
		if ((actual.getExcludes().equals(expected))) {
			failWithActual(fact("expected Excludes to be equal to", expected));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public ObjectArraySubject hasExcludes() {
		isNotNull();
		return check("getExcludes").that(actual.getExcludes());
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasGenerateAssertionsInPackageNotEqualTo(java.lang.String expected) {
		if (!(actual.getGenerateAssertionsInPackage().equals(expected))) {
			failWithActual(fact("expected GenerateAssertionsInPackage NOT to be equal to", expected));
		}
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasGenerateAssertionsInPackageEqualTo(java.lang.String expected) {
		if ((actual.getGenerateAssertionsInPackage().equals(expected))) {
			failWithActual(fact("expected GenerateAssertionsInPackage to be equal to", expected));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public StringSubject hasGenerateAssertionsInPackage() {
		isNotNull();
		return check("getGenerateAssertionsInPackage").that(actual.getGenerateAssertionsInPackage());
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasEntryPointClassPackageNotEqualTo(java.lang.String expected) {
		if (!(actual.getEntryPointClassPackage().equals(expected))) {
			failWithActual(fact("expected EntryPointClassPackage NOT to be equal to", expected));
		}
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasEntryPointClassPackageEqualTo(java.lang.String expected) {
		if ((actual.getEntryPointClassPackage().equals(expected))) {
			failWithActual(fact("expected EntryPointClassPackage to be equal to", expected));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public StringSubject hasEntryPointClassPackage() {
		isNotNull();
		return check("getEntryPointClassPackage").that(actual.getEntryPointClassPackage());
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasOutputDirectoryNotEqualTo(File expected) {
		if (!(actual.getOutputDirectory().equals(expected))) {
			failWithActual(fact("expected OutputDirectory NOT to be equal to", expected));
		}
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasOutputDirectoryEqualTo(java.io.File expected) {
		if ((actual.getOutputDirectory().equals(expected))) {
			failWithActual(fact("expected OutputDirectory to be equal to", expected));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public FileSubject hasOutputDirectory() {
		isNotNull();
		return check("getOutputDirectory").about(files()).that(actual.getOutputDirectory());
	}
}
