/*
 * Copyright (c) 2011 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.common.truth;

import static com.google.common.truth.GraphMatching.maximumCardinalityBipartiteMatching;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.fail;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.Map;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link GraphMatching}.
 *
 * @author Pete Gillin
 */
@RunWith(JUnit4.class)
public final class GraphMatchingTest {

  @Test
  public void maximumCardinalityBipartiteMatching_empty() {
    TestInstance.empty().testAgainstKnownSize(0);
  }

  @Test
  public void maximumCardinalityBipartiteMatching_exhaustive3x4() {
    for (int edgeCombination = 1; edgeCombination < (1L << (3 * 4)); edgeCombination++) {
      TestInstance.fromBits(3, 4, intBits(edgeCombination)).testAgainstBruteForce();
    }
  }

  @Test
  @GwtIncompatible("slow")
  public void maximumCardinalityBipartiteMatching_exhaustive4x4() {
    if (Platform.isAndroid()) {
      return; // slow
    }
    for (int edgeCombination = 1; edgeCombination < (1L << (4 * 4)); edgeCombination++) {
      TestInstance.fromBits(4, 4, intBits(edgeCombination)).testAgainstBruteForce();
    }
  }

  @Test
  @GwtIncompatible("slow")
  public void maximumCardinalityBipartiteMatching_exhaustive3x5() {
    if (Platform.isAndroid()) {
      return; // slow
    }
    for (int edgeCombination = 1; edgeCombination < (1L << (3 * 5)); edgeCombination++) {
      TestInstance.fromBits(3, 5, intBits(edgeCombination)).testAgainstBruteForce();
    }
  }

  @Test
  @GwtIncompatible("slow")
  public void maximumCardinalityBipartiteMatching_exhaustive5x3() {
    if (Platform.isAndroid()) {
      return; // slow
    }
    for (int edgeCombination = 1; edgeCombination < (1L << (5 * 3)); edgeCombination++) {
      TestInstance.fromBits(5, 3, intBits(edgeCombination)).testAgainstBruteForce();
    }
  }

  @Test
  public void maximumCardinalityBipartiteMatching_fullyConnected8x8() {
    TestInstance.fullyConnected(8, 8).testAgainstKnownSize(8);
  }

  @Test
  public void maximumCardinalityBipartiteMatching_random8x8() {
    Random rng = new Random(0x5ca1ab1e);
    for (int i = 0; i < 100; i++) {
      // Set each bit with probability 0.25, giving an average of 2 of the possible 8 edges per
      // vertex. By observation, the maximal matching most commonly has cardinality 6 (although
      // occasionally you do see a complete matching i.e. cardinality 8).
      TestInstance.fromBits(8, 8, randomBits(8 * 8, 0.25, rng)).testAgainstBruteForce();
    }
  }

  @Test
  public void maximumCardinalityBipartiteMatching_randomSparse8x8() {
    Random rng = new Random(0x0ddba11);
    for (int i = 0; i < 100; i++) {
      // Set each bit with probability 0.125, giving an average of 1 of the possible 8 edges per
      // vertex. By observation, the maximal matching most commonly has cardinality 4.
      TestInstance.fromBits(8, 8, randomBits(8 * 8, 0.125, rng)).testAgainstBruteForce();
    }
  }

  @Test
  @GwtIncompatible("slow")
  public void maximumCardinalityBipartiteMatching_randomDense8x8() {
    if (Platform.isAndroid()) {
      return; // slow
    }
    Random rng = new Random(0x5add1e5);
    for (int i = 0; i < 100; i++) {
      // Set each bit with probability 0.5, giving an average of 4 of the possible 8 edges per
      // vertex. By observation, a complete matching is almost always possible (although
      // occasionally you do see a maximum cardinality of 7 or even fewer).
      TestInstance.fromBits(8, 8, randomBits(8 * 8, 0.5, rng)).testAgainstBruteForce();
    }
  }

