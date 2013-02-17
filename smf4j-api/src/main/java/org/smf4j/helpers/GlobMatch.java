/*
 * Copyright 2013 Russell Morris (wrussellmorris@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smf4j.helpers;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.smf4j.Accumulator;
import org.smf4j.Calculator;
import org.smf4j.Registrar;
import org.smf4j.RegistryNode;

/**
 * {@code GlobMatch} represents the results of matching a single
 * {@link RegistryNode} against a <a href="{@docRoot}/org/smf4j/Registrar.html#GlobPattern">
 * .
 * <p>
 * {@GlobMatch} implements {@link RegistryNode}, and can thus be used in place
 * of one.  Instances are usually acquired by calling
 * {@link Registrar#match(java.lang.String) Registrar.match}.
 * </p>
 * <p>
 * If {@link #isNodeMatched()} is {@code true}, then the node represented by
 * this {@code GlobMatch} has matched the node portion of the glob  pattern.  If
 * {@link #isMembersMatched()} is {@code true}, then the node has matched the
 * node portion of the glob pattern, and at least one of the node's member
 * {@link Accumulator}s or {@link Calculator}s has also matched the member
 * portion of the glob pattern.
 * </p>
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class GlobMatch implements RegistryNode {
    /**
     * An empty set we use to indicate that no members matched.
     */
    private final static Set<String> EMPTY_MEMBERS_SET = Collections.emptySet();

    /**
     * An empty set returned when the node is queried about its child nodes.
     */
    private final static Map<String, RegistryNode> EMPTY_CHILD_MAP =
            Collections.emptyMap();

    /**
     * The node matched against.
     */
    private final RegistryNode node;

    /**
     * The list of member names that were matched.
     */
    private final Set<String> members;

    /**
     * The list of member names, with calculator properties collapsed into
     * just one calculator name.
     */
    private final Set<String> rootMembers;

    /**
     * A map of our matched accumulators.
     */
    private final Map<String, Accumulator> accs;

    /**
     * A map of our matched calculators.
     */
    private final Map<String, Calculator> calcs;

    /**
     * A read-only view of accs, suitable for returning to clients.
     */
    private final Map<String, Accumulator> roAccs;

    /**
     * A read-only view of calcs, suitable for returning to clients.
     */
    private final Map<String, Calculator> roCalcs;

    /**
     * Creates a new instance of {@code GlobMatch}, against the given
     * {@code node} and matching the given {@code members}.
     * @param node The node against which the matching was made.
     * @param members The set of members matched, including property names on
     *                calculator-returned non-integral types.
     */
    GlobMatch(RegistryNode node, Set<String> members) {
        this.node = node;
        this.members = members == null ? EMPTY_MEMBERS_SET : members;
        this.accs = new HashMap<String, Accumulator>();
        this.calcs = new HashMap<String, Calculator>();
        this.roAccs = Collections.unmodifiableMap(accs);
        this.roCalcs = Collections.unmodifiableMap(calcs);

        rootMembers = new HashSet<String>();
        for(String member : this.members) {
            // Since we may have a bunch of one single calculator's
            // sub properties in members, we'll extract the name of
            // the calculator itself alone.
            int i = member.indexOf('.');
            if(i != -1) {
                member = member.substring(0,i);
            }
            rootMembers.add(member);
        }

        if(node != null) {
            for(String member : rootMembers) {
                Accumulator acc = node.getAccumulator(member);
                Calculator calc = node.getCalculator(member);
                if(acc != NopAccumulator.INSTANCE) {
                    accs.put(member, acc);
                } else if(calc != NopCalculator.INSTANCE) {
                    calcs.put(member, calc);
                }
            }
        }
    }

    /**
     * Gets the {@link RegistryNode} against which matching was performed.
     * @return The {@link RegistryNode} against which matching was performed.
     */
    public RegistryNode getNode() {
        return node;
    }

    /**
     * Gets the set of member names matched, including {@link Accumulator}
     * names, and detailed {@link Calculator} property names.
     * @return The set of member names matched, including {@link Accumulator}
     *         names, and detailed {@link Calculator} property names.
     */
    public Set<String> getMemberNames() {
        return members;
    }

    /**
     * Indicates whether the {@link RegistryNode} involved in this match
     * successfully matched the node portion of the glob pattern.
     * @return A {@code boolean} indicating whether or not the
     *         {@link RegistryNode} involved in this match successfully matched
     *         the node portion of the glob pattern.
     */
    public boolean isNodeMatched() {
        return node != null;
    }

    /**
     * Indicates whether the any of the members of {@link RegistryNode} involved
     * in this match successfully matched the member portion of the glob
     * pattern.
     * @return A {@code boolean} indicating whether or not any members of the
     *         {@link RegistryNode} involved in this match successfully matched
     *         the member portion of the glob pattern.
     */
    public boolean isMembersMatched() {
        return !members.isEmpty();
    }

    /**
     * Gets the name of the {@link RegistryNode} if matched, or {@code null} if
     * the node did not match the node portion of the glob pattern.
     * @return The name of the {@link RegistryNode} if matched, or {@code null}
     *         if the node did not match the node portion of the glob pattern.
     */
    public String getName() {
        return node == null ? "" : node.getName();
    }

    /**
     * Always returns {@code null}, as {@link GlobMatch} does not allow
     * registration.
     * @param name na
     * @param accumulator na
     * @return Always returns {@code null}, as {@link GlobMatch} does not allow
     *         registration.
     */
    public Accumulator register(String name, Accumulator accumulator) {
        return NopAccumulator.INSTANCE;
    }

    /**
     * Always returns {@code null}, as {@link GlobMatch} does not allow
     * registration.
     * @param name na
     * @param calculator na
     * @return Always returns {@code null}, as {@link GlobMatch} does not allow
     *         registration.
     */
    public Calculator register(String name, Calculator calculator) {
        return NopCalculator.INSTANCE;
    }

    /**
     * Always returns {@code false}, as {@link GlobMatch} does not allow
     * unregistration.
     * @param name na
     * @param accumulator na
     * @return Always returns {@code false}, as {@link GlobMatch} does not allow
     *         unregistration.
     */
    public boolean unregister(String name, Accumulator accumulator) {
        return false;
    }

    /**
     * Always returns {@code false}, as {@link GlobMatch} does not allow
     * unregistration.
     * @param name na
     * @param calculator na
     * @return Always returns {@code false}, as {@link GlobMatch} does not allow
     *         unregistration.
     */
    public boolean unregister(String name, Calculator calculator) {
        return false;
    }

    /**
     * Gets a {@code Map} of {@code String->Accumulator} for all
     * {@link Accumulator}s that matched the member portion of the glob pattern.
     * @return A {@code Map} of {@code String->Accumulator} for all
     * {@link Accumulator}s that matched the member portion of the glob pattern.
     */
    public Map<String, Accumulator> getAccumulators() {
        return roAccs;
    }

    /**
     * Gets a {@code Map} of {@code String->Calculator} for all
     * {@link Calculator}s that matched the member portion of the glob pattern.
     * @return A {@code Map} of {@code String->Calculator} for all
     * {@link Calculator}s that matched the member portion of the glob pattern.
     */
    public Map<String, Calculator> getCalculators() {
        return roCalcs;
    }

    /**
     * Gets the named {@link Accumulator}, or {@link NopAccumulator#INSTANCE}
     * if it does not exist, or does not match the glob pattern.
     * @param name The name of the {@link Accumulator}.
     * @return The named {@link Accumulator}, or {@link NopAccumulator#INSTANCE}
     *         if it does not exist, or does not match the glob pattern.
     */
    public Accumulator getAccumulator(String name) {
        if(node == null || !rootMembers.contains(name)) {
            return NopAccumulator.INSTANCE;
        }
        return  node.getAccumulator(name);
    }

    /**
     * Gets the named {@link Calculator}, or {@link NopCalculator#INSTANCE}
     * if it does not exist, or does not match the glob pattern.
     * @param name The name of the {@link Calculator}.
     * @return The named {@link Calculator}, or {@link NopCalculator#INSTANCE}
     *         if it does not exist, or does not match the glob pattern.
     */
    public Calculator getCalculator(String name) {
        if(node == null || !rootMembers.contains(name)) {
            return NopCalculator.INSTANCE;
        }
        return node.getCalculator(name);
    }

    /**
     * Takes a snapshot of the {@link RegistryNode}'s state, but only includes
     * {@link Accumulator}s and {@link Calculator}s that matched the member
     * portion of the glob pattern.
     * @return Returns a {@code Map} of {@code name->Object} recording the
     *         output of matched {@link Accumulator}s' {@link Accumulator#get()}
     *         results and matched {@link Calculator}s'
     *         {@link Calculator#calculate(java.util.Map, java.util.Map) Calculator.calculate}
     *         results.
     */
    public Map<String, Object> snapshot() {
        Map<String, Object> snapshot = new HashMap<String, Object>();
        if(node == null) {
            return snapshot;
        }

        Map<String, Object> inner = node.snapshot();
        for(String name : accs.keySet()) {
            snapshot.put(name, inner.get(name));
        }
        for(String name : calcs.keySet()) {
            snapshot.put(name, inner.get(name));
        }

        return snapshot;
    }

    /**
     * Gets a {@code Map} of {@code String->RegistryNode} of this
     * {@link RegistryNode}s child nodes.
     * <p>
     * The members of this {@code Map} are <strong>NOT</strong> instances of
     * {@code GlobMatch}.
     * </p>
     * @return A {@code Map} of {@code String->RegistryNode} of this
     * {@link RegistryNode}s child nodes.
     */
    public Map<String, RegistryNode> getChildNodes() {
        if(node == null) {
            return EMPTY_CHILD_MAP;
        }
        return node.getChildNodes();
    }

    /**
     * Gets the child {@link RegistryNode} named {@code name}, or
     * {@link NopRegistryNode#INSTANCE} if the node did not match the node
     * portion of the glob pattern.
     * @param name The name of the child {@link RegistryNode}.
     * @return The child {@link RegistryNode} named {@code name}, or
     *         {@link NopRegistryNode#INSTANCE} if the node did not match the
     *         node portion of the glob pattern.
     */
    public RegistryNode getChildNode(String name) {
        return node == null || !rootMembers.contains(name)
                ? NopRegistryNode.INSTANCE : node.getChildNode(name);
    }

    /**
     * Indicates whether or not the matched {@link RegistryNode} is on, or
     * {@code false} if the {@link RegistryNode} did not match the node portion
     * of the glob pattern.
     * @return A {@code boolean} value that indicates whether or not the
     *         matched {@link RegistryNode} is on, or {@code false} if the
     *         {@link RegistryNode} did not match the node portion
     *         of the glob pattern.
     */
    public boolean isOn() {
        return node == null ? false : node.isOn();
    }

    /**
     * Sets the matched {@link RegistryNode}'s on/off status as long as it
     * matched the node portion of the glob pattern.
     * @param on Whether the node should be on ({@code true}) or off
     *           ({@code false}).
     */
    public void setOn(boolean on) {
        if(node != null) {
            node.setOn(on);
        }
    }

    /**
     * Forces the matched {@link RegistryNode} to follow its parent's on/off
     * state.
     */
    public void clearOn() {
        if(node != null) {
            node.clearOn();
        }
    }
}
