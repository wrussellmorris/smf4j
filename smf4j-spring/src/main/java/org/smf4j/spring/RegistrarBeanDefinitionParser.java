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

import java.util.List;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class RegistrarBeanDefinitionParser extends
        AbstractSingleBeanDefinitionParser {

    public static final String MASTER_REGISTRAR_ID = "__registrar__#ID#";

    public static final String NODE_TAG = "node";
    public static final String NODE_TEMPLATE_TAG = "node-template";

    public static final String NAME_ATTR = "name";
    public static final String NODES_ATTR = "nodeProxies";
    public static final String REF_ATTR = "ref";

    @Override
    protected String getBeanClassName(Element element) {
        return RegistrarFactoryBean.class.getCanonicalName();
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext,
        BeanDefinitionBuilder builder) {

        // Parse child nodes into a list of runtime bean references to
        // the proxy beans
        ManagedList<RuntimeBeanReference> nodes =
                parseChildren(parserContext, element);

        builder.addPropertyValue(NODES_ATTR, nodes);
    }

    protected ManagedList<RuntimeBeanReference> parseChildren(
            ParserContext context, Element element) {
        ManagedList<RuntimeBeanReference> nodes =
                new ManagedList<RuntimeBeanReference>();
        RegistryNodeTemplateDefinitionParser p =
                new RegistryNodeTemplateDefinitionParser(false);

        List<Element> children = DomUtils.getChildElements(element);
        for(Element child : children) {
            String childTagName = child.getLocalName();
            String childBeanId = null;
            if(NODE_TAG.equals(childTagName)) {
                BeanDefinitionBuilder bdb = BeanDefinitionBuilder
                        .genericBeanDefinition(RegistryNodeProxy.class);
                childBeanId = p.parseNode(context, child, bdb);
            } else if(NODE_TEMPLATE_TAG.equals(NODE_TEMPLATE_TAG)) {
                childBeanId = p.parseNodeTemplate(context, child);
            } else {
                context.getReaderContext().error("Unknown tag", child);
            }

            if(childBeanId != null) {
                nodes.add(new RuntimeBeanReference(childBeanId));
            }
        }

        return nodes;
    }

    @Override
    protected String resolveId(Element element,
            AbstractBeanDefinition definition, ParserContext parserContext)
    throws BeanDefinitionStoreException {
        String id = element.getAttribute(ID_ATTRIBUTE);
        if (!StringUtils.hasText(id)) {
            id = MASTER_REGISTRAR_ID;
        }
        return id;
    }

}
