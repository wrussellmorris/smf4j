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
package org.smf4j.core.util;

import org.smf4j.Registrar;
import static org.junit.Assert.*;

import java.io.File;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.Before;

import org.junit.Test;
import org.smf4j.RegistrarFactory;
import org.smf4j.RegistrarFactoryForUnitTests;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class PropertiesFileRegistrarEnablerTest {

    private final String FOO = "foo";
    private final String FOO_BAR = "foo.bar";
    private final String FOO_BAZ = "foo.baz";
    private final String PROPS1_CP =
            "org/smf4j/standalone/props1.properties";
    private final String PROPS2_CP =
            "org/smf4j/standalone/props2.properties";

    @Before
    public void before() {
        RegistrarFactoryForUnitTests.reset(false);
    }

    @Test
    public void loadPropertiesFromClasspathTest()
    throws Exception {
        PropertiesFileRegistrarEnabler pfre =
                new PropertiesFileRegistrarEnabler();

        Properties p = pfre.loadPropertiesFromClasspath(PROPS1_CP);

        assertEquals(2, p.entrySet().size());
        assertEquals("on", p.getProperty(FOO_BAR));
        assertEquals("true", p.getProperty(FOO_BAZ));
    }

    @Test
    public void onOffParseTest() {
        PropertiesFileRegistrarEnabler pfre =
                new PropertiesFileRegistrarEnabler();
        assertNull(pfre.onOrOff("foo"));

        assertTrue(pfre.onOrOff("on"));
        assertTrue(pfre.onOrOff("On"));
        assertTrue(pfre.onOrOff("true"));
        assertTrue(pfre.onOrOff("True"));

        assertFalse(pfre.onOrOff("off"));
        assertFalse(pfre.onOrOff("Off"));
        assertFalse(pfre.onOrOff("false"));
        assertFalse(pfre.onOrOff("False"));
    }

    @Test
    public void doEnablementDefault()
    throws Exception {
        PropertiesFileRegistrarEnabler pfre =
                new PropertiesFileRegistrarEnabler();

        pfre.doEnablement();

        assertFalse(RegistrarFactory.getNode(FOO).isOn());
        assertTrue(RegistrarFactory.getNode(FOO_BAR).isOn());
        assertFalse(RegistrarFactory.getNode(FOO_BAZ).isOn());
    }

    @Test
    public void doEnablementSinglePath()
    throws Exception {
        PropertiesFileRegistrarEnabler pfre =
                new PropertiesFileRegistrarEnabler();

        File rootPath = getFilePath(getClass());
        File propFile = new File(rootPath, PROPS1_CP);
        pfre.doEnablement(propFile.getAbsolutePath());

        assertFalse(RegistrarFactory.getNode(FOO).isOn());
        assertTrue(RegistrarFactory.getNode(FOO_BAR).isOn());
        assertTrue(RegistrarFactory.getNode(FOO_BAZ).isOn());
    }

    @Test
    public void doEnablementMultiplePaths()
    throws Exception {
        PropertiesFileRegistrarEnabler pfre =
                new PropertiesFileRegistrarEnabler();

        Registrar r = RegistrarFactory.getRegistrar();
        List<String> paths = new ArrayList<String>();
        paths.add("classpath:" + PROPS1_CP);
        paths.add("classpath:" + PROPS2_CP);
        pfre.doEnablement(paths);

        assertFalse(r.getNode(FOO).isOn());
        assertTrue(r.getNode(FOO_BAR).isOn());
        assertFalse(r.getNode(FOO_BAZ).isOn());
   }

    private File getFilePath(Class<?> clazz) {
        ProtectionDomain pd = clazz.getProtectionDomain();
        CodeSource cs = pd.getCodeSource();
        String extForm = cs.getLocation().toExternalForm();
        File file = new File(extForm.replace("file:", ""));
        return file;
    }
}
