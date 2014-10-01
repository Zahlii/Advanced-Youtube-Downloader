package de.zahlii.youtube.download.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import de.zahlii.youtube.download.basic.Media;

public class SearchFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JTextField artist;
	private final JTextField title;
	private final JTextField album;
	private final List<ActionListener> listeners;

	public SearchFrame(final String art, final String tit, final String alb) {
		super("Enter Additional Metadata to Search For");
		setIconImage(Media.ICON_DOWNLOAD.getImage());
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				for (final ActionListener i : listeners) {
					i.actionPerformed(null);
				}
				SearchFrame.this.setVisible(false);
			}
		});

		listeners = new ArrayList<ActionListener>();

		final GridBagLayout gbc_main = new GridBagLayout();
		gbc_main.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0 };
		gbc_main.columnWidths = new int[] { 10, 100, 100, 100, 10 };
		gbc_main.rowHeights = new int[] { 10, 30, 30, 30, 30, 10 };

		getContentPane().setLayout(gbc_main);
		setMinimumSize(new Dimension(sum(gbc_main.columnWidths),
				sum(gbc_main.rowHeights)));
		setResizable(false);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setLocationRelativeTo(null);

		final JLabel lblNewLabel = new JLabel("Artist");
		final GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 1;
		getContentPane().add(lblNewLabel, gbc_lblNewLabel);

		artist = new JTextField(art);
		final GridBagConstraints gbc_artist = new GridBagConstraints();
		gbc_artist.gridwidth = 2;
		gbc_artist.insets = new Insets(0, 0, 5, 5);
		gbc_artist.fill = GridBagConstraints.HORIZONTAL;
		gbc_artist.gridx = 2;
		gbc_artist.gridy = 1;
		getContentPane().add(artist, gbc_artist);
		artist.setColumns(10);

		final JLabel lblNewLabel_1 = new JLabel("Title");
		final GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 1;
		gbc_lblNewLabel_1.gridy = 2;
		getContentPane().add(lblNewLabel_1, gbc_lblNewLabel_1);

		title = new JTextField(tit);
		final GridBagConstraints gbc_title = new GridBagConstraints();
		gbc_title.gridwidth = 2;
		gbc_title.insets = new Insets(0, 0, 5, 5);
		gbc_title.fill = GridBagConstraints.HORIZONTAL;
		gbc_title.gridx = 2;
		gbc_title.gridy = 2;
		getContentPane().add(title, gbc_title);
		title.setColumns(10);

		final JLabel lblNewLabel_2 = new JLabel("Album");
		final GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 1;
		gbc_lblNewLabel_2.gridy = 3;
		getContentPane().add(lblNewLabel_2, gbc_lblNewLabel_2);

		album = new JTextField(alb);
		final GridBagConstraints gbc_album = new GridBagConstraints();
		gbc_album.gridwidth = 2;
		gbc_album.insets = new Insets(0, 0, 5, 5);
		gbc_album.fill = GridBagConstraints.HORIZONTAL;
		gbc_album.gridx = 2;
		gbc_album.gridy = 3;
		getContentPane().add(album, gbc_album);
		album.setColumns(10);

		final JButton btnNewButton = new JButton("Search");
		btnNewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				SearchFrame.this.setVisible(false);
				for (final ActionListener a : listeners) {
					a.actionPerformed(e);
				}
			}
		});
		btnNewButton.setIcon(Media.ICON_SEARCH);
		final GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.anchor = GridBagConstraints.EAST;
		gbc_btnNewButton.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 4;
		getContentPane().add(btnNewButton, gbc_btnNewButton);

		final JButton btnNewButton_1 = new JButton("Skip/Cancel");
		btnNewButton_1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				for (final ActionListener a : listeners) {
					a.actionPerformed(null);
				}
				SearchFrame.this.setVisible(false);
			}
		});
		btnNewButton_1.setIcon(Media.ICON_CANCEL);
		final GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.anchor = GridBagConstraints.EAST;
		gbc_btnNewButton_1.gridx = 3;
		gbc_btnNewButton_1.gridy = 4;
		getContentPane().add(btnNewButton_1, gbc_btnNewButton_1);
	}

	public void addActionListener(final ActionListener a) {
		listeners.add(a);
	}

	public String getAlbum() {
		return album.getText();
	}

	public String getArtist() {
		return artist.getText();
	}

	public String getSongTitle() {
		return title.getText();
	}

	public void removeActionListener(final ActionListener a) {
		listeners.remove(a);
	}

	private int sum(final int[] array) {
		int sum = 0;
		for (final int j : array) {
			sum += j;
		}
		return sum + 10 * array.length;
	}

}
