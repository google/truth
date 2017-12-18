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

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Multimap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Helper routines related to <a href="https://en.wikipedia.org/wiki/Matching_(graph_theory)">graph
 * matchings</a>.
 *
 * @author Pete Gillin
 */
final class GraphMatching {

  /**
   * Finds a <a
   * href="https://en.wikipedia.org/wiki/Matching_(graph_theory)#In_unweighted_bipartite_graphs">
   * maximum cardinality matching of a bipartite graph</a>. The vertices of one part of the
   * bipartite graph are identified by objects of type {@code U} using object equality. The vertices
   * of the other part are similarly identified by objects of type {@code V}. The input bipartite
   * graph is represented as a {@code Multimap<U, V>}: each entry represents an edge, with the key
   * representing the vertex in the first part and the value representing the value in the second
   * part. (Note that, even if {@code U} and {@code V} are the same type, equality between a key and
   * a value has no special significance: effectively, they are in different domains.) Fails if any
   * of the vertices (keys or values) are null. The output matching is similarly represented as a
   * {@code BiMap<U, V>} (the property that a matching has no common vertices translates into the
   * bidirectional uniqueness property of the {@link BiMap}).
   *
   * <p>If there are multiple matchings which share the maximum cardinality, an arbitrary one is
   * returned.
   */
  static <U, V> ImmutableBiMap<U, V> maximumCardinalityBipartiteMatching(Multimap<U, V> graph) {
    return HopcroftKarp.overBipartiteGraph(graph).perform();
  }

  private GraphMatching() {}

  /**
   * Helper which implements the <a
   * href="https://en.wikipedia.org/wiki/Hopcroft%E2%80%93Karp_algorithm">Hopcroft–Karp</a>
   * algorithm.
   *
   * <p>The worst-case complexity is {@code O(E V^0.5)} where the graph contains {@code E} edges and
   * {@code V} vertices. For dense graphs, where {@code E} is {@code O(V^2)}, this is {@code V^2.5}
   * (and non-dense graphs perform better than dense graphs with the same number of vertices).
   */
  private static class HopcroftKarp<U, V> {

    private final Multimap<U, V> graph;

    /**
     * Factory method which returns an instance ready to perform the algorithm over the bipartite
     * graph described by the given multimap.
     */
    static <U, V> HopcroftKarp<U, V> overBipartiteGraph(Multimap<U, V> graph) {
      return new HopcroftKarp<>(graph);
    }

    private HopcroftKarp(Multimap<U, V> graph) {
      this.graph = graph;
    }

    /** Performs the algorithm, and returns a bimap describing the matching found. */
    ImmutableBiMap<U, V> perform() {
      BiMap<U, V> matching = HashBiMap.create();
      while (true) {
        // Perform the BFS as described below. This finds the length of the shortest augmenting path
        // and a guide which locates all the augmenting paths of that length.
        Map<U, Integer> layers = new HashMap<>();
        Optional<Integer> freeRhsVertexLayer = breadthFirstSearch(matching, layers);
        if (!freeRhsVertexLayer.isPresent()) {
          // The BFS failed, i.e. we found no augmenting paths. So we're done.
          break;
        }
        // Perform the DFS and update the matching as described below starting from each free LHS
        // vertex. This finds a disjoint set of augmenting paths of the shortest length and updates
        // the matching by computing the symmetric difference with that set.
        for (U lhs : graph.keySet()) {
          if (!matching.containsKey(lhs)) {
            depthFirstSearch(matching, layers, freeRhsVertexLayer.get(), lhs);
          }
        }
      }
      return ImmutableBiMap.copyOf(matching);
    }

