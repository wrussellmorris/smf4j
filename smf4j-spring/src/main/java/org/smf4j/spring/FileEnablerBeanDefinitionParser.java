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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class FileEnablerBeanDefinitionParser extends
        AbstractSingleBeanDefinitionParser {

    private static final String PATHS_ATTR = "paths";
    private static final String PATH_ATTR = "path";
    private static final String PATH_TAG = "path";
    private static final String VALUE_ATTR = "value";

    @Override
    protected String getBeanClassName(Element element) {
        return FileEnablerBean.class.getCanonicalName();
    }

    @Override
    protected void doParse(Element element, ParserContext context,
        BeanDefinitionBuilder builder) {
        List<String> paths = new ArrayList<String>();

        String path = element.getAttribute(PATH_ATTR);
        if(StringUtils.hasLength(path)) {
            paths.add(path);
        }

        List<Element> pathElements =
                DomUtils.getChildElementsByTagName(element, PATH_TAG);
        for(Element pathElement : pathElements) {
            path = pathElement.getAttribute(VALUE_ATTR);
            if(StringUtils.hasLength(path)) {
                paths.add(path);
            }
        }

        builder.addPropertyValue(PATHS_ATTR, paths);
        builder.setLazyInit(false);
    }

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }
}
