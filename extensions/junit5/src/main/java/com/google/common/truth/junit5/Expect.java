package com.google.common.truth.junit5;

import com.google.common.truth.FailureStrategy;
import com.google.common.truth.StandardSubjectBuilder;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.opentest4j.MultipleFailuresError;

import java.util.ArrayList;
import java.util.List;

/**
 * An <tt>Extension</tt> that provides a {@link StandardSubjectBuilder} parameter to test methods.
 *
 * <p>Assertion failures on the given <tt>StandardSubjectBuilder</tt> will not immediately fail;
 * instead, failures will be reported at the <em>end</em> of the test.
 *
 * <p>Usage:
 *
 * <pre>
 * @ExtendWith(Expect.class) // or system property junit.jupiter.extensions.autodetection.enabled=true
 * ...
 *     @Test
 *     public void test(StandardSubjectBuilder expect) {
 *         ...
 *     }
 * </pre>
 */
public class Expect implements Extension, ParameterResolver, AfterEachCallback {
  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return parameterContext.getParameter().getType() == StandardSubjectBuilder.class;
  }

  @Override
  public StandardSubjectBuilder resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.Namespace.create(Expect.class));
    return store.getOrComputeIfAbsent(extensionContext.getUniqueId(), key -> new Holder(), Holder.class).get();
  }

  @Override
  public void afterEach(ExtensionContext context) {
    ExtensionContext.Store store = context.getStore(ExtensionContext.Namespace.create(Expect.class));
    Holder holder = store.get(context.getUniqueId(), Holder.class);
    List<AssertionError> failures = null;
    if (holder != null) {
      synchronized (holder) {
        failures = holder.failures;
        if (failures == null) {
          throw new IllegalStateException();
        }
        holder.failures = null;
      }
    }
    if (failures != null && !failures.isEmpty()) {
      throw failures.size() == 1 ? failures.get(0) : new MultipleFailuresError(null, failures);
    }
  }

  private static class Holder implements FailureStrategy, ExtensionContext.Store.CloseableResource {
    private List<AssertionError> failures = new ArrayList<>();

    private StandardSubjectBuilder get() {
      return StandardSubjectBuilder.forCustomFailureStrategy(this);
    }

    @Override
    public synchronized void fail(AssertionError failure) {
      failures.add(failure);
    }

    @Override
    public synchronized void close() {
      if (failures != null) {
        throw new IllegalStateException();
      }
    }
  }
}
