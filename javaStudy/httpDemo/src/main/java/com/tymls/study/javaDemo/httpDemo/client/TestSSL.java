package com.tymls.study.javaDemo.httpDemo.client;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class TestSSL {
	
	public static HttpClient wrapClient(HttpClient base) {
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] arg0,
						String arg1) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] arg0,
						String arg1) throws CertificateException {
				}
			};
			ctx.init(null, new TrustManager[] { tm }, null);
			SSLConnectionSocketFactory ssf = new SSLConnectionSocketFactory(ctx,NoopHostnameVerifier.INSTANCE);
			CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(ssf).build();
			return httpclient;
		} catch (Exception ex) {
			ex.printStackTrace();
			return HttpClients.createDefault();
		}
	}
	
	


	public static void main(String[] args) {
		// https://passport.zhaopin.com/checkcode/imgrd?r='+Date.parse(new
		// Date())
		String url = "https://passport.zhaopin.com/checkcode/imgrd";

		

	}

}
