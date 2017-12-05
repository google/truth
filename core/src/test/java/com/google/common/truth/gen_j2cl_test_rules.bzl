"""Generate j2cl tests per test file.

One j2cl test target will be generated for each file in `test_files` list.

This rule serves similar purpose to //java/com/google/testing/builddefs:GenTestRules.bzl but for
j2cl tests.
"""
load("//testing/build_defs/common:java_functions.bzl", "package_from_path")
load("//testing/build_defs/common:string_functions.bzl", "strip_right")
load("//java/com/google/testing/builddefs:GenTestRules.bzl", "GetTestNames")
load("//third_party/java_src/j2cl/build_def:j2cl_test.bzl", "j2cl_test")

# Builds a j2cl test suite that runs a single test class.
def gen_j2cl_test_rules(name, test_files, deps, prefix="J2cl", **kwargs):
  _ignored = [name]
  for test in GetTestNames(test_files):
    java_class = package_from_path(
        PACKAGE_NAME + "/" + strip_right(test, ".java"))
    j2cl_test(
        name = prefix + test,
        runtime_deps = deps,
        test_class = java_class,
        **kwargs)