    /**
     * Performs the Breadth-First Search phase of the algorithm. Specifically, treats the bipartite
     * graph as a directed graph where every unmatched edge (i.e. every edge not in the current
     * matching) is directed from the LHS vertex to the RHS vertex and every matched edge is
     * directed from the RHS vertex to the LHS vertex, and performs a BFS which starts from all of
     * the free LHS vertices (i.e. the LHS vertices which are not in the current matching) and stops
     * either at the end of a layer where a free RHS vertex is found or when the search is exhausted
     * if no free RHS vertex is found. Keeps track of which layer of the BFS each LHS vertex was
     * found in (for those LHS vertices visited during the BFS), so the free LHS vertices are in
     * layer 1, those reachable by following an unmatched edge from any free LHS vertex to any
     * non-free RHS vertex and then the matched edge back to a LHS vertex are in layer 2, etc. Note
     * that every path in a successful search starts with a free LHS vertex and ends with a free RHS
     * vertex, with every intermediate vertex being non-free.
     *
     * @param matching A bimap describing the matching to be used for the BFS, which is not modified
     *     by this method
     * @param layers A map to be filled with the layer of each LHS vertex visited during the BFS,
     *     which should be empty when passed into this method and will be modified by this method
     * @return The number of the layer in which the first free RHS vertex was found, if any, and the
     *     absent value if the BFS was exhausted without finding any free RHS vertex
     */
    private Optional<Integer> breadthFirstSearch(BiMap<U, V> matching, Map<U, Integer> layers) {
      Queue<U> queue = new ArrayDeque<>();
      Optional<Integer> freeRhsVertexLayer = Optional.absent();

      // Enqueue all free LHS vertices and assign them to layer 1.
      for (U lhs : graph.keySet()) {
        if (!matching.containsKey(lhs)) {
          layers.put(lhs, 1);
          queue.add(lhs);
        }
      }

      // Now proceed with the BFS.
      while (!queue.isEmpty()) {
        U lhs = queue.remove();
        int layer = layers.get(lhs);
        // If the BFS has proceeded past a layer in which a free RHS vertex was found, stop.
        if (freeRhsVertexLayer.isPresent() && layer > freeRhsVertexLayer.get()) {
          break;
        }
        // We want to consider all the unmatched edges from the current LHS vertex to the RHS, and
        // then all the matched edges from those RHS vertices back to the LHS, to find the next
        // layer of LHS vertices. We actually iterate over all edges, both matched and unmatched,
        // from the current LHS vertex: we'll just do nothing for matched edges.
        for (V rhs : graph.get(lhs)) {
          if (!matching.containsValue(rhs)) {
            // We found a free RHS vertex. Record the layer at which we found it. Since the RHS
            // vertex is free, there is no matched edge to follow. (Note that the edge from the LHS
            // to the RHS must be unmatched, because a matched edge cannot lead to a free vertex.)
            if (!freeRhsVertexLayer.isPresent()) {
              freeRhsVertexLayer = Optional.of(layer);
            }
          } else {
            // We found an RHS vertex with a matched vertex back to the LHS. If we haven't visited
            // that new LHS vertex yet, add it to the next layer. (If the edge from the LHS to the
            // RHS was matched then the matched edge from the RHS to the LHS will lead back to the
            // current LHS vertex, which has definitely been visited, so we correctly do nothing.)
            U nextLhs = matching.inverse().get(rhs);
            if (!layers.containsKey(nextLhs)) {
              layers.put(nextLhs, layer + 1);
              queue.add(nextLhs);
            }
          }
        }
      }

      return freeRhsVertexLayer;
    }

