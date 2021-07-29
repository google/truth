package com.google.common.truth.extension.generator;

import com.google.common.io.Resources;
import com.google.common.truth.extension.Employee;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public class TruthGeneratorTest {

    @Test
    public void poc() throws IOException {
        TruthGenerator truthGenerator = new TruthGenerator(getClass().getPackage().getName());
        String generated = truthGenerator.generate(Employee.class);

        String expectedFileName = "expected-EmployeeSubject.java.txt";
        String expecting = Resources.toString(Resources.getResource(expectedFileName), Charset.defaultCharset());

        assertThat(trim(generated)).isEqualTo(trim(expecting));
    }

    private String trim(String in) {
        return in.replaceAll("(?m)^(\\s)*|(\\s)*$", "");
    }

    @Test
    public void employee() throws FileNotFoundException {
        TruthGenerator truthGenerator = new TruthGenerator(getClass().getPackage().getName());
        String generate = truthGenerator.generate(MyEmployee.class);

        assertThat(trim("")).isEqualTo(trim(generate));

    }

}
