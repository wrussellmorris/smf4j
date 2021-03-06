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
package org.smf4j.impl;

import org.smf4j.spi.RegistrarProvider;
import org.smf4j.spi.SingletonProvider;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class StaticRegistrarBinder {

    private static final RegistrarProvider provider = new SingletonProvider();
    private static final StaticRegistrarBinder instance =
            new StaticRegistrarBinder();

    public static StaticRegistrarBinder getSingleton() {
        return instance;
    }

    public Object getRegistrarProvider() {
        return provider;
    }
}
