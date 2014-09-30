package de.zahlii.youtube.download.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.jaudiotagger.tag.FieldKey;

import radams.gracenote.webapi.GracenoteMetadata;
import de.zahlii.youtube.download.QueueEntry;
import de.zahlii.youtube.download.basic.ConfigManager;
import de.zahlii.youtube.download.basic.ConfigManager.ConfigKey;
import de.zahlii.youtube.download.basic.Helper;
import de.zahlii.youtube.download.basic.Logging;
import de.zahlii.youtube.download.basic.Media;
import de.zahlii.youtube.download.basic.TagEditor;

public class InfoFrame extends JFrame {
	private final JTextField artist;
	private final JTextField title;
	private final JTextField album;
	private final List<ActionListener> listeners;
	private final JTextField year;
	private final JTextField track;
	private final JTextField trackCount;
	private final JTextField albumartist;
	private final JTextField genre;
	private final JTextField tempo;
	private final JTextField mood;
	private final CoverPanel coverPanel;

	private TagEditor tagEdit;
	private QueueEntry e;
	private final JButton btnChoseImage;

	private void fillData(final QueueEntry e) {

		this.coverPanel.setImage(this.tagEdit.readArtwork());
		this.artist.setText(this.tagEdit.readField(FieldKey.ARTIST));
		this.title.setText(this.tagEdit.readField(FieldKey.TITLE));
		this.album.setText(this.tagEdit.readField(FieldKey.ALBUM));
		this.year.setText(this.tagEdit.readField(FieldKey.YEAR));
		this.albumartist.setText(this.tagEdit.readField(FieldKey.ALBUM_ARTIST));
		this.genre.setText(this.tagEdit.readField(FieldKey.GENRE));
		this.tempo.setText(this.tagEdit.readField(FieldKey.TEMPO));
		this.mood.setText(this.tagEdit.readField(FieldKey.MOOD));
		this.track.setText(this.tagEdit.readField(FieldKey.TRACK));
		this.trackCount.setText(this.tagEdit.readField(FieldKey.TRACK_TOTAL));
	}

	private void fillData(final GracenoteMetadata d) {
		if (d == null)
			return;

		try {
			final String art = d.getAlbum(0).get("album_coverart").toString();
			this.coverPanel.setImage(Helper.downloadImage(art));
		} catch (final Exception e) {
			this.coverPanel.setImage(null);
		}
		this.artist.setText(d.getArtist());
		this.title.setText(d.getTitle());
		this.album.setText(d.getString("album_title"));
		this.year.setText(d.getString("album_year"));
		this.albumartist.setText(d.getString("album_artist_name"));
		this.genre.setText(d.getArrString("genre"));
		this.tempo.setText(d.getArrString("tempo"));
		this.mood.setText(d.getArrString("mood"));
		this.track.setText(d.getString("track_number"));
		this.trackCount.setText(d.getString("track_count"));
	}

	public TagEditor getTagEditor() {
		return this.tagEdit;
	}

