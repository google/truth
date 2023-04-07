package com.google.common.truth;

public class FactFormattingStrategyFactory {
    FactFormattingStrategy getFactFormattingStrategy(Fact fact, boolean seenNewlineInValue) {
        if (fact.value == null) {
            return new NullFactFormattingStrategy();
        } else if (seenNewlineInValue) {
            return new NewlineFactFormattingStrategy();
        } else {
            return new NoNewlineFactFormattingStrategy();
        }
    }
}
