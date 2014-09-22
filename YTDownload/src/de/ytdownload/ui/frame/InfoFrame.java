/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 */
package de.ytdownload.ui.frame;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.io.FilenameUtils;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.StandardArtwork;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteGlassLookAndFeel;

import de.ytdownload.model.Song;
import de.ytdownload.service.Logging;
import de.ytdownload.ui.CoverPanel;
import de.ytdownload.ui.Media;

/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 * 
 */
public class InfoFrame extends JFrame {
	private JTextField artist;
	private JTextField title;
	private JTextField album;
	private List<ActionListener> listeners;
	private Song s;
	private JTextField year;
	private JTextField track;
	private JTextField trackCount;
	private JTextField albumartist;
	private JTextField genre;
	private JTextField tempo;
	private JTextField mood;
	private CoverPanel coverPanel;

	public InfoFrame(Song s) {
		super("Edit Audio Information");
		this.setIconImage(Media.ICON_YTDL.getImage());
		this.s = s;
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				for (ActionListener i : listeners) {
					i.actionPerformed(null);
				}
				setVisible(false);
			}
		});

		listeners = new ArrayList<ActionListener>();

		GridBagLayout gbc_main = new GridBagLayout();
		gbc_main.columnWeights = new double[] {
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0
		};
		gbc_main.columnWidths = new int[] {
				10, 100, 100, 100, 270, 10
		};
		gbc_main.rowHeights = new int[] {
				10, 30, 30, 30, 30, 30, 30, 30, 30, 30, 10
		};

		getContentPane().setLayout(gbc_main);
		setMinimumSize(new Dimension(sum(gbc_main.columnWidths), sum(gbc_main.rowHeights)));
		setResizable(false);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setLocationRelativeTo(null);

		JLabel lblNewLabel = new JLabel("Artist");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 1;
		getContentPane().add(lblNewLabel, gbc_lblNewLabel);

		artist = new JTextField(s.getTag().getFirst(FieldKey.ARTIST));
		GridBagConstraints gbc_artist = new GridBagConstraints();
		gbc_artist.gridwidth = 2;
		gbc_artist.insets = new Insets(0, 0, 5, 5);
		gbc_artist.fill = GridBagConstraints.HORIZONTAL;
		gbc_artist.gridx = 2;
		gbc_artist.gridy = 1;
		getContentPane().add(artist, gbc_artist);
		artist.setColumns(10);

		coverPanel = new CoverPanel(s);
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridheight = 9;
		gbc_panel.gridwidth = 2;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 4;
		gbc_panel.gridy = 1;
		getContentPane().add(coverPanel, gbc_panel);

		JLabel lblNewLabel_1 = new JLabel("Title");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 1;
		gbc_lblNewLabel_1.gridy = 2;
		getContentPane().add(lblNewLabel_1, gbc_lblNewLabel_1);

		title = new JTextField(s.getTag().getFirst(FieldKey.TITLE));
		GridBagConstraints gbc_title = new GridBagConstraints();
		gbc_title.gridwidth = 2;
		gbc_title.insets = new Insets(0, 0, 5, 5);
		gbc_title.fill = GridBagConstraints.HORIZONTAL;
		gbc_title.gridx = 2;
		gbc_title.gridy = 2;
		getContentPane().add(title, gbc_title);
		title.setColumns(10);

		JLabel lblNewLabel_2 = new JLabel("Album");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 1;
		gbc_lblNewLabel_2.gridy = 3;
		getContentPane().add(lblNewLabel_2, gbc_lblNewLabel_2);

		album = new JTextField(s.getTag().getFirst(FieldKey.ALBUM));
		GridBagConstraints gbc_album = new GridBagConstraints();
		gbc_album.gridwidth = 2;
		gbc_album.insets = new Insets(0, 0, 5, 5);
		gbc_album.fill = GridBagConstraints.HORIZONTAL;
		gbc_album.gridx = 2;
		gbc_album.gridy = 3;
		getContentPane().add(album, gbc_album);
		album.setColumns(10);

		JLabel lblYear = new JLabel("Year");
		GridBagConstraints gbc_lblYear = new GridBagConstraints();
		gbc_lblYear.anchor = GridBagConstraints.EAST;
		gbc_lblYear.insets = new Insets(0, 0, 5, 5);
		gbc_lblYear.gridx = 1;
		gbc_lblYear.gridy = 4;
		getContentPane().add(lblYear, gbc_lblYear);

		year = new JTextField(s.getTag().getFirst(FieldKey.YEAR));
		year.setColumns(10);
		GridBagConstraints gbc_year = new GridBagConstraints();
		gbc_year.gridwidth = 2;
		gbc_year.insets = new Insets(0, 0, 5, 5);
		gbc_year.fill = GridBagConstraints.HORIZONTAL;
		gbc_year.gridx = 2;
		gbc_year.gridy = 4;
		getContentPane().add(year, gbc_year);

		JLabel lblAlbumArtist = new JLabel("Album Artist");
		GridBagConstraints gbc_lblAlbumArtist = new GridBagConstraints();
		gbc_lblAlbumArtist.anchor = GridBagConstraints.EAST;
		gbc_lblAlbumArtist.insets = new Insets(0, 0, 5, 5);
		gbc_lblAlbumArtist.gridx = 1;
		gbc_lblAlbumArtist.gridy = 5;
		getContentPane().add(lblAlbumArtist, gbc_lblAlbumArtist);

		albumartist = new JTextField(s.getTag().getFirst(FieldKey.ALBUM_ARTIST));
		albumartist.setColumns(10);
		GridBagConstraints gbc_albumartist = new GridBagConstraints();
		gbc_albumartist.gridwidth = 2;
		gbc_albumartist.insets = new Insets(0, 0, 5, 5);
		gbc_albumartist.fill = GridBagConstraints.HORIZONTAL;
		gbc_albumartist.gridx = 2;
		gbc_albumartist.gridy = 5;
		getContentPane().add(albumartist, gbc_albumartist);

		JLabel lblTemo = new JLabel("Genre");
		GridBagConstraints gbc_lblTemo = new GridBagConstraints();
		gbc_lblTemo.anchor = GridBagConstraints.EAST;
		gbc_lblTemo.insets = new Insets(0, 0, 5, 5);
		gbc_lblTemo.gridx = 1;
		gbc_lblTemo.gridy = 6;
		getContentPane().add(lblTemo, gbc_lblTemo);

		genre = new JTextField(s.getTag().getFirst(FieldKey.GENRE));
		genre.setColumns(10);
		GridBagConstraints gbc_genre = new GridBagConstraints();
		gbc_genre.gridwidth = 2;
		gbc_genre.insets = new Insets(0, 0, 5, 5);
		gbc_genre.fill = GridBagConstraints.HORIZONTAL;
		gbc_genre.gridx = 2;
		gbc_genre.gridy = 6;
		getContentPane().add(genre, gbc_genre);

		JLabel lblMood = new JLabel("Tempo");
		GridBagConstraints gbc_lblMood = new GridBagConstraints();
		gbc_lblMood.anchor = GridBagConstraints.EAST;
		gbc_lblMood.insets = new Insets(0, 0, 5, 5);
		gbc_lblMood.gridx = 1;
		gbc_lblMood.gridy = 7;
		getContentPane().add(lblMood, gbc_lblMood);

		JButton btnNewButton = new JButton("Save");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				for (ActionListener a : listeners) {
					a.actionPerformed(e);
				}
			}
		});

		tempo = new JTextField(s.getTag().getFirst(FieldKey.TEMPO));
		tempo.setColumns(10);
		GridBagConstraints gbc_tempo = new GridBagConstraints();
		gbc_tempo.gridwidth = 2;
		gbc_tempo.insets = new Insets(0, 0, 5, 5);
		gbc_tempo.fill = GridBagConstraints.HORIZONTAL;
		gbc_tempo.gridx = 2;
		gbc_tempo.gridy = 7;
		getContentPane().add(tempo, gbc_tempo);

		JLabel lblMood_1 = new JLabel("Mood");
		GridBagConstraints gbc_lblMood_1 = new GridBagConstraints();
		gbc_lblMood_1.anchor = GridBagConstraints.EAST;
		gbc_lblMood_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblMood_1.gridx = 1;
		gbc_lblMood_1.gridy = 8;
		getContentPane().add(lblMood_1, gbc_lblMood_1);

		mood = new JTextField(s.getTag().getFirst(FieldKey.MOOD));
		mood.setColumns(10);
		GridBagConstraints gbc_mood = new GridBagConstraints();
		gbc_mood.gridwidth = 2;
		gbc_mood.insets = new Insets(0, 0, 5, 5);
		gbc_mood.fill = GridBagConstraints.HORIZONTAL;
		gbc_mood.gridx = 2;
		gbc_mood.gridy = 8;
		getContentPane().add(mood, gbc_mood);

		JLabel lblTrack = new JLabel("Track/Of");
		GridBagConstraints gbc_lblTrack = new GridBagConstraints();
		gbc_lblTrack.anchor = GridBagConstraints.EAST;
		gbc_lblTrack.insets = new Insets(0, 0, 5, 5);
		gbc_lblTrack.gridx = 1;
		gbc_lblTrack.gridy = 9;
		getContentPane().add(lblTrack, gbc_lblTrack);

		track = new JTextField(s.getTag().getFirst(FieldKey.TRACK));
		track.setColumns(10);
		GridBagConstraints gbc_track = new GridBagConstraints();
		gbc_track.fill = GridBagConstraints.HORIZONTAL;
		gbc_track.anchor = GridBagConstraints.EAST;
		gbc_track.insets = new Insets(0, 0, 5, 5);
		gbc_track.gridx = 2;
		gbc_track.gridy = 9;
		getContentPane().add(track, gbc_track);

		trackCount = new JTextField(s.getTag().getFirst(FieldKey.TRACK_TOTAL));
		trackCount.setColumns(10);
		GridBagConstraints gbc_trackCount = new GridBagConstraints();
		gbc_trackCount.fill = GridBagConstraints.HORIZONTAL;
		gbc_trackCount.anchor = GridBagConstraints.EAST;
		gbc_trackCount.insets = new Insets(0, 0, 5, 5);
		gbc_trackCount.gridx = 3;
		gbc_trackCount.gridy = 9;
		getContentPane().add(trackCount, gbc_trackCount);
		btnNewButton.setIcon(Media.ICON_OK);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.anchor = GridBagConstraints.EAST;
		gbc_btnNewButton.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 10;
		getContentPane().add(btnNewButton, gbc_btnNewButton);

		JButton btnNewButton_1 = new JButton("Cancel");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		btnNewButton_1.setIcon(Media.ICON_CANCEL);
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton_1.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton_1.anchor = GridBagConstraints.EAST;
		gbc_btnNewButton_1.gridx = 3;
		gbc_btnNewButton_1.gridy = 10;
		getContentPane().add(btnNewButton_1, gbc_btnNewButton_1);
	}

	public void addActionListener(ActionListener a) {
		this.listeners.add(a);
	}

	public void removeActionListener(ActionListener a) {
		this.listeners.remove(a);
	}

	private int sum(int[] array) {
		int sum = 0;
		for (int j : array)
			sum += j;
		return sum + 10 * (array.length);
	}

	public String getAlbum() {
		return album.getText();
	}

	public String getSongtitle() {
		return title.getText();
	}

	public String getArtist() {
		return artist.getText();
	}

	public String getAlbumArtist() {
		return albumartist.getText();
	}

	public String getGenre() {
		return genre.getText();
	}

	public String getTrack() {
		return track.getText();
	}

	public String getTrackCount() {
		return trackCount.getText();
	}

	public String getMood() {
		return mood.getText();
	}

	public String getTempo() {
		return tempo.getText();
	}

	public String getYear() {
		return year.getText();
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(new SubstanceGraphiteGlassLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
			Logging.log("failed setting LAF", e);
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				new InfoFrame(
						new Song(
								new File(
										"E:\\Eigene Musik\\ALL_INFOS\\Estas Tonne - The Song of the Golden Dragon.mp3")))
						.setVisible(true);

			}
		});

	}

	public String getNewFileName(Song s2) {

		String main = getArtist() + " - " + getSongtitle();

		main = Media.sanitize(main);

		return main + "." + FilenameUtils.getExtension(s2.getFile().getName());
	}

	/**
	 * TODO ADD METHOD DESCRIPTION
	 * 
	 * @author jfruehau
	 * 
	 * @return
	 */
	public Artwork loadArtWork() {
		if (this.coverPanel.getImageFile() == null)
			return null;

		StandardArtwork art = new StandardArtwork();
		try {
			art.setFromFile(this.coverPanel.getImageFile());
		} catch (IOException e) {
			Logging.log("failed loading artwork", e);
		}
		return art;
	}
}
