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
package org.smf4j.spring;

import org.springframework.beans.factory.FactoryBean;
import org.smf4j.Accumulator;
import org.smf4j.RegistrarFactory;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class ResolveFactoryBean implements FactoryBean<Accumulator> {

    private String path;

    @Override
    public Class getObjectType() {
        return Accumulator.class;
    }

    @Override
    public Accumulator getObject() throws Exception {
        return RegistrarFactory.getAccumulator(path);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
