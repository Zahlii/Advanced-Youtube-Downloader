package de.zahlii.youtube.download.ui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.pushingpixels.substance.api.skin.SubstanceGraphiteGlassLookAndFeel;

import de.zahlii.youtube.download.ProgressListener;
import de.zahlii.youtube.download.Queue;
import de.zahlii.youtube.download.QueueEntry;
import de.zahlii.youtube.download.basic.ConfigManager;
import de.zahlii.youtube.download.basic.ConfigManager.ConfigKey;
import de.zahlii.youtube.download.basic.Media;
import de.zahlii.youtube.download.step.Step;

public class DownloadFrame extends JFrame {
	private JTextField downloadLinkInput;
	private JButton startDownloadBtn;
	private JLabel lblCurrentStep;
	private JLabel lblLastStep;
	private JProgressBar stepProgressBar;
	private JProgressBar overallProgressBar;
	private JLabel lblQueue;
	private JProgressBar queueProgressBar;
	private JButton btnSettings;
	
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
			@Override
			public void run() {
				try {
					DownloadFrame frame = new DownloadFrame();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void progress(final JProgressBar p, final double pro) {
		if(SwingUtilities.isEventDispatchThread()) {
			p.setMinimum(0);
			p.setMaximum(1000);
			int v = Math.min(1000, (int)(pro*1000));
			int pr = (int)(v/10.0);
			p.setString(pr +"%");
			p.setValue(v);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					progress(p,pro);
				}
			});
		}
	}
	
	private void stepProgress(double progress) {
		progress(stepProgressBar,progress);
	}
	
	private void overallProgress(double progress) {
		progress(overallProgressBar,progress);
	}
	
	private void queueProgress(double progress) {
		progress(queueProgressBar, progress);
	}
	
	private void text(final JLabel l, final String t) {
		if(SwingUtilities.isEventDispatchThread()) {
			l.setText(t);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					text(l,t);
				}
			});
		}
	}
	
	private void textNL(final JLabel l, final String t) {
		if(SwingUtilities.isEventDispatchThread()) {
			l.setText(l.getText() + "<br>" + t);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					textNL(l,t);
				}
			});
		}
	}

	private SettingsFrame settings;
	
	/**
	 * Create the frame.
	 */
	public DownloadFrame() {
		setTitle("Advanced Youtube Download");
		setResizable(false);
		this.setIconImage(Media.ICON_DOWNLOAD.getImage());
		
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		GridBagLayout gbc_main = new GridBagLayout();
		gbc_main.columnWidths = new int[] {
				10, 100,200,200,100,10
		};
		gbc_main.rowHeights = new int[] {
				10, 30, 30, 30,30, 30, 30, 100,10
		};

		settings = new SettingsFrame();
		
		getContentPane().setLayout(gbc_main);
		setMinimumSize(new Dimension(sum(gbc_main.columnWidths), sum(gbc_main.rowHeights)));
		
		setLocationRelativeTo(null);
		
		btnSettings = new JButton("Settings");
		btnSettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				openSettings();
				
			}
		});
		btnSettings.setIcon(Media.ICON_PREF);
		
		
		
		GridBagConstraints gbc_btnSettings = new GridBagConstraints();
		gbc_btnSettings.fill = GridBagConstraints.BOTH;
		gbc_btnSettings.insets = new Insets(0, 0, 5, 5);
		gbc_btnSettings.gridx = 4;
		gbc_btnSettings.gridy = 1;
		getContentPane().add(btnSettings, gbc_btnSettings);
		
		downloadLinkInput = new JTextField();
		downloadLinkInput.setText("Enter Download Link");
		GridBagConstraints gbc_downloadLinkInput = new GridBagConstraints();
		gbc_downloadLinkInput.insets = new Insets(0, 0, 5, 5);
		gbc_downloadLinkInput.gridwidth = 3;
		gbc_downloadLinkInput.fill = GridBagConstraints.BOTH;
		gbc_downloadLinkInput.gridx = 1;
		gbc_downloadLinkInput.gridy = 2;
		getContentPane().add(downloadLinkInput, gbc_downloadLinkInput);
		downloadLinkInput.setColumns(10);
		
		startDownloadBtn = new JButton("Start Download");
		startDownloadBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Queue.getInstance().addDownload(downloadLinkInput.getText());
				downloadLinkInput.setText("Enter Download Link");
				setStage(Stage.WORKING);
			}
				
		});
		
		Queue.getInstance().addListener(new ProgressListener() {

			@Override
			public void onEntryBegin(QueueEntry entry) {
				setStage(Stage.WORKING);
				text(lblCurrentStep,entry.isDownloadTask() ? "Waiting for download to start..." : "Waiting for audio filter to start...");
				if(entry.isDownloadTask())
					text(lblLastStep,"<html>");
				queueProgress(Queue.getInstance().getQueueProgress());
				stepProgress(0);
				overallProgress(0);
			}

			@Override
			public void onEntryStepBegin(QueueEntry entry, Step step) {
				text(lblCurrentStep,"Running Step\"" + step.getStepDescriptor().getStepName() + "\"...");
				stepProgress(0);
			}

			@Override
			public void onEntryStepProgress(QueueEntry entry,
					Step step, double progress) {
				stepProgress(progress);
				
			}
			

			@Override
			public void onEntryStepEnd(QueueEntry entry, Step step,
					long t, double progress) {
				stepProgress(1);
				overallProgress(progress);
				textNL(lblLastStep,"Step \"" + step.getStepDescriptor().getStepName() +"\" finished: " + step.getStepResults());				
			}

			@Override
			public void onEntryEnd(QueueEntry entry) {
				text(lblCurrentStep,"Finished.");
				
				if(!entry.isDownloadTask())
					text(lblLastStep,"<html>");
				
				queueProgress(Queue.getInstance().getQueueProgress());
				stepProgress(0);
				overallProgress(0);
				setStage(Stage.IDLE);
			}
			
		});
		
	
		startDownloadBtn.setIcon(Media.ICON_DOWNLOAD);
		GridBagConstraints gbc_startDownloadBtn = new GridBagConstraints();
		gbc_startDownloadBtn.fill = GridBagConstraints.BOTH;
		gbc_startDownloadBtn.insets = new Insets(0, 0, 5, 5);
		gbc_startDownloadBtn.gridx = 4;
		gbc_startDownloadBtn.gridy = 2;
		getContentPane().add(startDownloadBtn, gbc_startDownloadBtn);
		
		lblQueue = new JLabel("Queue:");
		GridBagConstraints gbc_lblQueue = new GridBagConstraints();
		gbc_lblQueue.fill = GridBagConstraints.BOTH;
		gbc_lblQueue.insets = new Insets(0, 0, 5, 5);
		gbc_lblQueue.gridx = 1;
		gbc_lblQueue.gridy = 3;
		getContentPane().add(lblQueue, gbc_lblQueue);
		
		queueProgressBar = new JProgressBar();
		queueProgressBar.setStringPainted(true);
		GridBagConstraints gbc_queueProgressBar = new GridBagConstraints();
		gbc_queueProgressBar.insets = new Insets(0, 0, 5, 5);
		gbc_queueProgressBar.gridwidth = 3;
		gbc_queueProgressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_queueProgressBar.gridx = 2;
		gbc_queueProgressBar.gridy = 3;
		getContentPane().add(queueProgressBar, gbc_queueProgressBar);
		
		JLabel lblOverall = new JLabel("Song:");
		GridBagConstraints gbc_lblOverall = new GridBagConstraints();
		gbc_lblOverall.insets = new Insets(0, 0, 5, 5);
		gbc_lblOverall.fill = GridBagConstraints.BOTH;
		gbc_lblOverall.gridx = 1;
		gbc_lblOverall.gridy = 4;
		getContentPane().add(lblOverall, gbc_lblOverall);
		
		overallProgressBar = new JProgressBar();
		overallProgressBar.setStringPainted(true);
		GridBagConstraints gbc_overallProgressBar = new GridBagConstraints();
		gbc_overallProgressBar.insets = new Insets(0, 0, 5, 5);
		gbc_overallProgressBar.gridwidth = 3;
		gbc_overallProgressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_overallProgressBar.gridx = 2;
		gbc_overallProgressBar.gridy = 4;
		getContentPane().add(overallProgressBar, gbc_overallProgressBar);
		
		JLabel lblCurrent = new JLabel("Step:");
		GridBagConstraints gbc_lblCurrent = new GridBagConstraints();
		gbc_lblCurrent.insets = new Insets(0, 0, 5, 5);
		gbc_lblCurrent.fill = GridBagConstraints.BOTH;
		gbc_lblCurrent.gridx = 1;
		gbc_lblCurrent.gridy = 5;
		getContentPane().add(lblCurrent, gbc_lblCurrent);
		
		stepProgressBar = new JProgressBar();
		stepProgressBar.setStringPainted(true);
		GridBagConstraints gbc_stepProgressBar = new GridBagConstraints();
		gbc_stepProgressBar.insets = new Insets(0, 0, 5, 5);
		gbc_stepProgressBar.gridwidth = 3;
		gbc_stepProgressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_stepProgressBar.gridx = 2;
		gbc_stepProgressBar.gridy = 5;
		getContentPane().add(stepProgressBar, gbc_stepProgressBar);
		
		lblLastStep = new JLabel("<html>");
		lblLastStep.setVerticalAlignment(SwingConstants.TOP);
		GridBagConstraints gbc_lblLastStep = new GridBagConstraints();
		gbc_lblLastStep.fill = GridBagConstraints.BOTH;
		gbc_lblLastStep.gridwidth = 3;
		gbc_lblLastStep.anchor = GridBagConstraints.WEST;
		gbc_lblLastStep.insets = new Insets(0, 0, 5, 5);
		gbc_lblLastStep.gridx = 2;
		gbc_lblLastStep.gridy = 7;
		getContentPane().add(lblLastStep, gbc_lblLastStep);
		
		lblCurrentStep = new JLabel("");
		GridBagConstraints gbc_lblCurrentStep = new GridBagConstraints();
		gbc_lblCurrentStep.gridwidth = 3;
		gbc_lblCurrentStep.anchor = GridBagConstraints.WEST;
		gbc_lblCurrentStep.insets = new Insets(0, 0, 5, 5);
		gbc_lblCurrentStep.fill = GridBagConstraints.BOTH;
		gbc_lblCurrentStep.gridx = 2;
		gbc_lblCurrentStep.gridy = 6;
		getContentPane().add(lblCurrentStep, gbc_lblCurrentStep);
		
		setVisible(true);
		
		if(Boolean.valueOf(ConfigManager.getInstance().getConfig(ConfigKey.IS_DEFAULT, "true"))) {
			openSettings();
		}

	}
	
	protected void openSettings() {
		settings.reloadConfig();
		settings.setVisible(true);
	}

	public enum Stage {
		WORKING, IDLE
	}
	
	private void setStage(Stage s) {
		switch(s) {
		case IDLE:
			startDownloadBtn.setEnabled(true);
			downloadLinkInput.setEnabled(true);
			break;
		case WORKING:
			startDownloadBtn.setEnabled(false);
			downloadLinkInput.setEnabled(false);
			break;
		default:
			break;
		
		}
	}
	
	
	private int sum(int[] array) {
		int sum = 0;
		for (int j : array)
			sum += j;
		return sum + 10 * (array.length);
	}

}
