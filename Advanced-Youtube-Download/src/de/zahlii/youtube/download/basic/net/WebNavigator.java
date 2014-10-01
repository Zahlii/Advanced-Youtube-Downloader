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

	private static WebNavigator instance;

	public static WebNavigator getInstance() {
		if (instance == null) {
			instance = new WebNavigator();
		}

		return instance;
	}

	private HttpClient client;
	private HttpClientContext context;
	private CookieStore cookie;
	private URIBuilder host;

	private WebNavigator() {
		final RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.BEST_MATCH).build();

		host = new URIBuilder().setScheme("https").setHost("www.youtube.com");

		cookie = new BasicCookieStore();
		context = HttpClientContext.create();
		context.setCookieStore(cookie);

		client = HttpClients.custom().setDefaultRequestConfig(globalConfig).setDefaultCookieStore(cookie).addInterceptorFirst(new HttpRequestInterceptor() {

			@Override
			public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
				if (!request.containsHeader("Accept-Encoding")) {
					request.addHeader("Accept-Encoding", "gzip");
				}

			}
		}).addInterceptorFirst(new HttpResponseInterceptor() {

			@Override
			public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
				final HttpEntity entity = response.getEntity();
				if (entity != null) {
					final Header ceheader = entity.getContentEncoding();
					if (ceheader != null) {
						final HeaderElement[] codecs = ceheader.getElements();
						for (final HeaderElement codec : codecs) {
							if (codec.getName().equalsIgnoreCase("gzip")) {
								response.setEntity(new GzipDecompressingEntity(response.getEntity()));
								return;
							}
						}
					}
				}
			}

		}).build();

	}

	public List<Cookie> getCookies() {
		return context.getCookieStore().getCookies();
	}

	public URIBuilder getHost() {
		return host;
	}

	public NetResponse navigate(final URI uri) {
		final NetResponse r = new NetResponse();

		final HttpGet httpGet = new HttpGet(uri);
		httpGet.setHeader("accept-language", "de-DE,de;q=0.8,en-US;q=0.6,en;q=0.4");
		httpGet.setHeader("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36");
		httpGet.setHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");

		try {
			final BasicResponseHandler handle = new BasicResponseHandler();

			final HttpResponse response = client.execute(httpGet);
			final String body = handle.handleResponse(response);
			r.request = httpGet;
			r.requestHeaders = Arrays.asList(r.request.getAllHeaders());
			r.responseEntity = response.getEntity();
			r.responseType = r.responseEntity.getContentType().getValue();
			r.responseHeaders = Arrays.asList(response.getAllHeaders());
			r.responseStatusLine = response.getStatusLine();
			r.responseBody = body;
		} catch (final IOException e) {
			Logging.log("failed getting net response", e);
		}
		return r;
	}

	public InputStream navigateStream(final String uri) {
		final HttpGet httpGet = new HttpGet(uri);
		httpGet.setHeader("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36");

		try {
			final HttpResponse r = client.execute(httpGet, context);
			return r.getEntity().getContent();
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
