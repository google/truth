package com.google.common.truth.extension.generator;

import com.google.common.truth.Truth;
import com.google.common.truth.extension.generator.model.MyEmployee;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.Test;

import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

public class TestCreatorTest {

  @Test
  public void poc(){
    JavaClassSource generated = Roaster.create(JavaClassSource.class);
    TestGenerator testGenerator = new TestGenerator(Set.of());
    testGenerator.addTests(generated, MyEmployee.class);

    Truth.assertThat(generated.toString()).isEqualTo("");
  }
}
