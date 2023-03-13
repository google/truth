package com.google.common.truth.junit5;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.test;

import com.google.common.truth.StandardSubjectBuilder;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;
import org.opentest4j.MultipleFailuresError;

public class ExpectTest {
  @Test
  public void testExtension() {
    Events events = EngineTestKit.engine("junit-jupiter")
        .selectors(selectClass(TestCase.class))
        .execute()
        .testEvents()
        .assertStatistics(stats -> stats.started(3).failed(2));
    events.succeeded().assertEventsMatchExactly(event(test("testSuccess")));
    events.failed().assertEventsMatchExactly(
        event(test("testOneFailure"), finishedWithFailure(new Condition(t -> t instanceof AssertionError && !(t instanceof MultipleFailuresError), "AssertionError"))),
        event(test("testTwoFailures"), finishedWithFailure(new Condition(MultipleFailuresError.class::isInstance, "MultipleFailuresError"))));
  }

  @ExtendWith(Expect.class)
  @TestMethodOrder(OrderAnnotation.class)
  static class TestCase {
    @Test
    @Order(1)
    public void testSuccess(StandardSubjectBuilder expect) {
      expect.that(0).isEqualTo(0);
      expect.that(1).isEqualTo(1);
    }

    @Test
    @Order(2)
    @ExtendWith(Expect.class)
    public void testOneFailure(StandardSubjectBuilder expect) {
      expect.that(0).isEqualTo(1);
      expect.that(1).isEqualTo(1);
    }

    @Test
    @Order(3)
    @ExtendWith(Expect.class)
    public void testTwoFailures(StandardSubjectBuilder expect) {
      expect.that(0).isEqualTo(1);
      expect.that(1).isEqualTo(0);
    }
  }
}
