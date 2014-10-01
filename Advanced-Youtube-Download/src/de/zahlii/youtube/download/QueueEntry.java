package de.zahlii.youtube.download;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;

import de.zahlii.youtube.download.basic.ConfigManager;
import de.zahlii.youtube.download.basic.ConfigManager.ConfigKey;
import de.zahlii.youtube.download.basic.Logging;
import de.zahlii.youtube.download.step.Step;
import de.zahlii.youtube.download.step.StepConvert;
import de.zahlii.youtube.download.step.StepDownload;
import de.zahlii.youtube.download.step.StepListener;
import de.zahlii.youtube.download.step.StepMetaSearch;
import de.zahlii.youtube.download.step.StepRelocate;
import de.zahlii.youtube.download.step.StepReplayGain;
import de.zahlii.youtube.download.step.StepSilenceDetect;
import de.zahlii.youtube.download.step.StepVolumeAdjust;

/**
 * Represents one entry in the queue. This could either be a downloading action or a converting/improving action of an existing file.
 * 
 * @author Zahlii
 * 
 */
public class QueueEntry extends Thread {

	private static boolean enableGracenote;

	private static boolean enableSilence;

	private static boolean enableVolume;

	public static void setEnableGracenote(final boolean enableGracenote) {
		QueueEntry.enableGracenote = enableGracenote;
	}

	public static void setEnableSilence(final boolean enableSilence) {
		QueueEntry.enableSilence = enableSilence;
	}

	public static void setEnableVolume(final boolean enableVolume) {
		QueueEntry.enableVolume = enableVolume;
	}

	private File downloadTempFile;
	private File finalMP3File;
	private boolean isDownloadTask = true;

	private final ArrayList<ProgressListener> progressListeners = new ArrayList<>();
	private Step sold;

	private final HashMap<String, Object> stepInfo = new HashMap<>();

	private final LinkedList<Step> stepsToComplete = new LinkedList<>();
	private long told;
	private int totalSteps = 0;

	private String webURL;

	/**
	 * Creates a convert/improve entry out of a existing file.
	 * 
	 * @param file
	 */
	public QueueEntry(final File file) {

		downloadTempFile = file;
		isDownloadTask = false;
		if (Boolean.valueOf(ConfigManager.getInstance().getConfig(ConfigKey.IMPROVE_CONVERT, "true"))) {
			if (QueueEntry.enableSilence) {
				stepsToComplete.add(new StepSilenceDetect(this));
			}
			if (QueueEntry.enableVolume) {
				if (ConfigManager.getInstance().getConfig(ConfigKey.VOLUME_METHOD, "ReplayGain").equals("Peak Normalize")) {
					stepsToComplete.add(new StepVolumeAdjust(this));
				}
			}

			if (QueueEntry.enableSilence || QueueEntry.enableVolume) {
				stepsToComplete.add(new StepConvert(this));
			}

			if (QueueEntry.enableGracenote) {
				stepsToComplete.add(new StepMetaSearch(this));
			}

			stepsToComplete.add(new StepRelocate(this));

			if (QueueEntry.enableVolume) {
				if (ConfigManager.getInstance().getConfig(ConfigKey.VOLUME_METHOD, "ReplayGain").equals("ReplayGain")) {
					stepsToComplete.add(new StepReplayGain(this));
				}
			}
		}
	}

	/**
	 * Creates a downloading entry which is only responsible for downloading the video into a file. After the download is finished, it creates a new convert/improve-entry for the newly created file.
	 * 
	 * @param webURL
	 *            HTTP URL which contains the video (will be directly passed to youtube-dl.exe)
	 */
	public QueueEntry(final String webURL) {
		this.webURL = webURL;
		stepsToComplete.add(new StepDownload(this));
	}

	public void addDownloadTempFile(final File file) {
		downloadTempFile = file;
		final QueueEntry q = new QueueEntry(file);
		Queue.getInstance().addEntry(q);
	}

	public void addListener(final ProgressListener l) {
		progressListeners.add(l);
	}

