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
public class JmxExporterBeanDefinitionParser extends
        AbstractSingleBeanDefinitionParser {

    private static final String AUTOPUBLISH_ATTR = "auto-publish";
    private static final String AUTOPUBLISH_PROP = "autoPublish";
    private static final String DEPENDSON_ATTR = "depends-on";

    @Override
    protected String getBeanClassName(Element element) {
        return JmxExporterBean.class.getCanonicalName();
    }

    @Override
    protected void doParse(Element element, ParserContext context,
        BeanDefinitionBuilder builder) {
        String tmp = element.getAttribute(AUTOPUBLISH_ATTR);
        if(StringUtils.hasLength(tmp)) {
            builder.addPropertyValue(AUTOPUBLISH_PROP, tmp);
        }

        tmp = element.getAttribute(DEPENDSON_ATTR);
        if(StringUtils.hasLength(tmp)) {
            for(String id : StringUtils.commaDelimitedListToSet(tmp)) {
                builder.addDependsOn(id);
            }
        } else {
            builder.addDependsOn(
                    RegistrarBeanDefinitionParser.MASTER_REGISTRAR_ID);
        }
        builder.setLazyInit(false);
    }

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }
}
