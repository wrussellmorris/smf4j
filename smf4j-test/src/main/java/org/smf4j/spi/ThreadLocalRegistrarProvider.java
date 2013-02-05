/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.smf4j.spi;

import org.smf4j.Registrar;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class ThreadLocalRegistrarProvider implements RegistrarProvider{

    private final ThreadLocal<Registrar> registrars =
            new ThreadLocal<Registrar>() {
                @Override
                protected Registrar initialValue() {
                    return new TestableRegistrar();
                }
            };

    public Registrar getRegistrar() {
        return registrars.get();
    }

}
