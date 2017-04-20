package parser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.apache.commons.httpclient.Header;
//import org.apache.commons.httpclient.HttpClient;
//import org.apache.commons.httpclient.NameValuePair;
//import org.apache.commons.httpclient.methods.PostMethod;
//import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

//import ocr.MySSLProtocolSocketFactory;
import us.codecraft.webmagic.Site;
import us.codecraft.xsoup.Xsoup;

/**
 * @author 张鹏科
 *
 */
public class Client {

	static CookieStore cookieStore = null;
	private static PoolingHttpClientConnectionManager connectionManager;

	public static final Site site = new Site();
	static {
		site.setCharset("utf-8");
	}

	public static void printResponse(HttpResponse httpResponse) throws ParseException, IOException {
		// 获取响应消息实体
		HttpEntity entity = httpResponse.getEntity();
		// 响应状态
		System.out.println("status:" + httpResponse.getStatusLine());
		System.out.println("headers:");
		HeaderIterator iterator = httpResponse.headerIterator();
		while (iterator.hasNext()) {
			System.out.println("\t" + iterator.next());
		}
		// 判断响应实体是否为空
		if (entity != null) {
			String responseString = EntityUtils.toString(entity);
			System.out.println("response length:" + responseString.length());
			System.out.println("response content:" + responseString.replace("\r\n", ""));
		}
	}

	public void PageResolve() {

	}

	public static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		InputStream is = new FileInputStream("D:\\resolve.html");

		new Client();
		String node = Client.convertStreamToString(is);
		// System.out.println(node);

		List<HashMap<String, String>> retMaps = Lists.newArrayList();

		// String Listtr = Xsoup.select(node, "//table/tbody").get();
		// System.out.println(ListNode);
		List<String> ListNode = Xsoup.select(node, "//table/tbody/tr[@class='top']").list();
		List<String> ListInfo = Xsoup.select(node, "//table/tbody/tr[@class='info']").list();
		HashMap<String, String> map = null;
		// for (int i = 0; i < ListNode.size(); i++) {
		for (int i = 0; i < 1; i++) {

			map = Maps.newHashMap();

			// List<String> ListTd = Xsoup.select(ListNode.get(i),
			// "//td").list();
			// System.out.println(ListNode.get(i));
			// for (int j = 0; j < ListTd.size(); j++) {
			// System.out.println(ListTd.get(i));
			// }

			// String resumeName = Xsoup.select(ListNode.get(i),
			// "//html/body/a/text()").get();
			String positionInfo = Xsoup
					.select(ListInfo.get(i),
							"//html/body//div/div[@class='resumes-list-none-right']/div[@class='resumes-content']/div[@class='resumes-none-main']/span[@class='span-padding']/text()")
					.get();
			List<String> resumesContent = Xsoup
					.select(ListInfo.get(i),
							"//html/body//div/div[@class='resumes-list-none-right']/div[@class='resumes-content']")
					.list();
			String regex1 = "&nbsp.+<";
			Pattern pattern = Pattern.compile(regex1);
			Matcher matcher = pattern.matcher(resumesContent.get(1));
			String[] str = null;
			if (matcher.find()) {
				str = matcher.group().split("&nbsp;&nbsp;|&nbsp;&nbsp;");
			}
			String position = str[2].trim();
			String regex2 = "&nbsp.+";
			Pattern pattern2 = Pattern.compile(regex2);
			Matcher matcher2 = pattern2.matcher(resumesContent.get(2));
			String[] str2 = null;
			if (matcher2.find()) {
				str2 = matcher2.group().split("&nbsp;&nbsp;&nbsp;&nbsp;");
			}
			String school = str2[1];
			String profession = str2[2];
			String adu = str2[3];
			String address = positionInfo.substring(positionInfo.indexOf("：") + 1);

			// map.put("简历名称", resumeName);
			map.put("本科", adu);
			map.put("专业", profession);
			map.put("毕业学校", school);
			map.put("职位", position);
			map.put("地址", address);
			retMaps.add(map);
		}

	}

}