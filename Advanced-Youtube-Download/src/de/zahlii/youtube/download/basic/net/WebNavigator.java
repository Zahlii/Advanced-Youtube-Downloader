package de.zahlii.youtube.download.basic.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;

import de.zahlii.youtube.download.basic.Logging;

public class WebNavigator {
	public static final String IP = "79.219.97.90";

	public static WebNavigator getInstance() {
		if (instance == null)
			instance = new WebNavigator();

		return instance;
	}

	private static WebNavigator instance;

	private URIBuilder host;
	private CookieStore cookie;
	private HttpClientContext context;
	private HttpClient client;

	private WebNavigator() {
		RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.BEST_MATCH)
				.build();

		host = new URIBuilder().setScheme("https").setHost("www.youtube.com");

		cookie = new BasicCookieStore();
		context = HttpClientContext.create();
		context.setCookieStore(cookie);

		client = HttpClients.custom().setDefaultRequestConfig(globalConfig)
				.setDefaultCookieStore(cookie).addInterceptorFirst(new HttpRequestInterceptor() {

					@Override
					public void process(final HttpRequest request, final HttpContext context)
							throws HttpException, IOException {
						if (!request.containsHeader("Accept-Encoding")) {
							request.addHeader("Accept-Encoding", "gzip");
						}

					}
				}).addInterceptorFirst(new HttpResponseInterceptor() {

					@Override
					public void process(final HttpResponse response, final HttpContext context)
							throws HttpException, IOException {
						HttpEntity entity = response.getEntity();
						if (entity != null) {
							Header ceheader = entity.getContentEncoding();
							if (ceheader != null) {
								HeaderElement[] codecs = ceheader.getElements();
								for (int i = 0; i < codecs.length; i++) {
									if (codecs[i].getName().equalsIgnoreCase("gzip")) {
										response.setEntity(new GzipDecompressingEntity(response
												.getEntity()));
										return;
									}
								}
							}
						}
					}

				}).build();

	}

	public URIBuilder getHost() {
		return host;
	}

	public NetResponse navigate(URI uri) {
		NetResponse r = new NetResponse();

		HttpGet httpGet = new HttpGet(uri);
		httpGet.setHeader("accept-language", "de-DE,de;q=0.8,en-US;q=0.6,en;q=0.4");
		httpGet.setHeader(
				"user-agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36");
		httpGet.setHeader("accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");

		try {
			BasicResponseHandler handle = new BasicResponseHandler();

			HttpResponse response = client.execute(httpGet);
			String body = handle.handleResponse(response);
			r.request = httpGet;
			r.requestHeaders = Arrays.asList(r.request.getAllHeaders());
			r.responseEntity = response.getEntity();
			r.responseType = r.responseEntity.getContentType().getValue();
			r.responseHeaders = Arrays.asList(response.getAllHeaders());
			r.responseStatusLine = response.getStatusLine();
			r.responseBody = body;
		} catch (IOException e) {
			Logging.log("failed getting net response", e);
		}
		return r;
	}

	public InputStream navigateStream(String uri) {
		HttpGet httpGet = new HttpGet(uri);
		httpGet.setHeader(
				"user-agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36");

		try {
			HttpResponse r = client.execute(httpGet, context);
			return r.getEntity().getContent();
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<Cookie> getCookies() {
		return context.getCookieStore().getCookies();
	}
}
