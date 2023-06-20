/*
Name: Steve Regala
ID: 7293040280
CSCI 572 Homework 2: Web Crawling
3/5/2023
*/

import java.util.ArrayList;

public class CrawlResult {
	ArrayList<Fetched> fetched_aggregate;
	ArrayList<Discovered> discovered_aggregate;
	ArrayList<Visited> visited_aggregate;
	
	public CrawlResult() {
		fetched_aggregate = new ArrayList<>();
		discovered_aggregate = new ArrayList<>();
		visited_aggregate = new ArrayList<>();
	}
	
	public void add_fetch(String url, int code) {
		Fetched new_fetch_item = new Fetched(url, code);
		fetched_aggregate.add(new_fetch_item);
	}
	
	public void add_discovered(String url, String res) {
		Discovered new_discovered_item = new Discovered(url, res);
		discovered_aggregate.add(new_discovered_item);
	}
	
	public void add_visited(String url, int file_size, int num_outlinks, String content_type) {
		Visited new_visited_item = new Visited(url, file_size, num_outlinks, content_type);
		visited_aggregate.add(new_visited_item);
	}

}
