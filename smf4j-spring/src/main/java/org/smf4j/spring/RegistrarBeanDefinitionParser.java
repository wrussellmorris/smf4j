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

import static org.smf4j.spring.CounterConfig.*;

import java.util.List;
import org.smf4j.core.accumulator.hc.HighConcurrencyAccumulator;
import org.smf4j.core.accumulator.lc.LowConcurrencyAccumulator;
import org.smf4j.core.accumulator.PowersOfTwoIntervalStrategy;
import org.smf4j.core.accumulator.SecondsIntervalStrategy;
import org.smf4j.core.accumulator.hc.UnboundedAddMutator;
import org.smf4j.core.accumulator.hc.UnboundedMaxMutator;
import org.smf4j.core.accumulator.hc.UnboundedMinMutator;
import org.smf4j.core.accumulator.hc.WindowedAddMutator;
import org.smf4j.core.accumulator.hc.WindowedMinMutator;
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
    public static final String MIN_TAG = "min";
    public static final String MAX_TAG = "max";
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
                CounterConfig config = new CounterConfig(CounterType.ADD,
                        child);
                childProxyId = createCounter(context, child, config);
            } else if(MIN_TAG.equals(childTagName)) {
                CounterConfig config = new CounterConfig(CounterType.MIN,
                        child);
                childProxyId = createCounter(context, child, config);
            } else if(MAX_TAG.equals(childTagName)) {
                CounterConfig config = new CounterConfig(CounterType.MAX,
                        child);
                childProxyId = createCounter(context, child, config);
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

    private String createCounter(ParserContext context, Element element,
            CounterConfig config) {
        String name = getName(context, element);

        if(config.getConcurrencyType() == ConcurrencyType.UNKNOWN) {
            context.getReaderContext().error("Unknown concurrency type.",
                    context.extractSource(element));
            return null;
        }
        if(config.getDurationType() == DurationType.UNKNOWN) {
            context.getReaderContext().error("Unknown duration type.",
                    context.extractSource(element));
            return null;
        }
        if(config.getIntervalsType() == IntervalsType.UNKNOWN) {
            context.getReaderContext().error("Unknown intervals type.",
                    context.extractSource(element));
            return null;
        }

        // Create bean definition for the mutator factory
        String mutatorFactoryId;
        switch(config.getDurationType()) {
            case NA:
            case UNBOUNDED:
                mutatorFactoryId = createUnboundedMutatorFactory(context,
                        element, config);
                break;
            case WINDOWED:
                mutatorFactoryId = createWindowedMutatorFactory(context,
                        element, config);
                break;
            default:
                context.getReaderContext().error("Unexpected duration type.",
                        context.extractSource(element));
                return null;
        }

        if(mutatorFactoryId == null) {
            // Something went wrong creating the factory
            return null;
        }

        // Create bean definition for accumulator
        Class<?> accumulatorClass;
        switch(config.getConcurrencyType()) {
            case HIGH:
            case NA:
                accumulatorClass = HighConcurrencyAccumulator.class;
                break;
            case LOW:
                accumulatorClass = LowConcurrencyAccumulator.class;
                break;
            default:
                context.getReaderContext().error("Unexpected concurrency type.",
                        context.extractSource(element));
                return null;
        }
        BeanDefinitionBuilder accBdb =
                BeanDefinitionBuilder.genericBeanDefinition(accumulatorClass);
        accBdb.addConstructorArgReference(mutatorFactoryId);
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

    private String createUnboundedMutatorFactory(
            ParserContext context, Element element, CounterConfig config) {
        Class<?> mutatorFactoryClass;
        // We need to create unbounded mutators
        switch(config.getCounterType()) {
            case ADD:
                mutatorFactoryClass = UnboundedAddMutator.Factory.class;
                break;
            case MIN:
                mutatorFactoryClass = UnboundedMinMutator.Factory.class;
                break;
            case MAX:
                mutatorFactoryClass = UnboundedMaxMutator.Factory.class;
                break;
            default:
                context.getReaderContext().error(
                        "Unexpected counter type.",
                        context.extractSource(element));
                return null;
        }
        BeanDefinitionBuilder mutatorFactoryBdb = BeanDefinitionBuilder
                .genericBeanDefinition(mutatorFactoryClass);
        return context.getReaderContext().registerWithGeneratedName(
                mutatorFactoryBdb.getBeanDefinition());
    }

    private String createWindowedMutatorFactory(
            ParserContext context, Element element, CounterConfig config) {

        // Find the mutator factory class for this type of windowed counter
        Class<?> mutatorFactoryClass;
        switch(config.getCounterType()) {
            case ADD:
                mutatorFactoryClass = WindowedAddMutator.Factory.class;
                break;
            case MIN:
                mutatorFactoryClass = WindowedMinMutator.Factory.class;
                break;
            case MAX:
                mutatorFactoryClass = UnboundedMaxMutator.Factory.class;
                break;
            default:
                context.getReaderContext().error("Unexpected counter type.",
                        context.extractSource(element));
                return null;
        }

        // Find the interval strategy class for this kind of interval
        Class<?> intervalStrategyClass;
        switch(config.getIntervalsType()) {
            case SECONDS:
            case NA: // Seconds is the default
                intervalStrategyClass = SecondsIntervalStrategy.class;
                break;
            case POWERSOFTWO:
                intervalStrategyClass = PowersOfTwoIntervalStrategy.class;
                break;
            default:
                context.getReaderContext().error("Unexpected 'intervals' type.",
                        context.extractSource(element));
                return null;
        }

        // Make sure the timewindow and interval are specified correctly
        if(config.getTimeWindow() == null) {
            context.getReaderContext().error("'windowed' counters must "
                    + "specify a valid 'time-window'.",
                    context.extractSource(element));
            return null;
        }
        if(config.getNumIntervals() == null) {
            context.getReaderContext().error("'windowed' counters must "
                    + "specify an 'intervals'.",
                    context.extractSource(element));
            return null;
        }

        // Create a strategy instance for the strategy selected
        BeanDefinitionBuilder strategyBdb = BeanDefinitionBuilder
                .genericBeanDefinition(intervalStrategyClass);
        strategyBdb.addConstructorArgValue(config.getTimeWindow());
        strategyBdb.addConstructorArgValue(config.getNumIntervals());
        String strategyBeanId = context.getReaderContext()
                .registerWithGeneratedName(strategyBdb.getBeanDefinition());

        // Create a mutator factory referring to the strategy
        BeanDefinitionBuilder mutatorFactoryBdb = BeanDefinitionBuilder
                .genericBeanDefinition(mutatorFactoryClass);
        mutatorFactoryBdb.addConstructorArgReference(strategyBeanId);
        return context.getReaderContext().registerWithGeneratedName(
                mutatorFactoryBdb.getBeanDefinition());
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
