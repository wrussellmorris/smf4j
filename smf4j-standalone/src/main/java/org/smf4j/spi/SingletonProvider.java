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

import java.util.concurrent.atomic.AtomicReference;
import org.smf4j.Registrar;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class SingletonProvider implements RegistrarProvider {

    private final AtomicReference<Registrar> singleton =
            new AtomicReference<Registrar>();

    public Registrar getRegistrar() {
        if(singleton.get() == null) {
            initialize();
        }
        return singleton.get();
    }

    private void initialize() {
        Registrar next = null;

        while(true) {
            if(singleton.get() != null) {
                // Initialized already
                break;
            }

            if(next == null) {
                // Create new default registrar
                next = new DefaultRegistrar();
            }

            if(singleton.compareAndSet(null, next)) {
                // Success!
                break;
            }
        }
    }
}
