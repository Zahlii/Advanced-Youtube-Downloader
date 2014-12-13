package de.zahlii.youtube.download.basic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import radams.gracenote.webapi.GracenoteException;
import radams.gracenote.webapi.GracenoteMetadata;
import radams.gracenote.webapi.GracenoteWebAPI;

/**
 * Wrapper around the Gracenote Api
 * 
 * @author Zahlii
 * 
 */
public class SearchManager {
	private static SearchManager instance;

	public static SearchManager getInstance() {
		if (instance == null) {
			instance = new SearchManager();
		}

		return instance;
	}

	private GracenoteWebAPI api;

	/**
	 * Connect to the API
	 */
	private SearchManager() {
		try {
			api = new GracenoteWebAPI("10483968", "4EA6AAF67950AC0A07567F7195D7405B", "264115384938188959-9606FB329A8E8D0ECF20D5E0C1BF71B0");
		} catch (final GracenoteException e) {
			Logging.log("failed connecting to gracenote API", e);
		}
	}

	/**
	 * Searches for music information.
	 * 
	 * @param artist
	 * @param album
	 * @param title
	 * @return
	 */
	public GracenoteMetadata searchForSong(String artist, String album, String title) {
		artist = _niceName(artist);
		album = _niceName(album);
		title = _niceName(title);
		return api.searchTrack(artist, album, title);
	}

	/**
	 * Cuts off the part behind a search string.
	 * 
	 * @param tar
	 * @param search
	 * @return
	 */
	private String _cutOff(final String tar, final String search) {
		return tar.split(search)[0].trim();
	}

	/**
	 * Remove some unnecessary information which might be contained in the video title
	 * 
	 * @param trim
	 * @return
	 */
	private String _niceName(String trim) {
		final Pattern p = Pattern.compile("\\(.*\\)");
		final Matcher m = p.matcher(trim);
		trim = m.replaceAll("");

		trim = _cutOff(trim, "feat. ");
		trim = _cutOff(trim, "ft. ");
		trim = _cutOff(trim, ", ");
		trim = _cutOff(trim, "vs. ");
		// trim = _cutOff(trim, " and ");
		trim = _cutOff(trim, " & ").replace("&", "");
		final Pattern p1 = Pattern.compile("\\.");
		final Matcher m1 = p1.matcher(trim.trim());
		trim = m1.replaceAll("");
		return trim.trim();
	}
}
