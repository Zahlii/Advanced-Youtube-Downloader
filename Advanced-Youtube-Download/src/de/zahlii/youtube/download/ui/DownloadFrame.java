package de.zahlii.youtube.download.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
	private final JTextField downloadLinkInput;
	private final JButton startDownloadBtn;
	private final JLabel lblCurrentStep;
	private final JLabel lblLastStep;
	private final JProgressBar stepProgressBar;
	private final JProgressBar overallProgressBar;
	private final JLabel lblQueue;
	private final JProgressBar queueProgressBar;

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

	private void stepProgress(final double progress) {
		this.progress(this.stepProgressBar, progress);
	}

	private void overallProgress(final double progress) {
		this.progress(this.overallProgressBar, progress);
	}

	private void queueProgress(final double progress) {
		this.progress(this.queueProgressBar, progress);
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

	private final SettingsFrame settings;
	private final JMenuBar menuBar;
	private final JMenu mntmFile;

	/**
	 * Create the frame.
	 */
	public DownloadFrame() {
		this.setTitle("Advanced Youtube Download");
		this.setResizable(false);
		this.setIconImage(Media.ICON_DOWNLOAD.getImage());

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final GridBagLayout gbc_main = new GridBagLayout();
		gbc_main.columnWidths = new int[] { 10, 100, 200, 200, 100, 10 };
		gbc_main.rowHeights = new int[] { 10, 30, 30, 30, 30, 30, 30, 100, 10 };

		this.settings = new SettingsFrame();

		this.getContentPane().setLayout(gbc_main);
		this.setMinimumSize(new Dimension(this.sum(gbc_main.columnWidths), this
				.sum(gbc_main.rowHeights)));

		this.setLocationRelativeTo(null);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent arg0) {
				DownloadFrame.this.cleanUp();

			}

		});

		this.downloadLinkInput = new JTextField("Enter Download Link");
		this.downloadLinkInput.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				DownloadFrame.this.downloadLinkInput.setText("");
			}

			@Override
			public void mouseExited(final MouseEvent e) {
				if (DownloadFrame.this.downloadLinkInput.getText().equals("")) {
					DownloadFrame.this.downloadLinkInput
							.setText("Enter Download Link");
				}
			}
		});
		final GridBagConstraints gbc_downloadLinkInput = new GridBagConstraints();
		gbc_downloadLinkInput.insets = new Insets(0, 0, 5, 5);
		gbc_downloadLinkInput.gridwidth = 3;
		gbc_downloadLinkInput.fill = GridBagConstraints.BOTH;
		gbc_downloadLinkInput.gridx = 1;
		gbc_downloadLinkInput.gridy = 1;
		this.getContentPane()
				.add(this.downloadLinkInput, gbc_downloadLinkInput);
		this.downloadLinkInput.setColumns(10);

		this.startDownloadBtn = new JButton("Start Download");
		this.startDownloadBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				Queue.getInstance().addDownload(
						DownloadFrame.this.downloadLinkInput.getText());
				DownloadFrame.this.downloadLinkInput
						.setText("Enter Download Link");
				DownloadFrame.this.setStage(Stage.WORKING);
			}

		});

		Queue.getInstance().addListener(new ProgressListener() {

			@Override
			public void onEntryBegin(final QueueEntry entry) {
				DownloadFrame.this.setStage(Stage.WORKING);
				DownloadFrame.this.text(
						DownloadFrame.this.lblCurrentStep,
						entry.isDownloadTask() ? "Waiting for download to start..."
								: "Waiting for audio filter to start...");
				if (entry.isDownloadTask()) {
					DownloadFrame.this.text(DownloadFrame.this.lblLastStep,
							"<html>");
				}
				DownloadFrame.this.queueProgress(Queue.getInstance()
						.getQueueProgress());
				DownloadFrame.this.stepProgress(0);
				DownloadFrame.this.overallProgress(0);
			}

			@Override
			public void onEntryStepBegin(final QueueEntry entry, final Step step) {
				DownloadFrame.this.text(DownloadFrame.this.lblCurrentStep,
						"Running Step\""
								+ step.getStepDescriptor().getStepName()
								+ "\"...");
				DownloadFrame.this.stepProgress(0);
			}

			@Override
			public void onEntryStepProgress(final QueueEntry entry,
					final Step step, final double progress) {
				DownloadFrame.this.stepProgress(progress);

			}

			@Override
			public void onEntryStepEnd(final QueueEntry entry, final Step step,
					final long t, final double progress) {
				DownloadFrame.this.stepProgress(1);
				DownloadFrame.this.overallProgress(progress);
				DownloadFrame.this.textNL(DownloadFrame.this.lblLastStep,
						"Step \"" + step.getStepDescriptor().getStepName()
								+ "\" finished: " + step.getStepResults());
			}

			@Override
			public void onEntryEnd(final QueueEntry entry) {
				DownloadFrame.this.text(DownloadFrame.this.lblCurrentStep,
						"Finished.");

				if (!entry.isDownloadTask()) {
					DownloadFrame.this.text(DownloadFrame.this.lblLastStep,
							"<html>");
				}

				DownloadFrame.this.queueProgress(Queue.getInstance()
						.getQueueProgress());
				DownloadFrame.this.stepProgress(0);
				DownloadFrame.this.overallProgress(0);
				DownloadFrame.this.setStage(Stage.IDLE);
			}

		});

		this.startDownloadBtn.setIcon(Media.ICON_DOWNLOAD);
		final GridBagConstraints gbc_startDownloadBtn = new GridBagConstraints();
		gbc_startDownloadBtn.fill = GridBagConstraints.BOTH;
		gbc_startDownloadBtn.insets = new Insets(0, 0, 5, 5);
		gbc_startDownloadBtn.gridx = 4;
		gbc_startDownloadBtn.gridy = 1;
		this.getContentPane().add(this.startDownloadBtn, gbc_startDownloadBtn);

		this.lblQueue = new JLabel("Queue:");
		final GridBagConstraints gbc_lblQueue = new GridBagConstraints();
		gbc_lblQueue.fill = GridBagConstraints.BOTH;
		gbc_lblQueue.insets = new Insets(0, 0, 5, 5);
		gbc_lblQueue.gridx = 1;
		gbc_lblQueue.gridy = 2;
		this.getContentPane().add(this.lblQueue, gbc_lblQueue);

		this.queueProgressBar = new JProgressBar();
		this.queueProgressBar.setStringPainted(true);
		final GridBagConstraints gbc_queueProgressBar = new GridBagConstraints();
		gbc_queueProgressBar.insets = new Insets(0, 0, 5, 5);
		gbc_queueProgressBar.gridwidth = 3;
		gbc_queueProgressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_queueProgressBar.gridx = 2;
		gbc_queueProgressBar.gridy = 2;
		this.getContentPane().add(this.queueProgressBar, gbc_queueProgressBar);

		final JLabel lblOverall = new JLabel("Song:");
		final GridBagConstraints gbc_lblOverall = new GridBagConstraints();
		gbc_lblOverall.insets = new Insets(0, 0, 5, 5);
		gbc_lblOverall.fill = GridBagConstraints.BOTH;
		gbc_lblOverall.gridx = 1;
		gbc_lblOverall.gridy = 3;
		this.getContentPane().add(lblOverall, gbc_lblOverall);

		this.overallProgressBar = new JProgressBar();
		this.overallProgressBar.setStringPainted(true);
		final GridBagConstraints gbc_overallProgressBar = new GridBagConstraints();
		gbc_overallProgressBar.insets = new Insets(0, 0, 5, 5);
		gbc_overallProgressBar.gridwidth = 3;
		gbc_overallProgressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_overallProgressBar.gridx = 2;
		gbc_overallProgressBar.gridy = 3;
		this.getContentPane().add(this.overallProgressBar,
				gbc_overallProgressBar);

		final JLabel lblCurrent = new JLabel("Step:");
		final GridBagConstraints gbc_lblCurrent = new GridBagConstraints();
		gbc_lblCurrent.insets = new Insets(0, 0, 5, 5);
		gbc_lblCurrent.fill = GridBagConstraints.BOTH;
		gbc_lblCurrent.gridx = 1;
		gbc_lblCurrent.gridy = 4;
		this.getContentPane().add(lblCurrent, gbc_lblCurrent);

		this.stepProgressBar = new JProgressBar();
		this.stepProgressBar.setStringPainted(true);
		final GridBagConstraints gbc_stepProgressBar = new GridBagConstraints();
		gbc_stepProgressBar.insets = new Insets(0, 0, 5, 5);
		gbc_stepProgressBar.gridwidth = 3;
		gbc_stepProgressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_stepProgressBar.gridx = 2;
		gbc_stepProgressBar.gridy = 4;
		this.getContentPane().add(this.stepProgressBar, gbc_stepProgressBar);

		this.lblLastStep = new JLabel("<html>");
		this.lblLastStep.setVerticalAlignment(SwingConstants.TOP);
		final GridBagConstraints gbc_lblLastStep = new GridBagConstraints();
		gbc_lblLastStep.fill = GridBagConstraints.BOTH;
		gbc_lblLastStep.gridwidth = 3;
		gbc_lblLastStep.anchor = GridBagConstraints.WEST;
		gbc_lblLastStep.insets = new Insets(0, 0, 5, 5);
		gbc_lblLastStep.gridx = 2;
		gbc_lblLastStep.gridy = 6;
		this.getContentPane().add(this.lblLastStep, gbc_lblLastStep);

		this.lblCurrentStep = new JLabel("");
		final GridBagConstraints gbc_lblCurrentStep = new GridBagConstraints();
		gbc_lblCurrentStep.gridwidth = 3;
		gbc_lblCurrentStep.anchor = GridBagConstraints.WEST;
		gbc_lblCurrentStep.insets = new Insets(0, 0, 5, 5);
		gbc_lblCurrentStep.fill = GridBagConstraints.BOTH;
		gbc_lblCurrentStep.gridx = 2;
		gbc_lblCurrentStep.gridy = 5;
		this.getContentPane().add(this.lblCurrentStep, gbc_lblCurrentStep);

		this.menuBar = new JMenuBar();
		this.setJMenuBar(this.menuBar);

		this.mntmFile = new JMenu("File");
		final JMenuItem item = new JMenuItem("Preferences");
		item.setIcon(Media.ICON_PREF);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				DownloadFrame.this.openSettings();

			}
		});
		this.mntmFile.add(item);
		this.menuBar.add(this.mntmFile);

		this.setVisible(true);

		if (Boolean.valueOf(ConfigManager.getInstance().getConfig(
				ConfigKey.IS_DEFAULT, "true"))) {
			this.openSettings();
		}

	}

	protected void cleanUp() {
		final File[] f = ConfigManager.TEMP_DIR.listFiles();
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
		this.settings.reloadConfig();
		this.settings.setVisible(true);
	}

	public enum Stage {
		WORKING, IDLE
	}

	private void setStage(final Stage s) {
		switch (s) {
		case IDLE:
			this.startDownloadBtn.setEnabled(true);
			this.downloadLinkInput.setEnabled(true);
			break;
		case WORKING:
			this.startDownloadBtn.setEnabled(false);
			this.downloadLinkInput.setEnabled(false);
			break;
		default:
			break;

		}
	}

	private int sum(final int[] array) {
		int sum = 0;
		for (final int j : array) {
			sum += j;
		}
		return sum + 10 * (array.length);
	}

}
