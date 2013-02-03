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
package org.smf4j;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.smf4j.helpers.NopRegistrar;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smf4j.impl.StaticRegistrarBinder;
import org.smf4j.impl.StaticRegistrarBinderForUnitTests;
import org.smf4j.spi.RegistrarProvider;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class RegistrarFactory {
    private static final int NOT_INITIALIZED = 0;
    private static final int INITIALIZING = 1;
    private static final int NEED_RESOLUTION = 2;
    private static final int SUCCESS = 3;
    private static final int NOP = 4;

    private static final Logger log =
            LoggerFactory.getLogger("org.smf4j.RegistrarFactory");
    private static final AtomicReference<RegistrarProvider> provider =
            new AtomicReference<RegistrarProvider>();
    private static final AtomicReference<RegistrarProvider> testProvider =
            new AtomicReference<RegistrarProvider>();
    private static final Registrar nop = NopRegistrar.INSTANCE;
    private static final AtomicInteger initState =
            new AtomicInteger(NOT_INITIALIZED);
    private static final AtomicBoolean foundBinding = new AtomicBoolean(false);
    private static final AtomicBoolean foundTestBinding =
            new AtomicBoolean(false);

    private RegistrarFactory() {
    }

    public static Registrar getRegistrar() {
        initialize();
        switch(initState.get()) {
            case INITIALIZING:
            case NOP:
                return nop;
            case SUCCESS:
                if(foundTestBinding.get()) {
                    return testProvider.get().getRegistrar();
                } else {
                    return provider.get().getRegistrar();
                }
            default:
                return nop;
        }
    }

    private static void initialize() {
        while(true) {
            int curState = initState.get();
            switch(curState) {
                case NOT_INITIALIZED:
                    if(!initState.compareAndSet(curState, INITIALIZING)) {
                        break;
                    }

                    attemptTestBinder();
                    attemptBinder();
                    if(!foundBinding.get() && !foundTestBinding.get()) {
                        initState.set(NOP);
                        // Error: We could not find either provider.  We'll
                        // emit a nastygram and fall back to a NOP provider.
                        nastygram();
                        return;
                    }
                    initState.set(NEED_RESOLUTION);
                    break;
                case NEED_RESOLUTION:
                    if(foundTestBinding.get()) {
                        initTestBinder();
                    }
                    if(foundBinding.get()) {
                        initBinder();
                    }
                    initState.set(SUCCESS);
                    break;
                default:
                    return;
            }
        }
    }

    private static void attemptTestBinder() {
        try {
            StaticRegistrarBinderForUnitTests.getSingleton();
            foundTestBinding.set(true);
        } catch(NoClassDefFoundError e) {
            // Could not find a testing binding
            foundTestBinding.set(false);
        }
    }

    private static void initTestBinder() {
        StaticRegistrarBinderForUnitTests binding =
                StaticRegistrarBinderForUnitTests.getSingleton();
        if(binding == null) {
            // TODO: Something's messed up...
            return;
        }

        Object obj = binding.getRegistrarProvider();
        if(obj == null) {
            // TODO: Something's messed up...
            return;
        }
        if(!(obj instanceof RegistrarProvider)) {
           // TODO: Something's messed up...
            return;
        }

        testProvider.set((RegistrarProvider)obj);
    }

    private static void attemptBinder() {
        try {
            StaticRegistrarBinder.getSingleton();
            foundBinding.set(true);
        } catch(NoClassDefFoundError e) {
            // Could not find a testing binding
            foundBinding.set(false);
        }
    }

    private static void initBinder() {
        StaticRegistrarBinder binding = StaticRegistrarBinder.getSingleton();
        if(binding == null) {
            // TODO: Something's messed up...
            return;
        }

        Object obj = binding.getRegistrarProvider();
        if(obj == null) {
            // TODO: Something's messed up...
            return;
        }
        if(!(obj instanceof RegistrarProvider)) {
           // TODO: Something's messed up...
        }

        provider.set((RegistrarProvider)obj);
    }

    private static void nastygram() {
        log.error("Unable to bind to a normal or test RegistrarProvider.");
    }
}
