package org.junit.contrib.truth;

import java.util.ArrayList;
import java.util.List;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class Expect extends TestVerb implements TestRule {
	private static class ExpectationGatherer implements FailureStrategy {
		List<String> messages = new ArrayList<String>();
		
		@Override
		public void fail(String message) {
			messages.add(message);
		}
	}
	
	private final ExpectationGatherer gatherer;
	private boolean inRuleContext = false;
	
	public static Expect create() {
		return new Expect(new ExpectationGatherer());
	}
	
	private Expect(ExpectationGatherer gatherer) {
		super(gatherer);
		this.gatherer = gatherer;
	}

	@Override
	public Statement apply(final Statement base, Description description) {
		inRuleContext = true;
		return new Statement() {
			
			@Override
			public void evaluate() throws Throwable {
				base.evaluate();
				if (! gatherer.messages.isEmpty()) {
					String message = "All failed expectations:\n";
					for (int i = 0; i < gatherer.messages.size(); i++) {
						message += "  " + (i+1) + ". " + gatherer.messages.get(i) + "\n";
					}
					throw new AssertionError(message);
				}
			}
		};
	}
}
