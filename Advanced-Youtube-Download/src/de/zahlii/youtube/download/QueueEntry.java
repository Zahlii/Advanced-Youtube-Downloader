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
 * Represents one entry in the queue. This could either be a downloading action
 * or a converting/improving action of an existing file.
 * 
 * @author Zahlii
 * 
 */
public class QueueEntry extends Thread {
	private String webURL;
	private File downloadTempFile;
	private File finalMP3File;

	private Step sold;
	private long told;

	private int totalSteps = 0;

	private final HashMap<String, Object> stepInfo = new HashMap<>();
	private final ArrayList<ProgressListener> progressListeners = new ArrayList<>();
	private final LinkedList<Step> stepsToComplete = new LinkedList<>();

	private boolean isDownloadTask = true;

	/**
	 * Creates a downloading entry which is only responsible for downloading the
	 * video into a file. After the download is finished, it creates a new
	 * convert/improve-entry for the newly created file.
	 * 
	 * @param webURL
	 *            HTTP URL which contains the video (will be directly passed to
	 *            youtube-dl.exe)
	 */
	public QueueEntry(final String webURL) {
		this.webURL = webURL;
		this.stepsToComplete.add(new StepDownload(this));
	}

	/**
	 * Creates a convert/improve entry out of a existing file.
	 * 
	 * @param file
	 */
	public QueueEntry(final File file) {
		this.downloadTempFile = file;
		this.isDownloadTask = false;
		if (Boolean.valueOf(ConfigManager.getInstance().getConfig(
				ConfigKey.IMPROVE_CONVERT, "true"))) {
			this.stepsToComplete.add(new StepSilenceDetect(this));
			if (ConfigManager.getInstance()
					.getConfig(ConfigKey.VOLUME_METHOD, "ReplayGain")
					.equals("Peak Normalize")) {
				this.stepsToComplete.add(new StepVolumeAdjust(this));
			}

			this.stepsToComplete.add(new StepConvert(this));
			this.stepsToComplete.add(new StepMetaSearch(this));
			this.stepsToComplete.add(new StepRelocate(this));

			if (ConfigManager.getInstance()
					.getConfig(ConfigKey.VOLUME_METHOD, "ReplayGain")
					.equals("ReplayGain")) {
				this.stepsToComplete.add(new StepReplayGain(this));
			}
		}
	}

	public boolean isDownloadTask() {
		return this.isDownloadTask;
	}

	/**
	 * Deletes all temporary files after every step has finished. This depends
	 * on the setting ConfigKey.KEEP_VIDEO. A downloading entry won't delete the
	 * final file, this might only be done by a converting entry after the last
	 * step has finished.
	 */
	private void cleanUp() {
		if (!this.isDownloadTask()) {
			if (Boolean.valueOf(ConfigManager.getInstance().getConfig(
					ConfigKey.KEEP_VIDEO, "false"))) {
				try {
					FileUtils.moveFile(this.getDownloadTempFile(),
							this.getDownloadFinalFile());
				} catch (final IOException e) {
					Logging.log("failed moving video file to destination", e);
				}
			}
			this.remove(this.getDownloadTempFile());

		}

		this.remove(this.getCoverTempFile());
	}

	/**
	 * The file in the target directory with the same name as the temp file.
	 * 
	 * @return
	 */
	private File getDownloadFinalFile() {
		final String n = this.getDownloadTempFile().getName();
		return new File(ConfigManager.getInstance().getConfig(
				ConfigKey.DIR_TARGET, (new File("")).getAbsolutePath())
				+ ConfigManager.DS + n);
	}

	private void remove(final File f) {
		FileUtils.deleteQuietly(f);
	}

	public String getWebURL() {
		return this.webURL;
	}

	public void addListener(final ProgressListener l) {
		this.progressListeners.add(l);
	}

	public void removeListener(final ProgressListener l) {
		this.progressListeners.remove(l);
	}

	public HashMap<String, Object> getStepInfo() {
		return this.stepInfo;
	}

	/**
	 * Executes the Steps and informs the listeners of the begin/end.
	 */
	@Override
	public void run() {
		for (final ProgressListener l : this.progressListeners) {
			l.onEntryBegin(this);
		}
		this.totalSteps = this.stepsToComplete.size();
		this.nextStep();
	}

	public File getDownloadTempFile() {
		return this.downloadTempFile;
	}

	public void setDownloadTempFile(final File file) {
		this.downloadTempFile = file;
	}

	public File getConvertTempFile() {
		String f = this.downloadTempFile.getAbsolutePath();
		final int i = f.lastIndexOf(".");
		f = f.substring(0, i) + this.getExtension();
		return new File(f);
	}

	public String getExtension() {
		return this.isFLAC() ? ".flac" : ".mp3";
	}

	public boolean isFLAC() {
		return ConfigManager.getInstance()
				.getConfig(ConfigKey.AUDIO_BITRATE, "320")
				.equals("FLAC Lossless");
	}

	/**
	 * Starts the next Step. Executes the listeners when needed. Also, measures
	 * the time needed for each Step in ms. Keeps track of the last Step started
	 * and executes the onEntryStepEnd if necessary.
	 */
	public void nextStep() {
		// handle timing and onEntryStepEnd
		if (this.sold != null) {
			final long t = System.currentTimeMillis() - this.told;

			final double progress = 1 - (double) this.stepsToComplete.size()
					/ (double) this.totalSteps;

			for (final ProgressListener l : this.progressListeners) {
				l.onEntryStepProgress(this, this.sold, 1);
				l.onEntryStepEnd(this, this.sold, t, progress);
			}
		}

		final QueueEntry that = this;

		// we are finished
		if (this.stepsToComplete.isEmpty()) {
			for (final ProgressListener l : this.progressListeners) {
				l.onEntryEnd(this);
			}
			this.cleanUp();
			return;
		}

		// execute the next step
		final Step s = this.stepsToComplete.removeFirst();
		this.sold = s;
		this.told = System.currentTimeMillis();

		for (final ProgressListener l : this.progressListeners) {
			l.onEntryStepBegin(this, s);
			l.onEntryStepProgress(that, s, 0);
		}
		s.addListener(new StepListener() {
			@Override
			public void stepProgress(final double progress) {
				for (final ProgressListener l : QueueEntry.this.progressListeners) {
					l.onEntryStepProgress(that, s, progress);
				}
			}
		});
		s.doStep();
	}

	public File getCoverTempFile() {
		String f = this.downloadTempFile.getAbsolutePath();
		final int i = f.lastIndexOf(".");
		f = f.substring(0, i) + ".png";
		return new File(f);
	}

	public void addDownloadTempFile(final File file) {
		this.downloadTempFile = file;
		final QueueEntry q = new QueueEntry(file);
		Queue.getInstance().addEntry(q);
	}

	public File getFinalMP3File() {
		return this.finalMP3File;
	}

	public void setFinalMP3File(final File finalMP3File) {
		this.finalMP3File = finalMP3File;
	}
}
