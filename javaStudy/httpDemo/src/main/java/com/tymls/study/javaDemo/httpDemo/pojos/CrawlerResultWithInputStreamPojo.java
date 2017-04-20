package com.tymls.study.javaDemo.httpDemo.pojos;

import java.io.InputStream;

public class CrawlerResultWithInputStreamPojo extends CrawlerResultPojo{
	private InputStream pageContent;

	public InputStream getPageContent() {
		return pageContent;
	}

	public void setPageContent(InputStream pageContent) {
		this.pageContent = pageContent;
	}

}
