package radams.gracenote.webapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.zahlii.youtube.download.basic.Levenshtein;
import de.zahlii.youtube.download.basic.Logging;

public class GracenoteMetadata {
	// Members
	private final ArrayList<Map<String, Object>> _data = new ArrayList<Map<String, Object>>();

	// Construct from the XML response from GN API
	public GracenoteMetadata(final GracenoteWebAPI api, Document xml) {
		final Element root = xml.getDocumentElement();

		final NodeList nl = root.getElementsByTagName("ALBUM");
		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);

			// Album data map
			final Map<String, Object> albumData = new HashMap<String, Object>();

			// If there's no GOET data, do a fetch to get the full information
			// instead.
			if (_getTextValue(e, "ARTIST_ORIGIN") == null) {
				xml = api.fetchAlbumWithoutParsing(_getTextValue(e, "GN_ID"));
				if (xml == null) {
					Logging.log("[GRACE]\tno gracenote match");
					return;
				}
				e = xml.getDocumentElement();
			}

			// Album metadata
			albumData.put("album_gnid", _getTextValue(e, "GN_ID"));
			albumData.put("album_artist_name", _getTextValue(e, "ARTIST"));
			albumData.put("album_title", _getTextValue(e, "TITLE"));
			albumData.put("album_year", _getTextValue(e, "DATE"));
			albumData.put("genre", _getOETData(e, "GENRE"));
			albumData.put("track_count", _getTextValue(e, "TRACK_COUNT"));
			albumData.put("album_coverart", _getAttribElement(e, "URL", "TYPE", "COVERART"));

			// Artist metadata
			albumData.put("artist_image_url", _getAttribElement(e, "URL", "TYPE", "ARTIST_IMAGE"));
			albumData.put("artist_bio_url", _getAttribElement(e, "URL", "TYPE", "ARTIST_BIOGRAPHY"));
			albumData.put("review_url", _getAttribElement(e, "URL", "TYPE", "ARTIST_REVIEW"));

			// Artist OET metadata
			albumData.put("artist_era", _getOETData(e, "ARTIST_ERA"));
			albumData.put("artist_type", _getOETData(e, "ARTIST_TYPE"));
			albumData.put("artist_origin", _getOETData(e, "ARTIST_ORIGIN"));

			final int trackCount = e.getElementsByTagName("TRACK").getLength();

			// Parse track metadata if there is any.
			for (int j = 0; j < trackCount; j++) {
				final Element trackElement = (Element) e.getElementsByTagName("TRACK").item(j);

				final String title = _getTextValue(trackElement, "TITLE");
				final String search = api.getSearchedTrack();
				final boolean success = title.contains(search) || search.contains(title) || Levenshtein.distance(search, title) <= 0.1 * title.length();
				if (!success) {
					continue;
				}

				albumData.put("track_number", _getTextValue(trackElement, "TRACK_NUM"));
				albumData.put("track_gn_id", _getTextValue(trackElement, "GN_ID"));
				albumData.put("track_title", title);
				albumData.put("track_artist_name", _getTextValue(trackElement, "ARTIST"));

				albumData.put("mood", _getOETData(trackElement, "MOOD"));
				albumData.put("tempo", _getOETData(trackElement, "TEMPO"));

				// If track level GOET data exists, overwrite metadata from
				// album.
				if (trackElement.getElementsByTagName("GENRE").getLength() > 0) {
					albumData.put("genre", _getOETData(trackElement, "GENRE"));
				}
				if (trackElement.getElementsByTagName("ARTIST_ERA").getLength() > 0) {
					albumData.put("artist_era", _getOETData(trackElement, "ARTIST_ERA"));
				}
				if (trackElement.getElementsByTagName("ARTIST_TYPE").getLength() > 0) {
					albumData.put("artist_type", _getOETData(trackElement, "ARTIST_TYPE"));
				}
				if (trackElement.getElementsByTagName("ARTIST_ORIGIN").getLength() > 0) {
					albumData.put("artist_origin", _getOETData(trackElement, "ARTIST_ORIGIN"));
				}
				break;
			}

			_data.add(albumData);
		}
	}

	public Map<String, Object> getAlbum(final int index) {
		return _data.get(index);
	}

	public Object getAlbumData(final int albumIndex, final String dataKey) {
		return getAlbum(albumIndex).get(dataKey);
	}

	// Getters
	public ArrayList<Map<String, Object>> getAlbums() {
		return _data;
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////
	// Helpers

	@SuppressWarnings("unchecked")
	public String getArrString(final String key) {
		try {
			return ((ArrayList<GracenoteMetadataOET>) _data.get(0).get(key)).get(0).getText();
		} catch (NullPointerException | IndexOutOfBoundsException e) {
			return "";
		}
	}

	public String getArtist() {
		return !getString("track_artist_name").equals("") ? getString("track_artist_name") : getString("album_artist_name");
	}

	public String getString(final String key) {
		try {
			return _data != null && _data.size() >= 1 ? _data.get(0).containsKey(key) ? _data.get(0).get(key).toString() : "" : "";

		} catch (final NullPointerException e) {
			return "";
		}

	}

	public String getTitle() {
		return getString("track_title");
	}

	public boolean hasAllData() {
		return (!"".equals(getString("album_artist_name")) || !"".equals(getString("track_artist_name"))) && !"".equals(getString("track_title"));
	}

	// Dumps out the data in a raw format to the console
	@SuppressWarnings("unchecked")
	public void print() {
		System.out.println("** Metadata **");
		for (final Map<String, Object> m : _data) {
			System.out.println(" + ALBUM");
			for (final String key : m.keySet()) {
				final Object o = m.get(key);

				// Most stuff is string, we can just dump that out.
				if (o instanceof String) {
					System.out.println("   + " + key + ": " + (String) o);
				}

				if (o instanceof ArrayList) {
					System.out.println("   + " + key + ":");
					for (final Object oo : (ArrayList<GracenoteMetadataOET>) o) {
						if (oo instanceof GracenoteMetadataOET) {
							final GracenoteMetadataOET oet = (GracenoteMetadataOET) oo;
							oet.print();
						}
					}
				}
			}
		}
	}

	private String _getAttribElement(final Element root, final String nodeName, final String attribute, final String value) {
		final NodeList nl = root.getElementsByTagName(nodeName);
		for (int i = 0; i < nl.getLength(); i++) {
			final Element e = (Element) nl.item(i);
			final String attr = e.getAttribute(attribute);
			if (attr != null && attr.equals(value))
				return e.getFirstChild().getNodeValue();
		}

		return null;
	}

	private ArrayList<GracenoteMetadataOET> _getOETData(final Element root, final String name) {
		final ArrayList<GracenoteMetadataOET> al = new ArrayList<GracenoteMetadataOET>();

		final NodeList nl = root.getElementsByTagName(name);
		for (int i = 0; i < nl.getLength(); i++) {
			final Element e = (Element) nl.item(i);
			final GracenoteMetadataOET oet = new GracenoteMetadataOET(e.getAttribute("ID"), e.getFirstChild().getNodeValue());
			al.add(oet);
		}

		return al;
	}

	private String _getTextValue(final Element ele, final String tagName) {
		String textVal = null;
		final NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			final Element el = (Element) nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		return textVal;
	}
}
