/*
Name: Steve Regala
ID: 7293040280
CSCI 572 Homework 2: Web Crawling
3/5/2023
*/


/*
object that will go into visit_usatoday.csv
column 1 == successfully downloaded url & column 2 == size of downloaded file
column 3 == number of outlinks found & column 4 == file content type
- number of rows will be less than the number of rows in fetch_usatoday.csv
*/
public class Visited{
	String url_name;
	int file_size;
	int num_outlinks;
	String content_type;
	
	public Visited(String url, int size, int num, String type) {
		url_name = url;
		file_size = size;
		num_outlinks = num;
		content_type = type;
	}
}
