package parser;

/**
 * 
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.common.collect.Sets;

import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.utils.UrlUtils;

/**
 * @author 张鹏科
 *
 */
public class CommonParser {
	public final Logger log = LoggerFactory.getLogger(getClass());

	protected static Site flvcdSite = new Site();

	protected static Set<String> proxySite = new HashSet<String>();

	static {
		flvcdSite.addHeader(Constants.USER_AGENT, Constants.CHROME_49);
		flvcdSite.setCharset("utf-8");
		flvcdSite.setDomain("http://vpxm.flvcd.com/");
		flvcdSite.setTimeOut(1000 * 1000);

		proxySite.add("soundcloud");
	}

	private final Map<String, CloseableHttpClient> httpClients = new HashMap<String, CloseableHttpClient>();

	private HJCrawlerHttpClientGenerator httpClientGenerator = new HJCrawlerHttpClientGenerator();

	private CloseableHttpClient getHttpClient(Site site) {
		if (site == null) {
			return httpClientGenerator.getClient(null);
		}
		String domain = site.getDomain();

		CloseableHttpClient httpClient = httpClients.get(domain);

		if (httpClient == null) {
			synchronized (this) {
				if (httpClient == null) {
					httpClient = httpClientGenerator.getClient(site);
					httpClients.put(domain, httpClient);
				}
			}
		}
		return httpClient;
	}

	protected String getSortNum(String contentTime) {
		String sortnum = Constants.TASK_DEFAULT_SORTNUM;
		try {
			String[] arrays1 = contentTime.split(" ");
			String[] arrays2 = arrays1[0].split("-");
			String[] arrays3 = arrays1[1].split(":");

			sortnum = arrays2[0] + arrays2[1] + arrays2[2] + arrays3[0] + arrays3[1] + arrays3[2];

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return sortnum;
	}

	protected HttpResponse fetchResponse(Site site, String url) {
		Set<Integer> acceptStatCode;
		Map<String, String> headers = null;
		if (site != null) {
			acceptStatCode = site.getAcceptStatCode();
			headers = site.getHeaders();
		} else {
			acceptStatCode = Sets.newHashSet(200);
		}
		log.info("downloading page " + url);
		RequestBuilder requestBuilder = RequestBuilder.get().setUri(url);
		if (headers != null) {
			for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
				requestBuilder.addHeader(headerEntry.getKey(), headerEntry.getValue());
			}
		}
		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
				.setConnectionRequestTimeout(site.getTimeOut()).setConnectTimeout(site.getTimeOut())
				.setSocketTimeout(site.getTimeOut()).setCookieSpec(CookieSpecs.BEST_MATCH);
		if (site != null && site.getHttpProxy() != null) {
			requestConfigBuilder.setProxy(site.getHttpProxy());
		}
		requestBuilder.setConfig(requestConfigBuilder.build());
		CloseableHttpResponse httpResponse = null;

		try {
			httpResponse = getHttpClient(site).execute(requestBuilder.build());
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (acceptStatCode.contains(statusCode)) {
				return httpResponse;
			} else {
				log.warn("code error " + statusCode + "\t" + url);
				return null;
			}

		} catch (IOException e) {
			log.warn("download page " + url + " error", e);
			return null;
		} catch (Exception e) {
			log.warn("unknown exception," + url + " error", e);
			return null;
		}
	}

	protected HttpResponse fetchResponseWithPost(Site site, String url, Map<String, String> params,
			String stringEntity) {
		Set<Integer> acceptStatCode;
		Map<String, String> headers = null;
		if (site != null) {
			acceptStatCode = site.getAcceptStatCode();
			headers = site.getHeaders();
		} else {
			acceptStatCode = Sets.newHashSet(200);
		}
		log.info("downloading page " + url);
		RequestBuilder requestBuilder = RequestBuilder.post().setUri(url);
		if (headers != null) {
			for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
				requestBuilder.addHeader(headerEntry.getKey(), headerEntry.getValue());
			}
		}
		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
				.setConnectionRequestTimeout(site.getTimeOut()).setConnectTimeout(site.getTimeOut())
				.setSocketTimeout(site.getTimeOut()).setCookieSpec(CookieSpecs.BEST_MATCH);
		if (site != null && site.getHttpProxy() != null) {
			requestConfigBuilder.setProxy(site.getHttpProxy());
		}
		requestBuilder.setConfig(requestConfigBuilder.build());

