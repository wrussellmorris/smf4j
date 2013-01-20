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
package org.smf4j.standalone;

import org.smf4j.InvalidNodeNameException;
import org.smf4j.Registrar;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final String DEFAULT_CONFIG_FILE =
            CLASSPATH_TYPE + "/monitoring.properties";

    public void doEnablement(Registrar registrar) {
        doEnablement(registrar, DEFAULT_CONFIG_FILE);
    }

    public void doEnablement(Registrar registrar,
            List<String> propertyFilePaths) {
        for(String propertyFilePath : propertyFilePaths) {
            doEnablement(registrar, propertyFilePath);
        }
    }

    public void doEnablement(Registrar registrar, String propertyFilePath) {
        Properties p;

        if(propertyFilePath.startsWith(CLASSPATH_TYPE)) {
            // Load it from the classpath
            String path = propertyFilePath.replaceFirst(CLASSPATH_TYPE, "");
            p = loadPropertiesFromClasspath(path);
        } else {
            // Try to load it from the filesystem
            p = loadPropertiesFromFile(propertyFilePath);
        }

        loadProperties(registrar, p);
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

    protected void loadProperties(Registrar registrar, Properties p) {
        for(Map.Entry<Object, Object> entry : p.entrySet()) {
            String node = entry.getKey().toString();
            String enablement = entry.getValue().toString();
            Boolean onOrOff = onOrOff(enablement);
            try {
                if(onOrOff != null) {
                    registrar.setOn(node, onOrOff.booleanValue());
                } else {
                    log.warn("Unknown enablement setting '{}' for node '{}'.  "
                            + "Valid values are one of 'true', 'false', 'osn', "
                            + "or 'off'.",
                            enablement,
                            node);
                }
            } catch (InvalidNodeNameException ex) {
                log.warn(String.format(
                        "Could not set enablement for node '%s' because it is "
                        + "an invalid node name.", node),
                        ex);
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
