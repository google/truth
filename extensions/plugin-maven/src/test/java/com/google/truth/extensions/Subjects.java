package com.google.truth.extensions;

import com.google.common.truth.Truth;
import com.google.common.truth.extension.generator.SourceClassSets;
import com.google.common.truth.extension.generator.TruthGeneratorAPI;
import com.google.common.truth.extension.generator.plugin.GeneratorMojo;
import org.junit.Test;

import java.io.File;

public class Subjects {

  @Test
  public void makeSubjects() {
    TruthGeneratorAPI tg = TruthGeneratorAPI.create();
    SourceClassSets ss = new SourceClassSets(getClass());
    ss.generateFromShaded(File.class);
    ss.generateFrom(GeneratorMojo.class);
    tg.generate(ss);
  }

}
