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

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.List;
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
public class CsvFileBeanDefinitionParser extends
        AbstractSingleBeanDefinitionParser {

    private static final String CSVFILE_CLASS = "org.smf4j.to.csv.CsvFile";
    private static final String CSVFILELAYOUT_CLASS
            = "org.smf4j.to.csv.CsvFileLayout";

    private static final String FILTER_TAG = "filter";

    private static final String PATH_ATTR = "path";
    private static final String TIMESTAMP_COLUMN_ATTR = "timestampColumn";
    private static final String TIMESTAMP_COLUMN_HEADER_ATTR
            = "timestampColumnHeader";
    private static final String LINE_ENDING_ATTR = "lineEnding";
    private static final String APPEND_ATTR = "append";
    private static final String DELIMETER_ATTR = "delimeter";
    private static final String QUOTE_ATTR = "quote";
    private static final String CHARSET_ATTR = "charset";
    private static final String ROLLOVER_TIMESTAMP_PATTERN =
            "rolloverTimestampPattern";
    private static final String COLUMN_TIMESTAMP_PATTERN =
            "columnTimestampPattern";
    private static final String MAXSIZE_ATTR = "maxSize";
    private static final String VALUE_ATTR = "value";
    private static final String FILTERS_ATTR = "filters";
    private static final String LAYOUT_ATTR = "layout";

    private static final String CR = "cr";
    private static final String LF = "lf";
    private static final String CRLF = CR + LF;

    @Override
    protected String getBeanClassName(Element element) {
        return CSVFILE_CLASS;
    }

    @Override
    protected void doParse(Element element, ParserContext context,
        BeanDefinitionBuilder builder) {
        parseTopLevelProperties(element, context, builder);
        String layoutId = createLayout(element, context, builder);
        builder.addPropertyReference(LAYOUT_ATTR, layoutId);
    }

    private void parseTopLevelProperties(Element element, ParserContext context,
            BeanDefinitionBuilder builder) {
        String tmp;
        tmp = element.getAttribute(PATH_ATTR);
        if(StringUtils.hasLength(tmp)) {
            builder.addPropertyValue(PATH_ATTR, tmp);
        }
        tmp = element.getAttribute(TIMESTAMP_COLUMN_ATTR);
        if(StringUtils.hasLength(tmp)) {
            builder.addPropertyValue(TIMESTAMP_COLUMN_ATTR, tmp);
        }
        tmp = element.getAttribute(TIMESTAMP_COLUMN_HEADER_ATTR);
        if(StringUtils.hasLength(tmp)) {
            builder.addPropertyValue(TIMESTAMP_COLUMN_HEADER_ATTR, tmp);
        }
        tmp = element.getAttribute(LINE_ENDING_ATTR);
        if(StringUtils.hasLength(tmp)) {
            String lineEnding = System.getProperty("line.separator");
            if(CR.equals(tmp)) {
                lineEnding = "\r";
            } else if(LF.equals(tmp)) {
                lineEnding = "\n";
            } else if(CRLF.equals(tmp)) {
                lineEnding = "\r\n";
            } else {
                context.getReaderContext().warning(String.format(
                        "Unknown line ending '%s'.  Using system-default "
                        + "instead.", tmp),
                        context.getReaderContext().extractSource(element));
            }
            builder.addPropertyValue(LINE_ENDING_ATTR, lineEnding);
        }
        tmp = element.getAttribute(APPEND_ATTR);
        if(StringUtils.hasLength(tmp)) {
            builder.addPropertyValue(APPEND_ATTR, tmp);
        }
        tmp = element.getAttribute(DELIMETER_ATTR);
        if(StringUtils.hasLength(tmp)) {
            builder.addPropertyValue(DELIMETER_ATTR, tmp);
        }
        tmp = element.getAttribute(QUOTE_ATTR);
        if(StringUtils.hasLength(tmp)) {
            builder.addPropertyValue(QUOTE_ATTR, tmp);
        }
        tmp = element.getAttribute(CHARSET_ATTR);
        if(StringUtils.hasLength(tmp)) {
            boolean validCharset = false;
            try {
                validCharset = Charset.isSupported(tmp);
            } catch(IllegalCharsetNameException e) {
            }

            if(!validCharset) {
                String defaultCharset = Charset.defaultCharset().name();
                context.getReaderContext().warning(String.format(
                        "Unknown charset '%s'.  Using system-default charset "
                        + "'%s' instead.", tmp, defaultCharset),
                        context.getReaderContext().extractSource(element));

            }
            builder.addPropertyValue(CHARSET_ATTR, tmp);
        }
        tmp = element.getAttribute(ROLLOVER_TIMESTAMP_PATTERN);
        if(StringUtils.hasLength(tmp)) {
            builder.addPropertyValue(ROLLOVER_TIMESTAMP_PATTERN, tmp);
        }
        tmp = element.getAttribute(COLUMN_TIMESTAMP_PATTERN);
        if(StringUtils.hasLength(tmp)) {
            builder.addPropertyValue(COLUMN_TIMESTAMP_PATTERN, tmp);
        }
        tmp = element.getAttribute(MAXSIZE_ATTR);
        if(StringUtils.hasLength(tmp)) {
            builder.addPropertyValue(MAXSIZE_ATTR, tmp);
        }
    }

    private String createLayout(Element element, ParserContext context,
            BeanDefinitionBuilder builder) {
        List<String> filters = new ArrayList<String>();
        List<Element> children = DomUtils.getChildElementsByTagName(element,
                FILTER_TAG);
        for(Element child : children) {
            String tmp = child.getAttribute(VALUE_ATTR);
            if(StringUtils.hasLength(tmp)) {
                filters.add(tmp);
            }
        }

        BeanDefinitionBuilder bdb = BeanDefinitionBuilder.genericBeanDefinition(
                CSVFILELAYOUT_CLASS);
        bdb.addPropertyValue(FILTERS_ATTR, filters);
        return context.getReaderContext().registerWithGeneratedName(
                bdb.getBeanDefinition());
    }
}
