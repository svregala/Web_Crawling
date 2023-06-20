/*
Name: Steve Regala
ID: 7293040280
CSCI 572 Homework 2: Web Crawling
3/5/2023
*/

import edu.uci.ics.crawler4j.crawler.*;
import edu.uci.ics.crawler4j.fetcher.*;
import edu.uci.ics.crawler4j.robotstxt.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.opencsv.CSVWriter;


public class Controller {
	public static void main(String[] args) throws Exception {
		
		int NUM_CRAWLERS = 7;
		int NUM_PAGES_FETCH = 20000;
		int MAX_DEPTH = 16;
		int POLITENESS_DELAY = 400;
		
		String STORAGE_FOLDER = "src/crawl_results/";
		String URL_SEED = "https://www.usatoday.com";
		
		CrawlResult final_crawl_results = new CrawlResult();
	    
		CrawlConfig config = new CrawlConfig(); 
		config.setCrawlStorageFolder(STORAGE_FOLDER);
		config.setPolitenessDelay(POLITENESS_DELAY);
		config.setMaxPagesToFetch(NUM_PAGES_FETCH);
		config.setMaxDepthOfCrawling(MAX_DEPTH);
		config.setIncludeBinaryContentInCrawling(true);
        
		/*
         * Instantiate the controller for this crawl.
         */
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
		
		/*
		* For each crawl, you need to add some seed urls. These are the first * URLs that are fetched and then the crawler starts following links
		* which are found in these pages
		*/
		controller.addSeed(URL_SEED);
		
		/*
		 * Start the crawl. This is a blocking operation, meaning that your code
		 * will reach the line after this only when crawling is finished.
		 */
		controller.start(MyCrawler.class, NUM_CRAWLERS);
		
		/* 
		CITATION: https://github.com/yasserg/crawler4j/blob/master/crawler4j-examples/crawler4j-examples-base/src/test/java/edu/uci/ics/crawler4j/examples/localdata/LocalDataCollectorController.java
		Iterate through all the CrawlResult objects collected from 1,2,...,7
		*/
		List<Object> crawlersLocalData = controller.getCrawlersLocalData();
		for(Object result:crawlersLocalData) {
			CrawlResult res = (CrawlResult) result;
			final_crawl_results.fetched_aggregate.addAll(res.fetched_aggregate);
			final_crawl_results.discovered_aggregate.addAll(res.discovered_aggregate);
			final_crawl_results.visited_aggregate.addAll(res.visited_aggregate);
		}
		
		dataInCSV(final_crawl_results);
		statistics(final_crawl_results);
		
    }
	
	
	// Write the collected data into 3 separate CSV files
	private static void dataInCSV(CrawlResult all_results) throws Exception {
		
		// CITATION: https://www.geeksforgeeks.org/writing-a-csv-file-in-java-using-opencsv/
		
		File file_fetch = new File("fetch_usatoday.csv");
		try {
			FileWriter output = new FileWriter(file_fetch);
			CSVWriter writer = new CSVWriter(output);
			
			List<String[]> data = new ArrayList<String[]>();
			data.add(new String[] { "URL", "Status" });
			for(Fetched item:all_results.fetched_aggregate) {
				data.add(new String[] { item.url_name, Integer.toString(item.status_code) });
			}
			
			writer.writeAll(data);
			writer.close();
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		
		File file_visit = new File("visit_usatoday.csv");
		try {
			FileWriter output = new FileWriter(file_visit);
			CSVWriter writer = new CSVWriter(output);
			
			List<String[]> data = new ArrayList<String[]>();
			data.add(new String[] { "URL", "File Size (Bytes)", "# Outlinks", "Content Type" });
			for(Visited item:all_results.visited_aggregate) {
				data.add(new String[] { item.url_name, Integer.toString(item.file_size), Integer.toString(item.num_outlinks), item.content_type });
			}
			
			writer.writeAll(data);
			writer.close();
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		
		File file_discover = new File("urls_usatoday.csv");
		try {
			FileWriter output = new FileWriter(file_discover);
			CSVWriter writer = new CSVWriter(output);
			
			List<String[]> data = new ArrayList<String[]>();
			data.add(new String[] { "URL", "Reside" });
			for(Discovered item:all_results.discovered_aggregate) {
				data.add(new String[] { item.url_name, item.reside_website });
			}
			
			writer.writeAll(data);
			writer.close();
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	// Write out statistics in text file
	private static void statistics(CrawlResult all_results) throws Exception {
		
		// Fetch Statistics ------------------------------------
		// # fetches attempted
		int fetch_count = all_results.fetched_aggregate.size();
		TreeMap<Integer, Integer> status_code_count = new TreeMap<>();
		
		// # fetches succeeded (status code of 2XX), failed/aborted (redirections (3XX), 
		// client errors (4XX), server errors (5XX) and other network related errors)
		int fetch_succeed = 0;
		int fetch_failed = 0;
		for(Fetched item:all_results.fetched_aggregate) {
			if(item.status_code >= 200 && item.status_code < 300) {
				fetch_succeed++;
			}else {
				fetch_failed++;
			}
			// Status Codes ------------------------------------
			if(status_code_count.containsKey(item.status_code)) {
				status_code_count.put(item.status_code, status_code_count.get(item.status_code)+1);	
			}else {
				status_code_count.put(item.status_code, 1);
			}
		}
		
		
		// Outgoing URLs ------------------------------------
		//int total_URLs = all_results.discovered_aggregate.size();
		// ^^^ Must change because this has an imposed limit, our pre-set value is 20,000
		// change to sum the number of outlinks from visited array - go to line 195
		int num_within_website=0;
		int num_outside_website=0;
		HashSet<String> unique_URLs_encountered = new HashSet<>();
		for(Discovered item:all_results.discovered_aggregate) {
			if(!unique_URLs_encountered.contains(item.url_name)){
				if(item.reside_website.equals("OK")) {
					num_within_website++;
				}else {
					num_outside_website++;
				}
				unique_URLs_encountered.add(item.url_name);
			}
		}
		int total_unique_URLs = unique_URLs_encountered.size();
		
		
		// Content Types ------------------------------------
		int total_URLs = 0;
		HashMap<String, Integer> content_types_encountered = new HashMap<>();
		// < 1KB, 1KB ~ <10KB, 10KB ~ <100KB, 100KB ~ <1MB, >= 1MB
		// File Sizes ------------------------------------
		int less_1kb = 0;
		int bw_1kb_10kb = 0;
		int bw_10kb_100kb = 0;
		int bw_100kb_1mb = 0;
		int over_equal_1mb = 0;
		for(Visited item:all_results.visited_aggregate) {
			if(item.file_size<1024) {
				less_1kb++;
			}
			else if(item.file_size>=1024 && item.file_size<10240) {
				bw_1kb_10kb++;
			}
			else if(item.file_size>=10240 && item.file_size<102400) {
				bw_10kb_100kb++;
			}
			else if(item.file_size>=102400 && item.file_size<1048576) {
				bw_100kb_1mb++;
			}else {
				over_equal_1mb++;
			}
			
			if(content_types_encountered.containsKey(item.content_type)) {
				content_types_encountered.put(item.content_type, content_types_encountered.get(item.content_type)+1);
			}else {
				content_types_encountered.put(item.content_type, 1);
			}
			total_URLs += item.num_outlinks;
		}
		
		
		// CITATION: https://www.w3schools.com/java/java_files_create.asp
		try {
			FileWriter myWriter = new FileWriter("CrawlReport_usatoday.txt");
			myWriter.write("Name: Steve Regala\n");
			myWriter.write("USC ID: 7293040280\n");
			myWriter.write("News site crawled: usatoday.com\n");
			myWriter.write("Number of threads: 7\n");
			
			myWriter.write("\nFetch Statistics\n");
			myWriter.write("====================\n");
			myWriter.write("# fetches attempted: " + Integer.toString(fetch_count) + "\n");
			myWriter.write("# fetches succeeded: " + Integer.toString(fetch_succeed) + "\n");
			myWriter.write("# fetches failed or aborted: " + Integer.toString(fetch_failed) + "\n");
			
			myWriter.write("\nOutgoing URLs:\n");
			myWriter.write("====================\n");
			myWriter.write("Total URLs extracted: " + Integer.toString(total_URLs) + "\n");
			myWriter.write("# unique URLs extracted: " + Integer.toString(total_unique_URLs) + "\n");
			myWriter.write("# unique URLs within News Site: " + Integer.toString(num_within_website) + "\n");
			myWriter.write("# unique URLs outside News Site: " + Integer.toString(num_outside_website) + "\n");
			
			myWriter.write("\nStatus Codes:\n");
			myWriter.write("====================\n");
			for(Map.Entry<Integer,Integer> entry: status_code_count.entrySet()) {
				
				// Status codes listed in IBM website: https://www.ibm.com/docs/en/watson-explorer/11.0.1?topic=activity-http-status-codes-returned-web-crawler
				// < 200
				if(entry.getKey()<200) {
					myWriter.write(Integer.toString(entry.getKey()) + ": " + Integer.toString(entry.getValue()) + "\n");
				}
				
				// >=200 and <300
				else if(entry.getKey()>=200 && entry.getKey()<300) {
					if(entry.getKey()==200) {
						myWriter.write("200 OK: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==201) {
						myWriter.write("201 Created: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==202) {
						myWriter.write("202 Accepted: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==203) {
						myWriter.write("203 Non-authorative information: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==204) {
						myWriter.write("204 No content: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==205) {
						myWriter.write("205 Reset content: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==206) {
						myWriter.write("206 Partial Content: " + Integer.toString(entry.getValue()) + "\n");
					}
					else {
						myWriter.write(Integer.toString(entry.getKey()) + ": " + Integer.toString(entry.getValue()) + "\n");
					}
				}
				
				// >=300 and <400
				else if(entry.getKey()>=300 && entry.getKey()<400) {
					if(entry.getKey()==300) {
						myWriter.write("300 Multiple choices: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==301) {
						myWriter.write("301 Moved permanently: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==302) {
						myWriter.write("302 Found: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==303) {
						myWriter.write("303 See other: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==304) {
						myWriter.write("304 Not modified: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==305) {
						myWriter.write("305 Use proxy: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==306) {
						myWriter.write("306 (Unused): " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==307) {
						myWriter.write("307 Temporary redirect: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==308) {
						myWriter.write("308 Permanent redirect: " + Integer.toString(entry.getValue()) + "\n");
					}
					else {
						myWriter.write(Integer.toString(entry.getKey()) + ": " + Integer.toString(entry.getValue()) + "\n");
					}
				}
				
				// >=400 and <500
				else if(entry.getKey()>=400 && entry.getKey()<500) {
					if(entry.getKey()==400) {
						myWriter.write("400 Bad Request: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==401) {
						myWriter.write("401 Unauthorized: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==402) {
						myWriter.write("402 Payment required: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==403) {
						myWriter.write("403 Forbidden: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==404) {
						myWriter.write("404 Not found: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==405) {
						myWriter.write("405 Method not allowed: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==406) {
						myWriter.write("406 Not acceptable: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==407) {
						myWriter.write("407 Proxy authentication required: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==408) {
						myWriter.write("408 Request timeout: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==409) {
						myWriter.write("409 Conflict: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==410) {
						myWriter.write("410 Gone: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==411) {
						myWriter.write("411 Length required: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==412) {
						myWriter.write("412 Precondition failed: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==413) {
						myWriter.write("413 Request entity too large: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==414) {
						myWriter.write("414 Request URI is too long: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==415) {
						myWriter.write("415 Unsupported media type: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==416) {
						myWriter.write("416 Requested range not satisfiable: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==417) {
						myWriter.write("417 Expectation failed: " + Integer.toString(entry.getValue()) + "\n");
					}
					else {
						myWriter.write(Integer.toString(entry.getKey()) + ": " + Integer.toString(entry.getValue()) + "\n");
					}
				}
				
				// >=500 and <600
				else if(entry.getKey()>=500 && entry.getKey()<600) {
					if(entry.getKey()==500) {
						myWriter.write("500 Internal server error: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==501) {
						myWriter.write("501 Not implemented: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==502) {
						myWriter.write("502 Bad gateway: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==503) {
						myWriter.write("503 Service unavailable: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==504) {
						myWriter.write("504 Gateway timeout: " + Integer.toString(entry.getValue()) + "\n");
					}
					else if(entry.getKey()==505) {
						myWriter.write("505 HTTP version not supported: " + Integer.toString(entry.getValue()) + "\n");
					}
					else {
						myWriter.write(Integer.toString(entry.getKey()) + ": " + Integer.toString(entry.getValue()) + "\n");
					}
				}
				
				// >=600 and <700
				else if(entry.getKey()>=600 && entry.getKey()<700) {
					myWriter.write(Integer.toString(entry.getKey()) + ": " + Integer.toString(entry.getValue()) + "\n");
				}
				
				// 700 and above
				else {
					myWriter.write(Integer.toString(entry.getKey()) + ": " + Integer.toString(entry.getValue()) + "\n");
				}
			}
			
			
			myWriter.write("\nFile Sizes:\n");
			myWriter.write("====================\n");
			myWriter.write("< 1KB: " + Integer.toString(less_1kb) + "\n");
			myWriter.write("1KB ~ <10KB: " + Integer.toString(bw_1kb_10kb) + "\n");
			myWriter.write("10KB ~ <100KB: " + Integer.toString(bw_10kb_100kb) + "\n");
			myWriter.write("100KB ~ <1MB: " + Integer.toString(bw_100kb_1mb) + "\n");
			myWriter.write(">= 1MB: " + Integer.toString(over_equal_1mb) + "\n");
			
			
			myWriter.write("\nContent Types:\n");
			myWriter.write("====================\n");
			for(Map.Entry<String,Integer> entry: content_types_encountered.entrySet()) {
				myWriter.write(entry.getKey() + ": " + Integer.toString(entry.getValue()) + "\n");
			}
			
			myWriter.close();
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}