	public File getConvertTempFile() {
		String f = downloadTempFile.getAbsolutePath();
		final int i = f.lastIndexOf(".");
		f = f.substring(0, i) + getExtension();
		return new File(f);
	}

	public File getCoverTempFile() {
		String f = downloadTempFile.getAbsolutePath();
		final int i = f.lastIndexOf(".");
		f = f.substring(0, i) + ".png";
		return new File(f);
	}

	public File getDownloadTempFile() {
		return downloadTempFile;
	}

	public String getExtension() {
		// FilenameUtils.getExtension(this.downloadTempFile.getAbsolutePath());
		return isFLAC() ? ".flac" : ".mp3";
	}

	public File getFinalMP3File() {
		return finalMP3File;
	}

	public HashMap<String, Object> getStepInfo() {
		return stepInfo;
	}

	public String getWebURL() {
		return webURL;
	}

	public boolean isDownloadTask() {
		return isDownloadTask;
	}

	public boolean isFLAC() {
		return ConfigManager.getInstance().getConfig(ConfigKey.AUDIO_BITRATE, "320").equals("FLAC Lossless");
	}

	/**
	 * Starts the next Step. Executes the listeners when needed. Also, measures the time needed for each Step in ms. Keeps track of the last Step started and executes the onEntryStepEnd if necessary.
	 */
	public void nextStep() {
		// handle timing and onEntryStepEnd
		if (sold != null) {
			final long t = System.currentTimeMillis() - told;

			final double progress = 1 - (double) stepsToComplete.size() / (double) totalSteps;

			for (final ProgressListener l : progressListeners) {
				l.onEntryStepProgress(this, sold, 1);
				l.onEntryStepEnd(this, sold, t, progress);
			}
		}

		final QueueEntry that = this;

		// we are finished
		if (stepsToComplete.isEmpty()) {
			for (final ProgressListener l : progressListeners) {
				l.onEntryEnd(this);
			}
			cleanUp();
			return;
		}

		// execute the next step
		final Step s = stepsToComplete.removeFirst();
		sold = s;
		told = System.currentTimeMillis();

		for (final ProgressListener l : progressListeners) {
			l.onEntryStepBegin(this, s);
			l.onEntryStepProgress(that, s, 0);
		}
		s.addListener(new StepListener() {
			@Override
			public void stepProgress(final double progress) {
				for (final ProgressListener l : progressListeners) {
					l.onEntryStepProgress(that, s, progress);
				}
			}
		});
		s.doStep();
	}

	public void removeListener(final ProgressListener l) {
		progressListeners.remove(l);
	}

	/**
	 * Executes the Steps and informs the listeners of the begin/end.
	 */
	@Override
	public void run() {
		for (final ProgressListener l : progressListeners) {
			l.onEntryBegin(this);
		}
		totalSteps = stepsToComplete.size();
		nextStep();
	}

	public void setDownloadTempFile(final File file) {
		downloadTempFile = file;
	}

	public void setFinalMP3File(final File finalMP3File) {
		this.finalMP3File = finalMP3File;
	}

	/**
	 * Deletes all temporary files after every step has finished. This depends on the setting ConfigKey.KEEP_VIDEO. A downloading entry won't delete the final file, this might only be done by a
	 * converting entry after the last step has finished.
	 */
	private void cleanUp() {
		if (!isDownloadTask()) {
			if (Boolean.valueOf(ConfigManager.getInstance().getConfig(ConfigKey.KEEP_VIDEO, "false"))) {
				try {
					FileUtils.moveFile(getDownloadTempFile(), getDownloadFinalFile());
				} catch (final IOException e) {
					Logging.log("failed moving video file to destination", e);
				}
			}
		}

	}

	/**
	 * The file in the target directory with the same name as the temp file.
	 * 
	 * @return
	 */
	private File getDownloadFinalFile() {
		final String n = getDownloadTempFile().getName();
		return new File(ConfigManager.getInstance().getConfig(ConfigKey.DIR_TARGET, new File("").getAbsolutePath()) + ConfigManager.DS + n);
	}
}