  @Test
  public void maximumCardinalityBipartiteMatching_failsWithNullLhs() {
    ListMultimap<String, String> edges = LinkedListMultimap.create();
    edges.put(null, "R1");
    try {
      BiMap<String, String> unused = maximumCardinalityBipartiteMatching(edges);
      fail("Should have thrown.");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void maximumCardinalityBipartiteMatching_failsWithNullRhs() {
    ListMultimap<String, String> edges = LinkedListMultimap.create();
    edges.put("L1", null);
    try {
      BiMap<String, String> unused = maximumCardinalityBipartiteMatching(edges);
      fail("Should have thrown.");
    } catch (NullPointerException expected) {
    }
  }

  /** Representation of a bipartite graph to be used for testing. */
  private static class TestInstance {

    /** Generates a test instance with an empty bipartite graph. */
    static TestInstance empty() {
      return new TestInstance(ImmutableListMultimap.<String, String>of());
    }

    /**
     * Generates a test instance with a fully-connected bipartite graph where there are {@code
     * lhsSize} elements in one set of vertices (which we call the LHS) and {@code rhsSize} elements
     * in the other (the RHS).
     */
    static TestInstance fullyConnected(int lhsSize, int rhsSize) {
      ImmutableListMultimap.Builder<String, String> edges = ImmutableListMultimap.builder();
      for (int lhs = 0; lhs < lhsSize; lhs++) {
        for (int rhs = 0; rhs < rhsSize; rhs++) {
          edges.put("L" + lhs, "R" + rhs);
        }
      }
      return new TestInstance(edges.build());
    }

    /**
     * Generates a test instance with a bipartite graph where there are {@code lhsSize} elements in
     * one set of vertices (which we call the LHS) and {@code rhsSize} elements in the other (the
     * RHS) and whether or not each of the {@code lhsSize * rhsSize} possible edges is included or
     * not according to whether one of the first {@code lhsSize * rhsSize} bits of {@code bits} is
     * set or not.
     */
    static TestInstance fromBits(int lhsSize, int rhsSize, BitSet bits) {
      ImmutableListMultimap.Builder<String, String> edges = ImmutableListMultimap.builder();
      for (int lhs = 0; lhs < lhsSize; lhs++) {
        for (int rhs = 0; rhs < rhsSize; rhs++) {
          if (bits.get(lhs * rhsSize + rhs)) {
            edges.put("L" + lhs, "R" + rhs);
          }
        }
      }
      return new TestInstance(edges.build());
    }

    private final ImmutableListMultimap<String, String> edges;
    private final ImmutableList<String> lhsVertices;

    private TestInstance(ImmutableListMultimap<String, String> edges) {
      this.edges = edges;
      this.lhsVertices = edges.keySet().asList();
    }

    /**
     * Finds the maximum bipartite matching using the method under test and asserts both that it is
     * actually a matching of this bipartite graph and that it has the same size as a maximum
     * bipartite matching found by a brute-force approach.
     */
    void testAgainstBruteForce() {
      ImmutableBiMap<String, String> actual = maximumCardinalityBipartiteMatching(edges);
      for (Map.Entry<String, String> entry : actual.entrySet()) {
        assertWithMessage(
                "The returned bimap <%s> was not a matching of the bipartite graph <%s>",
                actual, edges)
            .that(edges)
            .containsEntry(entry.getKey(), entry.getValue());
      }
      ImmutableBiMap<String, String> expected = bruteForceMaximalMatching();
      assertWithMessage(
              "The returned matching for the bipartite graph <%s> was not the same size as "
                  + "the brute-force maximal matching <%s>",
              edges, expected)
          .that(actual)
          .hasSize(expected.size());
    }

    /**
     * Finds the maximum bipartite matching using the method under test and asserts both that it is
     * actually a matching of this bipartite graph and that it has the expected size.
     */
    void testAgainstKnownSize(int expectedSize) {
      ImmutableBiMap<String, String> actual = maximumCardinalityBipartiteMatching(edges);
      for (Map.Entry<String, String> entry : actual.entrySet()) {
        assertWithMessage(
                "The returned bimap <%s> was not a matching of the bipartite graph <%s>",
                actual, edges)
            .that(edges)
            .containsEntry(entry.getKey(), entry.getValue());
      }
      assertWithMessage(
              "The returned matching for the bipartite graph <%s> had the wrong size", edges)
          .that(actual)
          .hasSize(expectedSize);
    }

    /**
     * Returns a maximal bipartite matching of the bipartite graph, performing a brute force
     * evaluation of every possible matching.
     */
    private ImmutableBiMap<String, String> bruteForceMaximalMatching() {
      ImmutableBiMap<String, String> best = ImmutableBiMap.of();
      Matching candidate = new Matching();
      while (candidate.valid()) {
        if (candidate.size() > best.size()) {
          best = candidate.asBiMap();
        }
        candidate.advance();
      }
      return best;
    }

    /**
     * Mutable representation of a non-empty matching over the graph. This is a cursor which can be
     * advanced through the possible matchings in a fixed sequence. When advanced past the last
     * matching in the sequence, this cursor is considered invalid.
     */
    private class Matching {

      private final Deque<Edge> edgeStack;
      private final BiMap<String, String> selectedEdges;

      /** Constructs the first non-empty matching in the sequence. */
      Matching() {
        this.edgeStack = new ArrayDeque<Edge>();
        this.selectedEdges = HashBiMap.create();
        if (!edges.isEmpty()) {
          Edge firstEdge = new Edge();
          edgeStack.addLast(firstEdge);
          firstEdge.addToSelected();
        }
      }

      /**
       * Returns whether this cursor is valid. Returns true if it has been advanced past the end of
       * the sequence.
       */
      boolean valid() {
        // When advance() has advanced through all the non-empty maps, the final state is that
        // selectedEdges is empty, so we use that state as a marker of the final invalid cursor.
        return !selectedEdges.isEmpty();
      }

      /**
       * Returns an immutable representation of the current state of the matching as a bimap giving
       * the edges used in the matching, where the keys identify the vertices in the first set and
       * the values identify the vertices in the second set. The bimap is guaranteed not to be
       * empty. Fails if this cursor is invalid.
       */
      ImmutableBiMap<String, String> asBiMap() {
        Preconditions.checkState(valid());
        return ImmutableBiMap.copyOf(selectedEdges);
      }

      /**
       * Returns the size (i.e. the number of edges in) the current matching, which is guaranteed to
       * be positive (not zer). Fails if this cursor is invalid.
       */
      int size() {
        Preconditions.checkState(valid());
        return selectedEdges.size();
      }

      /**
       * Advances to the next matching in the sequence, or invalidates the cursor if this was the
       * last. Fails if this cursor is invalid.
       */
      void advance() {
        Preconditions.checkState(valid());
        // We essentially do a depth-first traversal through the possible matchings.
        // First we try to add an edge.
        Edge lastEdge = edgeStack.getLast();
        Edge nextEdge = new Edge(lastEdge);
        nextEdge.advance();
        if (nextEdge.valid()) {
          edgeStack.addLast(nextEdge);
          nextEdge.addToSelected();
          return;
        }
        // We can't add an edge, so we try to advance the edge at the top of the stack. If we can't
        // advance that edge, we remove it and attempt to advance the new top of stack instead.
        while (valid()) {
          lastEdge = edgeStack.getLast();
          lastEdge.removeFromSelected();
          lastEdge.advance();
          if (lastEdge.valid()) {
            lastEdge.addToSelected();
            return;
          } else {
            edgeStack.removeLast();
          }
        }
        // We have reached the end of the sequence, and edgeStack is empty.
      }

      /**
       * Mutable representation of an edge in a matching. This is a cursor which can be advanced
       * through the possible edges in a fixed sequence. When advanced past the last edge in the
       * sequence, this cursor is considered invalid.
       */
      private class Edge {

        private int lhsIndex; // index into lhsVertices
        private int rhsIndexForLhs; // index into edges.get(lhsVertices.get(lhsIndex))

        /** Constructs the first edge in the sequence. */
        Edge() {
          this.lhsIndex = 0;
          this.rhsIndexForLhs = 0;
        }

        /** Constructs a copy of the given edge. */
        Edge(Edge other) {
          this.lhsIndex = other.lhsIndex;
          this.rhsIndexForLhs = other.rhsIndexForLhs;
        }

        /**
         * Returns whether this cursor is valid. Returns true if it has been advanced past the end
         * of the sequence.
         */
        boolean valid() {
          // When advance() has advanced through all the edges, the final state is that lhsIndex ==
          // lhsVertices.size(), so we use that state as a marker of the final invalid cursor.
          return lhsIndex < lhsVertices.size();
        }

        /**
         * Adds the current edge to the matching. Fails if either of the vertices in the edge is
         * already in the matching. Fails if this cursor is invalid.
         */
        void addToSelected() {
          Preconditions.checkState(valid());
          Preconditions.checkState(!selectedEdges.containsKey(lhsVertex()));
          Preconditions.checkState(!selectedEdges.containsValue(rhsVertex()));
          selectedEdges.put(lhsVertex(), rhsVertex());
        }

        /**
         * Removes the current edge from the matching. Fails if this edge is not in the matching.
         * Fails if this cursor is invalid.
         */
        void removeFromSelected() {
          Preconditions.checkState(valid());
          Preconditions.checkState(selectedEdges.containsKey(lhsVertex()));
          Preconditions.checkState(selectedEdges.get(lhsVertex()).equals(rhsVertex()));
          selectedEdges.remove(lhsVertex());
        }

        /**
         * Advances to the next edge in the sequence, or invalidates the cursor if this was the
         * last. Skips over edges which cannot be added to the matching because either vertex is
         * already in it. Fails if this cursor is invalid.
         */
        void advance() {
          Preconditions.checkState(valid());
          // We iterate over the possible edges in a lexicographical order with the LHS index as the
          // most significant part and the RHS index as the least significant. So we first try
          // advancing to the next RHS index for the current LHS index, and if we can't we advance
          // to the next LHS index in the map and the first RHS index for that.
          ++rhsIndexForLhs;
          while (lhsIndex < lhsVertices.size()) {
            if (!selectedEdges.containsKey(lhsVertex())) {
              while (rhsIndexForLhs < edges.get(lhsVertex()).size()) {
                if (!selectedEdges.containsValue(rhsVertex())) {
                  return;
                }
                ++rhsIndexForLhs;
              }
            }
            ++lhsIndex;
            rhsIndexForLhs = 0;
          }
          // We have reached the end of the sequence, and lhsIndex == lhsVertices.size().
        }

        private String lhsVertex() {
          return lhsVertices.get(lhsIndex);
        }

        private String rhsVertex() {
          return edges.get(lhsVertex()).get(rhsIndexForLhs);
        }
      }
    }
  }

  /** Returns a bitset corresponding to the binary representation of the given integer. */
  private static BitSet intBits(int intValue) {
    BitSet bits = new BitSet();
    for (int bitIndex = 0; bitIndex < Integer.SIZE; bitIndex++) {
      bits.set(bitIndex, (intValue & (1L << bitIndex)) != 0);
    }
    return bits;
  }

  /**
   * Returns a bitset of up to {@code maxBits} bits where each bit is set with a probability {@code
   * bitProbability} using the given RNG.
   */
  private static BitSet randomBits(int maxBits, double bitProbability, Random rng) {
    BitSet bits = new BitSet();
    for (int bitIndex = 0; bitIndex < maxBits; bitIndex++) {
      bits.set(bitIndex, rng.nextDouble() < bitProbability);
    }
    return bits;
  }
}
