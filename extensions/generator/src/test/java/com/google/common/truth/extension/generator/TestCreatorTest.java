package com.google.common.truth.extension.generator;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class TestCreatorTest {

    @Test
    public void poc(){
        JavaClassSource generated = Roaster.create(JavaClassSource.class);
        TestCreator testCreator = new TestCreator();
        testCreator.addTests(generated, MyEmployee.class);

        assertThat(generated.toString()).isEqualTo("");
    }
}
