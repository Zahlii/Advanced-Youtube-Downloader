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
 * This class is used to download information from a website and easily extracting information out of it.
 * 
 * @author Zahlii
 * 
 */
public class NetResponse {

	/**
	 * Extracts the data between two strings
	 * 
	 * @param target
	 *            String containing the info to be extracted.
	 * @param string
	 *            String at the start of the extraction.
	 * @param string2
	 *            String at the end of the extraction.
	 * @return The first part between the first occurences of the start and end string, or "" if not found.
	 */
	public static String extract(final String target, final String string, final String string2) {
		final int pos = target.indexOf(string);
		if (pos == -1)
			return "";
		final int e = pos + string.length();
		final int pos2 = target.indexOf(string2, e);
		if (pos2 == -1)
			return "";

		return target.substring(e, pos2);
	}

	/**
	 * Extracts the data using RegEx
	 * 
	 * @param data
	 *            String containing the info to be extracted.
	 * @param string
	 *            RegEx string which should be applied.
	 * @param dotall
	 *            Whether the RegEx-option DOTALL shall be used.
	 * @return A list of matches. For each match, all groups will be captured, so for one subgroup and two matches there will be four entries in the results.
	 */
	public static List<String> rextract(final String data, final String string, final boolean dotall) {
		final Pattern p = dotall ? Pattern.compile(string, Pattern.DOTALL) : Pattern.compile(string);
		final Matcher m = p.matcher(data);
		final List<String> ret = new ArrayList<String>();

		while (m.find()) {
			for (int i = 0; i < m.groupCount() + 1; i++) {
				ret.add(m.group(i));
			}
		}

		return ret;
	}

	public HttpGet request;
	public List<Header> requestHeaders;
	public String responseBody;
	public HttpEntity responseEntity;
	public List<Header> responseHeaders;
	public StatusLine responseStatusLine;

	public BufferedInputStream responseStream;

	public String responseType;

	/**
	 * Convenience method to use on a NetResponse object
	 * 
	 * @param s1
	 *            Start seperator
	 * @param s2
	 *            End seperator
	 * @return Part in between start and end or ""
	 */
	public String extract(final String s1, final String s2) {
		return extract(responseBody, s1, s2);
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();

		b.append(request);
		for (final Header x : requestHeaders) {
			b.append(x);
			b.append("\n");
		}
		b.append("\n");
		b.append("type: " + responseType);
		b.append("\n");
		for (final Header x : responseHeaders) {
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
}
