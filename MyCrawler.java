import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.io.Files;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;


public class MyCrawler extends WebCrawler {
	
		int fetchSucceed = 0;
		int fetchFailed = 0;
		int fetchAttempt = 0;

		int unique = 0;
		int uniqueSOWK = 0;
		int uniqueUSC = 0;
		int uniqueOut = 0;
		
		int totalExtracted = 0;
		
		int status200 = 0;
		int status301 = 0;
		int status302 = 0;
		int status404 = 0;
		
		int fsizeRange1 = 0;
		int fsizeRange2 = 0;
		int fsizeRange3 = 0;
		int fsizeRange4 = 0;
		int fsizeRange5 = 0;
		
		int html = 0;
		int gif = 0;
		int jpeg = 0;
		int png = 0;
		int pdf = 0;
		
		private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg|jp|png|mp3|mp3|zip|gz))$");
		
			/**
			* This method receives two parameters. The first parameter is the page
			* in which we have discovered this new url and the second parameter is
			* the new url. You should implement this function to specify whether
			* the given url should be crawled or not (based on your crawling logic).
			* In this example, we are instructing the crawler to ignore urls that
			* have css, js, git, ... extensions and to only accept urls that start
			* with "http://www.viterbi.usc.edu/". In this case, we didn't need the
			* referringPage parameter to make the decision.
			*/
			@Override
			public boolean shouldVisit(Page referringPage, WebURL url) {
				
				String href = url.getURL().toLowerCase();
				unique++;
				
				//appending the URL discovered in urls.csv
				try{
					
					FileWriter urls = new FileWriter("C:\\MyCrawler\\crawl\\urls.csv",true);
					
					String indicator;
					if(href.startsWith("http://www.viterbi.usc.edu/")||href.startsWith("http://www.viterbi.usc.edu/"))
					{
						indicator = "OK";
						uniqueSOWK++;
					}
					else if(href.contains("usc.edu"))
					{
						indicator = "USC";
						uniqueUSC++;
					}
					else
					{
						indicator = "outUSC";
						uniqueOut++;
					}
					urls.append(url + "," + indicator + "\n");
					urls.flush();
					urls.close();
				}
				catch(IOException e){
					
					e.printStackTrace();
				}
				
				return href.startsWith("http://www.viterbi.usc.edu/")||href.startsWith("http://www.viterbi.usc.edu/");
				
			}

			@Override
			protected void handlePageStatusCode(WebURL url, int statusCode, String statusDescription)
			{
				fetchAttempt++;
				if(statusCode>=200 && statusCode<=299)
				{
					fetchSucceed++;
					if(statusCode == 200)
						status200++;
				}
				else
				{
					fetchFailed++;
					if(statusCode == 301)
						status301++;
					else if(statusCode == 302)
						status302++;
					else if(statusCode == 404)
						status404++;
				}
				//appending the url to fetch.csv
				try{
					
					FileWriter fetch = new FileWriter("C:\\MyCrawler\\crawl\\fetch.csv",true);
					fetch.append(url + "," + statusCode + "\n");
					fetch.flush();
					fetch.close();
				}
				catch(IOException e){
					
					e.printStackTrace();
				}
			}

			/**
			* This function is called when a page is fetched and ready
			* to be processed by your program.
			*/
			
			@Override
			public void visit(Page page) {
				
				String url = page.getWebURL().getURL();
				System.out.println("URL: " + url);
								
				if(page.getContentType().contains("html"))
					html++;
				else if(page.getContentType().contains("jpeg"))
					jpeg++;
				else if(page.getContentType().contains("gif"))
					gif++;
				else if(page.getContentType().contains("png"))
					png++;
				else if(page.getContentType().contains("pdf"))
					pdf++;
				
				//downloading the html, doc and pdf files
				Pattern download = Pattern.compile(".*(htm|html|doc|pdf).*");
				if (download.matcher(page.getContentType()).matches() && page.getStatusCode()==200)
				{
					String fileName  = url.substring(url.lastIndexOf("/") + 1, (url.indexOf('?') == -1)?url.length():url.indexOf('?'));
					
					//altering the filename if it has any reserved characters or if empty ( incase of home page)
					if(fileName.length() == 0)
						fileName = url.substring(7).replace('/', '-');
					if(!fileName.contains(".html") && !fileName.contains(".pdf"))
						fileName = fileName + ".html";
					
					String filePath = "C:\\MyCrawler\\crawl\\" + fileName;
					int outlinks = 0;
					
					try{
										
						if (page.getParseData() instanceof HtmlParseData) 
						{
							HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
							Set<WebURL> links = htmlParseData.getOutgoingUrls();
							outlinks = links.size();
							
							totalExtracted += outlinks;
							
							Object [] link_values = links.toArray();
							FileWriter rank = new FileWriter("C:\\MyCrawler\\crawl\\pageRank.csv",true);
							rank.append(fileName + ",");
							String href;
							
							for(int i=0; i<outlinks; i++)
							{
								href =  link_values[i].toString();
								if(!FILTERS.matcher(href).matches() && href.contains("viterbi.usc.edu"))
								{
									String temp  = href.substring(href.lastIndexOf("/") + 1, (href.indexOf('?') == -1)?href.length():href.indexOf('?'));
									
									if(temp.length() == 0)
										temp = href.substring(7).replace('/', '-');
									if(!temp.contains(".html"))
										temp = temp + ".html";
									rank.append(temp + ",");
								}
							}
							rank.append("\n");
							rank.flush();	
							rank.close();
							
						}
						
						int size = page.getContentData().length;
						if(size < 1024)
							fsizeRange1++;
						else if(size < 10240)
							fsizeRange2++;
						else if(size < 102400)
							fsizeRange3++;
						else if(size < 1024000)
							fsizeRange4++;
						else
							fsizeRange5++;
						
						Files.write(page.getContentData(), new File(filePath));
						
						FileWriter visit = new FileWriter("C:\\MyCrawler\\crawl\\visit.csv",true);
						visit.append(url + "," + page.getContentData().length + "," + outlinks + "," + page.getContentType() + "\n");
						visit.flush();
						visit.close();
							
					}
					catch(IOException e)
					{
						e.printStackTrace();
					}
					
				}
				
			}
			
			@Override
			public void onBeforeExit()
			{
				System.out.println("# fetches attempted: " + fetchAttempt);
				System.out.println("# fetches succeeded: " + fetchSucceed);
				System.out.println("# fetches failed or aborted: " + fetchFailed);
				System.out.println("Total URLs extracted: " + totalExtracted);
				System.out.println("# unique URLs extracted: " + unique);
				System.out.println("# unique URLs within School: " + uniqueSOWK);
				System.out.println("# unique USC URLs outside School: " + uniqueUSC);
				System.out.println("# unique URLs outside USC: " + uniqueOut);
				System.out.println("200 OK: " + status200);
				System.out.println("301 redirect: " + status301);
				System.out.println("302 moved permanently: " + status302);
				System.out.println("404 not found: " + status404);
				System.out.println("< 1KB: " + fsizeRange1);
				System.out.println("< 10KB: " + fsizeRange2);
				System.out.println("< 100KB: " + fsizeRange3);
				System.out.println("< 1MB: " + fsizeRange4);
				System.out.println("> 1MB: " + fsizeRange5);
				System.out.println(html + ", " + gif + ", " + jpeg + ", " + png + "," + pdf); 
				
			}
			
}//end of class
