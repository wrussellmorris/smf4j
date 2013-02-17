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
package org.smf4j;

import org.smf4j.helpers.GlobMatch;
import org.smf4j.helpers.NopAccumulator;
import org.smf4j.helpers.NopCalculator;
import org.smf4j.helpers.NopRegistryNode;

/**
 * {@code Registrar} implementations manage a hierarchy of {@link RegistryNode}s
 * and serve as the centralized mechanism for registering them, finding them,
 * and turning them off and on.
 * <p>
 * Similar to {@code LoggerFactory} implementations, {@code Registrar} serves
 * as a central place to go to get a {@link RegistryNode}, {@link Accumulator}
 * or {@link Calculator}.  In the case of {@link RegistryNode}, the
 * {@code Registrar} will create {@code RegistryNode}s if necessary to fulfill
 * the caller's request.
 * </p>
 * <p>
 * Under <strong>all</strong> circumstances, {@code Registrar} implementations are
 * <strong>required</strong> to return viable object instances when clients call
 * {@link #getRootNode() getRootNode},
 * {@link #getNode(java.lang.String) getNode},
 * {@link #getAccumulator(java.lang.String) getAccumulator}, or
 * {@link #getCalculator(java.lang.String) getCalculator}, regardless of any
 * errors that may be encountered when attempting to find and/or create the
 * requested object.  When the {@code Registrar} cannot respond with exactly
 * what the caller was asking for, it should instead return the appropriate
 * {@code no-op} instance {@link NopRegistryNode#INSTANCE},
 * {@link NopAccumulator#INSTANCE}, or {@link NopCalculator#INSTANCE}.
 * </p>
 * <p>
 * Unlike typical logging frameworks, the set of {@link RegistryNode}s and their
 * associated {@link Accumulator}s and {@link Calculator}s are not typically
 * created in an ad-hoc fashion, and most classes in an application will not
 * have need of them.  Instead, the application or library designer will
 * design the expected layout of these things, and initialize the
 * {@code Registrar} with them during application initialization, or communicate
 * through documentation the expected layout of them.
 * </p>
 * <a id="NodeNameAndPath" />
 * <h4>Node Names and Paths</h4>
 * A {@code RegistryNode}'s <em>name</em> consists of word characters (as in
 * the regular expression {@code [\w_]+}).  A {@code RegistryNode}'s path is the
 * dot-separated names of all of its parent {@code ResgistryNode}s, as in
 * <code><em>grandparent</em>.<em>parent</em>.<em>node</em></code>.
 * <dl>
 * <dt>{@code RegistryNode} name</dt>
 * <dd><strong>must</strong> match the pattern <strong>{@code [\w_]+}</strong>.</dd>
 * <dt>{@code RegistryNode} path</dt>
 * <dd><strong>must</strong> match the pattern <strong>{@code [\w_]+(\.[\w_]+)*}</strong>.</dd>
 * </dl>
 * <a id="AccumulatorAndCalculatorName" />
 * <h4>Member Names</h4>
 * {@code RegistryNode}s can contain named {@code Accumulator}s and
 * {@code Calculator}s.  Their <em>name</em> consists of word characters (as in
 * the regular expression {@code [\w_]+}).
 * <dl>
 * <dt>{@code Accumulator} and {@code Calculator} name</dt>
 * <dd><strong>must</strong> match the pattern <strong>{@code [\w_]+}</strong>.</dd>
 * </dl>
 * <a id="MemberPath" />
 * <h4>Member Paths</h4>
 * A <em>Member Path</em> identifies a specific {@code Accumulator} or
 * {@code Calculator} in a specific {@code RegistryNode}, and uses the form
 * <code><em>node_path</em>:<em>accumulator_or_calculator_name</em></code>
 * <dl>
 * <dt><strong>Member Path</strong></dt>
 * <dd><strong>must</strong> match the pattern <strong>{@code [\w_]+(\.[\w_]+)*:[\w_]+}</strong>.</dd>
 * </dl>
 * <a id="GlobPattern" />
 * <h4>Glob Pattern</h4>
 * A <em>GlobPattern</em> is used to match a subset of {@code RegistryNode}s and
 * their member {@code Accumulator}s and {@code Calculator}s.  The
 * <em>GlobPattern</em> format is like the <a href="#MemberPath">Member
 * Path</a> format, but it allows glob-style wildcard matching using
 * {@code *}, {@code ?}, and {@code **}, and assumes an omitted
 * <em>node_path</em> or <em>accumulator_or_calculator_name</em> means that
 * all should be matched.
 * <dl>
 * <dt><strong>*</strong></dt>
 * <dd>Matches zero or more characters in a <em>node_name</em> or an
 * <em>accumulator_or_calculator_name</em>, but does <strong>not</strong>
 * match <em>. (dot)</em>.</dd>
 * <dt><strong>?</strong></dt>
 * <dd>Matches zero or one character in a <em>node_name</em> or an
 * <em>accumulator_or_calculator_name</em>, but does <strong>not</strong>
 * match <em>. (dot)</em>.</dd>
 * <dt><strong>**</strong></dt>
 * <dd>Matches zero or more characters in a <em>node_name</em> or an
 * <em>accumulator_or_calculator_name</em>, and <strong>does</strong>
 * match <em>.</em>, meaning that it can be used to match nodes inside
 * a hierarchy.</dd>
 * </dl>
 * </p>
 * <p>
 * {@code Registrar} implementations <strong>must</strong> be thread-safe.
 * </p>
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public interface Registrar {

    /**
     * Returns the {@link RegistryNode} that is considered the root of this
     * {@code Registrar}.
     * <p>
     * While this node is a proper {@link RegistryNode} instance, it is
     * considered bad form to add any {@link Accumulator}s or
     * {@link Calculator}s to it directly.
     * </p>
     * <p>
     * This method is guaranteed to return a working instance of
     * {@link RegistryNode}.  However, if there is some error finding, creating,
     * or otherwise getting the instance that the caller wanted,
     * {@link NopRegistryNode#INSTANCE} will be returned instead.
     * </p>
     * @return The {@link RegistryNode} that acts as the root of the
     *         {@code Registrar}, or {@link NopRegistryNode#INSTANCE} if there
     *         is some unspecified error acquiring the root.
     */
    RegistryNode getRootNode();

    /**
     * Gets (or creates and registers) a {@link RegistryNode} that exists at
     * the given {@code nodePath}.
     * @param nodePath The <a href="#NodeNameAndPath">path</a> of the node.
     * @return <strong>Always</strong> returns an instance of
     *         {@link RegistryNode}.  This instance is either for the node
     *         at {@code nodePath}, or {@link NopRegistryNode#INSTANCE} if there
     *         is an error finding, creating, or registering the given node.
     */
    RegistryNode getNode(String nodePath);

    /**
     * Gets an {@link Accumulator} previously registered for a
     * {@link RegistryNode}, as specified by {@code memberPath}.
     * @param memberPath The <a href="#MemberPath">member path</a> that fully
     *                   identifies a previously registered {@code RegistryNode}
     *                   and {@link Accumulator}.
     * @return <strong>Always</strong> returns an instance of
     *         {@link Accumulator}.  This instance is either for the
     *         requested {@code Accumulator}, or {@link NopAccumulator#INSTANCE}
     *         if there is an error finding the named {@code Accumulator} or
     *         if the {@code RegistryNode} identified by {@code memberPath} does
     *         not exist already.
     */
    Accumulator getAccumulator(String memberPath);

    /**
     * Gets a {@link Calculator} previously registered for a
     * {@link RegistryNode}, as specified by {@code memberPath}.
     * @param memberPath The <a href="#MemberPath">member path</a> that fully
     *                   identifies a previously registered {@code RegistryNode}
     *                   and {@link Calculator}.
     * @return <strong>Always</strong> returns an instance of
     *         {@link Calculator}.  This instance is either for the
     *         requested {@code Calculator}, or {@link NopCalculator#INSTANCE}
     *         if there is an error finding the named {@code Calculator} or
     *         if the {@code RegistryNode} identified by {@code memberPath} does
     *         not exist already.
     */
    Calculator getCalculator(String memberPath);

    /**
     * Matches both {@link RegistryNode}s and their member {@link Accumulator}s
     * and {@link Calculator}s, and returns an {@code Iterable} containing
     * {@link GlobMatch}s (which descend from {@link RegistryNode}).
     * <p>
     * Because {@link GlobMatch} descends from {@link RegistryNode}, this method
     * can be used to easily iterate over a subset of {@link RegistryNode}s and
     * their member {@link Accumulator}s and {@link Calculator}s.
     * <pre>
     * for(RegistryNode node : registrar.match("com.foo.**:bar*")) {
     *   // node's path is somewhere under 'com.foo'
     *   // node's members' names all start with 'bar'
     * }
     * </pre>
     * </p>
     * @param globPattern The <a href="#GlobPattern">pattern</a> to match.
     * @return Returns a potentially-empty {@code Iterable} of
     *         {@link GlobMatch}s (which descend from {@link RegistryNode}) that
     *         identify the matched nodes, and can be used to access the matched
     *         {@code Calculator}s and {@code Accumulator}s.
     */
    Iterable<GlobMatch> match(String globPattern);

    /**
     * Turns on or off the {@link RegistryNode} identified by
     * <a href="#NodeNameAndPath">{@code nodePath}</a>.
     * based on the {@code on} parameter.
     * @param nodePath The <a href="#NodeNameAndPath">path</a> of the node
     *                 to turn on or off.
     * @param on Whether to turn the node on ({@code true}) or off
     *           ({@code false}).
     */
    void setOn(String nodePath, boolean on);

    /**
     * Forces the {@link RegistryNode} identified by <a href="#NodeNameAndPath">
     * {@code nodePath}</a> to follow its parent's on or off state.
     * @param nodePath The <a href="#NodeNameAndPath">path</a> of the node to
     *                 turn on or off.
     */
    void clearOn(String nodePath);
}
