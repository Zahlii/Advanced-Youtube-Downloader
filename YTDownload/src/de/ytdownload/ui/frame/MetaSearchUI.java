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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Stack;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jaudiotagger.tag.FieldKey;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteGlassLookAndFeel;

import radams.gracenote.webapi.GracenoteMetadata;
import de.ytdownload.controller.ConfigManager;
import de.ytdownload.controller.ConfigManager.ConfigKey;
import de.ytdownload.controller.DownloadManager;
import de.ytdownload.controller.DownloadProgressListener;
import de.ytdownload.model.SilenceInfo;
import de.ytdownload.controller.SearchManager;
import de.ytdownload.controller.SongManager;
import de.ytdownload.model.DownloadInfo;
import de.ytdownload.model.Song;
import de.ytdownload.model.VideoInfo;
import de.ytdownload.ui.Media;
import de.ytdownload.ui.songtable.SearchField;
import de.ytdownload.ui.songtable.SongTable;
import de.ytdownload.ui.songtable.SongTableModel;

/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 * 
 */
public class MetaSearchUI extends JFrame {
	private JTextField txtInputDownload;
	private JButton btnAddLink;
	final JButton btnTargetDir;
	private JLabel lblCurAction;
	private JLabel lblCurrentProgress;
	private JProgressBar progressBar;
	private JButton btnStart;
	private JLabel progressTotal;
	private JProgressBar progressSingle;
	private JLabel lblCurrentDir;
	private JList<String> listDownloadTargets;
	private SongTable songTable;

