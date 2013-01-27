/*
 * Copyright 2012 Russell Morris (wrussellmorris@gmail.com).
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
package org.smf4j.standalone;

import org.smf4j.InvalidNodeNameException;
import org.smf4j.RegistryNode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import org.smf4j.Calculator;
import org.smf4j.Accumulator;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
class DefaultRegistryNode implements RegistryNode {
    private static final Pattern invalidNameChars = Pattern.compile("[+*.]");

    private final DefaultRegistrar registrar;
    private final DefaultRegistryNode parent;
    private final String name;
    private final ConcurrentMap<String, Accumulator> accumulators;
    private final Map<String, Accumulator> readOnlyAccumulators;
    private final ConcurrentMap<String, Calculator> calcuations;
    private final Map<String, Calculator> readOnlyCalculations;
    private final ConcurrentMap<String, RegistryNode> childNodes;
    private final Map<String, RegistryNode> readOnlyChildNodes;
    private volatile boolean state;
    private volatile Boolean localState;

    public DefaultRegistryNode(DefaultRegistrar registrar,
            DefaultRegistryNode parent, String name) {
        this.registrar = registrar;
        this.parent = parent;
        this.accumulators = new ConcurrentHashMap<String, Accumulator>();
        this.readOnlyAccumulators =
                Collections.unmodifiableMap(accumulators);
        this.calcuations = new ConcurrentHashMap<String, Calculator>();
        this.readOnlyCalculations =
                Collections.unmodifiableMap(calcuations);
        this.childNodes = new ConcurrentHashMap<String, RegistryNode>();
        this.readOnlyChildNodes =
                Collections.unmodifiableMap(childNodes);
        this.localState = null;

        if(parent == null) {
            // We're the root node
            this.name = "";
        } else if(parent.name.equals("")) {
            // Immediate child of root node
            this.name = name;
        } else {
            // Normal node
            this.name = parent.getName() + "." + name;
        }
    }

    DefaultRegistryNode add(String name, DefaultRegistryNode child) {
        DefaultRegistryNode added = (DefaultRegistryNode)
                childNodes.putIfAbsent(name, child);
        if(added == null) {
            // We won the add - must recalculate state
            added = child;
            added.recalculateState();
        }

        return added;
    }

    boolean remove(String name, DefaultRegistryNode child) {
        return childNodes.remove(name, child);
    }

    @Override
    public boolean register(String name, Accumulator acc) {
        if(null == accumulators.putIfAbsent(name, acc)) {
            acc.setOn(isOn());
            registrar.fireAccumulatorAdded(this, acc);
            return true;
        }
        return false;
    }

    @Override
    public boolean register(String name, Calculator calc) {
        if(null == calcuations.putIfAbsent(name, calc)) {
            registrar.fireCalculatorAdded(this, calc);
            return true;
        }
        return false;
    }

    @Override
    public boolean unregister(String name, Accumulator acc) {
        if(accumulators.remove(name, acc)) {
            registrar.fireAccumulatorRemoved(this, acc);
            return true;
        }
        return false;
    }

    @Override
    public boolean unregister(String name, Calculator calc) {
        if(calcuations.remove(name, calc)) {
            registrar.fireCalculationRemoved(this, calc);
            return true;
        }
        return false;
    }

    void recalculateState() {
        registrar.stateLock.lock();
        try {
            boolean calculatedState;

            if(localState != null) {
                // If we have a local state, set it
                calculatedState = localState;
            } else {
                // If we don't have a local state set, use
                // our parent's state (or false, if we're the root)
                if(parent != null) {
                    calculatedState = parent.isOn();
                } else {
                    calculatedState = false;
                }
            }

            if(state == calculatedState) {
                // Nothing has changed...
                return;
            }

            // Set our new state now
            state = calculatedState;

            // Set the new state on all of our existing accumulators
            for(Accumulator accumulator : accumulators.values()) {
                accumulator.setOn(calculatedState);
            }

            // Our state has changed - we need to tell all of our
            // children to recalculate their states
            for(RegistryNode childNode : childNodes.values()) {
                ((DefaultRegistryNode)childNode).recalculateState();
            }
        } finally {
            registrar.stateLock.unlock();
        }
    }

    String validateAndCleanName(String name)
    throws InvalidNodeNameException {
        if(invalidNameChars.matcher(name).find()) {
            throw new InvalidNodeNameException(name,
                    "'*', '+', and '.' are not allowed in names.");
        }

        name = name.trim();
        if(name.trim().length() == 0) {
            throw new InvalidNodeNameException(name, "Names cannot be empty.");
        }
        return name;
    }

    @Override
    public Map<String, Object> snapshot() {

        // Snapshot the values for all of the accumulators
        Map<String, Long> vals = new HashMap<String, Long>();
        for(Map.Entry<String, Accumulator> entry : accumulators.entrySet()) {
            vals.put(entry.getKey(), entry.getValue().getValue());
        }

        // Run calculations with accumulator values as input
        Map<String, Object> results = new HashMap<String, Object>();
        for(Map.Entry<String, Calculator> entry : calcuations.entrySet()) {
            Object o = null;
            try {
                o = entry.getValue().calculate(vals, readOnlyAccumulators);
            } catch(Throwable t) {
                // TODO: Log
            }
            results.put(entry.getKey(), o);
        }

        // Stuff all recorded accumulator values into the results as well.
        results.putAll(vals);

        return results;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, Accumulator> getAccumulators() {
        return readOnlyAccumulators;
    }

    @Override
    public Accumulator getAccumulator(String name) {
        return accumulators.get(name);
    }

    @Override
    public Map<String, Calculator> getCalculators() {
        return readOnlyCalculations;
    }

    @Override
    public Calculator getCalculator(String name) {
        return calcuations.get(name);
    }

    @Override
    public Map<String, RegistryNode> getChildNodes() {
        return readOnlyChildNodes;
    }

    @Override
    public RegistryNode getChildNode(String name) {
        return childNodes.get(name);
    }

    @Override
    public boolean isOn() {
        if(localState != null) {
            return localState.equals(Boolean.TRUE);
        }
        return state;
    }

    @Override
    public void setOn(boolean on) {
        registrar.stateLock.lock();
        try {

            localState = on;
            recalculateState();
        } finally {
            registrar.stateLock.unlock();
        }
    }

    @Override
    public void clearOn() {
        registrar.stateLock.lock();
        try {

            localState = null;
            recalculateState();
        } finally {
            registrar.stateLock.unlock();
        }
    }
}
