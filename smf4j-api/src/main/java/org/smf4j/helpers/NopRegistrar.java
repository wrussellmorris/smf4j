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
import java.util.List;
import org.smf4j.Registrar;
import org.smf4j.RegistryNode;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class NopRegistrar implements Registrar {

    public static final Registrar INSTANCE = new NopRegistrar();
    public static final List<GlobMatch> empty = Collections.emptyList();

    private NopRegistrar() {
    }

    @Override
    public RegistryNode getNode(String fullNodeName) {
        return NopRegistryNode.INSTANCE;
    }

    @Override
    public RegistryNode getRootNode() {
        return NopRegistryNode.INSTANCE;
    }


    @Override
    public Iterable<GlobMatch> match(String globPattern) {
        return empty;
    }

    @Override
    public void setOn(String fullNodeName, boolean on) {
    }

    @Override
    public void clearOn(String fullNodeName) {
    }

}
