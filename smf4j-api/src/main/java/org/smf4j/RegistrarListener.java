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

import org.smf4j.RegistryNode;
import org.smf4j.Registrar;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public interface RegistrarListener {

    void initializationComplete(Registrar registrar);

    void nodeAdded(Registrar registrar, RegistryNode node);

    void nodeRemoved(Registrar registrar, RegistryNode node);

    void accumulatorAdded(Registrar registrar, RegistryNode node,
            Accumulator accumulator);

    void accumulatorRemoved(Registrar registrar, RegistryNode node,
            Accumulator accumulator);

    void calculationAdded(Registrar registrar, RegistryNode node,
            Calculator calculation);

    void calculationRemoved(Registrar registrar, RegistryNode node,
            Calculator calculation);


}
