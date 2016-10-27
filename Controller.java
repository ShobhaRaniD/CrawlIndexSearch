import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;


public class Controller {

	/**
	 * This class which specifies the seeds (starting URLs) of the crawl, 
	 * the folder in which intermediate crawl data should be stored 
	 * and the number of concurrent threads, and other configurations dealing with politeness.
	 */
	public static void main(String[] args) {


		String crawlStorageFolder = "C:\\MyCrawler\\crawl";
		int numberOfCrawlers = 7;
		CrawlConfig config = new CrawlConfig();
		
		//Configuring the crawler
		config.setCrawlStorageFolder(crawlStorageFolder);
		config.setMaxDepthOfCrawling(5);
		config.setMaxPagesToFetch(5000);
		config.setPolitenessDelay(1000);
		config.setUserAgentString("kiriUSC");
		
		config.setIncludeHttpsPages(true);
		config.setMaxDownloadSize(10485760);
		config.setIncludeBinaryContentInCrawling(true);
		
		/*
		* Instantiate the controller for this crawl.
		*/
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		
		try{
			CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

			/*
			* For each crawl, you need to add some seed urls. These are the first
			* URLs that are fetched and then the crawler starts following links
			* which are found in these pages
			*/
			controller.addSeed("http://www.viterbi.usc.edu/");
			/*
			* Start the crawl. This is a blocking operation, meaning that your code
			7
			* will reach the line after this only when crawling is finished.
			*/
			controller.start(MyCrawler.class, numberOfCrawlers);

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
