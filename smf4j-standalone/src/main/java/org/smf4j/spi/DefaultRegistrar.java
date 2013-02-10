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
package org.smf4j.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smf4j.Accumulator;
import org.smf4j.Calculator;
import org.smf4j.helpers.NopRegistryNode;
import org.smf4j.RegistryNode;
import org.smf4j.Registrar;
import org.smf4j.helpers.GlobMatch;
import org.smf4j.helpers.GlobMatcher;
import org.smf4j.helpers.NopAccumulator;
import org.smf4j.helpers.NopCalculator;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
class DefaultRegistrar implements Registrar {
    private static final Pattern validPartChars =
            Pattern.compile("[a-zA-Z0-9_]+");
    private volatile DefaultRegistryNode root;

    private final Logger log = LoggerFactory.getLogger(DefaultRegistrar.class);
    final ReentrantLock stateLock;

    DefaultRegistrar() {
        this.stateLock = new ReentrantLock();
        this.root = new DefaultRegistryNode(this, null, "");
    }

    @Override
    public RegistryNode getRootNode() {
        return root;
    }

    @Override
    public RegistryNode getNode(String fullNodeName) {
        return findNode(fullNodeName);
    }

    @Override
    public Accumulator getAccumulator(String memberPath) {
        String[] split = splitMemberPath(memberPath);
        if(split == null) {
            return NopAccumulator.INSTANCE;
        }
        return findNode(split[0]).getAccumulator(split[1]);
    }

    @Override
    public Calculator getCalculator(String memberPath) {
        String[] split = splitMemberPath(memberPath);
        if(split == null) {
            return NopCalculator.INSTANCE;
        }
        return findNode(split[0]).getCalculator(split[1]);
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
            return getRootNode();
        }

        DefaultRegistryNode cur = (DefaultRegistryNode)getRootNode();
        for(String part : parts) {
            RegistryNode node = cur.getChildNode(part);
            if(node == NopRegistryNode.INSTANCE) {
                // No node yet - we need to create it
                node = cur.add(part, new DefaultRegistryNode(this, cur, part));
            }

            // Next!
            cur = (DefaultRegistryNode)node;
        }
        return cur;
    }

    void dfs(RegistryNode node, RegistryNodeCall call) {
        call.call(node);
        for(RegistryNode child : node.getChildNodes().values()) {
            dfs(child, call);
        }
    }

    @Override
    public Iterable<GlobMatch> match(String globPattern) {
        final ArrayList<GlobMatch> list = new ArrayList<GlobMatch>();
        final GlobMatcher matcher = new GlobMatcher(globPattern);
        dfs(getRootNode(), new MatcherCall(list, matcher));
        return list;
    }

    private String[] splitMemberPath(String memberPath) {
        if(memberPath == null) {
            return null;
        }

        String[] results = memberPath.split(":");
        if(results.length != 2) {
            return null;
        }

        return results;
    }

    interface RegistryNodeCall {
        void call(RegistryNode node);
    }

    static class MatcherCall implements RegistryNodeCall {
        private final List<GlobMatch> list;
        private final GlobMatcher matcher;

        public MatcherCall(List<GlobMatch> list, GlobMatcher matcher) {
            this.list = list;
            this.matcher = matcher;
        }

        public void call(RegistryNode node) {
            GlobMatch match = matcher.match(node);
            if(match.isNodeMatched()) {
                list.add(match);
            }
        }
    }
}
