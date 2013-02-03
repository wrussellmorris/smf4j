/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.smf4j;

import org.smf4j.impl.StaticRegistrarBinderForUnitTests;
import org.smf4j.spi.RegistrarProvider;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class RegistrarFactoryForUnitTests {

    private static Registrar getRegistrar() {
        RegistrarProvider provider = (RegistrarProvider)
                StaticRegistrarBinderForUnitTests.getSingleton()
                .getRegistrarProvider();
        return provider.getRegistrar();
    }

    public static void reset(boolean rootNodeOn) {
        Registrar r = getRegistrar();
        r.clear();
        r.getRootNode().setOn(rootNodeOn);
    }
}
