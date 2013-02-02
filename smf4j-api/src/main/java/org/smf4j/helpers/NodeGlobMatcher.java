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

import java.util.regex.Pattern;
import org.smf4j.RegistryNode;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class NodeGlobMatcher {

    private final Pattern filterPattern;

    public NodeGlobMatcher(String globPattern) {
        if(globPattern != null && !globPattern.trim().equals("")) {
            this.filterPattern = createPattern(globPattern);
        } else {
            filterPattern = null;
        }
    }

    public boolean match(RegistryNode node) {
        return filterPattern == null ||
                filterPattern.matcher(node.getName()).matches();
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
}
