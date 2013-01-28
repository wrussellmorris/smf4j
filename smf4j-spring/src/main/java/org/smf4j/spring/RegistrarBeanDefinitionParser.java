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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.smf4j.core.accumulator.Counter;
import org.smf4j.core.accumulator.MinCounter;
import org.smf4j.core.accumulator.MaxCounter;
import org.smf4j.core.accumulator.WindowedCounter;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class RegistrarBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    public static final String MASTER_REGISTRAR_ID = "__registrar__#ID#";

    public static final String NODE_TAG = "node";
    public static final String COUNTER_TAG = "counter";
    public static final String MINCOUNTER_TAG = "mincounter";
    public static final String MAXCOUNTER_TAG = "maxcounter";
    public static final String WINDOWEDCOUNTER_TAG = "windowedcounter";
    public static final String CUSTOM_TAG = "custom";

    public static final String CHILD_ATTR = "child";
    public static final String CHILDREN_ATTR = "children";
    public static final String MAX_ATTR = "max";
    public static final String NAME_ATTR = "name";
    public static final String NODES_ATTR = "nodeProxies";
    public static final String REF_ATTR = "ref";

    private static final String TIMEWINDOW_ATTR = "timewindow";
    private static final String INTERVALS_ATTR = "intervals";
    private static final String TIMEREPORTER_ATTR = "timereporter";
    private static final String INTERVAL_ATTR = "intervalType";
    private static final String INTERVAL_SECONDS = "seconds";
    private static final String INTERVAL_POWERSOF2 = "powersof2";

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
                parseChildren(parserContext, builder, "", element);

        builder.addPropertyValue(NODES_ATTR, nodes);
    }

    protected ManagedList<RuntimeBeanReference> parseChildren(
            ParserContext context, BeanDefinitionBuilder builder,
            String owningNodeName, Element element) {
        ManagedList<RuntimeBeanReference> nodes =
                new ManagedList<RuntimeBeanReference>();

        List<Element> children = DomUtils.getChildElements(element);
        for(Element child : children) {
            String childTagName = child.getLocalName();
            if(NODE_TAG.equals(childTagName)) {
                parseNode(context, "", child, nodes);
            } else {
                context.getReaderContext().error("Unknown tag", child);
            }
        }

        return nodes;
    }

    protected void parseNode(ParserContext context,
            String owningNodeName, Element element,
            ManagedList<RuntimeBeanReference> nodeReferences) {
        String name = element.getAttribute(NAME_ATTR);
        if(!StringUtils.hasLength(name)) {
            context.getReaderContext().error(
                    "'node' elements must have a 'name' attribute.",
                    element);
        }

        // Create full name by appending the owning node's name
        String fullName = owningNodeName + name;
        String owningName = fullName + ".";

        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(RegistryNodeProxy.class);

        // Process all child node tags
        List<Element> children = DomUtils.getChildElements(element);
        ManagedList<RuntimeBeanReference> childProxyIds =
                new ManagedList<RuntimeBeanReference>();

        for(Element child : children) {
            String childTagName = child.getLocalName();
            String childProxyId = null;

            // Parse the child node
            if(NODE_TAG.equals(childTagName)) {
                parseNode(context, owningName, child, nodeReferences);
            } else if(COUNTER_TAG.equals(childTagName)) {
                childProxyId = parseCounter(context, child);
            } else if(MINCOUNTER_TAG.equals(childTagName)) {
                childProxyId = parseMinOrMaxCounter(context, child, false);
            } else if(MAXCOUNTER_TAG.equals(childTagName)) {
                childProxyId = parseMinOrMaxCounter(context, child, true);
            } else if(WINDOWEDCOUNTER_TAG.equals(childTagName)) {
                childProxyId = parseWindowedCounter(context, child);
            } else if(CUSTOM_TAG.equals(childTagName)) {
                childProxyId = parseCustom(context, child,
                        builder.getRawBeanDefinition());
            } else {
                context.getReaderContext().error("Unknown tag", child);
            }

            if(childProxyId != null) {
                childProxyIds.add(new RuntimeBeanReference(childProxyId));
            }
        }

        // Finish building the node proxy
        builder.addPropertyValue(NAME_ATTR, fullName);
        builder.addPropertyValue(CHILDREN_ATTR, childProxyIds);
        String nodeId = context.getReaderContext().registerWithGeneratedName(
                builder.getBeanDefinition());

        // Add a runtime ref to the node proxy to the list of node proxies
        nodeReferences.add(new RuntimeBeanReference(nodeId));
    }

    protected String parseCounter(ParserContext context, Element element) {
        String name = getName(context, element);

        // Create bean definition for accumulator
        BeanDefinitionBuilder accBdb =
                BeanDefinitionBuilder.genericBeanDefinition(Counter.class);
        String accBeanId = context.getReaderContext()
                .registerWithGeneratedName(accBdb.getBeanDefinition());

        // Create proxy that carries name
        BeanDefinitionBuilder accProxyBdb = BeanDefinitionBuilder.
            genericBeanDefinition(RegistryNodeChildProxy.class);
        accProxyBdb.addPropertyValue(NAME_ATTR, name);
        accProxyBdb.addPropertyReference(CHILD_ATTR, accBeanId);
        return context.getReaderContext()
                .registerWithGeneratedName(accProxyBdb.getBeanDefinition());
    }

    protected String parseMinOrMaxCounter(ParserContext context,
            Element element, boolean max) {
        String name = getName(context, element);

        // Create bean definition for accumulator
        BeanDefinitionBuilder accBdb = BeanDefinitionBuilder
                .genericBeanDefinition(max ?
                MaxCounter.class : MinCounter.class);
        accBdb.addPropertyValue(MAX_ATTR, max);
        String accBeanId = context.getReaderContext()
                .registerWithGeneratedName(accBdb.getBeanDefinition());

        // Create proxy that carries name
        BeanDefinitionBuilder accProxyBdb = BeanDefinitionBuilder.
            genericBeanDefinition(RegistryNodeChildProxy.class);
        accProxyBdb.addPropertyValue(NAME_ATTR, name);
        accProxyBdb.addPropertyReference(CHILD_ATTR, accBeanId);
        return context.getReaderContext()
                .registerWithGeneratedName(accProxyBdb.getBeanDefinition());
    }

    protected String parseWindowedCounter(ParserContext context,
            Element element) {
        String name = getName(context, element);

        // Create bean definition for accumulator
        BeanDefinitionBuilder accBdb = BeanDefinitionBuilder
                .genericBeanDefinition(WindowedCounter.class);

        // Parse time window parameters
        String timewindow = element.getAttribute(TIMEWINDOW_ATTR);
        if(!StringUtils.hasText(timewindow)) {
            context.getReaderContext().error(
                    "timewindow is required.",
                    element);
        }

        String intervals = element.getAttribute(INTERVALS_ATTR);
        if(!StringUtils.hasText(intervals)) {
            context.getReaderContext().error(
                    "intervals is required.",
                    element);
        }

        accBdb.addConstructorArgValue(timewindow);
        accBdb.addConstructorArgValue(intervals);

        String interval = element.getAttribute(INTERVAL_ATTR);
        if(StringUtils.hasText(interval)) {
            if(interval.equals(INTERVAL_POWERSOF2)) {
                accBdb.addConstructorArgValue(true);
            } else if(interval.equals(INTERVAL_SECONDS)) {
                accBdb.addConstructorArgValue(false);
            }
        }

        String timeReporter = element.getAttribute(TIMEREPORTER_ATTR);
        if(StringUtils.hasText(timeReporter)) {
            accBdb.addConstructorArgReference(timeReporter);
        }

        String accBeanId = context.getReaderContext()
                .registerWithGeneratedName(accBdb.getBeanDefinition());

        // Create proxy that carries name
        BeanDefinitionBuilder accProxyBdb = BeanDefinitionBuilder.
            genericBeanDefinition(RegistryNodeChildProxy.class);
        accProxyBdb.addPropertyValue(NAME_ATTR, name);
        accProxyBdb.addPropertyReference(CHILD_ATTR, accBeanId);
        return context.getReaderContext()
                .registerWithGeneratedName(accProxyBdb.getBeanDefinition());
    }

    protected String parseCustom(ParserContext context, Element element,
            BeanDefinition containingBean) {
        String name = getName(context, element);
        String ref = element.getAttribute(REF_ATTR);

        String customBeanId = null;
        if(StringUtils.hasLength(ref)) {
            customBeanId = ref;
        } else {
            // Grab the single child element, that should define or point
            // to the custom Accumulator or Calcuation bean definition.
            NodeList childList = element.getChildNodes();
            Element child = null;
            for(int i=0; i<childList.getLength(); i++) {
                Node childNode = childList.item(i);
                if(!(childNode instanceof Element)) {
                    continue;
                }

                if(child != null) {
                    context.getReaderContext().error(
                            "'custom' elements without a 'ref' attribute must "
                            + "have exactly one 'bean', 'ref', or 'idref' child"
                            + " element.",
                            context.extractSource(element));
                }
                child = (Element)childNode;
            }

            if(child == null) {
                context.getReaderContext().error(
                        "'custom' elements must specify a 'ref' attribute or a "
                        + "single 'bean', 'ref', or 'idref' child element.",
                        context.extractSource(element));
            }

            // Parse the contents of the custom bean
            Object o = context.getDelegate().parsePropertySubElement(child,
                    containingBean);

            if(o instanceof BeanDefinitionHolder) {
                BeanDefinitionHolder bdh = (BeanDefinitionHolder)o;
                customBeanId = bdh.getBeanName();
                if(!StringUtils.hasLength(customBeanId)) {
                    // They didn't give their bean an id, so we'll need to
                    // generate one for it now.
                    customBeanId = context.getReaderContext()
                            .generateBeanName(bdh.getBeanDefinition());
                }

                // Register this bean
                context.getRegistry().registerBeanDefinition(customBeanId,
                        bdh.getBeanDefinition());
            } else if(o instanceof RuntimeBeanReference) {
                RuntimeBeanReference rbr = (RuntimeBeanReference)o;
                customBeanId = rbr.getBeanName();
            } else if(o instanceof RuntimeBeanNameReference) {
                RuntimeBeanNameReference rbnr = (RuntimeBeanNameReference)o;
                customBeanId = rbnr.getBeanName();
            }
        }

        // Create proxy that associates the given name with this child
        BeanDefinitionBuilder accProxyBdb = BeanDefinitionBuilder.
            genericBeanDefinition(RegistryNodeChildProxy.class);
        accProxyBdb.addPropertyValue(NAME_ATTR, name);
        accProxyBdb.addPropertyReference(CHILD_ATTR, customBeanId);
        accProxyBdb.getRawBeanDefinition().setSource(element);

        return context.getReaderContext()
                .registerWithGeneratedName(accProxyBdb.getBeanDefinition());
    }

    protected String getName(ParserContext context, Element element) {
        String name = element.getAttribute(NAME_ATTR);
        if(!StringUtils.hasLength(name)) {
            context.getReaderContext().error(
                    "'" + element.getTagName() + "' tag requires a 'name'.",
                    element);
        }
        return name;
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
