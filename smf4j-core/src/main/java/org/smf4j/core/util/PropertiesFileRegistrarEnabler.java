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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smf4j.RegistrarFactory;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class PropertiesFileRegistrarEnabler {
    private static final String CLASSPATH_TYPE = "classpath:";
    private static final String ON = "on";
    private static final String TRUE = "true";
    private static final String OFF = "off";
    private static final String FALSE = "false";

    private final Logger log = LoggerFactory.getLogger(
            PropertiesFileRegistrarEnabler.class);

    public static final String DEFAULT_CONFIG_FILE =
            CLASSPATH_TYPE + "smf4j.properties";

    public void doEnablement() {
        doEnablement(DEFAULT_CONFIG_FILE);
    }

    public void doEnablement(List<String> propertyFilePaths) {
        for(String propertyFilePath : propertyFilePaths) {
            doEnablement(propertyFilePath);
        }
    }

    public void doEnablement(String propertyFilePath) {
        Properties p;

        if(propertyFilePath.startsWith(CLASSPATH_TYPE)) {
            // Load it from the classpath
            String path = propertyFilePath.replaceFirst(CLASSPATH_TYPE, "");
            p = loadPropertiesFromClasspath(path);
        } else {
            // Try to load it from the filesystem
            p = loadPropertiesFromFile(propertyFilePath);
        }

        loadProperties(p);
    }

    protected Properties loadPropertiesFromClasspath(String path) {
        InputStream is = null;
        Properties p = new Properties();

        try {
            is = getClass().getClassLoader().getResourceAsStream(path);
            if(is != null) {
                p.load(is);
            } else {
                log.warn("Unable to load '{}' from classpath.", path);
            }
        } catch (IOException ex) {
            log.warn(String.format("Failed to load '%s' from classpath.", path),
                    ex);
        } finally {
            if(is != null) {
                try { is.close(); } catch(IOException e) {}
            }
        }

        return p;
    }

    protected Properties loadPropertiesFromFile(String path) {
        FileInputStream fis = null;
        Properties p = new Properties();

        try {
            fis = new FileInputStream(path);
            p.load(fis);
        } catch (IOException ex) {
            log.warn(String.format("Failed to load '%s'.", path),
                    ex);
        } finally {
            if(fis != null) {
                try { fis.close(); } catch(IOException e) {}
            }
        }

        return p;
    }

    protected void loadProperties(Properties p) {
        Registrar r = RegistrarFactory.getRegistrar();
        for(Map.Entry<Object, Object> entry : p.entrySet()) {
            String node = entry.getKey().toString();
            String enablement = entry.getValue().toString();
            Boolean onOrOff = onOrOff(enablement);
            if(onOrOff != null) {
                r.setOn(node, onOrOff);
            } else {
                log.warn("Unknown enablement setting '{}' for node '{}'.  "
                        + "Valid values are one of 'true', 'false', 'on', "
                        + "or 'off'.",
                        enablement,
                        node);
            }
        }
    }

    protected Boolean onOrOff(String val) {
        if(val == null) {
            return null;
        }

        if(val.equalsIgnoreCase(ON) || val.equalsIgnoreCase(TRUE)) {
            return Boolean.TRUE;
        }

        if(val.equalsIgnoreCase(OFF) || val.equalsIgnoreCase(FALSE)) {
            return Boolean.FALSE;
        }

        return null;
    }
}
