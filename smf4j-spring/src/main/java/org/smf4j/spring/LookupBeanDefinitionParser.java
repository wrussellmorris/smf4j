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
import java.util.List;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class LookupBeanDefinitionParser extends
        AbstractSingleBeanDefinitionParser {

    private static final String REGISTRAR_ATTR = "registrar";
    private static final String PATH_ATTR = "path";
    private static final String NODE_ATTR = "node";
    private static final String ACCUMULATOR_ATTR = "accumulator";

    @Override
    protected String getBeanClassName(Element element) {
        return LookupFactoryBean.class.getCanonicalName();
    }

    @Override
    protected void doParse(Element element, ParserContext context,
        BeanDefinitionBuilder builder) {

        // The path path.to.node.accumulator to the accumulator we're looking
        // up.
        String path = element.getAttribute(PATH_ATTR);
        if(!StringUtils.hasLength(path)) {
            context.getReaderContext().error(
                    "'lookup' elements must have a 'path' attribute.",
                    element);
        }

        // Parse the path and set it
        parseAndSetPath(path, element, context, builder);

        // Make sure that spring knows we depend on a <registrar>
        // bean definition.
        String registrarId = element.getAttribute(REGISTRAR_ATTR);
        if(StringUtils.hasLength(registrarId)) {
            // Depend on the indicated registrar name
            builder.addDependsOn(registrarId);
        } else {
            // Depend on the default registrar name
            builder.addDependsOn(
                    RegistrarBeanDefinitionParser.MASTER_REGISTRAR_ID);
        }
    }

    protected void parseAndSetPath(String path, Element element,
            ParserContext context, BeanDefinitionBuilder builder) {
        String[] parts = path.split("\\.");
        if(parts.length == 0) {
            context.getReaderContext().error(
                    "'lookup' elements must have a 'path' attribute.",
                    element);
        }

        List<String> nodeParts = new ArrayList<String>();
        for(String part : parts) {
            if(part.length() == 0) {
                context.getReaderContext().error(
                        "'lookup' element 'path' cannot have empty path parts.",
                        element);
            }
            nodeParts.add(part);
        }

        String accumulator = nodeParts.remove(nodeParts.size()-1);
        String node = StringUtils.collectionToDelimitedString(nodeParts, ".");

        builder.addPropertyValue(NODE_ATTR, node);
        builder.addPropertyValue(ACCUMULATOR_ATTR, accumulator);
    }
}
