visibility("private")

def drop_java_srcs_with_kotlin_equivalents(srcs):
    java_srcs = [s for s in srcs if s.endswith(".java")]
    kotlin_srcs = [s for s in srcs if s.endswith(".kt")]
    unknown_srcs = [s for s in srcs if s not in java_srcs and s not in kotlin_srcs]
    if len(unknown_srcs) > 0:
        fail("Got non-Java, non-Kotlin srcs: %s" % unknown_srcs)
    return kotlin_srcs + [s for s in java_srcs if s.removesuffix(".java") + ".kt" not in kotlin_srcs]
