package com.google.common.truth;

public class NullFactFormattingStrategy implements FactFormattingStrategy {
    public String formatFact(Fact fact, int longestKeyLength) {
        StringBuilder builder = new StringBuilder();
        builder.append(fact.key);
        return builder.toString();
    }
}
