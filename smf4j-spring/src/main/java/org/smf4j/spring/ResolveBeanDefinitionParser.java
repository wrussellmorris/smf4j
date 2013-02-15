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

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class ResolveBeanDefinitionParser extends
        AbstractSingleBeanDefinitionParser {

    private static final String DEPENDSON_ATTR = "depends-on";
    private static final String PATH_ATTR = "path";

    @Override
    protected String getBeanClassName(Element element) {
        return ResolveFactoryBean.class.getCanonicalName();
    }

    @Override
    protected void doParse(Element element, ParserContext context,
        BeanDefinitionBuilder builder) {

        // The path path.to.node.accumulator to the accumulator we're looking
        // up.
        String path = element.getAttribute(PATH_ATTR);
        if(!StringUtils.hasLength(path)) {
            context.getReaderContext().error(
                    "'resolve' elements must have a 'path' attribute.",
                    element);
        }

        // Set the path
        builder.addPropertyValue(PATH_ATTR, path);

        // Make sure that spring knows we depend on the given beans, which are
        // probably <registrar> nodes.
        String dependsOn = element.getAttribute(DEPENDSON_ATTR);
        if(StringUtils.hasLength(dependsOn)) {
            for(String id : StringUtils.commaDelimitedListToSet(dependsOn)) {
                // Depend on the indicated registrar name
                builder.addDependsOn(id);
            }
        } else {
            // Depend on the default registrar name
            builder.addDependsOn(
                    RegistrarBeanDefinitionParser.MASTER_REGISTRAR_ID);
        }
    }
}
