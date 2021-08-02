package com.google.common.truth.extension.generator;

import com.google.common.truth.Truth;
import com.google.common.truth.extension.generator.internal.SubjectMethodGenerator;
import com.google.common.truth.extension.generator.internal.model.ParentClass;
import com.google.common.truth.extension.generator.internal.model.ThreeSystem;
import com.google.common.truth.extension.generator.testModel.MyEmployee;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.util.Set;

@RunWith(JUnit4.class)
public class SubjectMethodGeneratorTests {

  @Test
  public void poc(){
    JavaClassSource generated = Roaster.create(JavaClassSource.class);
    SubjectMethodGenerator subjectMethodGenerator = new SubjectMethodGenerator(Set.of());
    ThreeSystem threeSystem = new ThreeSystem(MyEmployee.class, new ParentClass(generated), null, null);
    subjectMethodGenerator.addTests(threeSystem);
    Truth.assertThat(generated.toString()).isEqualTo("");
  }
}
