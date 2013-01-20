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
import org.smf4j.DynamicFilter;
import org.smf4j.RegistryNode;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
class DefaultDynamicFilter implements DynamicFilter {

    private final Pattern filterPattern;
    private final ConcurrentLinkedQueue<RegistryNode> matchedNodes;
    private final Queue<WeakRefWithEq<DynamicFilterListener>> listeners;

    public DefaultDynamicFilter(String filterString) {
        this.filterPattern = createPattern(filterString);
        this.matchedNodes = new ConcurrentLinkedQueue<RegistryNode>();
        this.listeners =
            new ConcurrentLinkedQueue<WeakRefWithEq<DynamicFilterListener>>();
    }

    public void inspectNode(RegistryNode node, boolean adding) {
        boolean fireEvent = false;

        if(filterPattern.matcher(node.getName()).matches()) {
            if(adding) {
                fireEvent = matchedNodes.add(node);
            } else {
                fireEvent = matchedNodes.remove(node);
            }
        }

        if(!fireEvent) {
            return;
        }

        for(WeakRefWithEq<DynamicFilterListener> ref : listeners) {
            DynamicFilterListener listener = ref.get();
            if(listener != null) {
                if(adding) {
                    listener.nodeAdded(node);
                } else {
                    listener.nodeRemoved(node);
                }
            }
        }
    }

    @Override
    public void registerListener(DynamicFilterListener listener) {
        listeners.add(new WeakRefWithEq<DynamicFilterListener>(listener));
    }

    @Override
    public void unregisterListener(DynamicFilterListener listener) {
        listeners.remove(new WeakRefWithEq<DynamicFilterListener>(listener));
    }

    static final String SINGLE_NAME_CHAR = "[a-zA-Z0-9_]";
    static final String ANY_NAME_CHARS_PATTERN =
            "(" + SINGLE_NAME_CHAR + ")*";
    static final String ARBITRARY_DEPTH_PATTERN =
            "((" + SINGLE_NAME_CHAR + ")*(\\.(" + SINGLE_NAME_CHAR + ")*)*)";
    static Pattern createPattern(String filterPattern) {
        String[] parts = filterPattern.split("\\.");
        StringBuilder regexString = new StringBuilder();
        for(int i=0; i<parts.length; i++) {
            String part = parts[i].trim();
            if(part.equals("**")) {
                regexString.append(ARBITRARY_DEPTH_PATTERN);
            } else {
                part = part.replace("*", ANY_NAME_CHARS_PATTERN);
                part = part.replace("?", SINGLE_NAME_CHAR);
                regexString.append(part);
                if(i < parts.length-1) {
                    regexString.append("\\.");
                }
            }
        }

        return Pattern.compile(regexString.toString());
    }

    @Override
    public Iterator<RegistryNode> iterator() {
        final Iterator<RegistryNode> inner = matchedNodes.iterator();
        return new Iterator<RegistryNode>() {
            @Override
            public boolean hasNext() {
                return inner.hasNext();
            }
            @Override
            public RegistryNode next() {
                return inner.next();
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported.");
            }
        };
    }
}