		try {

			HttpEntity entity = null;
			if (StringUtils.isNotBlank(stringEntity)) {
				// 以简单的string形式提交数据
				entity = new StringEntity(stringEntity);
			} else {
				// 以表单的形式发送数据
				List<NameValuePair> formparams = new ArrayList<NameValuePair>();
				for (Entry<String, String> nameValuePair : params.entrySet()) {
					formparams.add(new BasicNameValuePair(nameValuePair.getKey(), nameValuePair.getValue()));
				}
				entity = new UrlEncodedFormEntity(formparams, "UTF-8");
			}

			requestBuilder.setEntity(entity);
		} catch (Exception e1) {
			log.error("httpPost set params error,url={}", url, e1);
			return null;
		}

		CloseableHttpResponse httpResponse = null;

		try {

			httpResponse = getHttpClient(site).execute(requestBuilder.build());
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (acceptStatCode.contains(statusCode)) {
				return httpResponse;
			} else {
				log.warn("code error " + statusCode + "\t" + url);
				return null;
			}

		} catch (IOException e) {
			log.warn("download page " + url + " error", e);
			return null;
		} catch (Exception e) {
			log.warn("unknown exception," + url + " error", e);
			return null;
		}
	}

	protected Document fetchDocument(Site site, String url) {

		Document document = null;
		HttpResponse httpResponse = fetchResponse(site, url);

		if (httpResponse != null) {

			InputStream in;
			try {
				in = httpResponse.getEntity().getContent();
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating(false);
				factory.setIgnoringComments(false);
				factory.setIgnoringElementContentWhitespace(true);

				DocumentBuilder builder = factory.newDocumentBuilder();
				document = builder.parse(in);

			} catch (Exception e) {
				log.error("fetch document error,url is:{}", url);
			} finally {
				try {
					if (httpResponse != null) {
						// ensure the connection is released back to pool
						EntityUtils.consume(httpResponse.getEntity());
					}
				} catch (IOException e) {
					log.warn("close response fail", e);
				}
			}

		}

		return document;

	}

	protected String fetch(Site site, String url) {
		String charset = null;
		if (site != null) {
			charset = site.getCharset();
		}
		String result = null;
		HttpResponse httpResponse = fetchResponse(site, url);

		if (httpResponse != null) {
			try {

				if (charset == null) {
					String value = httpResponse.getEntity().getContentType().getValue();
					charset = UrlUtils.getCharset(value);
				}
				result = IOUtils.toString(httpResponse.getEntity().getContent(), charset);

			} catch (Exception e) {
				log.warn("fetch error" + url + " error", e);
				return null;
			} finally {
				try {
					if (httpResponse != null) {

						// ensure the connection is released back to pool
						EntityUtils.consume(httpResponse.getEntity());
					}
				} catch (IOException e) {
					log.warn("close response fail", e);
				}
			}
		}
		return result;
	}

	/**
	 * 以Post的方式发起请求
	 * 
	 * @param site
	 * @param url
	 * @param params
	 * @return
	 */
	protected String fetch(Site site, String url, Map<String, String> params, String stringEntity) {
		String charset = null;
		if (site != null) {
			charset = site.getCharset();
		}
		String result = null;

		HttpResponse httpResponse = fetchResponseWithPost(site, url, params, stringEntity);

		if (httpResponse != null) {
			try {

				if (charset == null) {
					String value = httpResponse.getEntity().getContentType().getValue();
					charset = UrlUtils.getCharset(value);
				}
				result = IOUtils.toString(httpResponse.getEntity().getContent(), charset);

			} catch (Exception e) {
				log.warn("fetch error" + url + " error", e);
				return null;
			} finally {
				try {
					if (httpResponse != null) {
						// ensure the connection is released back to pool
						EntityUtils.consume(httpResponse.getEntity());
					}
				} catch (IOException e) {
					log.warn("close response fail", e);
				}
			}
		}
		return result;
	}

}
