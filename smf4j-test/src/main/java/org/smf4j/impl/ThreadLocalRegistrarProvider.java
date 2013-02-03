/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.smf4j.impl;

import org.smf4j.Registrar;
import org.smf4j.spi.RegistrarProvider;
import org.smf4j.standalone.DefaultRegistrar;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class ThreadLocalRegistrarProvider implements RegistrarProvider{

    private static final ThreadLocal<Registrar> registrars =
            new ThreadLocal<Registrar>() {
                @Override
                protected Registrar initialValue() {
                    return new DefaultRegistrar();
                }
            };

    public Registrar getRegistrar() {
        return registrars.get();
    }

}
