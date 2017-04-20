package parser;
/**
 * 
 */


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import com.google.common.collect.Sets;

import us.codecraft.webmagic.Site;

/**
 * 
 * @author 张鹏科
 *
 */
public class HJCrawlerHttpClientGenerator {

	private PoolingHttpClientConnectionManager connectionManager;

	public HJCrawlerHttpClientGenerator() {

		Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory> create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", SSLConnectionSocketFactory.getSocketFactory()).build();
		connectionManager = new PoolingHttpClientConnectionManager(reg);
		connectionManager.setDefaultMaxPerRoute(200);
		// connectionManager.setMaxTotal(300);
		// connectionManager.setConnectionConfig(host, connectionConfig)
	}

	public HJCrawlerHttpClientGenerator setPoolSize(int poolSize) {
		connectionManager.setMaxTotal(poolSize);
		return this;
	}

	public CloseableHttpClient getClient(Site site) {
		return generateClient(site);
	}

	public CloseableHttpClient getDefaultClient() {
		return generateClient(
				Site.me().setAcceptStatCode(Sets.newHashSet(200, 206)).setCharset("utf-8").setRetryTimes(2));
	}

	ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {

		public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
			// Honor 'keep-alive' header
			HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
			while (it.hasNext()) {
				HeaderElement he = it.nextElement();
				String param = he.getName();
				String value = he.getValue();
				if (value != null && param.equalsIgnoreCase("timeout")) {
					try {
						return Long.parseLong(value) * 1000;
					} catch (NumberFormatException ignore) {
					}
				}
			}
			HttpHost target = (HttpHost) context.getAttribute(HttpClientContext.HTTP_TARGET_HOST);
			if ("www.naughty-server.com".equalsIgnoreCase(target.getHostName())) {
				// Keep alive for 5 seconds only
				return 5 * 1000;
			} else {
				// otherwise keep alive for 30 seconds
				return 30 * 1000;
			}
		}
	};

	private CloseableHttpClient generateClient(Site site) {
		HttpClientBuilder httpClientBuilder = HttpClients.custom().setConnectionManager(connectionManager);
		if (site != null && site.getUserAgent() != null) {
			httpClientBuilder.setUserAgent(site.getUserAgent());
		} else {
			httpClientBuilder.setUserAgent("");
		}

		if (site == null || site.isUseGzip()) {
			httpClientBuilder.addInterceptorFirst(new HttpRequestInterceptor() {

				public void process(final HttpRequest request, final HttpContext context)
						throws HttpException, IOException {
					if (!request.containsHeader("Accept-Encoding")) {
						request.addHeader("Accept-Encoding", "gzip");
					}

				}
			});
		}
		httpClientBuilder.setKeepAliveStrategy(myStrategy);
		SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(true).setTcpNoDelay(true)
				.setSoTimeout(2 * 60 * 1000).build();
		httpClientBuilder.setDefaultSocketConfig(socketConfig);
		// Http client has some problem handling compressing entity for redirect
		// So I disable it and do it manually
		// https://issues.apache.org/jira/browse/HTTPCLIENT-1432
		httpClientBuilder.disableContentCompression();
		httpClientBuilder.addInterceptorFirst(new HttpResponseInterceptor() {

			private ResponseContentEncoding contentEncoding = new ResponseContentEncoding();

			public void process(final HttpResponse response, final HttpContext context)
					throws HttpException, IOException {
				if (response.getStatusLine().getStatusCode() == 301
						|| response.getStatusLine().getStatusCode() == 302) {
					return;
				}
				contentEncoding.process(response, context);
			}

		});
		if (site != null) {
			httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(site.getRetryTimes(), true));
		}
		// set request config timeout
		RequestConfig.Builder requestBuilder = RequestConfig.custom();
		requestBuilder = requestBuilder.setConnectTimeout(100 * 1000);
		requestBuilder = requestBuilder.setConnectionRequestTimeout(100 * 1000);
		requestBuilder = requestBuilder.setSocketTimeout(100 * 1000);

		httpClientBuilder.setDefaultRequestConfig(requestBuilder.build());
		generateCookie(httpClientBuilder, site);
		return httpClientBuilder.build();
	}

	private void generateCookie(HttpClientBuilder httpClientBuilder, Site site) {
		CookieStore cookieStore = new BasicCookieStore();
		if (site.getCookies() != null) {
			for (Map.Entry<String, String> cookieEntry : site.getCookies().entrySet()) {
				BasicClientCookie cookie = new BasicClientCookie(cookieEntry.getKey(), cookieEntry.getValue());
				cookie.setDomain(site.getDomain());
				cookieStore.addCookie(cookie);
			}
		}
		httpClientBuilder.setDefaultCookieStore(cookieStore);
	}

	static class MyConnectionSocketFactory implements ConnectionSocketFactory {

		public static final MyConnectionSocketFactory INSTANCE = new MyConnectionSocketFactory();

		@Override
		public Socket createSocket(HttpContext context) throws IOException {

			InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute("socks.address");
			if (!ObjectUtils.equals(null, socksaddr)) {
				Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
				return new Socket(proxy);

			} else {
				return new Socket();
			}

		}

		@Override
		public Socket connectSocket(final int connectTimeout, final Socket socket, final HttpHost host,
				final InetSocketAddress remoteAddress, final InetSocketAddress localAddress, final HttpContext context)
				throws IOException {
			final Socket sock = socket != null ? socket : createSocket(context);
			if (localAddress != null) {
				sock.bind(localAddress);
			}
			try {
				sock.connect(remoteAddress, connectTimeout);
			} catch (final IOException ex) {
				try {
					sock.close();
				} catch (final IOException ignore) {
				}
				throw ex;
			}
			return sock;
		}

	}
}