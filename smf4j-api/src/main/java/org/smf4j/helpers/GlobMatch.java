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
import org.smf4j.RegistryNode;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class GlobMatch implements RegistryNode {
    private final static Set<String> EMPTY_MEMBERS_SET = Collections.emptySet();
    private final static Map<String, RegistryNode> EMPTY_CHILD_MAP =
            Collections.emptyMap();
    private final RegistryNode node;
    private final Set<String> members;
    private final Set<String> rootMembers;
    private final Map<String, Accumulator> accs;
    private final Map<String, Calculator> calcs;
    private final Map<String, Accumulator> roAccs;
    private final Map<String, Calculator> roCalcs;

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

    public RegistryNode getNode() {
        return node;
    }

    public Set<String> getMemberNames() {
        return members;
    }

    public boolean isNodeMatched() {
        return node != null;
    }

    public boolean isMembersMatched() {
        return !members.isEmpty();
    }

    @Override
    public String getName() {
        return node == null ? "" : node.getName();
    }

    @Override
    public Accumulator register(String name, Accumulator accumulator) {
        return NopAccumulator.INSTANCE;
    }

    @Override
    public Calculator register(String name, Calculator calculator) {
        return NopCalculator.INSTANCE;
    }

    @Override
    public boolean unregister(String name, Accumulator accumulator) {
        return false;
    }

    @Override
    public boolean unregister(String name, Calculator calculator) {
        return false;
    }

    @Override
    public Map<String, Accumulator> getAccumulators() {
        return roAccs;
    }

    @Override
    public Map<String, Calculator> getCalculators() {
        return roCalcs;
    }

    @Override
    public Accumulator getAccumulator(String name) {
        if(node == null || !rootMembers.contains(name)) {
            return NopAccumulator.INSTANCE;
        }
        return  node.getAccumulator(name);
    }

    @Override
    public Calculator getCalculator(String name) {
        if(node == null || !rootMembers.contains(name)) {
            return NopCalculator.INSTANCE;
        }
        return node.getCalculator(name);
    }

    @Override
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

    @Override
    public Map<String, RegistryNode> getChildNodes() {
        if(node == null) {
            return EMPTY_CHILD_MAP;
        }
        return node.getChildNodes();
    }

    @Override
    public RegistryNode getChildNode(String name) {
        return node == null || !rootMembers.contains(name)
                ? NopRegistryNode.INSTANCE : node.getChildNode(name);
    }

    @Override
    public boolean isOn() {
        return node == null ? false : node.isOn();
    }

    @Override
    public void setOn(boolean on) {
        if(node != null) {
            node.setOn(on);
        }
    }

    @Override
    public void clearOn() {
        if(node != null) {
            node.clearOn();
        }
    }
}
