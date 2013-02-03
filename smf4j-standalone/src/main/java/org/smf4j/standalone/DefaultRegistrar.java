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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smf4j.helpers.NopRegistryNode;
import org.smf4j.RegistryNode;
import org.smf4j.Registrar;
import org.smf4j.helpers.NodeGlobMatcher;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class DefaultRegistrar implements Registrar {
    private static final Pattern validPartChars =
            Pattern.compile("[a-zA-Z0-9_]+");
    private volatile DefaultRegistryNode root;

    private final Logger log = LoggerFactory.getLogger(DefaultRegistrar.class);
    final ReentrantLock stateLock;

    public DefaultRegistrar() {
        this.stateLock = new ReentrantLock();
        this.root = new DefaultRegistryNode(this, null, "");
    }

    @Override
    public void clear() {
        root = new DefaultRegistryNode(this, null, "");
    }

    @Override
    public RegistryNode getNode(String fullNodeName) {
        return findNode(fullNodeName);
    }

    @Override
    public RegistryNode getRootNode() {
        return root;
    }

    @Override
    public void setOn(String fullNodeName, boolean on) {
        findNode(fullNodeName).setOn(on);
    }

    @Override
    public void clearOn(String fullNodeName) {
        findNode(fullNodeName).clearOn();
    }

    String[] splitFullNodeName(String fullNodeName) {
        if(fullNodeName == null) {
            log.warn("Error in node name: Node name is null.");
            return null;
        }

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
                log.warn("Error in node name '{}': No part of a "
                        + "node name can be empty (or all whitespace).",
                        fullNodeName);
                return null;
            } else if(!validPartChars.matcher(parts[i]).matches()) {
                log.warn("Error in node name '{}': A part of a node name can "
                        + "only consist of the characters 'a'-'z', 'A'-'Z', "
                        + "'0'-'9', and '_' .",
                        fullNodeName);
                return null;
            }
        }

        return parts;
    }

    RegistryNode findNode(String fullNodeName) {
        String[] parts = splitFullNodeName(fullNodeName);

        if(parts == null) {
            return NopRegistryNode.INSTANCE;
        }

        if(parts.length == 1 && parts[0].length() == 0) {
            return root;
        }

        DefaultRegistryNode cur = root;
        for(String part : parts) {
            DefaultRegistryNode node = (DefaultRegistryNode)
                    cur.getChildNode(part);
            if(node == null) {
                // No node yet - we need to create it
                node = cur.add(part, new DefaultRegistryNode(this, cur, part));
            }

            // Next!
            cur = node;
        }
        return cur;
    }

    void dfs(RegistryNode node, RegistryNodeCall call) {
        call.call(node);
        for(RegistryNode child : node.getChildNodes().values()) {
            dfs(child, call);
        }
    }

    public Iterable<RegistryNode> match(String globPattern) {
        final ArrayList<RegistryNode> list = new ArrayList<RegistryNode>();
        final NodeGlobMatcher matcher = new NodeGlobMatcher(globPattern);
        dfs(root, new MatcherCall(list, matcher));
        return list;
    }

    interface RegistryNodeCall {
        void call(RegistryNode node);
    }

    static class MatcherCall implements RegistryNodeCall {
        private final List<RegistryNode> list;
        private final NodeGlobMatcher matcher;

        public MatcherCall(List<RegistryNode> list, NodeGlobMatcher matcher) {
            this.list = list;
            this.matcher = matcher;
        }

        public void call(RegistryNode node) {
            if(matcher.match(node)) {
                list.add(node);
            }
        }
    }
}
