package com.google.common.truth;

import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

final class DiffCalc {
    private List<String> stringList = new ArrayList<>();
//    // A map to record each unique string and its incremental id.
//    private final Map<String, Integer> stringToId = new HashMap<>();
    private int[] original;
    private int[] revised;
    // lcs[i][j] is the length of the longest common sequence of original[1..i] and revised[1..j].
    private int[][] lcs;
    private final List<Character> unifiedDiffType = new ArrayList<>();
    private final List<Integer> unifiedDiffContentId = new ArrayList<>();

    private List<String> reducedUnifiedDiff = new ArrayList<>();
    private int offsetHead = 0;


    public DiffCalc(int[] original, int[] revised, int[][] lcs, int offsetHead, List<String> reducedUnifiedDiff, List<String> stringList) {
        this.original = original;
        this.revised = revised;
        this.lcs = lcs;
        this.offsetHead = offsetHead;
        this.reducedUnifiedDiff = reducedUnifiedDiff;
        this.stringList = stringList;
    }

    void calcUnifiedDiff(int i, int j) {
        while (i > 0 || j > 0) {
            if (i > 0
                    && j > 0
                    && original[i] == revised[j]
                    // Make sure the diff output is identical to the diff command line tool when there are
                    // multiple solutions.
                    && lcs[i - 1][j - 1] + 1 > lcs[i - 1][j]
                    && lcs[i - 1][j - 1] + 1 > lcs[i][j - 1]) {
                unifiedDiffType.add(' ');
                unifiedDiffContentId.add(original[i]);
                i--;
                j--;
            } else if (j > 0 && (i == 0 || lcs[i][j - 1] >= lcs[i - 1][j])) {
                unifiedDiffType.add('+');
                unifiedDiffContentId.add(revised[j]);
                j--;
            } else if ((i > 0) && ((j == 0) || (lcs[i][j - 1] < lcs[i - 1][j]))) {
                unifiedDiffType.add('-');
                unifiedDiffContentId.add(original[i]);
                i--;
            }
        }
        Collections.reverse(unifiedDiffType);
        Collections.reverse(unifiedDiffContentId);
    }


    void calcReducedUnifiedDiff(int contextSize) {
        // The index of the next line we're going to process in fullDiff.
        int next = 0;
        // The number of lines in original/revised file after the diff lines we've processed.
        int lineNumOrigin = offsetHead;
        int lineNumRevised = offsetHead;
        while (next < unifiedDiffType.size()) {
            // The start and end index of the current block in fullDiff
            int start;
            int end;
            // The start line number of the block in original/revised file.
            int startLineOrigin;
            int startLineRevised;
            // Find the next diff line that is not an equal line.
            while (next < unifiedDiffType.size() && unifiedDiffType.get(next).equals(' ')) {
                next++;
                lineNumOrigin++;
                lineNumRevised++;
            }
            if (next == unifiedDiffType.size()) {
                break;
            }
            // Calculate the start line index of the current block in fullDiff
            start = max(0, next - contextSize);

            // Record the start line number in original and revised file of the current block
            startLineOrigin = lineNumOrigin - (next - start - 1);
            startLineRevised = lineNumRevised - (next - start - 1);

            // The number of consecutive equal lines in fullDiff, we must find at least
            // contextSize * 2 + 1 equal lines to identify the end of the block.
            int equalLines = 0;
            // Let `end` points to the last non-equal diff line
            end = next;
            while (next < unifiedDiffType.size() && equalLines < contextSize * 2 + 1) {
                if (unifiedDiffType.get(next).equals(' ')) {
                    equalLines++;
                    lineNumOrigin++;
                    lineNumRevised++;
                } else {
                    equalLines = 0;
                    // Record the latest non-equal diff line
                    end = next;
                    if (unifiedDiffType.get(next).equals('-')) {
                        lineNumOrigin++;
                    } else {
                        // line starts with "+"
                        lineNumRevised++;
                    }
                }
                next++;
            }
            // Calculate the end line index of the current block in fullDiff
            end = min(end + contextSize + 1, unifiedDiffType.size());

            // Calculate the size of the block content in original/revised file
            int blockSizeOrigin = lineNumOrigin - startLineOrigin - (next - end - 1);
            int blockSizeRevised = lineNumRevised - startLineRevised - (next - end - 1);

            StringBuilder header = new StringBuilder();
            header
                    .append("@@ -")
                    .append(startLineOrigin)
                    .append(",")
                    .append(blockSizeOrigin)
                    .append(" +")
                    .append(startLineRevised)
                    .append(",")
                    .append(blockSizeRevised)
                    .append(" @@");

            reducedUnifiedDiff.add(header.toString());
            for (int i = start; i < end; i++) {
                reducedUnifiedDiff.add(
                        unifiedDiffType.get(i) + stringList.get(unifiedDiffContentId.get(i)));
            }
        }
    }
}
