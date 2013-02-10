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
package org.smf4j.spring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.smf4j.core.util.PropertiesFileRegistrarEnabler;
import org.springframework.beans.factory.InitializingBean;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class FileEnablerBean implements InitializingBean {

    private List<String> paths = new ArrayList<String>();

    public void afterPropertiesSet() throws Exception {
        PropertiesFileRegistrarEnabler pfre =
                new PropertiesFileRegistrarEnabler();
        if(paths == null || paths.isEmpty()) {
            pfre.doEnablement();
        } else {
            pfre.doEnablement(getPaths());
        }
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        if(paths == null) {
            paths = Collections.emptyList();
        }
        this.paths = paths;
    }
}
