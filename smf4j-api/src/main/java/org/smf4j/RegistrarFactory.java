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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smf4j.helpers.NopAccumulator;
import org.smf4j.helpers.NopCalculator;
import org.smf4j.helpers.NopRegistrar;
import org.smf4j.helpers.NopRegistryNode;
import org.smf4j.impl.StaticRegistrarBinder;
import org.smf4j.impl.StaticRegistrarBinderForUnitTests;
import org.smf4j.spi.RegistrarProvider;

/**
 * {@code RegistrarFactory} is a static singleton that manages finding, binding,
 * and providing an instance of {@link Registrar} to clients.
 * <p>
 * The design of {@code RegistrarFactory} is heavily influenced by the
 * design of the similar class {@code LoggerFactory} in the excellent
 * <a href="http://www.slf4j.org">SLF4J - Simple Logging Facade for Java</a>
 * logging library designed and written by the guys at
 * <a href="http://www.qos.ch">QOS.ch</a>.
 * </p>
 * <p>
 * {@code RegistrarFactory} allows the actual implementation of the
 * {@code smf4j-api} to be bound at runtime, allowing application developers
 * to code to the {@code smf4j-api} and defer the actual underlying
 * implementation selection to deployment.
 * </p>
 * <p>
 * In non-IOC scenarios, code would call {@link #getRegistrar() getRegistrar()}
 * to acquire the application's {@link Registrar}.
 * </p>
 * <pre>
 * Registrar r = RegistrarFactory.getRegistrar();
 * </pre>
 * <p>
 * Convenience methods are also present to directly acquire a
 * {@link RegistryNode} with {@link #getNode(java.lang.String) getNode},
 * an {@link Accumulator} with
 * {@link #getAccumulator(java.lang.String) getAccumulator}, or a
 * {@link Calculator} with
 * {@link #getCalculator(java.lang.String) getCalculator} from the application's
 * {@link Registrar}:
 * </p>
 * <pre>
 * RegistryNode n = RegistrarFactory.getNode("path.to.node");
 * Accumulator a = RegistrarFactory.getAccumulator("path.to.node:accumulator");
 * Calculator c = RegistrarFactory.getCalculator("path.to.node:calculator");
 * </pre>
 * <p>
 * {@code RegistrarFactory} is thread-safe.
 * </p>
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class RegistrarFactory {
    // Our logger
    private static final Logger log =
            LoggerFactory.getLogger("org.smf4j.RegistrarFactory");

    // Our internal initialization states
    private static final int NOT_INITIALIZED = 0;
    private static final int INITIALIZING = 1;
    private static final int NEED_RESOLUTION = 2;
    private static final int RESOLVING = 3;
    private static final int SUCCESS = 4;
    private static final int NOP = 5;

    // The names of the classes we use for binding to a provider.  We
    // search for these so that we can report to the user that we found
    // more than one (which is a serious configuration error.
    private static final String BINDER =
            "org/smf4j/impl/StaticRegistrarBinder.class";
    private static final String BINDER_FOR_UNIT_TESTS =
            "org/smf4j/impl/StaticRegistrarBinderForUnitTests.class";

    // Threadsafe containers for the providers we find.
    private static final AtomicReference<RegistrarProvider> provider =
            new AtomicReference<RegistrarProvider>();
    private static final AtomicReference<RegistrarProvider> testProvider =
            new AtomicReference<RegistrarProvider>();

    // Threadsafe containers for our initialization state management.
    private static final AtomicInteger initState =
            new AtomicInteger(NOT_INITIALIZED);
    private static volatile boolean foundBinding = false;
    private static volatile boolean foundTestBinding = false;
    private static volatile boolean foundDuplicates = false;

    /**
     * Private constructor - {@code RegistrarFactory} is a static singleton.
     */
    private RegistrarFactory() {
    }

    /**
     * Gets <strong>the</strong> {@link Registrar} instance for the client.
     * <p>
     * This method is idempotent - it will always return the same value no
     * matter how many times it is called.
     * </p>
     * <p>
     * If there is an error during initialization that leaves it in a state
     * that it cannot successfully bind to a provider, this method will
     * return {@link NopRegistrar#INSTANCE}.
     * </p>
     * @return <strong>The</strong> {@link Registrar} instance for the client,
     * or {@link NopRegistrar#INSTANCE} if a provider cannot be obtained.
     */
    public static Registrar getRegistrar() {
        initialize();
        switch(initState.get()) {
            case NOP:
                return NopRegistrar.INSTANCE;
            case SUCCESS:
                if(foundTestBinding) {
                    return testProvider.get().getRegistrar();
                } else {
                    return provider.get().getRegistrar();
                }
            default:
                // We shouldn't get here...
                return NopRegistrar.INSTANCE;
        }
    }

    /**
     * Gets (or creates and registers) a {@link RegistryNode} at the given
     * <a href="{@docRoot}/org/smf4j/Registrar.html#NodeNameAndPath">nodePath</a>
     * in the application's {@link Registrar}.
     * @param nodePath The path of the {@link RegistryNode} in the application's
     *                 {@link Registrar}
     * @return The {@link RegistryNode} at the given {@code nodePath} in the
     *         application's {@link Registrar}, or
     *         {@link NopRegistryNode#INSTANCE} if an error is encountered.
     *
     * @see Registrar#getNode(java.lang.String)
     */
    public static RegistryNode getNode(String nodePath) {
        return getRegistrar().getNode(nodePath);
    }

    /**
     * Gets the {@link Accumulator} at the given
     * <a href="{@docRoot}/org/smf4j/Registrar.html#MemberPath">memberPath</a>
     * in the application's {@link Registrar}.
     * @param memberPath The path of the {@link RegistryNode} and
     *                 {@link Accumulator} in the application's
     *                 {@link Registrar}.
     * @return The {@link Accumulator} at the given {@code memberPath} in the
     *         application's {@link Registrar}, or
     *         {@link NopAccumulator#INSTANCE} if an error is encountered.
     *
     * @see Registrar#getAccumulator(java.lang.String)
     */
    public static Accumulator getAccumulator(String memberPath) {
        return getRegistrar().getAccumulator(memberPath);
    }

    /**
     * Gets the {@link Calculator} at the given
     * <a href="{@docRoot}/org/smf4j/Registrar.html#MemberPath">memberPath</a>
     * in the application's {@link Registrar}.
     * @param memberPath The path of the {@link RegistryNode} and
     *                 {@link Calculator} in the application's
     *                 {@link Registrar}.
     * @return The {@link Calculator} at the given {@code memberPath} in the
     *         application's {@link Registrar}, or
     *         {@link NopCalculator#INSTANCE} if an error is encountered.
     *
     * @see Registrar#getCalculator(java.lang.String)
     */
    public static Calculator getCalculator(String memberPath) {
        return getRegistrar().getCalculator(memberPath);
    }

    /**
     * Initializes our internal state, if necessary.
     */
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
                    if(!foundBinding&& !foundTestBinding) {
                        initState.set(NOP);
                        // Error: We could not find either provider.  We'll
                        // emit a nastygram and fall back to a NOP provider.
                        log.error("Using No-Operation (nop) Registrar "
                                + "because neither a normal nor a test "
                                + "RegistrarProvider could be found.");
                        return;
                    }
                    if(foundDuplicates) {
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

                    boolean success = false;
                    if(foundTestBinding) {
                        success |= initTestBinder();
                    }
                    if(foundBinding) {
                        success |= initBinder();
                    }
                    if(!success) {
                        log.error("Using No-Operation (nop) Registrar since "
                                + "no binders were successfully registered.  "
                                + "See prior log entries for details.");
                    }
                    initState.set(success ? SUCCESS : NOP);
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

    /**
     * Attempts to bind to the
     * {@code org.smf4j.impl.StaticRegistrarBinderForUnitTests} class.
     */
    private static void attemptTestBinder() {
        try {
            if(detectAndReportDuplicateBinders(BINDER_FOR_UNIT_TESTS)) {
               // There are duplicate test binders
               foundDuplicates = true;
            }

            StaticRegistrarBinderForUnitTests.getSingleton();
            foundTestBinding = true;
        } catch(NoClassDefFoundError e) {
            // Could not find a testing binding
            log.debug("Did not find a StaticRegistrarBinderForUnitTests.");
            foundTestBinding = false;
        }
    }

    /**
     * Initializes the previously-found
     * {@code StaticRegistrarBinderForUnitTests} and records the
     * {@link RegistrarProvider} it provides.
     * @return A {@code boolean} value indicating whether or not the
     *         initialization succeeded.
     */
    private static boolean initTestBinder() {
        StaticRegistrarBinderForUnitTests binding =
                StaticRegistrarBinderForUnitTests.getSingleton();
        if(binding == null) {
            log.error("StaticRegistrarBinderForUnitTests.getSingleton() "
                    + "returned null.");
            return false;
        }

        Object obj = binding.getRegistrarProvider();
        if(obj == null) {
            log.error(
                    "StaticRegistrarBinderForUnitTests.getRegistrarProvider() "
                    + "returned null.");
            return false;
        }
        if(!(obj instanceof RegistrarProvider)) {
            log.error(
                    "StaticRegistrarBinderForUnitTests.getRegistrarProvider() "
                    + "returned an object that was not an instance of "
                    + "org.smf4j.spi.RegistrarProvider.  The returned value "
                    + "was an instance of '"
                    + obj.getClass().getCanonicalName() + "'");
            return false;
        }

        testProvider.set((RegistrarProvider)obj);
        return true;
    }

    /**
     * Attempts to bind to the
     * {@code org.smf4j.impl.StaticRegistrarBinder} class.
     */
    private static void attemptBinder() {
        try {
            if(detectAndReportDuplicateBinders(BINDER)) {
                // There are duplicate binders
                foundDuplicates = true;
            }

            StaticRegistrarBinder.getSingleton();
            foundBinding = true;
        } catch(NoClassDefFoundError e) {
            // Could not find a testing binding
            foundBinding = false;
        }
    }

    /**
     * Initializes the previously-found
     * {@code StaticRegistrarBinder} and records the
     * {@link RegistrarProvider} it provides.
     * @return A {@code boolean} value indicating whether or not the
     *         initialization succeeded.
     */
    private static boolean initBinder() {
        StaticRegistrarBinder binding = StaticRegistrarBinder.getSingleton();
        if(binding == null) {
            log.error("StaticRegistrarBinder.getSingleton() returned null.");
            return false;
        }

        Object obj = binding.getRegistrarProvider();
        if(obj == null) {
            log.error(
                    "StaticRegistrarBinder.getRegistrarProvider() "
                    + "returned null.");
            return false;
        }
        if(!(obj instanceof RegistrarProvider)) {
            log.error(
                    "StaticRegistrarBinder.getRegistrarProvider() returned an "
                    + "object that was not an instance of "
                    + "org.smf4j.spi.RegistrarProvider.  The returned value "
                    + "was an instance of '"
                    + obj.getClass().getCanonicalName() + "'");
            return false;
        }

        provider.set((RegistrarProvider)obj);
        return true;
    }

    /**
     * Detects and reports duplicate classes found at the path {@code fqcn}.
     * @param fqcn The name of the class, in resource form, to search for.
     * @return A {@code boolean} value indicating whether or not duplicates
     *         were found.
     */
    private static boolean detectAndReportDuplicateBinders(String fqcn) {
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

    /**
     * Gets a {@code Set} of all of the classes that are named {@code fqcn}.
     * @param fqcn The name of the class, in resource form, to search for.
     * @return A {@code Set} of all of the classes found matching {@code fqcn}.
     */
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
