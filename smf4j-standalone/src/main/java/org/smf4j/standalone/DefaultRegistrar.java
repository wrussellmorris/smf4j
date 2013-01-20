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

import org.smf4j.DynamicFilterListener;
import org.smf4j.InvalidNodeNameException;
import org.smf4j.DynamicFilter;
import org.smf4j.RegistrarListener;
import org.smf4j.RegistryNode;
import org.smf4j.Registrar;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import org.smf4j.Calculator;
import org.smf4j.Accumulator;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class DefaultRegistrar implements Registrar {

    private static final Pattern validPartChars =
            Pattern.compile("[a-zA-Z0-9_]+");
    private final ReentrantLock stateLock;
    private final DefaultRegistryNode root;
    private final ConcurrentLinkedQueue<WeakRefWithEq<RegistrarListener>>
            listeners;
    private final ConcurrentLinkedQueue<WeakRefWithEq<DynamicFilter>>
            filters;
    private String name;

    public DefaultRegistrar() {
        this.stateLock = new ReentrantLock();
        this.root = new DefaultRegistryNode(stateLock, null, "");
        this.listeners =
            new ConcurrentLinkedQueue<WeakRefWithEq<RegistrarListener>>();
        this.filters =
            new ConcurrentLinkedQueue<WeakRefWithEq<DynamicFilter>>();
    }

    @Override
    public RegistryNode register(String fullNodeName)
    throws InvalidNodeNameException {
        assertFullNodeName(fullNodeName);

        // Find or create the registry node
        return findNode(fullNodeName, true);
    }

    @Override
    public RegistryNode unregister(String fullNodeName)
    throws InvalidNodeNameException {
        assertFullNodeName(fullNodeName);

        // Remove this node, if it's the same one that was
        // originally registered under this name
        RegistryNode node = removeNode(fullNodeName);
        if(node != null) {
            fireNodeRemoved(node);
        }
        return node;
    }

    @Override
    public RegistryNode getNode(String fullNodeName)
    throws InvalidNodeNameException {
        assertFullNodeName(fullNodeName);

        return findNode(fullNodeName, false);
    }

    @Override
    public RegistryNode getRootNode() {
        return root;
    }

    private void assertFullNodeName(String fullNodeName)
    throws InvalidNodeNameException {
        if(fullNodeName == null) {
            throw new NullPointerException("'name' cannot be null.");
        }
        if(fullNodeName.trim().equals("")) {
            throw new InvalidNodeNameException("'name' cannot be emptry or "
                    + "all whitespace.");
        }
    }

    @Override
    public DynamicFilter createDynamicFilter(String pattern) {
        final DefaultDynamicFilter filter = new DefaultDynamicFilter(pattern);
        dfs(root, new RegistryNodeCall() {
            @Override
            public void call(RegistryNode node) {
                filter.inspectNode(node, true);
            }
        });
        filters.add(new WeakRefWithEq<DynamicFilter>(filter));
        return filter;
    }

    @Override
    public void removeDynamicFilter(DynamicFilter dynamicFilter) {
        filters.remove(new WeakRefWithEq<DynamicFilter>(dynamicFilter));
    }

    @Override
    public void setOn(String hierarchy, boolean on)
    throws InvalidNodeNameException {
        findNode(hierarchy, true).setOn(on);
    }

    @Override
    public void clearOn(String hierarchy)
    throws InvalidNodeNameException {
        findNode(hierarchy, true).clearOn();
    }

    String[] splitFullNodeName(String fullNodeName)
    throws InvalidNodeNameException {
        assertFullNodeName(fullNodeName);

        // Trim whitespace off of full node name
        fullNodeName = fullNodeName.trim();

        // Split by dots
        String[] parts = fullNodeName.split("\\.", -1);

        // Ensure that for non-root full node names, no part of the name
        // is empty
        for(int i=0; i<parts.length; i++) {
            // Trim whitespace off of part
            parts[i] = parts[i].trim();

            if(parts[i].length() == 0) {
                // A part cannot be all whitespace (only the root node can
                // be empty).
                throw new InvalidNodeNameException(
                        parts[i],
                        "No part of a node name can be empty (or all "
                        + "whitespace).");
            } else if(!validPartChars.matcher(parts[i]).matches()) {
                throw new InvalidNodeNameException(
                    fullNodeName,
                    "A part of a node name can only consist of the "
                        + "characters 'a'-'z', 'A'-'Z', '0'-'9', and '_' .");
            }
        }

        return parts;
    }

    DefaultRegistryNode findNode(String fullNodeName, boolean create)
    throws InvalidNodeNameException {
        String[] parts = splitFullNodeName(fullNodeName);

        if(parts.length == 1 && parts[0].length() == 0) {
            return root;
        }

        DefaultRegistryNode cur = root;
        for(String part : parts) {
            DefaultRegistryNode node = (DefaultRegistryNode)
                    cur.getChildNode(part);
            if(node == null) {
                if(create) {
                    // No node yet - we need to create it
                    node = cur.add(part, new DefaultRegistryNode(stateLock, cur,
                            part));
                    fireNodeAdded(node);
                } else {
                    // Not creating non-existent nodes on this pass.
                    return null;
                }
            }

            // Next!
            cur = node;
        }
        return cur;
    }

    DefaultRegistryNode removeNode(String fullNodeName)
    throws InvalidNodeNameException {
        String[] parts = splitFullNodeName(fullNodeName);

        if(parts.length == 1 && parts[0].length() == 0) {
            // Not going to remove the root node.
            return null;
        }

        DefaultRegistryNode cur = root;
        DefaultRegistryNode parent = null;
        for(String part : parts) {
            DefaultRegistryNode node = (DefaultRegistryNode)
                    cur.getChildNode(part);

            // Next!
            parent = cur;
            cur = node;
        }

        if(cur != null) {
            if(parent.remove(parts[parts.length-1], cur)) {
                return cur;
            }
        }

        return null;
    }

    /**
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void initializationComplete() {
        fireOnInitializationComplete();
    }

    protected void fireNodeAdded(final RegistryNode node) {
        final Registrar me = this;
        eachListener(new ListenerCall() {
           @Override
            public void call(RegistrarListener listener) {
               listener.nodeAdded(me, node);
            }
        });
    }

    protected void fireNodeRemoved(final RegistryNode node) {
        final Registrar me = this;
        eachListener(new ListenerCall() {
           @Override
            public void call(RegistrarListener listener) {
               listener.nodeRemoved(me, node);
            }
        });
    }

    protected void fireAccumulatorAdded(final RegistryNode node,
            final Accumulator accumulator) {
        final Registrar me = this;
        eachListener(new ListenerCall() {
           @Override
            public void call(RegistrarListener listener) {
               listener.accumulatorAdded(me, node, accumulator);
            }
        });
    }

    protected void fireAccumulatorRemoved(final RegistryNode node,
            final Accumulator accumulator) {
        final Registrar me = this;
        eachListener(new ListenerCall() {
           @Override
            public void call(RegistrarListener listener) {
               listener.accumulatorRemoved(me, node, accumulator);
            }
        });
    }

    protected void fireCalculationAdded(final RegistryNode node,
            final Calculator calculation) {
        final Registrar me = this;
        eachListener(new ListenerCall() {
           @Override
            public void call(RegistrarListener listener) {
               listener.calculationAdded(me, node, calculation);
            }
        });
    }

    protected void fireCalculationRemoved(final RegistryNode node,
            final Calculator calculation) {
        final Registrar me = this;
        eachListener(new ListenerCall() {
           @Override
            public void call(RegistrarListener listener) {
               listener.calculationRemoved(me, node, calculation);
            }
        });
    }

    protected void fireOnInitializationComplete() {
        final Registrar me = this;
        eachListener(new ListenerCall() {
           @Override
            public void call(RegistrarListener listener) {
               listener.initializationComplete(me);
            }
        });
    }

    private void eachListener(ListenerCall call) {
        for(WeakRefWithEq<RegistrarListener> ref : listeners) {
            RegistrarListener listener = ref.get();
            if(listener == null) {
                // Already cleaned up...
                continue;
            }

            try {
                call.call(listener);
            } catch(Throwable t) {
                // You throw an exception, and you're outta here
                listeners.remove(ref);
            }
        }
    }

    @Override
    public void addRegistrationListener(RegistrarListener listener) {
        WeakRefWithEq<RegistrarListener> ref =
                new WeakRefWithEq<RegistrarListener>(listener);
        if(!listeners.contains(ref)) {
            listeners.add(ref);
        }
    }

    void dfs(RegistryNode node, RegistryNodeCall call) {
        call.call(node);
        for(RegistryNode child : node.getChildNodes().values()) {
            dfs(child, call);
        }
    }

    interface RegistryNodeCall {
        void call(RegistryNode node);
    }

    interface ListenerCall {
        void call(RegistrarListener listener);
    }

    interface FilterCall {
        void call(DynamicFilterListener listener);
    }
}
