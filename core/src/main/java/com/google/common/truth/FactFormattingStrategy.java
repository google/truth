package com.google.common.truth;

public interface FactFormattingStrategy {
    String formatFact(Fact fact, int longestKeyLength);
}
