package com.google.common.truth.extension.generator;

import com.google.common.truth.extension.generator.internal.TruthGenerator;
import com.google.common.truth.extension.generator.internal.model.SourceClassSets;
import com.google.common.truth.extension.generator.internal.model.ThreeSystem;

import java.util.List;
import java.util.Set;

/**
 *
 */
public interface TruthGeneratorAPI {

  static TruthGeneratorAPI create() {
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
   * @param ss
   * @return
   */
  Set<ThreeSystem> generate(SourceClassSets ss);

  /**
   *
   * @param classes
   * @return
   */
  Set<ThreeSystem> generate(Set<Class<?>> classes);

}