	private Stack<DownloadManager> downloads;
	private int totalDownloads;
	private int downloadDone;
	private JScrollPane paneSongs;
	private JTextField searchField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(new SubstanceGraphiteGlassLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MetaSearchUI window = new MetaSearchUI();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MetaSearchUI() {
		super("Youtube Download and Meta Search");
		setIconImage(Media.ICON_YTDL.getImage());
		setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);

		downloads = new Stack<DownloadManager>();

		GridBagLayout gbc_main = new GridBagLayout();
		gbc_main.columnWidths = new int[] {
				10, 20, 250, 200, 10
		};
		gbc_main.rowHeights = new int[] {
				10, 20, 20, 20, 20, 30, 20, 150, 20, 20, 150, 10
		};
		gbc_main.columnWeights = new double[] {
				0.0, 0.0, 1.0, 0.0, 0.0
		};
		gbc_main.rowWeights = new double[] {
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
		};
		getContentPane().setLayout(gbc_main);
		setMinimumSize(new Dimension(sum(gbc_main.columnWidths), sum(gbc_main.rowHeights)));
		setLocationRelativeTo(null);

		progressTotal = new JLabel("Total Progress");
		GridBagConstraints gbc_progressTotal = new GridBagConstraints();
		gbc_progressTotal.insets = new Insets(0, 0, 5, 5);
		gbc_progressTotal.anchor = GridBagConstraints.WEST;

		gbc_progressTotal.gridx = 1;
		gbc_progressTotal.gridy = 1;
		gbc_progressTotal.gridwidth = 2;
		getContentPane().add(progressTotal, gbc_progressTotal);

		btnStart = new JButton("Start Download");
		btnStart.setEnabled(false);
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultListModel<String> model = ((DefaultListModel<String>) listDownloadTargets
						.getModel());
				int l = model.getSize();
				for (int i = 0; i < l; i++) {
					String target = model.get(i);
					DownloadManager d = new DownloadManager(target);
					downloads.push(d);
				}
				totalDownloads = l;

				model.removeAllElements();

				startQueueLoading();

			}

		});

		btnStart.setIcon(Media.ICON_DOWNLOAD);
		GridBagConstraints gbc_btnStart = new GridBagConstraints();
		gbc_btnStart.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnStart.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnStart.insets = new Insets(0, 0, 5, 5);
		gbc_btnStart.gridx = 3;
		gbc_btnStart.gridy = 1;
		getContentPane().add(btnStart, gbc_btnStart);

		progressBar = new JProgressBar();
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.insets = new Insets(0, 0, 5, 5);
		gbc_progressBar.gridwidth = 2;
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;

		gbc_progressBar.gridx = 2;
		gbc_progressBar.gridy = 2;
		getContentPane().add(progressBar, gbc_progressBar);

		lblCurrentProgress = new JLabel("Current Progress");
		GridBagConstraints gbc_lblCurrentProgress = new GridBagConstraints();
		gbc_lblCurrentProgress.insets = new Insets(0, 0, 5, 5);
		gbc_lblCurrentProgress.anchor = GridBagConstraints.WEST;
		gbc_lblCurrentProgress.gridwidth = 2;

		gbc_lblCurrentProgress.gridx = 1;
		gbc_lblCurrentProgress.gridy = 3;
		getContentPane().add(lblCurrentProgress, gbc_lblCurrentProgress);

		lblCurAction = new JLabel("");
		GridBagConstraints gbc_lblCurAction = new GridBagConstraints();
		gbc_lblCurAction.gridwidth = 2;
		gbc_lblCurAction.anchor = GridBagConstraints.EAST;
		gbc_lblCurAction.insets = new Insets(0, 0, 5, 5);
		gbc_lblCurAction.gridx = 2;
		gbc_lblCurAction.gridy = 4;
		getContentPane().add(lblCurAction, gbc_lblCurAction);

		progressSingle = new JProgressBar();
		progressSingle.setMinimum(0);
		progressSingle.setStringPainted(true);
		progressSingle.setMaximum(100);
		GridBagConstraints gbc_progressSingle = new GridBagConstraints();
		gbc_progressSingle.insets = new Insets(0, 0, 5, 5);
		gbc_progressSingle.gridwidth = 2;
		gbc_progressSingle.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressSingle.gridx = 2;
		gbc_progressSingle.gridy = 5;
		getContentPane().add(progressSingle, gbc_progressSingle);

		lblCurrentDir = new JLabel(SongManager.getInstance().getWorkingDirectory()
				.getAbsolutePath());
		GridBagConstraints gbc_lblCurrentDir = new GridBagConstraints();
		gbc_lblCurrentDir.insets = new Insets(0, 0, 5, 5);
		gbc_lblCurrentDir.anchor = GridBagConstraints.WEST;
		gbc_lblCurrentDir.gridx = 2;
		gbc_lblCurrentDir.gridy = 6;
		getContentPane().add(lblCurrentDir, gbc_lblCurrentDir);

		btnTargetDir = new JButton("Set Target Dir");
		btnTargetDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chose = new JFileChooser(
						"Set the target directory for music downloads");
				chose.setCurrentDirectory(new File(ConfigManager.getInstance().getConfig(
						ConfigKey.DIR_TARGET)));
				chose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int res = chose.showDialog(btnTargetDir.getParent(), "change download directory");
				if (res == JFileChooser.APPROVE_OPTION) {
					ConfigManager.getInstance().setConfig(ConfigKey.DIR_TARGET,
							chose.getSelectedFile().getAbsolutePath());
					SongManager.getInstance().setWorkingDirectory(chose.getSelectedFile());
					((SongTableModel) songTable.getModel()).update();
					lblCurrentDir.setText(chose.getSelectedFile().getAbsolutePath());
				}
			}
		});
		btnTargetDir.setIcon(Media.ICON_MUSIC);
		GridBagConstraints gbc_btnTargetDir = new GridBagConstraints();
		gbc_btnTargetDir.insets = new Insets(0, 0, 5, 5);
		gbc_btnTargetDir.anchor = GridBagConstraints.EAST;
		gbc_btnTargetDir.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnTargetDir.gridx = 3;
		gbc_btnTargetDir.gridy = 6;
		getContentPane().add(btnTargetDir, gbc_btnTargetDir);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.gridwidth = 2;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 2;
		gbc_scrollPane.gridy = 7;
		getContentPane().add(scrollPane, gbc_scrollPane);

		listDownloadTargets = new JList<String>();
		scrollPane.setViewportView(listDownloadTargets);
		listDownloadTargets.setModel(new DefaultListModel<String>());

		txtInputDownload = new JTextField();
		btnAddLink = new JButton("Add Link");
		btnAddLink.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String link = txtInputDownload.getText();
				if (link.equals(""))
					return;
				if (!link.contains("http"))
					link = "http://www.youtube.com/watch?v=" + link;

				((DefaultListModel<String>) (listDownloadTargets.getModel())).addElement(link);
				btnStart.setEnabled(true);
			}
		});

		GridBagConstraints gbc_txtInputDownload = new GridBagConstraints();
		gbc_txtInputDownload.insets = new Insets(0, 0, 5, 5);
		gbc_txtInputDownload.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtInputDownload.gridx = 2;
		gbc_txtInputDownload.gridy = 8;
		getContentPane().add(txtInputDownload, gbc_txtInputDownload);
		txtInputDownload.setColumns(10);
		btnAddLink.setIcon(Media.ICON_ADD);
		GridBagConstraints gbc_btnAddLink = new GridBagConstraints();
		gbc_btnAddLink.insets = new Insets(0, 0, 5, 5);
		gbc_btnAddLink.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAddLink.anchor = GridBagConstraints.EAST;
		gbc_btnAddLink.gridx = 3;
		gbc_btnAddLink.gridy = 8;
		getContentPane().add(btnAddLink, gbc_btnAddLink);

		searchField = new SearchField();
		GridBagConstraints gbc_searchField = new GridBagConstraints();
		gbc_searchField.fill = GridBagConstraints.HORIZONTAL;
		gbc_searchField.anchor = GridBagConstraints.EAST;
		gbc_searchField.insets = new Insets(0, 0, 5, 5);
		gbc_searchField.gridx = 3;
		gbc_searchField.gridy = 9;
		getContentPane().add(searchField, gbc_searchField);
		searchField.setColumns(10);

		paneSongs = new JScrollPane();
		songTable = new SongTable(this);
		paneSongs.setViewportView(songTable);
		GridBagConstraints gbc_paneSongs = new GridBagConstraints();
		gbc_paneSongs.gridwidth = 2;
		gbc_paneSongs.insets = new Insets(0, 0, 5, 5);
		gbc_paneSongs.fill = GridBagConstraints.BOTH;
		gbc_paneSongs.gridx = 2;
		gbc_paneSongs.gridy = 10;
		getContentPane().add(paneSongs, gbc_paneSongs);

		searchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String text = searchField.getText();
				songTable.filterSongs(text);
			}
		});
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);

	}

	private void setStage(String text) {
		text = text == null ? "" : text;
		lblCurrentProgress.setText(!text.equals("") ? "Current Progress [" + text + "]"
				: "Current Progress");
	}

	private void startQueueLoading() {
		if (downloads.empty()) {
			resetProgress();
			enableManageButtons(true);
			lblCurAction.setText("");
			return;
		}
		enableManageButtons(false);
		DownloadManager x = downloads.pop();
		x.addListener(new DownloadProgressListener() {

			@Override
			public void infoStart(String video) {
				setStage("Getting video info from website.");
				lblCurAction.setText(video);

			}

			@Override
			public void infoMetaComplete(VideoInfo f) {
				setStage("Extracted video info.");
				lblCurAction.setText(f.getTitle());
			}

			@Override
			public void infoImageComplete(VideoInfo f) {
				setStage("Thumb Saved");
			}

			@Override
			public void infoComplete(VideoInfo f) {

			}

			@Override
			public void downloadStart(VideoInfo f) {
				setStage("Downloading");
			}

			@Override
			public void downloadProgress(VideoInfo f, DownloadInfo i) {
				updateSingleBar(i);
			}

			@Override
			public void downloadComplete(VideoInfo f) {
				setStage("Downloading complete. Running silencedetect.");
			}

			@Override
			public void convertComplete(VideoInfo f) {
				setStage("Converting complete. Searching for meta-info on Gracenote.");
				handleMetaSearch(f);
			}

			@Override
			public void complete(VideoInfo v, double totalTime) {
				downloadDone++;
				setStage(null);
				updateTotalBar();
				startQueueLoading();
			}

			@Override
			public void metaSearchComplete(VideoInfo v, GracenoteMetadata d) {
				lblCurAction.setText(d.getArtist() + "-" + d.getTitle());
			}

			@Override
			public void silenceComplete(VideoInfo f, List<SilenceInfo> l) {
				String s = "";
				for(SilenceInfo i : l)
					s = s + i.toString();
				
				setStage("Silence detection completed. Results: ("+s+"). Getting info to adjust volume.");
			}

			@Override
			public void volumeComplete(VideoInfo vinfo, double volume) {
				setStage("Volume info completed, volume has to be set to "+volume+"dB. Converting.");
			}

		});
		x.start();
	}

	public void handleMetaSearch(Song s) {
		VideoInfo f = new VideoInfo();
		f.setTitle(s.getTag().getFirst(FieldKey.ARTIST) + "-" + s.getTag().getFirst(FieldKey.TITLE));
		
		if(f.getTitle().equals("-"))
			f.setTitle(s.getName());
		
		f.setMp3File(s.getFile());
		handleMetaSearch(f);
	}

	/**
	 * TODO ADD METHOD DESCRIPTION
	 * 
	 * @author jfruehau
	 * 
	 * @param f
	 */
	public void handleMetaSearch(final VideoInfo f) {
		final String[] parts = f.getTitle().split("-");
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				final SearchFrame search = parts.length > 1 ? new SearchFrame(parts[0], parts[1])
						: (parts.length > 0 ? new SearchFrame(parts[0], "") : new SearchFrame("",""));
				;
				search.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(e == null) {
							handleMetaSearch(f);
							return;
						}

						final Song s = new Song(f.getMp3File());

						final GracenoteMetadata d = SearchManager.getInstance().searchForSong(
								search.getArtist(), search.getAlbum(), search.getSongtitle());

						s.applyData(d);

						final InfoFrame f = new InfoFrame(s);
						f.setVisible(true);
						f.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								if (e != null)
									SongManager.getInstance().safeFile(s, f);

							}

						});

					}
				});
				search.setVisible(true);

			}

		});

	}

	private void updateSingleBar(final DownloadInfo i) {
		if (SwingUtilities.isEventDispatchThread()) {
			progressSingle.setValue((int) (i.progress));
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					updateSingleBar(i);
				}
			});
		}

	}

	private void updateTotalBar() {

		if (SwingUtilities.isEventDispatchThread()) {
			progressBar.setMinimum(0);
			progressBar.setMaximum(totalDownloads);
			progressBar.setValue(downloadDone);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					updateTotalBar();
				}
			});
		}

	}

	private void enableManageButtons(boolean enabled) {
		txtInputDownload.setEnabled(enabled);
		btnAddLink.setEnabled(enabled);
		btnTargetDir.setEnabled(enabled);
		btnStart.setEnabled(enabled);
		listDownloadTargets.setEnabled(enabled);
	}

	private void resetProgress() {
		if (SwingUtilities.isEventDispatchThread()) {
			progressBar.setValue(0);
			progressSingle.setValue(0);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					resetProgress();
				}
			});
		}

	}

	private int sum(int[] array) {
		int sum = 0;
		for (int j : array)
			sum += j;
		return sum + 10 * (array.length);
	}

}
