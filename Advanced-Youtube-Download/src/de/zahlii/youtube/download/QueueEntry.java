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

public class QueueEntry extends Thread {
	private String webURL;
	private File downloadTempFile;
	private File finalMP3File;

	private Step sold;
	private long told;
	
	private int totalSteps = 0;
	
	private HashMap<String, Object> stepInfo = new HashMap<>();
	private ArrayList<ProgressListener> progressListeners = new ArrayList<>();
	private LinkedList<Step> stepsToComplete = new LinkedList<>();
	
	private boolean isDownloadTask = true;
	
	public QueueEntry(String webURL) {
		this.webURL = webURL;
		stepsToComplete.add(new StepDownload(this));
	}
	
	public QueueEntry(File file) {
		this.downloadTempFile = file;
		isDownloadTask = false;
		if(Boolean.valueOf(ConfigManager.getInstance().getConfig(ConfigKey.IMPROVE_CONVERT, "true"))) {
			stepsToComplete.add(new StepSilenceDetect(this));
			if(ConfigManager.getInstance().getConfig(ConfigKey.VOLUME_METHOD, "ReplayGain").equals("Peak Normalize"))
				stepsToComplete.add(new StepVolumeAdjust(this));
			
			stepsToComplete.add(new StepConvert(this));
			stepsToComplete.add(new StepMetaSearch(this));
			stepsToComplete.add(new StepRelocate(this));
			
			String s = ConfigManager.getInstance().getConfig(ConfigKey.VOLUME_METHOD, "ReplayGain");
			
			if(ConfigManager.getInstance().getConfig(ConfigKey.VOLUME_METHOD, "ReplayGain").equals("ReplayGain"))
				stepsToComplete.add(new StepReplayGain(this));
		}
	}
	
	public boolean isDownloadTask() {
		return isDownloadTask;
	}
	
	private void cleanUp() {
		if(!isDownloadTask()) {
			if(Boolean.valueOf(ConfigManager.getInstance().getConfig(ConfigKey.KEEP_VIDEO, "false"))) {
				try {
					FileUtils.moveFile(getDownloadTempFile(), getDownloadFinalFile());
				} catch (IOException e) {
					Logging.log("failed moving video file to destination",e);
				}
			}
			remove(getDownloadTempFile());
			
		}
			
		remove(getCoverTempFile());
	}
	
	private File getDownloadFinalFile() {
		String n = getDownloadTempFile().getName();
		return new File(ConfigManager.getInstance().getConfig(ConfigKey.DIR_TARGET, (new File("")).getAbsolutePath()) + ConfigManager.DS + n);
	}

	private void remove(File f) {
		FileUtils.deleteQuietly(f);
	}

	public String getWebURL() {
		return webURL;
	}	
	
	
	public void addListener(ProgressListener l) {
		progressListeners.add(l);
	}
	
	public void removeListener(ProgressListener l) {
		progressListeners.remove(l);
	}
	
	public HashMap<String,Object> getStepInfo() {
		return stepInfo;
	}

	@Override
	public void run() {
		for(ProgressListener l : progressListeners) {
			l.onEntryBegin(this);
		}
		totalSteps = stepsToComplete.size();
		nextStep();
	}

	public File getDownloadTempFile() {
		return downloadTempFile;
	}

	public void setDownloadTempFile(File file) {
		downloadTempFile = file;		
	}

	public File getConvertTempFile() {
		String f = downloadTempFile.getAbsolutePath();
		int i = f.lastIndexOf(".");
		f = f.substring(0,i) + getExtension();
		return new File(f);
	}

	public String getExtension() {
		 return isFLAC() ? ".flac" : ".mp3";
	}
	
	public boolean isFLAC() {
		return ConfigManager.getInstance().getConfig(ConfigKey.AUDIO_BITRATE,"320").equals("FLAC Lossless");
	}

	public void nextStep() {
		if(sold != null) {
			long t = System.currentTimeMillis()-told;
			
			double progress = 1 - (double)stepsToComplete.size()/(double)totalSteps;
			
			for(ProgressListener l : progressListeners) {
				l.onEntryStepProgress(this, sold, 1);
				l.onEntryStepEnd(this, sold, t, progress);
			}	
		}
		
		final QueueEntry that = this;		
		
		if(stepsToComplete.isEmpty()) {
			for(ProgressListener l : progressListeners) {
				l.onEntryEnd(this);
			}
			cleanUp();
			return;
		}
		
		final Step s = stepsToComplete.removeFirst();
		sold = s;
		told = System.currentTimeMillis();
		
		for(ProgressListener l : progressListeners) {
			l.onEntryStepBegin(this, s);
			l.onEntryStepProgress(that, s, 0);
		}
		s.addListener(new StepListener() {
			@Override
			public void stepProgress(double progress) {
				for(ProgressListener l : progressListeners) {
					l.onEntryStepProgress(that, s, progress);
				}
			}				
		});
		s.doStep();		
	}

	public File getCoverTempFile() {
		String f = downloadTempFile.getAbsolutePath();
		int i = f.lastIndexOf(".");
		f = f.substring(0,i) + ".png";
		return new File(f);
	}

	public void addDownloadTempFile(File file) {
		downloadTempFile = file;
		QueueEntry q = new QueueEntry(file);
		Queue.getInstance().addEntry(q);	
	}

	public File getFinalMP3File() {
		return finalMP3File;
	}

	public void setFinalMP3File(File finalMP3File) {
		this.finalMP3File = finalMP3File;
	}
}
