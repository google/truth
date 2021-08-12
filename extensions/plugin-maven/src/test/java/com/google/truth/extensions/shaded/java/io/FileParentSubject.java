package com.google.truth.extensions.shaded.java.io;

import com.google.common.truth.Subject;
import javax.annotation.processing.Generated;
import java.io.File;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.LongSubject;
import static com.google.common.truth.Fact.*;
import com.google.common.truth.BooleanSubject;
import com.google.common.truth.StringSubject;
import static com.google.truth.extensions.shaded.java.io.FileSubject.files;

import java.io.IOException;

import com.google.truth.extensions.shaded.java.io.FileSubject;

/**
 * Truth Subject for the {@link File}.
 * 
 * Note that this class is generated / managed, and will change over time. So
 * any changes you might make will be overwritten.
 * 
 * @see File
 * @see FileSubject
 * @see FileChildSubject
 */
@Generated("truth-generator")
public class FileParentSubject extends Subject {

	protected final File actual;

	protected FileParentSubject(FailureMetadata failureMetadata, java.io.File actual) {
		super(failureMetadata, actual);
		this.actual = actual;
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasTotalSpaceNotEqualTo(long expected) {
		if (!(actual.getTotalSpace() == expected)) {
			failWithActual(fact("expected TotalSpace NOT to be equal to", expected));
		}
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasTotalSpaceEqualTo(long expected) {
		if ((actual.getTotalSpace() == expected)) {
			failWithActual(fact("expected TotalSpace to be equal to", expected));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public LongSubject hasTotalSpace() {
		isNotNull();
		return check("getTotalSpace").that(actual.getTotalSpace());
	}

	/**
	 * Simple is or is not expectation for boolean fields.
	 */
	public void isHidden() {
		if (actual.isHidden()) {
			failWithActual(simpleFact("expected to be Hidden"));
		}
	}

	/**
	 * Simple is or is not expectation for boolean fields.
	 */
	public void isNotHidden() {
		if (!actual.isHidden()) {
			failWithActual(simpleFact("expected NOT to be Hidden"));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public BooleanSubject hasHidden() {
		isNotNull();
		return check("isHidden").that(actual.isHidden());
	}

	/**
	 * Simple is or is not expectation for boolean fields.
	 */
	public void isDirectory() {
		if (actual.isDirectory()) {
			failWithActual(simpleFact("expected to be Directory"));
		}
	}

	/**
	 * Simple is or is not expectation for boolean fields.
	 */
	public void isNotDirectory() {
		if (!actual.isDirectory()) {
			failWithActual(simpleFact("expected NOT to be Directory"));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public BooleanSubject hasDirectory() {
		isNotNull();
		return check("isDirectory").that(actual.isDirectory());
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasAbsolutePathNotEqualTo(java.lang.String expected) {
		if (!(actual.getAbsolutePath().equals(expected))) {
			failWithActual(fact("expected AbsolutePath NOT to be equal to", expected));
		}
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasAbsolutePathEqualTo(java.lang.String expected) {
		if ((actual.getAbsolutePath().equals(expected))) {
			failWithActual(fact("expected AbsolutePath to be equal to", expected));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public StringSubject hasAbsolutePath() {
		isNotNull();
		return check("getAbsolutePath").that(actual.getAbsolutePath());
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasAbsoluteFileNotEqualTo(java.io.File expected) {
		if (!(actual.getAbsoluteFile().equals(expected))) {
			failWithActual(fact("expected AbsoluteFile NOT to be equal to", expected));
		}
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasAbsoluteFileEqualTo(java.io.File expected) {
		if ((actual.getAbsoluteFile().equals(expected))) {
			failWithActual(fact("expected AbsoluteFile to be equal to", expected));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public FileSubject hasAbsoluteFile() {
		isNotNull();
		return check("getAbsoluteFile").about(files()).that(actual.getAbsoluteFile());
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasParentNotEqualTo(java.lang.String expected) {
		if (!(actual.getParent().equals(expected))) {
			failWithActual(fact("expected Parent NOT to be equal to", expected));
		}
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasParentEqualTo(java.lang.String expected) {
		if ((actual.getParent().equals(expected))) {
			failWithActual(fact("expected Parent to be equal to", expected));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public StringSubject hasParent() {
		isNotNull();
		return check("getParent").that(actual.getParent());
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasCanonicalFileNotEqualTo(java.io.File expected) throws IOException {
		if (!(actual.getCanonicalFile().equals(expected))) {
			failWithActual(fact("expected CanonicalFile NOT to be equal to", expected));
		}
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasCanonicalFileEqualTo(java.io.File expected) throws IOException {
		if ((actual.getCanonicalFile().equals(expected))) {
			failWithActual(fact("expected CanonicalFile to be equal to", expected));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public FileSubject hasCanonicalFile() throws IOException {
		isNotNull();
		return check("getCanonicalFile").about(files()).that(actual.getCanonicalFile());
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasUsableSpaceNotEqualTo(long expected) {
		if (!(actual.getUsableSpace() == expected)) {
			failWithActual(fact("expected UsableSpace NOT to be equal to", expected));
		}
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasUsableSpaceEqualTo(long expected) {
		if ((actual.getUsableSpace() == expected)) {
			failWithActual(fact("expected UsableSpace to be equal to", expected));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public LongSubject hasUsableSpace() {
		isNotNull();
		return check("getUsableSpace").that(actual.getUsableSpace());
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasFreeSpaceNotEqualTo(long expected) {
		if (!(actual.getFreeSpace() == expected)) {
			failWithActual(fact("expected FreeSpace NOT to be equal to", expected));
		}
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasFreeSpaceEqualTo(long expected) {
		if ((actual.getFreeSpace() == expected)) {
			failWithActual(fact("expected FreeSpace to be equal to", expected));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public LongSubject hasFreeSpace() {
		isNotNull();
		return check("getFreeSpace").that(actual.getFreeSpace());
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasCanonicalPathNotEqualTo(java.lang.String expected) throws IOException {
		if (!(actual.getCanonicalPath().equals(expected))) {
			failWithActual(fact("expected CanonicalPath NOT to be equal to", expected));
		}
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasCanonicalPathEqualTo(java.lang.String expected) throws IOException {
		if ((actual.getCanonicalPath().equals(expected))) {
			failWithActual(fact("expected CanonicalPath to be equal to", expected));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public StringSubject hasCanonicalPath() throws IOException {
		isNotNull();
		return check("getCanonicalPath").that(actual.getCanonicalPath());
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasNameNotEqualTo(java.lang.String expected) {
		if (!(actual.getName().equals(expected))) {
			failWithActual(fact("expected Name NOT to be equal to", expected));
		}
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasNameEqualTo(java.lang.String expected) {
		if ((actual.getName().equals(expected))) {
			failWithActual(fact("expected Name to be equal to", expected));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public StringSubject hasName() {
		isNotNull();
		return check("getName").that(actual.getName());
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasParentFileNotEqualTo(java.io.File expected) {
		if (!(actual.getParentFile().equals(expected))) {
			failWithActual(fact("expected ParentFile NOT to be equal to", expected));
		}
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasParentFileEqualTo(java.io.File expected) {
		if ((actual.getParentFile().equals(expected))) {
			failWithActual(fact("expected ParentFile to be equal to", expected));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public FileSubject hasParentFile() {
		isNotNull();
		return check("getParentFile").about(files()).that(actual.getParentFile());
	}

	/**
	 * Simple is or is not expectation for boolean fields.
	 */
	public void isFile() {
		if (actual.isFile()) {
			failWithActual(simpleFact("expected to be File"));
		}
	}

	/**
	 * Simple is or is not expectation for boolean fields.
	 */
	public void isNotFile() {
		if (!actual.isFile()) {
			failWithActual(simpleFact("expected NOT to be File"));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public BooleanSubject hasFile() {
		isNotNull();
		return check("isFile").that(actual.isFile());
	}

	/**
	 * Simple is or is not expectation for boolean fields.
	 */
	public void isAbsolute() {
		if (actual.isAbsolute()) {
			failWithActual(simpleFact("expected to be Absolute"));
		}
	}

	/**
	 * Simple is or is not expectation for boolean fields.
	 */
	public void isNotAbsolute() {
		if (!actual.isAbsolute()) {
			failWithActual(simpleFact("expected NOT to be Absolute"));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public BooleanSubject hasAbsolute() {
		isNotNull();
		return check("isAbsolute").that(actual.isAbsolute());
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasPathNotEqualTo(java.lang.String expected) {
		if (!(actual.getPath().equals(expected))) {
			failWithActual(fact("expected Path NOT to be equal to", expected));
		}
	}

	/**
	 * Simple check for equality for all fields.
	 */
	public void hasPathEqualTo(java.lang.String expected) {
		if ((actual.getPath().equals(expected))) {
			failWithActual(fact("expected Path to be equal to", expected));
		}
	}

	/**
	 * Returns the Subject for the given field type, so you can chain on other
	 * assertions.
	 */
	public StringSubject hasPath() {
		isNotNull();
		return check("getPath").that(actual.getPath());
	}
}
