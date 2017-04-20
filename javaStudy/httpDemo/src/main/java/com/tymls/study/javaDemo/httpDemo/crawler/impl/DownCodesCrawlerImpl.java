package com.tymls.study.javaDemo.httpDemo.crawler.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.log4j.chainsaw.Main;

import com.tymls.study.javaDemo.httpDemo.crawler.Crawler;
import com.tymls.study.javaDemo.httpDemo.pojos.CrawlerResultPojo;
import com.tymls.study.javaDemo.httpDemo.pojos.CrawlerResultWithInputStreamPojo;

import ocr.ImagePreProcess;

public class DownCodesCrawlerImpl implements Crawler {

	public static void configureHttpClient(HttpClientBuilder clientBuilder) {
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				// 信任所有
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			}).build();

			clientBuilder.setSSLContext(sslContext);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public CrawlerResultPojo crawlerfor4Content(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void crawler4fileContentAndRrturnHeaders(String url, String filename) {

	}

	public HashMap<String, String> downloadPicture(String url, String dirPath, String filePath) {
		HashMap<String, String> headers = new HashMap<String, String>();
		CloseableHttpResponse response = null;
		InputStream is = null;
		try {
			HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
			configureHttpClient(httpClientBuilder);
			CloseableHttpClient httpClient = httpClientBuilder.build();
			HttpGet get = new HttpGet();
			get.setURI(new URI(url));
			response = httpClient.execute(get);

			Header[] Allheader = response.getAllHeaders();
			for (int i = 0; i < Allheader.length; i++) {
				headers.put(Allheader[i].getName(), Allheader[i].getValue());
			}
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
			savePicToDisk(is, dirPath, filePath);
			System.out.println("保存图片 " + filePath + " 成功....");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return headers;

	}

	private static void savePicToDisk(InputStream in, String dirPath, String filePath) {
		try {
			File dir = new File(dirPath);
			if (dir == null || !dir.exists()) {
				dir.mkdirs();
			}
			// 文件真实路径
			String realPath = dirPath.concat(filePath);
			System.out.println(realPath);
			File file = new File(realPath);
			if (file == null || !file.exists()) {
				file.createNewFile();
			}

			FileOutputStream fos = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len = 0;
			while ((len = in.read(buf)) != -1) {
				fos.write(buf, 0, len);
			}
			fos.flush();
			fos.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void login4WelcomePage(String url,String codesValue) {
		url = "https://passport.zhaopin.com/org/login";
		HashMap<String, String> headers = new HashMap<String, String>();
		CloseableHttpResponse response = null;
		InputStream is = null;
		try {
			HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
			configureHttpClient(httpClientBuilder);
			CloseableHttpClient httpClient = httpClientBuilder.build();
			HttpPost post  = new HttpPost();
			post.setURI(new URI(url));
			List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("LoginName", "ymm25434018g"));
            params.add(new BasicNameValuePair("Password", "Wo871027"));
            params.add(new BasicNameValuePair("CheckCode", codesValue));
            UrlEncodedFormEntity e = new UrlEncodedFormEntity(params, "UTF-8");
			post.setEntity(e);
			response = httpClient.execute(post);

			Header[] Allheader = response.getAllHeaders();
			for (int i = 0; i < Allheader.length; i++) {
				headers.put(Allheader[i].getName(), Allheader[i].getValue());
			}
			HttpEntity entity = response.getEntity();
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 

	public static void main(String[] args) throws IOException {
		DownCodesCrawlerImpl codesCrawlerImpl = new DownCodesCrawlerImpl();
		String url = "https://passport.zhaopin.com/checkcode/imgrd";
		String dirPath = System.getProperty("user.dir");
		String filePath = "\\imgrd.gif";
		HashMap<String, String> headers = codesCrawlerImpl.downloadPicture(url, dirPath, filePath);
		String codesValue = null;
		ImagePreProcess imagePreProcess = new ImagePreProcess();
		codesValue = imagePreProcess.resultCodesValue(dirPath+filePath);
		System.out.println(codesValue);
//		codesCrawlerImpl.login4WelcomePage(url, codesValue);
	}

}
