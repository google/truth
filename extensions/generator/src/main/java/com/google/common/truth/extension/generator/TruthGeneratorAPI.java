package com.google.common.truth.extension.generator;

import com.google.common.truth.extension.generator.internal.TruthGenerator;
import com.google.common.truth.extension.generator.internal.model.ThreeSystem;

import java.util.Map;
import java.util.Set;

/**
 *
 */
public interface TruthGeneratorAPI {

  static TruthGenerator create() {
    return new TruthGenerator();
  }

  /**
   * Takes a user maintained source file, and adds boiler plate and Subject methods that are missing. If aggressively
   * skips parts if it thinks the user has overridden something.
   * <p>
   * Not implemented yet.
   */
  String maintain(Class source, Class userAndGeneratedMix);

  /**
   * todo
   */
  <T> String combinedSystem(Class<T> source);

  /**
   * todo
   */
  void combinedSystem(String... modelPackages);

  /**
   * @param modelPackages
   */
  void generate(String... modelPackages);

  /**
   * @param classes
   */
  void generateFromPackagesOf(Class<?>... classes);

  /**
   * @param ss
   */
  void combinedSystem(SourceClassSets ss);

  /**
   * Use this entry point to generate for a large and differing set of source classes - which will also generate a
   * single point of entry for all of them.
   *
   * <p>
   * There are many different ways to add, check out the different methods in {@link SourceClassSets}.
   *
   * @see SourceClassSets
   */
  Map<Class<?>, ThreeSystem> generate(SourceClassSets ss);

  /**
   * @param classes
   * @return
   */
  Map<Class<?>, ThreeSystem> generate(Set<Class<?>> classes);

  Map<Class<?>, ThreeSystem> generate(Class<?>... classes);
}
