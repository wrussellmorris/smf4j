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

import java.util.Set;
import org.junit.After;
import static org.junit.Assert.*;

import org.junit.Test;
import org.smf4j.RegistrarFactory;
import org.smf4j.RegistryNode;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class LookupTest {

    @After
    public void afterClass()
    throws Exception {
        RegistryNode rootNode = RegistrarFactory.getRegistrar().getRootNode();
        Set<String> children = rootNode.getChildNodes().keySet();
        for(String child : children) {
            RegistrarFactory.getRegistrar().unregister(child);
        }
    }

    @Test
    public void simple()
    throws Exception {
        ApplicationContext context = loadContext("lookup.xml");
        MockBean bean = context.getBean("mockBean", MockBean.class);
        assertNotNull(bean);
        assertNotNull(bean.getAccumulator());
    }

    private ApplicationContext loadContext(String path) {
        return new ClassPathXmlApplicationContext(path, getClass());
    }
}
