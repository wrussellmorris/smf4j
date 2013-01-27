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

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public interface Registrar {

    String getName();

    RegistryNode register(String fullNodeName)
    throws InvalidNodeNameException;

    RegistryNode unregister(String fullNodeName)
    throws InvalidNodeNameException;

    RegistryNode getNode(String fullNodeName)
    throws InvalidNodeNameException;

    RegistryNode getRootNode();

    FilteredRegistrarListener createDynamicFilter(String pattern);
    void removeDynamicFilter(FilteredRegistrarListener dynamicFilter);

    void setOn(String node, boolean on)
    throws InvalidNodeNameException;

    void clearOn(String node)
    throws InvalidNodeNameException;

    void initializationComplete();
    void addRegistrationListener(RegistrarListener listener);
}
