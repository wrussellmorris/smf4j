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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smf4j.Calculator;
import org.smf4j.Registrar;
import org.smf4j.RegistryNode;

/**
 * {@code GlobMatcher} parses and prepares a
 * <a href="{@docRoot}/org/smf4j/Registrar.html#GlobPattern">glob pattern</a>
 * and manages produces {@link GlobMatch} instances representing
 * {@link RegistryNode}s matched against this glob pattern.
 * <p>
 * {@link Registrar} implementations should use this class to implement their
 * {@link Registrar#match(java.lang.String) match} method.
 * </p>
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class GlobMatcher {

    /**
     * The logger used by this class
     */
    private static final Logger log =
            LoggerFactory.getLogger(GlobMatcher.class);

    // Glob patterns and their regex equivalents
    private static final String ALL_GLOB = "**";
    private static final String SINGLE_NAME_CHAR = "[a-zA-Z0-9_]";
    private static final String ANY_NAME_CHARS_PATTERN =
            "(" + SINGLE_NAME_CHAR + ")*";
    private static final String ARBITRARY_DEPTH_PATTERN =
            "((" + SINGLE_NAME_CHAR + ")*(\\.(" + SINGLE_NAME_CHAR + ")*)*)";

    /**
     * The list of all node patterns in the glob pattern.
     */
    private final List<Pattern> nodePatterns;

    /**
     * The list of all of the member patterns in the glob pattern.
     */
    private final List<Pattern> memberPatterns;

    /**
     * Creates a new instance of {@code GlobMatcher} that matches
     * {@link RegistryNode}s against the given {@code globPattern}.
     * @param globPattern The <a href="{@docRoot}/org/smf4j/Registrar.html#GlobPattern">
     *                    glob pattern</a> to match against.
     */
    public GlobMatcher(String globPattern) {
        if(globPattern == null) {
            throw new NullPointerException();
        }

        // Split pattern into (hopefully) [node patterns]:[member patterns]
        List<List<String>> splits = splitPatterns(globPattern);
        nodePatterns = new ArrayList<Pattern>();
        for(String pattern : splits.get(0)) {
            nodePatterns.add(createPattern(pattern));
        }
        memberPatterns = new ArrayList<Pattern>();
        for(String pattern : splits.get(1)) {
            memberPatterns.add(createPattern(pattern));
        }
    }

    /**
     * Creates a {@link GlobMatch} representing the matching of
     * {@code node} against this {@code GlobMatcher}'s glob pattern.
     * @param node The {@link RegistryNode} to match.
     * @return A {@link GlobMatch} representing the matching of
     *         {@code node} against this {@code GlobMatcher}'s glob pattern.
     */
    public GlobMatch match(RegistryNode node) {
        if(node == null) {
            throw new NullPointerException();
        }

        // Attempt to match the node first
        boolean nodeMatch = false;
        for(Pattern nodePattern : nodePatterns) {
            if(nodePattern.matcher(node.getName()).matches()) {
                nodeMatch = true;
                break;
            }
        }

        if(!nodeMatch) {
            // The node didn't match
            return new GlobMatch(false, node, Collections.<String>emptySet());
        }

        // Gather all accumulator and calculator attribute names
        Set<String> exsitingMemberNames = new HashSet<String>();
        Set<String> matchedMemberNames = new HashSet<String>();
        exsitingMemberNames.addAll(node.getAccumulators().keySet());
        for(Map.Entry<String, Calculator> entry : node.getCalculators()
                .entrySet()) {
            List<CalculatorProperty> calcAttrs = CalculatorHelper
                    .getCalculatorAttributes(entry.getKey(), entry.getValue());
            for(CalculatorProperty calcAttr : calcAttrs) {
                exsitingMemberNames.add(calcAttr.getName());
            }
        }

        for(Pattern memberPattern : memberPatterns) {
            for(String memberName : exsitingMemberNames) {
                if(memberPattern.matcher(memberName).matches()) {
                    matchedMemberNames.add(memberName);
                }
            }
            // No sense in attempting to match elements that we've already
            // matched
            exsitingMemberNames.removeAll(matchedMemberNames);
        }

        // And now we're done...
        return new GlobMatch(true, node, matchedMemberNames);
    }

    /**
     * Splits the given {@code globPattern} and returns a two-element
     * {@code List} of {@code List<String>}, where the first list is the
     * list of node patterns, and the second list is a list of member patterns.
     * @param globPattern The glob pattern to be split.
     * @return A two-element {@code List} of {@code List<String>}, where the
     *         first list is the list of node patterns, and the second list is
     *         a list of member patterns.
     */
    static List<List<String>> splitPatterns(String globPattern) {
        List<List<String>> result = new ArrayList<List<String>>();
        List<String> nodePatterns = new ArrayList<String>();
        List<String> memberPatterns = new ArrayList<String>();
        result.add(nodePatterns);
        result.add(memberPatterns);

        // Split the pattern into groups based on ':'
        String[] top = globPattern.split(":");
        String nodePatternsString;
        String memberPatternsString;

        if(top.length == 0) {
            // We'll just assume they mean everything...
            nodePatternsString = ALL_GLOB;
            memberPatternsString = ALL_GLOB;
        } else if(top.length == 1) {
            // If there's just one, we'll assume that it's the node pattern
            nodePatternsString = top[0].trim();
            memberPatternsString = ALL_GLOB;
        } else {
            // There's at least 2, we'll interpret them as
            // [node filters]:[member filters]
            nodePatternsString = top[0].trim();
            memberPatternsString = top[1].trim();

            if(top.length > 2) {
                // They've formatted the filter poorly, but let's just log
                // it as an error instead of totally freaking out...
                log.error("The globPattern '{}' contains too many parts.",
                        globPattern);
            }
        }

        // Split up each of the pattern types based on comma
        for(String nodePatternString : nodePatternsString.split(",")) {
            String tmp = nodePatternString.trim();
            if(tmp.length() > 0) {
                nodePatterns.add(tmp);
            } else {
                log.error("The globPattern '{}' contains an empty "
                        + "node filter expression.");
            }
        }
        for(String memberPatternString : memberPatternsString.split(",")) {
            String tmp = memberPatternString.trim();
            if(tmp.length() > 0) {
                memberPatterns.add(tmp);
            } else {
                log.error("The globPattern '{}' contains an empty "
                        + "node filter expression.");
            }
        }

        // Make sure that if they did something bizarre they'll end up
        // with everything.
        if(nodePatterns.isEmpty()) {
            nodePatterns.add(ALL_GLOB);
        }
        if(memberPatterns.isEmpty()) {
            memberPatterns.add(ALL_GLOB);
        }

        return result;
    }

    /**
     * Creates a {@link Pattern} that will match against {@code filterPattern}.
     * @param filterPattern The filter pattern.
     * @return A {@link Pattern} that will match against {@code filterPattern}.
     */
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
