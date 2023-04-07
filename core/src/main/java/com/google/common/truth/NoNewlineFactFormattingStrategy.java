package com.google.common.truth;


import static com.google.common.base.Strings.padEnd;

class NoNewlineFactFormattingStrategy implements FactFormattingStrategy{
    public String formatFact(Fact fact, int longestKeyLength) {
        StringBuilder builder = new StringBuilder();
        builder.append(padEnd(fact.key, longestKeyLength, ' '));
        builder.append(": ");
        builder.append(fact.value);
        return builder.toString();
    }
}