    /**
     * Performs the Depth-First Search phase of the algorithm. The DFS is guided by the BFS phase,
     * i.e. it only uses paths which were used in the BFS. That means the steps in the DFS proceed
     * from an LHS vertex via an unmatched edge to an RHS vertex and from an RHS vertex via a
     * matched edge to an LHS vertex only if that LHS vertex is one layer deeper in the BFS than the
     * previous one. It starts from the specified LHS vertex and stops either when it finds one of
     * the free RHS vertices located by the BFS or when the search is exhausted. If a free RHS
     * vertex is found then all the unmatched edges in the search path and added to the matching and
     * all the matched edges in the search path are removed from the matching; in other words, the
     * direction (which is determined by the matched/unmatched status) of every edge in the search
     * path is flipped. Note several properties of this update to the matching:
     *
     * <ul>
     *   <li>Because the search path must contain one more unmatched than matched edges, the effect
     *       of this modification is to increase the size of the matching by one.
     *   <li>This modification results in the free LHS vertex at the start of the path and the free
     *       RHS vertex at the end of the path becoming non-free, while the intermediate non-free
     *       vertices stay non-free.
     *   <li>None of the edges used in this search path may be used in any further DFS. They cannot
     *       be used in the same direction as they were in this DFS because their directions are
     *       flipped; and they cannot be used in their new directions because we only use edges
     *       leading to the next layer of the BFS and, after flipping the directions, these edges
     *       now lead to the previous layer.
     *   <li>As a consequence of the previous property, repeated invocations of this method will
     *       find only paths which were used in the BFS and which were not used in any previous DFS
     *       (i.e. the set of edges used in the paths found by repeated DFSes are disjoint).
     * </ul>
     *
     * @param matching A bimap describing the matching to be used for the BFS, which will be
     *     modified by this method as described above
     * @param layers A map giving the layer of each LHS vertex visited during the BFS, which will
     *     not be modified by this method
     * @param freeRhsVertexLayer The number of the layer in which the first free RHS vertex was
     *     found
     * @param lhs The LHS vertex from which to start the DFS
     * @return Whether or not the DFS was successful
     */
    @CanIgnoreReturnValue
    private boolean depthFirstSearch(
        BiMap<U, V> matching, Map<U, Integer> layers, int freeRhsVertexLayer, U lhs) {
      // Note that this differs from the method described in the text of the wikipedia article (at
      // time of writing) in two ways. Firstly, we proceed from a free LHS vertex to a free RHS
      // vertex in the target layer instead of the other way around, which makes no difference.
      // Secondly, we update the matching using the path found from each DFS after it is found,
      // rather than using all the paths at the end of the phase. As explained above, the effect of
      // this is that we automatically find only the disjoint set of paths, as required. This is,
      // fact, the approach taken in the pseudocode of the wikipedia article (at time of writing).
      int layer = layers.get(lhs);
      if (layer > freeRhsVertexLayer) {
        // We've gone past the target layer, so we're not going to find what we're looking for.
        return false;
      }
      // Consider every edge from this LHS vertex.
      for (V rhs : graph.get(lhs)) {
        if (!matching.containsValue(rhs)) {
          // We found a free RHS vertex. (This must have been in the target layer because, by
          // definition, no free RHS vertex is reachable in any earlier layer, and because we stop
          // when we get past that layer.) We add the unmatched edge used to get here to the
          // matching, and remove any previous matched edge leading to the LHS vertex.
          matching.forcePut(lhs, rhs);
          return true;
        } else {
          // We found a non-free RHS vertex. Follow the matched edge from that RHS vertex to find
          // the next LHS vertex.
          U nextLhs = matching.inverse().get(rhs);
          if (layers.containsKey(nextLhs) && layers.get(nextLhs) == layer + 1) {
            // The next LHS vertex is in the next layer of the BFS, so we can use this path for our
            // DFS. Recurse into the DFS.
            if (depthFirstSearch(matching, layers, freeRhsVertexLayer, nextLhs)) {
              // The DFS succeeded, and we're reversing back up the search path. At each stage we
              // put the unmatched edge from the LHS to the RHS into the matching, and remove any
              // matched edge previously leading to the LHS. The combined effect of all the
              // modifications made while reversing all the way back up the search path is to update
              // the matching as described in the javadoc.
              matching.forcePut(lhs, rhs);
              return true;
            }
          }
        }
      }
      return false;
    }
  }
}
