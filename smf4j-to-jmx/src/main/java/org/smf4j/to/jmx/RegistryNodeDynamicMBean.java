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
package org.smf4j.to.jmx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.Descriptor;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.JMX;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.modelmbean.DescriptorSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smf4j.Accumulator;
import org.smf4j.Calculator;
import org.smf4j.util.helpers.CalculatorHelper;
import org.smf4j.RegistryNode;
import org.smf4j.util.helpers.CalculatorProperty;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class RegistryNodeDynamicMBean implements DynamicMBean {

    private static final String ATTR_ON = "On";
    private static final String OPER_CLEAR_ON = "clearOn";
    private static final String ROOT_NAME = "[root]";
    private static final String DEFAULT_DOMAIN = "smf4j";

    private static final String JMX_DESC_KEY_UNITS = "units";

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final String domain;
    private final RegistryNode registryNode;
    private final MBeanInfo mBeanInfo;
    private final ObjectName objectName;
    private final String name;

    public RegistryNodeDynamicMBean(RegistryNode registryNode) {
        this(DEFAULT_DOMAIN, registryNode);
    }

    public RegistryNodeDynamicMBean(String domain, RegistryNode registryNode) {
        this.domain = valueOrDefault(domain, DEFAULT_DOMAIN);
        this.registryNode = registryNode;
        this.name = valueOrDefault(registryNode.getName(), ROOT_NAME);
        this.mBeanInfo = buildMBeanInfo();
        this.objectName = buildObjectName(domain, name);
    }

    public Object getAttribute(String attribute)
    throws AttributeNotFoundException, MBeanException, ReflectionException {
        if(ATTR_ON.equals(attribute)) {
            return registryNode.isOn();
        }

        // Is it a named accumulator?
        Accumulator acc = registryNode.getAccumulator(attribute);
        if(acc != null) {
            return acc.get();
        }

        // Try to treat it like a value created by a registered Calcuator
        // instance.
        try {
            Map<String, Object> snapshot = registryNode.snapshot();
            return CalculatorHelper.resolveValue(snapshot, attribute);
        } catch(UnsupportedOperationException e) {
            log.error(String.format("An error occurred attempting to resolve "
                    + "the Calculator-derived value '%s' in node '%s'.",
                    attribute, name), e);
        }

        throw new AttributeNotFoundException();
    }

    public void setAttribute(Attribute attribute)
    throws AttributeNotFoundException, InvalidAttributeValueException,
    MBeanException, ReflectionException {
        if(ATTR_ON.equals(attribute.getName())) {
            Object obj = attribute.getValue();
            if(obj instanceof Boolean) {
                registryNode.setOn((Boolean)obj);
                return;
            }
            throw new InvalidAttributeValueException(attribute.getName());
        }

        throw new AttributeNotFoundException(attribute.getName());
    }

    public AttributeList getAttributes(String[] attributes) {
        AttributeList list = new AttributeList();
        if(attributes == null) {
            return list;
        }

        for(String attribute : attributes) {
            try {
                list.add(new Attribute(attribute, getAttribute(attribute)));
            } catch (AttributeNotFoundException ex) {
                log.error(String.format("An error occurred attempting to "
                        + "resolve the attribute '%s' in node '%s'.", attribute,
                        name), ex);
            } catch (MBeanException ex) {
                log.error(String.format("An error occurred attempting to "
                        + "resolve the attribute '%s' in node '%s'.", attribute,
                        name), ex);
            } catch (ReflectionException ex) {
                log.error(String.format("An error occurred attempting to "
                        + "resolve the attribute '%s' in node '%s'.", attribute,
                        name), ex);
            }
        }
        return list;
    }

    public AttributeList setAttributes(AttributeList attributes) {
        return new AttributeList();
    }

    public Object invoke(String actionName, Object[] params, String[] signature)
    throws MBeanException, ReflectionException {
        if(OPER_CLEAR_ON.equals(actionName)) {
            registryNode.clearOn();
        }
        return null;
    }

    public MBeanInfo getMBeanInfo() {
        return mBeanInfo;
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    static ObjectName buildObjectName(String domain, String name) {
        try {
            ObjectName tmp =
                    new ObjectName(domain + ":type=RegistryNode,name=" + name);
            return tmp;
        } catch(MalformedObjectNameException e) {
            return null;
        }
    }

    private MBeanInfo buildMBeanInfo() {
        List<MBeanAttributeInfo> attrs = new ArrayList<MBeanAttributeInfo>();
        attrs.add(new MBeanAttributeInfo(ATTR_ON,
                boolean.class.getCanonicalName(),
                "Whether the node is off or on.",
                true, true, true));

        gatherAccumulatorAttributeInfos(attrs);
        gatherCalculatorAttributeInfos(attrs);

        MBeanOperationInfo[] opers = {
            new MBeanOperationInfo(OPER_CLEAR_ON,
                "Forces the node to use its parent's off/on state.", null,
                java.lang.Void.class.getCanonicalName(),
                MBeanOperationInfo.ACTION)
        };

        DescriptorSupport desc = new DescriptorSupport();
        desc.setField(JMX.IMMUTABLE_INFO_FIELD, Boolean.FALSE.toString());

        return new MBeanInfo(getClass().getCanonicalName(), name,
                attrs.toArray(new MBeanAttributeInfo[attrs.size()]), null,
                opers, null, desc);
    }

    protected void gatherAccumulatorAttributeInfos(
            List<MBeanAttributeInfo> attrs) {
        for(Map.Entry<String, Accumulator> entry :
                registryNode.getAccumulators().entrySet()) {
            attrs.add(createAccumluatorAttributeInfo(entry.getKey(),
                    entry.getValue()));
        }
    }

    protected MBeanAttributeInfo createAccumluatorAttributeInfo(
            String accumulatorName, Accumulator accumulator) {
        Descriptor desc = null;
        String units = accumulator.getUnits();
        if(units != null) {
            // If the accumulator provides a units description we'll drop
            // into our MBeanAttributeInfo
            desc = new DescriptorSupport();
            desc.setField(JMX_DESC_KEY_UNITS, units);
        }

        return new MBeanAttributeInfo(accumulatorName,
                long.class.getCanonicalName(), accumulatorName, true, false,
                false, desc);
    }

    protected void gatherCalculatorAttributeInfos(List<MBeanAttributeInfo> attrs) {
        for(Map.Entry<String, Calculator> entry :
                registryNode.getCalculators().entrySet()) {
            List<CalculatorProperty> cattrs =
                    CalculatorHelper.getCalculatorAttributes(entry.getKey(),
                    entry.getValue());
            for(CalculatorProperty cattr : cattrs) {
                attrs.add(createCalculatorAttributeInfo(cattr));
            }
        }
    }

    protected MBeanAttributeInfo createCalculatorAttributeInfo(
            CalculatorProperty cattr) {
        Descriptor desc = null;
        String units = cattr.getUnits();
        if(units != null) {
            desc = new DescriptorSupport();
            desc.setField(JMX_DESC_KEY_UNITS, units);
        }

        return new MBeanAttributeInfo(cattr.getName(),
                long.class.getCanonicalName(),
                cattr.getName(), true, false, false, desc);
    }

    private String valueOrDefault(String value, String defaultValue) {
        if(value == null || value.equals("")) {
            return defaultValue;
        }
        return value;
    }
}
