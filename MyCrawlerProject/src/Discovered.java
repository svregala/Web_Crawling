/*
Name: Steve Regala
ID: 7293040280
CSCI 572 Homework 2: Web Crawling
3/5/2023
*/


/*
object that will go into urls_usatoday.csv
column 1 == encountered url & column 2 == resides in website or not (OK vs. N_OK)
- many many more rows than fetched because we are considering all embedded links
*/
public class Discovered{
	String url_name;
	String reside_website;
	
	public Discovered(String url, String reside) {
		url_name = url;
		reside_website = reside;
	}
}