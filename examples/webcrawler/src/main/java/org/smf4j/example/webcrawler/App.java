package org.smf4j.example.webcrawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import java.io.File;
import org.smf4j.Accumulator;
import org.smf4j.RegistrarFactory;
import org.smf4j.to.jmx.JmxRegistrarPublisher;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App {
    public static void main( String[] args )
    throws Exception {
        ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext("context.xml");
        context.start();
        RegistrarFactory.getRegistrar().getRootNode().setOn(true);
        JmxRegistrarPublisher p = new JmxRegistrarPublisher();
        p.publish();
        try {
            String rootFolder = getDataFolder();
            int numberOfCrawlers = 8;
            Accumulator downloaded = RegistrarFactory.getRegistrar().getNode("crawler").getAccumulator("download");

            CrawlConfig config = new CrawlConfig();
            config.setCrawlStorageFolder(rootFolder);
            config.setMaxPagesToFetch(10);
            config.setPolitenessDelay(1000);

            PageFetcher pageFetcher = new CountingPageFetcher(config, downloaded);
            RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
            RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
            CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

            controller.addSeed("http://www.ics.uci.edu/");
            controller.start(Smf4jDataCollectorCrawler.class, numberOfCrawlers);
        } finally {
            if(context != null) {
                context.close();
            }
            p.unpublish();
        }
    }

    private static String getDataFolder() {
        File pwd = new File(".").getAbsoluteFile();
        File target = new File(pwd, "target").getAbsoluteFile();
        File workdir = new File(target, "workdir").getAbsoluteFile();
        return workdir.getAbsolutePath();
    }
}