	private InfoFrame() {
		super("Edit Audio Information");
		this.setIconImage(Media.ICON_DOWNLOAD.getImage());
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				for (final ActionListener i : InfoFrame.this.listeners) {
					i.actionPerformed(null);
				}
				InfoFrame.this.setVisible(false);
			}
		});

		this.listeners = new ArrayList<ActionListener>();

		final GridBagLayout gbc_main = new GridBagLayout();
		gbc_main.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		gbc_main.columnWidths = new int[] { 10, 100, 100, 100, 270, 10 };
		gbc_main.rowHeights = new int[] { 10, 30, 30, 30, 30, 30, 30, 30, 30,
				30, 10 };

		this.getContentPane().setLayout(gbc_main);
		this.setMinimumSize(new Dimension(this.sum(gbc_main.columnWidths), this
				.sum(gbc_main.rowHeights)));
		this.setResizable(false);
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		this.setLocationRelativeTo(null);

		this.btnRestoreArtwork = new JButton("Restore Artwork");
		this.btnRestoreArtwork.setIcon(Media.ICON_CANCEL);
		this.btnRestoreArtwork.setEnabled(false);
		this.btnRestoreArtwork.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				if (InfoFrame.this.tempImage != null) {
					InfoFrame.this.coverPanel
							.setImage(InfoFrame.this.tempImage);
					InfoFrame.this.btnRestoreArtwork.setEnabled(false);
				}
			}
		});
		final GridBagConstraints gbc_btnRestoreArtwork = new GridBagConstraints();
		gbc_btnRestoreArtwork.insets = new Insets(0, 0, 5, 5);
		gbc_btnRestoreArtwork.gridx = 4;
		gbc_btnRestoreArtwork.gridy = 0;
		this.getContentPane()
				.add(this.btnRestoreArtwork, gbc_btnRestoreArtwork);

		final JLabel lblNewLabel = new JLabel("Artist");
		final GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 1;
		this.getContentPane().add(lblNewLabel, gbc_lblNewLabel);

		this.artist = new JTextField();
		final GridBagConstraints gbc_artist = new GridBagConstraints();
		gbc_artist.gridwidth = 2;
		gbc_artist.insets = new Insets(0, 0, 5, 5);
		gbc_artist.fill = GridBagConstraints.HORIZONTAL;
		gbc_artist.gridx = 2;
		gbc_artist.gridy = 1;
		this.getContentPane().add(this.artist, gbc_artist);
		this.artist.setColumns(10);

		this.coverPanel = new CoverPanel(null);
		final GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridheight = 9;
		gbc_panel.gridwidth = 2;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 4;
		gbc_panel.gridy = 1;
		this.getContentPane().add(this.coverPanel, gbc_panel);

		final JLabel lblNewLabel_1 = new JLabel("Title");
		final GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 1;
		gbc_lblNewLabel_1.gridy = 2;
		this.getContentPane().add(lblNewLabel_1, gbc_lblNewLabel_1);

		this.title = new JTextField();
		final GridBagConstraints gbc_title = new GridBagConstraints();
		gbc_title.gridwidth = 2;
		gbc_title.insets = new Insets(0, 0, 5, 5);
		gbc_title.fill = GridBagConstraints.HORIZONTAL;
		gbc_title.gridx = 2;
		gbc_title.gridy = 2;
		this.getContentPane().add(this.title, gbc_title);
		this.title.setColumns(10);

		final JLabel lblNewLabel_2 = new JLabel("Album");
		final GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 1;
		gbc_lblNewLabel_2.gridy = 3;
		this.getContentPane().add(lblNewLabel_2, gbc_lblNewLabel_2);

		this.album = new JTextField();
		final GridBagConstraints gbc_album = new GridBagConstraints();
		gbc_album.gridwidth = 2;
		gbc_album.insets = new Insets(0, 0, 5, 5);
		gbc_album.fill = GridBagConstraints.HORIZONTAL;
		gbc_album.gridx = 2;
		gbc_album.gridy = 3;
		this.getContentPane().add(this.album, gbc_album);
		this.album.setColumns(10);

		final JLabel lblYear = new JLabel("Year");
		final GridBagConstraints gbc_lblYear = new GridBagConstraints();
		gbc_lblYear.anchor = GridBagConstraints.EAST;
		gbc_lblYear.insets = new Insets(0, 0, 5, 5);
		gbc_lblYear.gridx = 1;
		gbc_lblYear.gridy = 4;
		this.getContentPane().add(lblYear, gbc_lblYear);

		this.year = new JTextField();
		this.year.setColumns(10);
		final GridBagConstraints gbc_year = new GridBagConstraints();
		gbc_year.gridwidth = 2;
		gbc_year.insets = new Insets(0, 0, 5, 5);
		gbc_year.fill = GridBagConstraints.HORIZONTAL;
		gbc_year.gridx = 2;
		gbc_year.gridy = 4;
		this.getContentPane().add(this.year, gbc_year);

		final JLabel lblAlbumArtist = new JLabel("Album Artist");
		final GridBagConstraints gbc_lblAlbumArtist = new GridBagConstraints();
		gbc_lblAlbumArtist.anchor = GridBagConstraints.EAST;
		gbc_lblAlbumArtist.insets = new Insets(0, 0, 5, 5);
		gbc_lblAlbumArtist.gridx = 1;
		gbc_lblAlbumArtist.gridy = 5;
		this.getContentPane().add(lblAlbumArtist, gbc_lblAlbumArtist);

		this.albumartist = new JTextField();
		this.albumartist.setColumns(10);
		final GridBagConstraints gbc_albumartist = new GridBagConstraints();
		gbc_albumartist.gridwidth = 2;
		gbc_albumartist.insets = new Insets(0, 0, 5, 5);
		gbc_albumartist.fill = GridBagConstraints.HORIZONTAL;
		gbc_albumartist.gridx = 2;
		gbc_albumartist.gridy = 5;
		this.getContentPane().add(this.albumartist, gbc_albumartist);

		final JLabel lblTemo = new JLabel("Genre");
		final GridBagConstraints gbc_lblTemo = new GridBagConstraints();
		gbc_lblTemo.anchor = GridBagConstraints.EAST;
		gbc_lblTemo.insets = new Insets(0, 0, 5, 5);
		gbc_lblTemo.gridx = 1;
		gbc_lblTemo.gridy = 6;
		this.getContentPane().add(lblTemo, gbc_lblTemo);

		this.genre = new JTextField();
		this.genre.setColumns(10);
		final GridBagConstraints gbc_genre = new GridBagConstraints();
		gbc_genre.gridwidth = 2;
		gbc_genre.insets = new Insets(0, 0, 5, 5);
		gbc_genre.fill = GridBagConstraints.HORIZONTAL;
		gbc_genre.gridx = 2;
		gbc_genre.gridy = 6;
		this.getContentPane().add(this.genre, gbc_genre);

		final JLabel lblMood = new JLabel("Tempo");
		final GridBagConstraints gbc_lblMood = new GridBagConstraints();
		gbc_lblMood.anchor = GridBagConstraints.EAST;
		gbc_lblMood.insets = new Insets(0, 0, 5, 5);
		gbc_lblMood.gridx = 1;
		gbc_lblMood.gridy = 7;
		this.getContentPane().add(lblMood, gbc_lblMood);

		final JButton btnNewButton = new JButton("Save");
		btnNewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				InfoFrame.this.setVisible(false);
				for (final ActionListener a : InfoFrame.this.listeners) {
					a.actionPerformed(e);
				}
			}
		});

		this.tempo = new JTextField();
		this.tempo.setColumns(10);
		final GridBagConstraints gbc_tempo = new GridBagConstraints();
		gbc_tempo.gridwidth = 2;
		gbc_tempo.insets = new Insets(0, 0, 5, 5);
		gbc_tempo.fill = GridBagConstraints.HORIZONTAL;
		gbc_tempo.gridx = 2;
		gbc_tempo.gridy = 7;
		this.getContentPane().add(this.tempo, gbc_tempo);

		final JLabel lblMood_1 = new JLabel("Mood");
		final GridBagConstraints gbc_lblMood_1 = new GridBagConstraints();
		gbc_lblMood_1.anchor = GridBagConstraints.EAST;
		gbc_lblMood_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblMood_1.gridx = 1;
		gbc_lblMood_1.gridy = 8;
		this.getContentPane().add(lblMood_1, gbc_lblMood_1);

		this.mood = new JTextField();
		this.mood.setColumns(10);
		final GridBagConstraints gbc_mood = new GridBagConstraints();
		gbc_mood.gridwidth = 2;
		gbc_mood.insets = new Insets(0, 0, 5, 5);
		gbc_mood.fill = GridBagConstraints.HORIZONTAL;
		gbc_mood.gridx = 2;
		gbc_mood.gridy = 8;
		this.getContentPane().add(this.mood, gbc_mood);

		final JLabel lblTrack = new JLabel("Track/Of");
		final GridBagConstraints gbc_lblTrack = new GridBagConstraints();
		gbc_lblTrack.anchor = GridBagConstraints.EAST;
		gbc_lblTrack.insets = new Insets(0, 0, 5, 5);
		gbc_lblTrack.gridx = 1;
		gbc_lblTrack.gridy = 9;
		this.getContentPane().add(lblTrack, gbc_lblTrack);

		this.track = new JTextField();
		this.track.setColumns(10);
		final GridBagConstraints gbc_track = new GridBagConstraints();
		gbc_track.fill = GridBagConstraints.HORIZONTAL;
		gbc_track.anchor = GridBagConstraints.EAST;
		gbc_track.insets = new Insets(0, 0, 5, 5);
		gbc_track.gridx = 2;
		gbc_track.gridy = 9;
		this.getContentPane().add(this.track, gbc_track);

		this.trackCount = new JTextField();
		this.trackCount.setColumns(10);
		final GridBagConstraints gbc_trackCount = new GridBagConstraints();
		gbc_trackCount.fill = GridBagConstraints.HORIZONTAL;
		gbc_trackCount.anchor = GridBagConstraints.EAST;
		gbc_trackCount.insets = new Insets(0, 0, 5, 5);
		gbc_trackCount.gridx = 3;
		gbc_trackCount.gridy = 9;
		this.getContentPane().add(this.trackCount, gbc_trackCount);
		btnNewButton.setIcon(Media.ICON_OK);
		final GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.anchor = GridBagConstraints.EAST;
		gbc_btnNewButton.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 10;
		this.getContentPane().add(btnNewButton, gbc_btnNewButton);

		final JButton btnNewButton_1 = new JButton("Cancel");
		btnNewButton_1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				InfoFrame.this.setVisible(false);
			}
		});
		btnNewButton_1.setIcon(Media.ICON_CANCEL);
		final GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton_1.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton_1.anchor = GridBagConstraints.EAST;
		gbc_btnNewButton_1.gridx = 3;
		gbc_btnNewButton_1.gridy = 10;
		this.getContentPane().add(btnNewButton_1, gbc_btnNewButton_1);

		this.btnChoseImage = new JButton("Change Artwork");
		this.btnChoseImage.setIcon(Media.ICON_SEARCH);
		this.btnChoseImage.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();
				fc.setDialogTitle("Chose Artwork Image");
				fc.setApproveButtonText("Set Artwork");
				final String path = ConfigManager.getInstance().getConfig(
						ConfigKey.DIR_IMAGES, (new File("")).getAbsolutePath());
				fc.setCurrentDirectory(new File(path));
				fc.setMultiSelectionEnabled(false);

				final int res = fc.showOpenDialog(null);
				if (res == JFileChooser.APPROVE_OPTION) {
					final File f = fc.getSelectedFile();
					ConfigManager.getInstance().setConfig(ConfigKey.DIR_IMAGES,
							f.getParentFile().getAbsolutePath());
					InfoFrame.this.reloadCoverImage(f);
				}
			}

		});
		final GridBagConstraints gbc_btnChoseImage = new GridBagConstraints();
		gbc_btnChoseImage.insets = new Insets(0, 0, 0, 5);
		gbc_btnChoseImage.gridx = 4;
		gbc_btnChoseImage.gridy = 10;
		this.getContentPane().add(this.btnChoseImage, gbc_btnChoseImage);
	}

	private BufferedImage tempImage;
	private final JButton btnRestoreArtwork;

	private void reloadCoverImage(final File f) {
		try {
			this.tempImage = this.coverPanel.getImage();
			this.coverPanel.setImage(ImageIO.read(f));
			this.btnRestoreArtwork.setEnabled(true);
		} catch (final IOException e) {
			Logging.log("failed to load new artwork from file", e);
		}
	}

	public InfoFrame(final QueueEntry e) {
		this();
		this.e = e;
		this.tagEdit = new TagEditor(
				e.getConvertTempFile() != null ? e.getConvertTempFile()
						: e.getDownloadTempFile(), e);
		this.fillData(e);
	}

	public InfoFrame(final QueueEntry e, final GracenoteMetadata d) {
		this();
		this.e = e;
		this.tagEdit = new TagEditor(
				e.getConvertTempFile() != null ? e.getConvertTempFile()
						: e.getDownloadTempFile(), e);
		this.fillData(d);
	}

	public void addActionListener(final ActionListener a) {
		this.listeners.add(a);
	}

	public void removeActionListener(final ActionListener a) {
		this.listeners.remove(a);
	}

	private int sum(final int[] array) {
		int sum = 0;
		for (final int j : array) {
			sum += j;
		}
		return sum + 10 * (array.length);
	}

	public String getAlbum() {
		return this.album.getText();
	}

	public String getSongtitle() {
		return this.title.getText();
	}

	public String getArtist() {
		return this.artist.getText();
	}

	public String getAlbumArtist() {
		return this.albumartist.getText();
	}

	public String getGenre() {
		return this.genre.getText();
	}

	public String getTrack() {
		return this.track.getText();
	}

	public String getTrackCount() {
		return this.trackCount.getText();
	}

	public String getMood() {
		return this.mood.getText();
	}

	public String getTempo() {
		return this.tempo.getText();
	}

	public String getYear() {
		return this.year.getText();
	}

	public BufferedImage getArtworkImage() {
		return this.coverPanel.getImage();
	}

	public void fillInfo(final String artist2, final String title2,
			final String album2) {
		this.artist.setText(artist2);
		this.title.setText(title2);
		this.album.setText(album2);
	}
}
