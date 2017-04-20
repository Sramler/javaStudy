package com.tymls.study.javaDemo.httpDemo.pojos;

import java.util.HashMap;

public class CrawlerResultPojo {
	
	private int httpStatuCode;
	
	
	private HashMap<String, String> headers;


	public int getHttpStatuCode() {
		return httpStatuCode;
	}


	public void setHttpStatuCode(int httpStatuCode) {
		this.httpStatuCode = httpStatuCode;
	}


	public HashMap<String, String> getHeaders() {
		return headers;
	}


	public void setHeaders(HashMap<String, String> headers) {
		this.headers = headers;
	}
	
	
	

}
