package com.google.common.truth;

public class NewlineFactFormattingStrategy implements FactFormattingStrategy{

//    private String indent(String value) {
//        return value.lines().map(line -> "    " + line).collect(Collectors.joining("\n"));
//    }
    private static String indent(String value) {
        // We don't want to indent with \t because the text would align exactly with the stack trace.
        // We don't want to indent with \t\t because it would be very far for people with 8-space tabs.
        // Let's compromise and indent by 4 spaces, which is different than both 2- and 8-space tabs.
        return "    " + value.replace("\n", "\n    ");
    }
    public String formatFact(Fact fact, int longestKeyLength) {
        StringBuilder builder = new StringBuilder();
        builder.append(fact.key);
        builder.append(":\n");
        builder.append(indent(fact.value));
        return builder.toString();
    }
}
