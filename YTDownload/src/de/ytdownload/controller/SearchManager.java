/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 */
package de.ytdownload.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jaudiotagger.tag.FieldKey;

import radams.gracenote.webapi.GracenoteException;
import radams.gracenote.webapi.GracenoteMetadata;
import radams.gracenote.webapi.GracenoteWebAPI;
import de.ytdownload.model.Song;
import de.ytdownload.service.Logging;

/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 * 
 */
public class SearchManager {
	private static SearchManager instance;
	private GracenoteWebAPI api;

	private SearchManager() {
		try {
			api = new GracenoteWebAPI("10483968", "4EA6AAF67950AC0A07567F7195D7405B",
					"264115384938188959-9606FB329A8E8D0ECF20D5E0C1BF71B0");
		} catch (GracenoteException e) {
			Logging.log("failed connecting to gracenote API", e);
		}
	}

	public static SearchManager getInstance() {
		if (instance == null)
			instance = new SearchManager();

		return instance;
	}

	public String searchLyrics(Song s) {
		return null;
	}

	private Map<FieldKey, Integer> positions = new HashMap<FieldKey, Integer>() {

		private static final long serialVersionUID = 2370932660554692493L;

		{
			put(FieldKey.ARTIST, 0);
			put(FieldKey.TITLE, 1);
		}
	};

	private String checkTag(FieldKey key, Song s) {
		String found = null;
		if (s.getTag().hasField(key)) {
			found = s.getTag().getFirst(key);
		} else {
			String[] parts = s.getName().split("-");
			found = positions.containsKey(key) ? parts[positions.get(key)] : "";
		}

		return _niceName(found);
	}

	private String _cutOff(String tar, String search) {
		return tar.split(search)[0].trim();
	}

	private String _niceName(String trim) {
		Pattern p = Pattern.compile("\\(.*\\)");
		Matcher m = p.matcher(trim);
		trim = m.replaceAll("");

		trim = _cutOff(trim, "feat. ");
		trim = _cutOff(trim, "ft. ");
		trim = _cutOff(trim, ", ");
		trim = _cutOff(trim, "vs. ");
		// trim = _cutOff(trim, " and ");
		trim = _cutOff(trim, " & ").replace("&", "");
		Pattern p1 = Pattern.compile("\\.");
		Matcher m1 = p1.matcher(trim.trim());
		trim = m1.replaceAll("");
		return trim.trim();
	}

	public GracenoteMetadata searchForSong(Song s) {
		String artist = checkTag(FieldKey.ARTIST, s);
		String album = "";// alb != null ? alb : "";// checkTag(FieldKey.ALBUM,
							// s);
		String title = checkTag(FieldKey.TITLE, s);
		return api.searchTrack(artist, album, title);
	}

	public GracenoteMetadata searchForSong(String artist, String album, String title) {
		artist = _niceName(artist);
		album = _niceName(album);
		title = _niceName(title);
		return api.searchTrack(artist, album, title);
	}
}
