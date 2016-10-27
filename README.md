This project starts with a web crawler in Java, using Crawler4j, which crawls a specific domain and downloads the HTML pages encountered - Controller.java and MyCrawler.java
The downloaded pages were then ranked based on PageRank algorithm using NetworkX package.
Apache Solr is used to index these pages (solrconfig.xml), and a simple Web UI (callSolr.php) is given to search for queries specific to that domain.
The search also includes spell check and autosuggest features.
