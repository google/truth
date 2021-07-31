package com.google.common.truth.extension.generator;

import com.google.common.truth.Truth;
import com.google.common.truth.extension.generator.internal.SubjectMethodGenerator;
import com.google.common.truth.extension.generator.testModel.MyEmployee;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Set;

@RunWith(JUnit4.class)
public class SubjectMethodGeneratorTests {

  @Test
  public void poc(){
    JavaClassSource generated = Roaster.create(JavaClassSource.class);
    SubjectMethodGenerator subjectMethodGenerator = new SubjectMethodGenerator(Set.of());
    subjectMethodGenerator.addTests(generated, MyEmployee.class);

    Truth.assertThat(generated.toString()).isEqualTo("");
  }
}
