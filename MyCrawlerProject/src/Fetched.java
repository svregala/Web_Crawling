/*
Name: Steve Regala
ID: 7293040280
CSCI 572 Homework 2: Web Crawling
3/5/2023
*/


/*
object that will go into fetch_usatoday.csv
column 1 == url & column 2 == HTTP/HTTPS status code
- number of rows should be no more than 20,000 as that is our pre set limit.
*/
public class Fetched{
	String url_name;
	int status_code;
	
	public Fetched(String url, int status) {
		url_name = url;
		status_code = status;
	}
}
