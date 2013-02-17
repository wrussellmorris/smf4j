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
package org.smf4j.spi;

import org.smf4j.Registrar;
import org.smf4j.RegistrarFactory;

/**
 * {@code RegistrarProvider} is the interface implemented by providers
 * of {@link Registrar}s.
 *
 * <p>
 * These providers must fully implement thread-safe
 * versions of {@link Registrar} and {@link RegistryNode} and provide them to
 * clients via this interface.  What constitutes a 'client' is up to the
 * implementation of {@code RegistrarProvider} - for example, a specific
 * contained J2EE app, a root-level Spring context, a JVM instance, etc...
 * </p>
 * <p>
 * These providers are found and bound at runtime by {@link RegistrarFactory}.
 * </p>
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public interface RegistrarProvider {

    /**
     * Gets the {@link Registrar} instance for the client, as determined by
     * the implementation.
     * @return The {@link Registrar} instance for the client, as determined by
     *         the implementation.
     */
    Registrar getRegistrar();
}
