package com.tymls.study.javaDemo.httpDemo.crawler;

import com.tymls.study.javaDemo.httpDemo.pojos.CrawlerResultPojo;



public interface Crawler {
	public CrawlerResultPojo crawlerfor4Content(String url);
	public void crawler4fileContentAndRrturnHeaders(String url,String filename);
	
}
