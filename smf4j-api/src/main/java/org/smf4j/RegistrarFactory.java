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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smf4j.helpers.NopRegistrar;
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
    private static final int RESOLVING = 3;
    private static final int SUCCESS = 4;
    private static final int NOP = 5;

    private static final String BINDER =
            "org/smf4j/impl/StaticRegistrarBinder.class";
    private static final String BINDER_FOR_UNIT_TESTS =
            "org/smf4j/impl/StaticRegistrarBinderForUnitTests.class";

    private static final Logger log =
            LoggerFactory.getLogger("org.smf4j.RegistrarFactory");
    private static final AtomicReference<RegistrarProvider> provider =
            new AtomicReference<RegistrarProvider>();
    private static final AtomicReference<RegistrarProvider> testProvider =
            new AtomicReference<RegistrarProvider>();
    private static final Registrar NOP_REGISTRAR = NopRegistrar.INSTANCE;
    private static final AtomicInteger initState =
            new AtomicInteger(NOT_INITIALIZED);
    private static final AtomicBoolean foundBinding = new AtomicBoolean(false);
    private static final AtomicBoolean foundTestBinding =
            new AtomicBoolean(false);
    private static final AtomicBoolean foundDuplicates =
            new AtomicBoolean(false);

    private RegistrarFactory() {
    }

    public static Registrar getRegistrar() {
        initialize();
        switch(initState.get()) {
            case NOP:
                return NOP_REGISTRAR;
            case SUCCESS:
                if(foundTestBinding.get()) {
                    return testProvider.get().getRegistrar();
                } else {
                    return provider.get().getRegistrar();
                }
            default:
                // We shouldn't get here...
                return NOP_REGISTRAR;
        }
    }

    public static RegistryNode getNode(String fullNodeName) {
        return getRegistrar().getNode(fullNodeName);
    }

    public static Accumulator getAccumulator(String path) {
        return getRegistrar().getAccumulator(path);
    }

    public static Calculator getCalculator(String path) {
        return getRegistrar().getCalculator(path);
    }

    private static void initialize() {
        while(true) {
            int curState = initState.get();
            switch(curState) {
                case NOT_INITIALIZED:
                    if(!initState.compareAndSet(curState, INITIALIZING)) {
                        // Someone else beat us - let's take another pass
                        break;
                    }

                    attemptTestBinder();
                    attemptBinder();
                    if(!foundBinding.get() && !foundTestBinding.get()) {
                        initState.set(NOP);
                        // Error: We could not find either provider.  We'll
                        // emit a nastygram and fall back to a NOP provider.
                        log.error("Using No-Operation (nop) Registrar "
                                + "because neither a normal nor a test "
                                + "RegistrarProvider could be found.");
                        return;
                    }
                    if(foundDuplicates.get()) {
                        initState.set(NOP);
                        // Error: We found duplicate providers
                        log.error("Using No-Operation (nop) Registrar since "
                                + "duplicate binders were found.  See prior "
                                + "log entries for details.");
                        return;
                    }
                    initState.set(NEED_RESOLUTION);
                    break;

                case NEED_RESOLUTION:
                    if(!initState.compareAndSet(NEED_RESOLUTION, RESOLVING)) {
                        // Someone else beat us - let's take another pass
                        break;
                    }
                    if(foundTestBinding.get()) {
                        initTestBinder();
                    }
                    if(foundBinding.get()) {
                        initBinder();
                    }
                    initState.set(SUCCESS);
                    return;

                case INITIALIZING:
                case RESOLVING:
                    // Somebody else is actively initializing stuff, let's wait
                    // for them to finish.
                    break;

                case SUCCESS:
                case NOP:
                    // Initializing is done, for better or worse
                    return;
            }
        }
    }

    private static void attemptTestBinder() {
        try {
            if(detectDuplicateBinders(BINDER_FOR_UNIT_TESTS)) {
               // There are duplicate test binders
               foundDuplicates.set(true);
            }

            StaticRegistrarBinderForUnitTests.getSingleton();
            foundTestBinding.set(true);
        } catch(NoClassDefFoundError e) {
            // Could not find a testing binding
            log.debug("Did not find a StaticRegistrarBinderForUnitTests.");
            foundTestBinding.set(false);
        }
    }

    private static void initTestBinder() {
        StaticRegistrarBinderForUnitTests binding =
                StaticRegistrarBinderForUnitTests.getSingleton();
        if(binding == null) {
            log.error("StaticRegistrarBinderForUnitTests.getSingleton() "
                    + "returned null.");
            return;
        }

        Object obj = binding.getRegistrarProvider();
        if(obj == null) {
            log.error(
                    "StaticRegistrarBinderForUnitTests.getRegistrarProvider() "
                    + "returned null.");
            return;
        }
        if(!(obj instanceof RegistrarProvider)) {
            log.error(
                    "StaticRegistrarBinderForUnitTests.getRegistrarProvider() "
                    + "returned an object that was not an instance of "
                    + "org.smf4j.spi.RegistrarProvider.  The returned value "
                    + "was an instance of '"
                    + obj.getClass().getCanonicalName() + "'");
            return;
        }

        testProvider.set((RegistrarProvider)obj);
    }

    private static void attemptBinder() {
        try {
            if(detectDuplicateBinders(BINDER)) {
                // There are duplicate binders
                foundDuplicates.set(true);
            }

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
            log.error("StaticRegistrarBinder.getSingleton() returned null.");
            return;
        }

        Object obj = binding.getRegistrarProvider();
        if(obj == null) {
            log.error(
                    "StaticRegistrarBinder.getRegistrarProvider() "
                    + "returned null.");
            return;
        }
        if(!(obj instanceof RegistrarProvider)) {
            log.error(
                    "StaticRegistrarBinder.getRegistrarProvider() returned an "
                    + "object that was not an instance of "
                    + "org.smf4j.spi.RegistrarProvider.  The returned value "
                    + "was an instance of '"
                    + obj.getClass().getCanonicalName() + "'");
            return;
        }

        provider.set((RegistrarProvider)obj);
    }

    private static boolean detectDuplicateBinders(String fqcn) {
        Set<URL> binders = findMatchingBinders(fqcn);
        if(binders.size() > 1) {
            log.error("Found duplicate registrar binders:");
            for(URL url : binders) {
                log.error(url.toString());
            }
            return true;
        }
        return false;
    }

    private static Set<URL> findMatchingBinders(String fqcn) {
        Set<URL> matchingBinders = new HashSet<URL>();
        ClassLoader classLoader = RegistrarFactory.class.getClassLoader();
        Enumeration<URL> matches = null;
        try {
            if(classLoader != null) {
                matches = classLoader.getResources(fqcn);
            } else {
                matches = ClassLoader.getSystemResources(fqcn);
            }
        } catch (IOException e) {
            log.error("Erroring getting resources from classpath.", e);
        }

        while(matches != null && matches.hasMoreElements()) {
            matchingBinders.add(matches.nextElement());
        }

        return matchingBinders;
    }

}
