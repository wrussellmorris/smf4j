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
package org.smf4j.example.webcrawler;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Pattern;
import org.smf4j.Accumulator;
import org.smf4j.RegistrarFactory;
import org.smf4j.RegistryNode;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class Smf4jDataCollectorCrawler extends WebCrawler {

    private final RegistryNode crawlerNode = RegistrarFactory.getRegistrar().getNode("crawler");
    private final Accumulator download = crawlerNode.getAccumulator("download");
    private final Accumulator processedPages = crawlerNode.getAccumulator("processedPages");
    private final Accumulator linksFound = crawlerNode.getAccumulator("linksFound");
    private final Accumulator textSize = crawlerNode.getAccumulator("textSize");

    Pattern filters = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" + "|png|tiff?|mid|mp2|mp3|mp4"
            + "|wav|avi|mov|mpeg|ram|m4v|pdf" + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

    public Smf4jDataCollectorCrawler() {
    }

    @Override
    public boolean shouldVisit(WebURL url) {
        String href = url.getURL().toLowerCase();
        return !filters.matcher(href).matches() && href.startsWith("http://www.ics.uci.edu/");
    }

    @Override
    public void visit(Page page) {
        System.out.println("Visited: " + page.getWebURL().getURL());
        processedPages.getMutator().put(1L);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData parseData = (HtmlParseData) page.getParseData();
            List<WebURL> links = parseData.getOutgoingUrls();
            linksFound.getMutator().put(links.size());
            try {
                textSize.getMutator().put(parseData.getText().getBytes("UTF-8").length);
            } catch (UnsupportedEncodingException ignored) {
            }
        }
    }
}