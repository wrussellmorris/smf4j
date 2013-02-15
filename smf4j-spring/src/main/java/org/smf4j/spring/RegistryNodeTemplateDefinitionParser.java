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

import static org.smf4j.spring.CounterConfig.*;

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

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class RegistryNodeTemplateDefinitionParser extends
        AbstractSingleBeanDefinitionParser {

    public static final String MASTER_REGISTRAR_ID = "__registrar__#ID#";

    public static final String NODE_TAG = "node";
    public static final String NODE_TEMPLATE_TAG = "node-template";
    public static final String COUNTER_TAG = "counter";
    public static final String MIN_TAG = "min";
    public static final String MAX_TAG = "max";
    public static final String NORMALIZE_TAG = "normalize";
    public static final String RATIO_TAG = "ratio";
    public static final String RANGEGROUP_TAG = "range-group";
    public static final String CUSTOM_TAG = "custom";

    public static final String CHILD_ATTR = "child";
    public static final String CHILDREN_ATTR = "children";
    public static final String MAX_ATTR = "max";
    public static final String NAME_ATTR = "name";
    public static final String NODES_ATTR = "nodeProxies";
    public static final String REF_ATTR = "ref";
    public static final String UNITS_ATTR = "units";
    public static final String FREQUENCY_ATTR = "frequency";
    public static final String ACCUMULATOR_ATTR = "accumulator";
    public static final String NUMERATOR_ATTR = "numerator";
    public static final String DENOMINATOR_ATTR = "denominator";
    public static final String NORMALIZE_ATTR = "normalize";
    public static final String GROUPINGS_ATTR = "groupings";
    public static final String RANGES_ATTR = "ranges";
    public static final String SUFFIXES_ATTR = "suffixes";
    public static final String RANGE_ATTR = "range";
    public static final String SUFFIX_ATTR = "suffix";
    public static final String THRESHOLD_ATTR = "threshold";
    public static final String FORMAT_ATTR = "format";
    public static final String FORMATSTRING_ATTR = "formatString";
    public static final String TEMPLATE_ATTR = "template";

    public static final String HC_ACCUMULATOR_CLASS =
            "org.smf4j.core.accumulator.hc.HighContentionAccumulator";
    public static final String HC_UNBOUNDED_ADD_MUTATOR_CLASS =
            "org.smf4j.core.accumulator.hc.UnboundedAddMutator.Factory";
    public static final String HC_UNBOUNDED_MAX_MUTATOR_CLASS =
            "org.smf4j.core.accumulator.hc.UnboundedMaxMutator.Factory";
    public static final String HC_UNBOUNDED_MIN_MUTATOR_CLASS =
            "org.smf4j.core.accumulator.hc.UnboundedMinMutator.Factory";
    public static final String HC_WINDOWED_ADD_MUTATOR_CLASS =
            "org.smf4j.core.accumulator.hc.WindowedAddMutator.Factory";
    public static final String HC_WINDOWED_MAX_MUTATOR_CLASS =
            "org.smf4j.core.accumulator.hc.WindowedMaxMutator.Factory";
    public static final String HC_WINDOWED_MIN_MUTATOR_CLASS =
            "org.smf4j.core.accumulator.hc.WindowedMinMutator.Factory";

    public static final String LC_ACCUMULATOR_CLASS =
            "org.smf4j.core.accumulator.lc.LowContentionAccumulator";
    public static final String LC_UNBOUNDED_ADD_MUTATOR_CLASS =
            "org.smf4j.core.accumulator.lc.UnboundedAddMutator.Factory";
    public static final String LC_UNBOUNDED_MAX_MUTATOR_CLASS =
            "org.smf4j.core.accumulator.lc.UnboundedMaxMutator.Factory";
    public static final String LC_UNBOUNDED_MIN_MUTATOR_CLASS =
            "org.smf4j.core.accumulator.lc.UnboundedMinMutator.Factory";
    public static final String LC_WINDOWED_ADD_MUTATOR_CLASS =
            "org.smf4j.core.accumulator.lc.WindowedAddMutator.Factory";
    public static final String LC_WINDOWED_MAX_MUTATOR_CLASS =
            "org.smf4j.core.accumulator.lc.WindowedMaxMutator.Factory";
    public static final String LC_WINDOWED_MIN_MUTATOR_CLASS =
            "org.smf4j.core.accumulator.lc.WindowedMinMutator.Factory";

    public static final String SECONDS_INTERVAL_STRATEGY_CLASS =
            "org.smf4j.core.accumulator.SecondsIntervalStrategy";
    public static final String POWERS_OF_TWO_INTERVAL_STRATEGY_CLASS =
            "org.smf4j.core.accumulator.PowersOfTwoIntervalStrategy";
    public static final String FREQUENCY_CLASS =
            "org.smf4j.core.calculator.Frequency";
    public static final String NORMALIZER_CLASS =
            "org.smf4j.core.calculator.Normalizer";
    public static final String RATIO_CLASS =
            "org.smf4j.core.calculator.Ratio";
    public static final String RANGEGROUP_CLASS =
            "org.smf4j.core.calculator.RangeGroup";
    public static final String RANGEGROUP_GROUPING_CLASS =
            RANGEGROUP_CLASS + ".Grouping";

    private final boolean createPrototype;

    public RegistryNodeTemplateDefinitionParser() {
        this(true);
    }

    RegistryNodeTemplateDefinitionParser(boolean createPrototype) {
        this.createPrototype = createPrototype;
    }

    @Override
    protected String getBeanClassName(Element element) {
        return RegistryNodeProxy.class.getCanonicalName();
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext,
        BeanDefinitionBuilder builder) {
        parseNode(parserContext, element, builder);
    }

    protected String createNodeTemplateRef(ParserContext context,
            Element element) {
        String id = element.getAttribute(REF_ATTR);
        if(!StringUtils.hasLength(id)) {
            context.getReaderContext().error(
                    "<node-template> MUST must have a 'name'.",
                    context.extractSource(element));
            return null;
        }

        BeanDefinitionBuilder bdb = getBdb(RegistryNodeTemplateProxy.class);
        String newName = element.getAttribute(NAME_ATTR);
        if(StringUtils.hasLength(newName)) {
            bdb.addPropertyValue(NAME_ATTR, newName);
        }

        bdb.addPropertyReference(TEMPLATE_ATTR, id);
        return context.getReaderContext().registerWithGeneratedName(
                bdb.getBeanDefinition());
    }

    protected String parseNode(ParserContext context, Element element,
            BeanDefinitionBuilder builder) {
        String name = element.getAttribute(NAME_ATTR);
        if(!StringUtils.hasLength(name)) {
            context.getReaderContext().error(
                    "'node' elements must have a 'name' attribute.",
                    element);
        }

        // Process all child node tags
        List<Element> children = DomUtils.getChildElements(element);
        ManagedList<RuntimeBeanReference> childProxyIds =
                new ManagedList<RuntimeBeanReference>();

        for(Element child : children) {
            String childTagName = child.getLocalName();
            String childProxyId = null;

            // Parse the child node
            if(NODE_TAG.equals(childTagName)) {
                BeanDefinitionBuilder bdb = getBdb(RegistryNodeProxy.class);
                childProxyId = parseNode(context, child, bdb);
            } else if(NODE_TEMPLATE_TAG.equals(childTagName)) {
                childProxyId = createNodeTemplateRef(context, child);
            } else if(COUNTER_TAG.equals(childTagName)) {
                CounterConfig config = new CounterConfig(
                        CounterType.ADD, child);
                childProxyId = createCounter(context, child, config);
            } else if(MIN_TAG.equals(childTagName)) {
                CounterConfig config = new CounterConfig(
                        CounterType.MIN, child);
                childProxyId = createCounter(context, child, config);
            } else if(MAX_TAG.equals(childTagName)) {
                CounterConfig config = new CounterConfig(
                        CounterType.MAX, child);
                childProxyId = createCounter(context, child, config);
            } else if(CUSTOM_TAG.equals(childTagName)) {
                childProxyId = parseCustom(context, child,
                        builder.getRawBeanDefinition());
            } else if(NORMALIZE_TAG.equals(childTagName)) {
                childProxyId = createNormalize(context, child);
            } else if(RATIO_TAG.equals(childTagName)) {
                childProxyId = createRatio(context, child);
            } else if(RANGEGROUP_TAG.equals(childTagName)) {
                childProxyId = createRangeGroup(context, child);
            } else {
                context.getReaderContext().error("Unknown tag", child);
            }

            if(childProxyId != null) {
                childProxyIds.add(new RuntimeBeanReference(childProxyId));
            }
        }

        // Finish building the node proxy
        builder.addPropertyValue(NAME_ATTR, name);
        builder.addPropertyValue(CHILDREN_ATTR, childProxyIds);
        String nodeId = context.getReaderContext().registerWithGeneratedName(
                builder.getBeanDefinition());

        return nodeId;
    }

    private String createCounter(ParserContext context, Element element,
            CounterConfig config) {
        String name = getName(context, element);

        if(config.getContentionType() == ContentionType.UNKNOWN) {
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
        String accumulatorClass;
        switch(config.getContentionType()) {
            case HIGH:
            case NA:
                accumulatorClass = HC_ACCUMULATOR_CLASS;
                break;
            case LOW:
                accumulatorClass = LC_ACCUMULATOR_CLASS;
                break;
            default:
                context.getReaderContext().error("Unexpected concurrency type.",
                        context.extractSource(element));
                return null;
        }
        BeanDefinitionBuilder accBdb = getBdb(accumulatorClass);
        accBdb.addConstructorArgReference(mutatorFactoryId);
        String accBeanId = context.getReaderContext()
                .registerWithGeneratedName(accBdb.getBeanDefinition());

        // Create proxy that carries name
        BeanDefinitionBuilder accProxyBdb =
                getBdb(RegistryNodeChildProxy.class);
        accProxyBdb.addPropertyValue(NAME_ATTR, name);
        accProxyBdb.addPropertyValue(CHILD_ATTR, accBeanId);
        return context.getReaderContext()
                .registerWithGeneratedName(accProxyBdb.getBeanDefinition());
    }

    private String createUnboundedMutatorFactory(
            ParserContext context, Element element, CounterConfig config) {
        String mutatorFactoryClass;
        // We need to create unbounded mutators
        switch(config.getCounterType()) {
            case ADD:
                mutatorFactoryClass =
                        (config.getContentionType() == ContentionType.HIGH) ?
                        HC_UNBOUNDED_ADD_MUTATOR_CLASS :
                        LC_UNBOUNDED_ADD_MUTATOR_CLASS;
                break;
            case MIN:
                mutatorFactoryClass =
                        (config.getContentionType() == ContentionType.HIGH) ?
                        HC_UNBOUNDED_MIN_MUTATOR_CLASS :
                        LC_UNBOUNDED_MIN_MUTATOR_CLASS;
                break;
            case MAX:
                mutatorFactoryClass =
                        (config.getContentionType() == ContentionType.HIGH) ?
                        HC_UNBOUNDED_MAX_MUTATOR_CLASS :
                        LC_UNBOUNDED_MAX_MUTATOR_CLASS;
                break;
            default:
                context.getReaderContext().error(
                        "Unexpected counter type.",
                        context.extractSource(element));
                return null;
        }
        BeanDefinitionBuilder mutatorFactoryBdb = getBdb(mutatorFactoryClass);
        return context.getReaderContext().registerWithGeneratedName(
                mutatorFactoryBdb.getBeanDefinition());
    }

    private String createWindowedMutatorFactory(
            ParserContext context, Element element, CounterConfig config) {

        // Find the mutator factory class for this type of windowed counter
        String mutatorFactoryClass;
        switch(config.getCounterType()) {
            case ADD:
                mutatorFactoryClass =
                        (config.getContentionType() == ContentionType.HIGH) ?
                        HC_WINDOWED_ADD_MUTATOR_CLASS :
                        LC_WINDOWED_ADD_MUTATOR_CLASS;
                break;
            case MIN:
                mutatorFactoryClass =
                        (config.getContentionType() == ContentionType.HIGH) ?
                        HC_WINDOWED_MIN_MUTATOR_CLASS :
                        LC_WINDOWED_MIN_MUTATOR_CLASS;
                break;
            case MAX:
                mutatorFactoryClass =
                        (config.getContentionType() == ContentionType.HIGH) ?
                        HC_WINDOWED_MAX_MUTATOR_CLASS :
                        LC_WINDOWED_MAX_MUTATOR_CLASS;
                break;
            default:
                context.getReaderContext().error("Unexpected counter type.",
                        context.extractSource(element));
                return null;
        }

        // Find the interval strategy class for this kind of interval
        String intervalStrategyClass;
        switch(config.getIntervalsType()) {
            case SECONDS:
            case NA: // Seconds is the default
                intervalStrategyClass = SECONDS_INTERVAL_STRATEGY_CLASS;
                break;
            case POWERSOFTWO:
                intervalStrategyClass = POWERS_OF_TWO_INTERVAL_STRATEGY_CLASS;
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
        BeanDefinitionBuilder strategyBdb = getBdb(intervalStrategyClass);
        strategyBdb.addConstructorArgValue(config.getTimeWindow());
        strategyBdb.addConstructorArgValue(config.getNumIntervals());
        String strategyBeanId = context.getReaderContext()
                .registerWithGeneratedName(strategyBdb.getBeanDefinition());

        // Create a mutator factory referring to the strategy
        BeanDefinitionBuilder mutatorFactoryBdb = getBdb(mutatorFactoryClass);
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
        BeanDefinitionBuilder accProxyBdb =
                getBdb(RegistryNodeChildProxy.class);
        accProxyBdb.addPropertyValue(NAME_ATTR, name);
        accProxyBdb.addPropertyValue(CHILD_ATTR, customBeanId);
        accProxyBdb.getRawBeanDefinition().setSource(element);

        return context.getReaderContext()
                .registerWithGeneratedName(accProxyBdb.getBeanDefinition());
    }

    protected String createNormalize(ParserContext context, Element element) {
        String name = getName(context, element);
        String accumulator = element.getAttribute(ACCUMULATOR_ATTR);
        String units = element.getAttribute(UNITS_ATTR);
        String freq = element.getAttribute(FREQUENCY_ATTR);
        Object frequency = getFrequency(context, element, freq);
        BeanDefinitionBuilder bdb = getBdb(NORMALIZER_CLASS);
        bdb.addPropertyValue(UNITS_ATTR, units);
        bdb.addPropertyValue(FREQUENCY_ATTR, frequency);
        bdb.addPropertyValue(ACCUMULATOR_ATTR, accumulator);
        String normId = context.getReaderContext().registerWithGeneratedName(
                bdb.getBeanDefinition());

        // Create proxy that carries name
        BeanDefinitionBuilder calcProxyBdb =
                getBdb(RegistryNodeChildProxy.class);
        calcProxyBdb.addPropertyValue(NAME_ATTR, name);
        calcProxyBdb.addPropertyValue(CHILD_ATTR, normId);
        return context.getReaderContext()
                .registerWithGeneratedName(calcProxyBdb.getBeanDefinition());
    }

    protected String createRatio(ParserContext context, Element element) {
        String name = getName(context, element);
        String numerator = element.getAttribute(NUMERATOR_ATTR);
        String denominator = element.getAttribute(DENOMINATOR_ATTR);
        String units = element.getAttribute(UNITS_ATTR);

        BeanDefinitionBuilder bdb = getBdb(RATIO_CLASS);
        bdb.addPropertyValue(UNITS_ATTR, units);
        bdb.addPropertyValue(NUMERATOR_ATTR, numerator);
        bdb.addPropertyValue(DENOMINATOR_ATTR, denominator);
        String ratioId = context.getReaderContext().registerWithGeneratedName(
                bdb.getBeanDefinition());

        // Create proxy that carries name
        BeanDefinitionBuilder calcProxyBdb =
                getBdb(RegistryNodeChildProxy.class);
        calcProxyBdb.addPropertyValue(NAME_ATTR, name);
        calcProxyBdb.addPropertyValue(CHILD_ATTR, ratioId);
        return context.getReaderContext()
                .registerWithGeneratedName(calcProxyBdb.getBeanDefinition());
    }

    protected String createRangeGroup(ParserContext context, Element element) {
        String name = getName(context, element);
        String accumulator = element.getAttribute(ACCUMULATOR_ATTR);
        String units = element.getAttribute(UNITS_ATTR);
        String ranges = element.getAttribute(RANGES_ATTR);
        String suffixes = element.getAttribute(SUFFIXES_ATTR);
        String norm = element.getAttribute(NORMALIZE_ATTR);
        String freq = element.getAttribute(FREQUENCY_ATTR);
        String threshold = element.getAttribute(THRESHOLD_ATTR);
        String format = element.getAttribute(FORMAT_ATTR);

        ManagedList<RuntimeBeanReference> groupings =
                createGroupings(context, element, ranges, suffixes);
        if(groupings == null) {
            return null;
        }

        boolean normalize = false;
        if(StringUtils.hasLength(norm)) {
            normalize = Boolean.parseBoolean(norm);
        }

        BeanDefinitionBuilder bdb = getBdb(RANGEGROUP_CLASS);
        bdb.addPropertyValue(UNITS_ATTR, units);
        bdb.addPropertyValue(GROUPINGS_ATTR, groupings);
        bdb.addPropertyValue(NORMALIZE_ATTR, normalize);
        Object frequency = getFrequency(context, element, freq);
        bdb.addPropertyValue(FREQUENCY_ATTR, frequency);
        bdb.addPropertyValue(ACCUMULATOR_ATTR, accumulator);
        bdb.addPropertyValue(THRESHOLD_ATTR, threshold);
        bdb.addPropertyValue(FORMATSTRING_ATTR, format);

        String grpId = context.getReaderContext().registerWithGeneratedName(
                bdb.getBeanDefinition());

        // Create proxy that carries name
        BeanDefinitionBuilder calcProxyBdb =
                getBdb(RegistryNodeChildProxy.class);
        calcProxyBdb.addPropertyValue(NAME_ATTR, name);
        calcProxyBdb.addPropertyValue(CHILD_ATTR, grpId);
        return context.getReaderContext()
                .registerWithGeneratedName(calcProxyBdb.getBeanDefinition());
    }

    protected ManagedList<RuntimeBeanReference> createGroupings(
            ParserContext context, Element element, String ranges,
            String suffixes) {
        ManagedList<RuntimeBeanReference> groupings =
                new ManagedList<RuntimeBeanReference>();
        String[] rangeArray =
                StringUtils.commaDelimitedListToStringArray(ranges);
        String[] suffixArray =
                StringUtils.commaDelimitedListToStringArray(suffixes);

        if(rangeArray.length != suffixArray.length) {
            context.getReaderContext().error(
                    "'ranges' and 'suffixes' must have the same number of "
                    + "elements", context.extractSource(element));
            return null;
        }

        for(int i=0; i<rangeArray.length; i++) {
            String range = rangeArray[i];
            String suffix = suffixArray[i];
            BeanDefinitionBuilder bdb = getBdb(RANGEGROUP_GROUPING_CLASS);
            bdb.addPropertyValue(RANGE_ATTR, range);
            bdb.addPropertyValue(SUFFIX_ATTR, suffix);
            String groupId = context.getReaderContext()
                    .registerWithGeneratedName(bdb.getBeanDefinition());
            groupings.add(new RuntimeBeanReference(groupId));
        }
        return groupings;
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

    protected BeanDefinitionBuilder getBdb(String className) {
        BeanDefinitionBuilder bdb =
                BeanDefinitionBuilder.genericBeanDefinition(className);
        if(createPrototype) {
            bdb.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        }
        return bdb;
    }

    protected BeanDefinitionBuilder getBdb(Class<?> clazz) {
        return getBdb(clazz.getCanonicalName());
    }

    protected Object getFrequency(ParserContext context, Element element,
            String freq) {
        Class<Enum> frequencyClass;
        try {
            frequencyClass = (Class<Enum>)Class.forName(FREQUENCY_CLASS);
        } catch(ClassNotFoundException e) {
                context.getReaderContext().error(
                        "Unable to load Frequency class '" +
                        FREQUENCY_CLASS + "'",
                        context.extractSource(element));
                return null;
        }

        Object frequency = null;
        if(StringUtils.hasLength(freq)) {
            try {
                frequency = Enum.valueOf(frequencyClass, freq.toUpperCase());
            } catch(IllegalArgumentException e) {
                context.getReaderContext().error(
                        "Unknown frequency '" + freq + "'",
                        context.extractSource(element));
                        return null;
            }
        }
        return frequency;
    }
}
