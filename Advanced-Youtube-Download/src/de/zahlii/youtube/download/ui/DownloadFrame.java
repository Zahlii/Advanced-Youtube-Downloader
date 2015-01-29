package de.zahlii.youtube.download.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;

import de.zahlii.youtube.download.ProgressListener;
import de.zahlii.youtube.download.Queue;
import de.zahlii.youtube.download.QueueEntry;
import de.zahlii.youtube.download.basic.ConfigManager;
import de.zahlii.youtube.download.basic.ConfigManager.ConfigKey;
import de.zahlii.youtube.download.basic.Logging;
import de.zahlii.youtube.download.basic.Media;
import de.zahlii.youtube.download.step.Step;

public class DownloadFrame extends JFrame {
	public enum Stage {
		IDLE, WORKING
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JTextField downloadLinkInput;
	private final JLabel lblCurrentStep;
	private final JLabel lblLastStep;
	private final JLabel lblQueue;
	private final JMenuBar menuBar;
	private final JMenu mntmFile;
	private final JProgressBar overallProgressBar;

	private final JProgressBar queueProgressBar;

	private final SettingsFrame settings;

	private final JButton startDownloadBtn;

	private final JProgressBar stepProgressBar;

	/**
	 * Create the frame.
	 */
	public DownloadFrame() {
		setTitle("Advanced Youtube Download");
		setResizable(false);
		setIconImage(Media.ICON_DOWNLOAD.getImage());

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final GridBagLayout gbc_main = new GridBagLayout();
		gbc_main.columnWidths = new int[] {
				10, 100, 200, 200, 100, 10
		};
		gbc_main.rowHeights = new int[] {
				10, 30, 30, 30, 30, 30, 30, 100, 10
		};

		settings = new SettingsFrame();

		getContentPane().setLayout(gbc_main);
		setMinimumSize(new Dimension(sum(gbc_main.columnWidths), sum(gbc_main.rowHeights)));

		setLocationRelativeTo(null);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent arg0) {
				DownloadFrame.this.cleanUp();

			}

		});

		downloadLinkInput = new JTextField("Enter Download Link");
		downloadLinkInput.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				downloadLinkInput.setText("");
			}

			@Override
			public void mouseExited(final MouseEvent e) {
				if (downloadLinkInput.getText().equals("")) {
					downloadLinkInput.setText("Enter Download Link");
				}
			}
		});
		downloadLinkInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent arg0) {
				final String s = downloadLinkInput.getText();
				final boolean en = !s.equals("Enter Download Link") && !s.equals("");
				startDownloadBtn.setEnabled(en);
			}
		});
		final GridBagConstraints gbc_downloadLinkInput = new GridBagConstraints();
		gbc_downloadLinkInput.insets = new Insets(0, 0, 5, 5);
		gbc_downloadLinkInput.gridwidth = 3;
		gbc_downloadLinkInput.fill = GridBagConstraints.BOTH;
		gbc_downloadLinkInput.gridx = 1;
		gbc_downloadLinkInput.gridy = 1;
		getContentPane().add(downloadLinkInput, gbc_downloadLinkInput);
		downloadLinkInput.setColumns(10);

		startDownloadBtn = new JButton("Start Download");
		startDownloadBtn.setEnabled(false);
		startDownloadBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				Queue.getInstance().addDownload(downloadLinkInput.getText());
				downloadLinkInput.setText("Enter Download Link");
				DownloadFrame.this.setStage(Stage.WORKING);
			}

		});

		Queue.getInstance().addListener(new ProgressListener() {

			@Override
			public void onEntryBegin(final QueueEntry entry) {
				DownloadFrame.this.setStage(Stage.WORKING);
				DownloadFrame.this.text(lblCurrentStep,
						entry.isDownloadTask() ? "Waiting for download to start..."
								: "Waiting for audio filter to start...");
				if (entry.isDownloadTask()) {
					DownloadFrame.this.text(lblLastStep, "<html>");
				}
				DownloadFrame.this.queueProgress(Queue.getInstance().getQueueProgress());
				DownloadFrame.this.stepProgress(0);
				DownloadFrame.this.overallProgress(0);
			}

			@Override
			public void onEntryEnd(final QueueEntry entry) {
				DownloadFrame.this.text(lblCurrentStep, "Finished.");

				if (!entry.isDownloadTask()) {
					DownloadFrame.this.text(lblLastStep, "<html>");
				}

				DownloadFrame.this.queueProgress(Queue.getInstance().getQueueProgress());
				DownloadFrame.this.stepProgress(0);
				DownloadFrame.this.overallProgress(0);
				DownloadFrame.this.setStage(Stage.IDLE);
			}

			@Override
			public void onEntryStepBegin(final QueueEntry entry, final Step step) {
				DownloadFrame.this.text(lblCurrentStep, "Running Step\""
						+ step.getStepDescriptor().getStepName() + "\"...");
				DownloadFrame.this.stepProgress(0);
			}

			@Override
			public void onEntryStepEnd(final QueueEntry entry, final Step step, final long t,
					final double progress) {
				DownloadFrame.this.stepProgress(1);
				DownloadFrame.this.overallProgress(progress);
				DownloadFrame.this.textNL(
						lblLastStep,
						"Step \"" + step.getStepDescriptor().getStepName() + "\" finished: "
								+ step.getStepResults());
			}

			@Override
			public void onEntryStepProgress(final QueueEntry entry, final Step step,
					final double progress) {
				DownloadFrame.this.stepProgress(progress);

			}

		});

		startDownloadBtn.setIcon(Media.ICON_DOWNLOAD);
		final GridBagConstraints gbc_startDownloadBtn = new GridBagConstraints();
		gbc_startDownloadBtn.fill = GridBagConstraints.BOTH;
		gbc_startDownloadBtn.insets = new Insets(0, 0, 5, 5);
		gbc_startDownloadBtn.gridx = 4;
		gbc_startDownloadBtn.gridy = 1;
		getContentPane().add(startDownloadBtn, gbc_startDownloadBtn);

		lblQueue = new JLabel("Queue:");
		final GridBagConstraints gbc_lblQueue = new GridBagConstraints();
		gbc_lblQueue.fill = GridBagConstraints.BOTH;
		gbc_lblQueue.insets = new Insets(0, 0, 5, 5);
		gbc_lblQueue.gridx = 1;
		gbc_lblQueue.gridy = 2;
		getContentPane().add(lblQueue, gbc_lblQueue);

		queueProgressBar = new JProgressBar();
		queueProgressBar.setStringPainted(true);
		final GridBagConstraints gbc_queueProgressBar = new GridBagConstraints();
		gbc_queueProgressBar.insets = new Insets(0, 0, 5, 5);
		gbc_queueProgressBar.gridwidth = 3;
		gbc_queueProgressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_queueProgressBar.gridx = 2;
		gbc_queueProgressBar.gridy = 2;
		getContentPane().add(queueProgressBar, gbc_queueProgressBar);

		final JLabel lblOverall = new JLabel("Song:");
		final GridBagConstraints gbc_lblOverall = new GridBagConstraints();
		gbc_lblOverall.insets = new Insets(0, 0, 5, 5);
		gbc_lblOverall.fill = GridBagConstraints.BOTH;
		gbc_lblOverall.gridx = 1;
		gbc_lblOverall.gridy = 3;
		getContentPane().add(lblOverall, gbc_lblOverall);

		overallProgressBar = new JProgressBar();
		overallProgressBar.setStringPainted(true);
		final GridBagConstraints gbc_overallProgressBar = new GridBagConstraints();
		gbc_overallProgressBar.insets = new Insets(0, 0, 5, 5);
		gbc_overallProgressBar.gridwidth = 3;
		gbc_overallProgressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_overallProgressBar.gridx = 2;
		gbc_overallProgressBar.gridy = 3;
		getContentPane().add(overallProgressBar, gbc_overallProgressBar);

		final JLabel lblCurrent = new JLabel("Step:");
		final GridBagConstraints gbc_lblCurrent = new GridBagConstraints();
		gbc_lblCurrent.insets = new Insets(0, 0, 5, 5);
		gbc_lblCurrent.fill = GridBagConstraints.BOTH;
		gbc_lblCurrent.gridx = 1;
		gbc_lblCurrent.gridy = 4;
		getContentPane().add(lblCurrent, gbc_lblCurrent);

		stepProgressBar = new JProgressBar();
		stepProgressBar.setStringPainted(true);
		final GridBagConstraints gbc_stepProgressBar = new GridBagConstraints();
		gbc_stepProgressBar.insets = new Insets(0, 0, 5, 5);
		gbc_stepProgressBar.gridwidth = 3;
		gbc_stepProgressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_stepProgressBar.gridx = 2;
		gbc_stepProgressBar.gridy = 4;
		getContentPane().add(stepProgressBar, gbc_stepProgressBar);

		lblLastStep = new JLabel("<html>");
		lblLastStep.setVerticalAlignment(SwingConstants.TOP);
		final GridBagConstraints gbc_lblLastStep = new GridBagConstraints();
		gbc_lblLastStep.fill = GridBagConstraints.BOTH;
		gbc_lblLastStep.gridwidth = 3;
		gbc_lblLastStep.anchor = GridBagConstraints.WEST;
		gbc_lblLastStep.insets = new Insets(0, 0, 5, 5);
		gbc_lblLastStep.gridx = 2;
		gbc_lblLastStep.gridy = 6;
		getContentPane().add(lblLastStep, gbc_lblLastStep);

		lblCurrentStep = new JLabel("");
		final GridBagConstraints gbc_lblCurrentStep = new GridBagConstraints();
		gbc_lblCurrentStep.gridwidth = 3;
		gbc_lblCurrentStep.anchor = GridBagConstraints.WEST;
		gbc_lblCurrentStep.insets = new Insets(0, 0, 5, 5);
		gbc_lblCurrentStep.fill = GridBagConstraints.BOTH;
		gbc_lblCurrentStep.gridx = 2;
		gbc_lblCurrentStep.gridy = 5;
		getContentPane().add(lblCurrentStep, gbc_lblCurrentStep);

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mntmFile = new JMenu("File");
		final JMenuItem item = new JMenuItem("Preferences");
		item.setIcon(Media.ICON_PREF);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				DownloadFrame.this.openSettings();

			}
		});
		mntmFile.add(item);
		menuBar.add(mntmFile);

		setVisible(true);

		if (Boolean.valueOf(ConfigManager.getInstance().getConfig(ConfigKey.IS_DEFAULT, "true"))) {
			openSettings();
		}

	}

	private void overallProgress(final double progress) {
		progress(overallProgressBar, progress);
	}

	private void progress(final JProgressBar p, final double pro) {
		if (SwingUtilities.isEventDispatchThread()) {
			p.setMinimum(0);
			p.setMaximum(1000);
			final int v = Math.min(1000, (int) (pro * 1000));
			final int pr = (int) (v / 10.0);
			p.setString(pr + "%");
			p.setValue(v);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					DownloadFrame.this.progress(p, pro);
				}
			});
		}
	}

	private void queueProgress(final double progress) {
		progress(queueProgressBar, progress);
	}

	private void setStage(final Stage s) {
		switch (s) {
		case IDLE:
			downloadLinkInput.setEnabled(true);
			stepProgress(0);
			overallProgress(0);
			queueProgress(0);
			break;
		case WORKING:
			startDownloadBtn.setEnabled(false);
			downloadLinkInput.setEnabled(false);
			break;
		default:
			break;

		}
	}

	private void stepProgress(final double progress) {
		progress(stepProgressBar, progress);
	}

	private int sum(final int[] array) {
		int sum = 0;
		for (final int j : array) {
			sum += j;
		}
		return sum + 10 * array.length;
	}

	private void text(final JLabel l, final String t) {
		if (SwingUtilities.isEventDispatchThread()) {
			l.setText(t);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					DownloadFrame.this.text(l, t);
				}
			});
		}
	}

	private void textNL(final JLabel l, final String t) {
		if (SwingUtilities.isEventDispatchThread()) {
			l.setText(l.getText() + "<br>" + t);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					DownloadFrame.this.textNL(l, t);
				}
			});
		}
	}

	protected void cleanUp() {
		final File[] f = ConfigManager.TEMP_DIR.listFiles();
		if (f == null)
			return;

		for (final File ftodelete : f) {
			FileUtils.deleteQuietly(ftodelete);
		}
		try {
			Runtime.getRuntime().exec("taskkill /F /IM ffmpeg.exe");
		} catch (final IOException e) {
			Logging.log("failed to run taskkilL", e);
		}
	}

	protected void openSettings() {
		settings.reloadConfig();
		settings.setVisible(true);
	}

}
