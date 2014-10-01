package radams.gracenote.webapi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

// You will need a Gracenote Client ID to use this. Visit
// https://developer.gracenote.com/ for info.

public class GracenoteWebAPI {
	// Members
	private String _clientID = "";
	private String _clientTag = "";
	private String _userID = "";
	private String _apiURL = "https://[[CLID]].web.cddbp.net/webapi/xml/1.0/";
	private String searchTrack;

	// Constructor
	public GracenoteWebAPI(final String clientID, final String clientTag)
			throws GracenoteException {
		this(clientID, clientTag, "");
	}

	public GracenoteWebAPI(final String clientID, final String clientTag,
			final String userID) throws GracenoteException {
		// Sanity checks
		if (clientID.equals(""))
			throw new GracenoteException("Invalid input specified: clientID.");
		if (clientTag.equals(""))
			throw new GracenoteException("Invalid input specified: clientTag.");

		_clientID = clientID;
		_clientTag = clientTag;
		_userID = userID;
		_apiURL = _apiURL.replace("[[CLID]]", clientID);
	}

	// Checks the response for any Gracenote API errors, and converts to an XML
	// document.
	private Document _checkResponse(final String response) {
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			// Get and parse into a document
			final DocumentBuilder db = dbf.newDocumentBuilder();
			final Document doc = db.parse(new InputSource(new StringReader(
					response)));

			// Navigate to the status code and read it.
			final Element root = doc.getDocumentElement();
			final NodeList nl = root.getElementsByTagName("RESPONSE");
			String status = "ERROR";
			if (nl != null && nl.getLength() > 0) {
				status = nl.item(0).getAttributes().getNamedItem("STATUS")
						.getNodeValue();
			}

			// Handle error codes accordingly
			if (status.equals("ERROR"))
				throw new GracenoteException("API response error.");
			if (status.equals("NO_MATCH"))
				throw new GracenoteException("No match response.");
			if (!status.equals("OK"))
				throw new GracenoteException("Non-OK API response.");

			return doc;
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	// Constructs the main request body, including some default options for
	// metadata, etc.
	protected String _constructQueryBody(final String artist,
			final String album, final String track) {
		return this._constructQueryBody(artist, album, track, "",
				"ALBUM_SEARCH");
	}

	protected String _constructQueryBody(final String artist,
			final String album, final String track, final String gn_id,
			final String command) {
		String body = "";

		// If a fetch scenario, user the Gracenote ID.
		if (command.equals("ALBUM_FETCH")) {
			body += "<GN_ID>" + gn_id + "</GN_ID>";

			// Include extended data.
			body += "<OPTION>"
					+ "<PARAMETER>SELECT_EXTENDED</PARAMETER>"
					+ "<VALUE>COVER,REVIEW,ARTIST_BIOGRAPHY,ARTIST_IMAGE,ARTIST_OET,MOOD,TEMPO</VALUE>"
					+ "</OPTION>";

			// Include more detailed responses.
			body += "<OPTION>"
					+ "<PARAMETER>SELECT_DETAIL</PARAMETER>"
					+ "<VALUE>GENRE:3LEVEL,MOOD:2LEVEL,TEMPO:3LEVEL,ARTIST_ORIGIN:4LEVEL,ARTIST_ERA:2LEVEL,ARTIST_TYPE:2LEVEL</VALUE>"
					+ "</OPTION>";

			// Only want the thumbnail cover art for now
			// (LARGE,XLARGE,SMALL,MEDIUM,THUMBNAIL)
			body += "<OPTION>" + "<PARAMETER>COVER_SIZE</PARAMETER>"
					+ "<VALUE>MEDIUM</VALUE>" + "</OPTION>";
		}
		// Otherwise, just do a search.
		else {
			// Only want the single best match.
			body += "<MODE>SINGLE_BEST</MODE>";

			// If a search scenario, then need the text input
			if (!artist.equals("")) {
				body += "<TEXT TYPE=\"ARTIST\">" + artist + "</TEXT>";
			}
			if (!track.equals("")) {
				body += "<TEXT TYPE=\"TRACK_TITLE\">" + track + "</TEXT>";
			}
			if (!album.equals("")) {
				body += "<TEXT TYPE=\"ALBUM_TITLE\">" + album + "</TEXT>";
			}
		}

		return body;
	}

	// This will construct the Gracenote query, adding in the authentication
	// header, etc.
	protected String _constructQueryRequest(final String body) {
		return this._constructQueryRequest(body, "ALBUM_SEARCH");
	}

	protected String _constructQueryRequest(final String body,
			final String command) {
		return "<QUERIES>" + "<AUTH>" + "<CLIENT>" + _clientID + "-"
				+ _clientTag + "</CLIENT>" + "<USER>" + _userID + "</USER>"
				+ "</AUTH>" + "<QUERY CMD=\"" + command + "\">" + body
				+ "</QUERY>" + "</QUERIES>";
	}

	// Simply executes the query to Gracenote WebAPI
	protected GracenoteMetadata _execute(final String data) {
		final String response = _httpPostRequest(_apiURL, data);
		return _parseResponse(response);
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////

	// Performs a HTTP POST request and returns the response as a string.
	protected String _httpPostRequest(final String url, final String data) {
		try {
			final URL u = new URL(url);
			final HttpURLConnection connection = (HttpURLConnection) u
					.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setInstanceFollowRedirects(false);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "text/plain");
			connection.setRequestProperty("Charset", "utf-8");
			connection.setRequestProperty("Content-Length",
					"" + Integer.toString(data.getBytes().length));
			connection.setUseCaches(false);

			// Write the POST data
			final BufferedWriter wr = new BufferedWriter(
					new OutputStreamWriter(connection.getOutputStream(),
							"UTF-8"));
			wr.write(data);
			wr.flush();
			wr.close();

			// Read the output
			final StringBuffer output = new StringBuffer();
			final BufferedReader reader = new BufferedReader(
					new InputStreamReader(connection.getInputStream(), "UTF-8"));

			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line);
			}

			reader.close();
			connection.disconnect();

			final String put = output.toString();
			// System.err.println(put);
			return put;
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	// This parses the API response into a GracenoteMetadata object
	protected GracenoteMetadata _parseResponse(final String response) {
		final Document xml = _checkResponse(response);
		return new GracenoteMetadata(this, xml);
	}

	// This looks up an album directly using it's Gracenote identifier. Will
	// return all the
	// additional GOET data.
	public GracenoteMetadata fetchAlbum(final String gn_id) {
		// Sanity check
		if (_userID.equals("")) {
			this.register();
		}

		final String body = this._constructQueryBody("", "", "", gn_id,
				"ALBUM_FETCH");
		final String data = this._constructQueryRequest(body, "ALBUM_FETCH");
		return _execute(data);
	}

	// This looks up an album directly using it's Gracenote identifier. Returns
	// the document, without
	// parsing the data first.
	public Document fetchAlbumWithoutParsing(final String gn_id) {
		// Sanity check
		if (_userID.equals("")) {
			this.register();
		}

		final String body = this._constructQueryBody("", "", "", gn_id,
				"ALBUM_FETCH");
		final String data = this._constructQueryRequest(body, "ALBUM_FETCH");
		final String response = _httpPostRequest(_apiURL, data);
		return _checkResponse(response);
	}

	public String getSearchedTrack() {
		return searchTrack;
	}

	// Will register your clientID and Tag in order to get a userID. The userID
	// should be stored
	// in a persistent form (filesystem, db, etc) otherwise you will hit your
	// user limit.
	public String register() {
		return this.register(_clientID + "-" + _clientTag);
	}

	public String register(final String clientID) {
		// Make sure user doesn't try to register again if they already have a
		// userID in the ctor.
		if (!_userID.equals("")) {
			System.out
					.println("Warning: You already have a userID, no need to register another. Using current ID.");
			return _userID;
		}

		// Do the register request
		final String request = "<QUERIES>" + "<QUERY CMD=\"REGISTER\">"
				+ "<CLIENT>" + clientID + "</CLIENT>" + "</QUERY>"
				+ "</QUERIES>";

		final String response = _httpPostRequest(_apiURL, request);
		final Document xml = _checkResponse(response);

		// Cache it locally then return to user.
		_userID = xml.getDocumentElement().getElementsByTagName("USER").item(0)
				.getFirstChild().getNodeValue();
		return _userID;
	}

	// Queries the Gracenote service for an album.
	public GracenoteMetadata searchAlbum(final String artistName,
			final String albumTitle) {
		return searchTrack(artistName, albumTitle, "");
	}

	// Queries the Gracenote service for an artist.
	public GracenoteMetadata searchArtist(final String artistName) {
		return searchTrack(artistName, "", "");
	}

	// Queries the Gracenote service for a track
	public GracenoteMetadata searchTrack(final String artistName,
			final String albumTitle, final String trackTitle) {
		// Sanity check
		if (_userID.equals("")) {
			this.register();
		}

		searchTrack = trackTitle;

		final String body = this._constructQueryBody(artistName, albumTitle,
				trackTitle);
		final String data = this._constructQueryRequest(body);
		return _execute(data);
	}
}
