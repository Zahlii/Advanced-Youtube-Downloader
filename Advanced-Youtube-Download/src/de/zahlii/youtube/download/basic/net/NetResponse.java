/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 */
package de.zahlii.youtube.download.basic.net;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;

/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 * 
 */
public class NetResponse {

	public HttpGet request;
	public List<Header> responseHeaders;
	public String responseBody;
	public StatusLine responseStatusLine;
	public HttpEntity responseEntity;
	public String responseType;
	public BufferedInputStream responseStream;
	public List<Header> requestHeaders;

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();

		b.append(request);
		for (Header x : requestHeaders) {
			b.append(x);
			b.append("\n");
		}
		b.append("\n");
		b.append("type: " + responseType);
		b.append("\n");
		for (Header x : responseHeaders) {
			b.append(x);
			b.append("\n");
		}
		b.append("size: " + responseBody.length());
		b.append("\n");
		b.append("====");
		// b.append(responseBody);
		b.append("====");

		return b.toString();
	}

	public String extract(String s1, String s2) {
		return extract(responseBody, s1, s2);
	}

	public static String extract(String target, String string, String string2) {
		int pos = target.indexOf(string);
		if (pos == -1)
			return "";
		int e = pos + string.length();
		int pos2 = target.indexOf(string2, e);
		if (pos2 == -1)
			return "";

		return target.substring(e, pos2);
	}

	/**
	 * TODO ADD METHOD DESCRIPTION
	 * 
	 * @author jfruehau
	 * 
	 * @param data
	 * @param string
	 * @return
	 */
	public static List<String> rextract(String data, String string,
			boolean dotall) {
		Pattern p = dotall ? Pattern.compile(string, Pattern.DOTALL) : Pattern
				.compile(string);
		Matcher m = p.matcher(data);
		List<String> ret = new ArrayList<String>();

		while (m.find()) {
			for (int i = 0; i < m.groupCount() + 1; i++)
				ret.add(m.group(i));
		}

		return ret;
	}
}
