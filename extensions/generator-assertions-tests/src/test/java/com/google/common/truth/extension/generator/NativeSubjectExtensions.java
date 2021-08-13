package com.google.common.truth.extension.generator;

import com.google.common.truth.extension.generator.internal.extensions.ManagedTruth;
import com.google.common.truth.extension.generator.internal.extensions.MyEmployeeSubject;
import com.google.common.truth.extension.generator.testModel.MyEmployee;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.extension.generator.InstanceUtils.createInstance;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class NativeSubjectExtensions {

  @Test
  public void my_string() {
    String nameWithSpace = "tony  ";
    MyEmployee emp = createInstance(MyEmployee.class).toBuilder().workNickName(nameWithSpace).build();
    MyEmployeeSubject es = ManagedTruth.assertThat(emp);

    // my strings
    es.hasWorkNickName().ignoringTrailingWhiteSpace().equalTo("tony");

    // my maps
    assertThatThrownBy(() -> es.hasProjectMap().containsKeys("key1", "key2")).isInstanceOf(AssertionError.class);
    List<String> keys = new ArrayList<>(emp.getProjectMap().keySet());
    es.hasProjectMap().containsKeys(keys.get(0), keys.get(1));
  }

}
