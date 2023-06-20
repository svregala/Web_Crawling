/*
Name: Steve Regala
ID: 7293040280
CSCI 572 Homework 2: Web Crawling
3/5/2023
*/

import java.util.Set;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.*;
import edu.uci.ics.crawler4j.parser.*;
import edu.uci.ics.crawler4j.url.WebURL;


public class MyCrawler extends WebCrawler {

	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|mid|mp2|mp3|mp4|vcf|zip|gz|wav|avi|mov|mpeg|ram|m4v|xml|json|rm|smil|wmv|swf|wma|rar))$");
	CrawlResult crawl_result;
	String NEWSITE_NAME = "usatoday.com";
	
	
	/*
	CITATION: https://github.com/yasserg/crawler4j/blob/master/crawler4j-examples/crawler4j-examples-base/src/test/java/edu/uci/ics/crawler4j/examples/localdata/LocalDataCollectorCrawler.java
	initialize crawl_result once MyCrawler is instantiated
	*/
	public MyCrawler() {
		crawl_result = new CrawlResult();
	}
	
	
	/*
	CITATION: WebCrawler API - https://javadoc.io/static/edu.uci.ics/crawler4j/4.4.0/edu/uci/ics/crawler4j/crawler/WebCrawler.html
	Use this function to record both the url and the corresponding status code
	*/
	@Override
	public void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
		String url_link = webUrl.getURL();
		crawl_result.add_fetch(url_link, statusCode);
	}
	
	
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
		String original = url.getURL();
	    String href = url.getURL();
	    Boolean is_newsite_domain;
	    
	    // Clean the URL
	    // remove / at the end
 		if(href.charAt(href.length()-1) == '/') {
 			href = href.substring(0,href.length()-1);
 		}
 		// CITATION: Question-Answer in spec
 		href = href.toLowerCase().replace(',', '_');
 		
 		// Remove https || http || www. for analysis purposes
 		href = href.replace("https://", "").replace("http://", "").replace("www.", "");
	    
	    // Add to discovered URLs list
	    if(href.startsWith(NEWSITE_NAME)){
	    	is_newsite_domain = true;
	    	crawl_result.add_discovered(original, "OK");
	    }else {
	    	is_newsite_domain = false;
	    	crawl_result.add_discovered(original, "N_OK");
	    }
	    
	    return !FILTERS.matcher(href).matches() && is_newsite_domain;
	}
	    
	    
	/**
	* This function is called when a page is fetched and ready * to be processed by your program.
	*/
	@Override
	public void visit(Page page) {
	    String url = page.getWebURL().getURL();
	    String content_type = page.getContentType().toLowerCase();
	    if(content_type.contains(";")) {
	    	int end_index = content_type.indexOf(";");
	    	content_type = content_type.substring(0,end_index);
	    }
	    
	    if(content_type.startsWith("image") || content_type.equals("text/html") || content_type.equals("application/msword") 
	    		|| content_type.equals("application/pdf") || content_type.equals("application/document") || content_type.equals("application/application/vnd.openxmlformats-officedocument.wordprocessingml.document")){
	    	int file_size = page.getContentData().length;
	    	int num_outlinks = page.getParseData().getOutgoingUrls().size();
	    	crawl_result.add_visited(url, file_size, num_outlinks, content_type);
	    }

	}
	
	
	@Override
	/*
	CITATION: https://github.com/yasserg/crawler4j/blob/master/crawler4j-examples/crawler4j-examples-base/src/test/java/edu/uci/ics/crawler4j/examples/localdata/LocalDataCollectorCrawler.java
	*/
	public Object getMyLocalData() {
		return crawl_result;
	}
	
}
