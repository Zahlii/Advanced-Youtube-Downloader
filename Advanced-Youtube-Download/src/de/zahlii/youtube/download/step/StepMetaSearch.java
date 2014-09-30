package de.zahlii.youtube.download.step;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;
import org.jaudiotagger.tag.FieldKey;

import radams.gracenote.webapi.GracenoteMetadata;
import de.zahlii.youtube.download.QueueEntry;
import de.zahlii.youtube.download.basic.SearchManager;
import de.zahlii.youtube.download.ui.InfoFrame;
import de.zahlii.youtube.download.ui.SearchFrame;

public class StepMetaSearch extends Step {
	private String artist = "", title = "", album = "";
	private GracenoteMetadata d;

	public StepMetaSearch(final QueueEntry entry) {
		super(
				entry,
				new StepDescriptor("GracenoteSearch",
						"Searches the Gracenote music DB for further information and cover art"));
	}

	@Override
	public void doStep() {
		final String baseName = FilenameUtils.getBaseName(this.entry
				.getDownloadTempFile().getAbsolutePath());

		final String[] parts = baseName.split("-");

		switch (parts.length) {
		case 1:
			this.title = parts[0].trim();
			break;
		case 2:
			this.artist = parts[0].trim();
			this.title = parts[1].trim();
			break;
		case 3:
			this.artist = parts[0].trim();
			this.title = parts[1].trim();
			this.album = parts[2].trim();
			break;
		default:
			final int n = parts.length - 3;
			this.artist = parts[n].trim();
			this.title = parts[n + 1].trim();
			this.album = parts[n + 2].trim();
		}

		this.handleMetaSearch();
	}

	@Override
	public String getStepResults() {
		return this.d == null ? "No Gracenote match found."
				: "Found possible match with songtitle " + this.d.getTitle()
						+ ".";
	}

	private void handleMetaSearch() {

		if (SwingUtilities.isEventDispatchThread()) {
			final SearchFrame sf = new SearchFrame(this.artist, this.title,
					this.album);
			sf.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent arg0) {
					if (arg0 == null) {
						StepMetaSearch.this.handleMetaResult();
					} else {
						StepMetaSearch.this.d = SearchManager.getInstance()
								.searchForSong(sf.getArtist(), sf.getAlbum(),
										sf.getSongTitle());
						StepMetaSearch.this.handleMetaResult();
					}
				}

			});
			sf.setVisible(true);
		} else {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					StepMetaSearch.this.handleMetaSearch();

				}

			});
		}
	}

	private void handleMetaResult() {
		if (SwingUtilities.isEventDispatchThread()) {
			final InfoFrame i = new InfoFrame(this.entry, this.d);
			if (this.d == null) {
				i.fillInfo(this.artist, this.title, this.album);
			}
			i.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent arg0) {
					if (arg0 == null) {
						StepMetaSearch.this.handleMetaSearch();
					} else {
						StepMetaSearch.this.saveMetaData(i);
					}
				}

			});
			i.setVisible(true);
		} else {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					StepMetaSearch.this.handleMetaResult();

				}

			});
		}

	}

	private void saveMetaData(final InfoFrame fr) {
		final Map<FieldKey, String> data = new HashMap<FieldKey, String>();
		data.put(FieldKey.ARTIST, fr.getArtist());
		data.put(FieldKey.ALBUM_ARTIST, fr.getAlbumArtist());
		data.put(FieldKey.CONDUCTOR, fr.getArtist());
		data.put(FieldKey.ALBUM, fr.getAlbum());
		data.put(FieldKey.TITLE, fr.getSongtitle());
		data.put(FieldKey.TRACK, fr.getTrack());
		data.put(FieldKey.TRACK_TOTAL, fr.getTrackCount());
		data.put(FieldKey.YEAR, fr.getYear());
		data.put(FieldKey.MOOD, fr.getMood());

		data.put(FieldKey.GENRE, fr.getGenre());
		data.put(FieldKey.TEMPO, fr.getTempo());
		data.put(FieldKey.COMMENT, this.entry.getDownloadTempFile()
				.getAbsolutePath());

		this.entry.getStepInfo().put("meta.data", data);

		fr.getTagEditor().writeAllFields(data);
		fr.getTagEditor().writeArtwork(fr.getArtworkImage());
		fr.getTagEditor().commit();
		this.nextStep();
	}
}
