/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 */
package de.ytdownload.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;







import de.ytdownload.controller.ConfigManager.ConfigKey;
import de.ytdownload.model.DownloadFormat;
import de.ytdownload.model.DownloadInfo;
import de.ytdownload.model.SilenceInfo;
import de.ytdownload.model.VideoInfo;
import de.ytdownload.service.Logging;
import de.ytdownload.service.cli.CLI;
import de.ytdownload.service.cli.ProcessAdapter;
import de.ytdownload.service.cli.ProcessListener;
import de.ytdownload.ui.Media;

/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 * 
 */
public class DownloadManager extends Thread {

	private VideoInfo vinfo;
	private List<DownloadProgressListener> listeners;

	private void getVideoInfo() {

		final List<DownloadFormat> formats = new ArrayList<DownloadFormat>();

		ProcessBuilder p = new ProcessBuilder(ConfigManager.YOUTUBE_DL.getAbsolutePath(),
				"--get-description", "--get-title", "--get-thumbnail", vinfo.url);

		CLI x = new CLI(p);
		x.addProcessListener(new ProcessAdapter() {
			private int count = 0;

			@Override
			public void processLineOut(String line) {
				count++;
				if (count == 1)
					vinfo.setTitle(line);
				if (count == 2)
					vinfo.setThumbnail(line);
				if (count == 3)
					vinfo.setDescription(line);

			}

			@Override
			public void processStop() {

				for (DownloadProgressListener l : listeners) {
					l.infoMetaComplete(vinfo);
				}
			}
		});
		for (DownloadProgressListener l : listeners) {
			l.infoStart(vinfo.url);
		}
		x.run();

		ProcessBuilder n = new ProcessBuilder(ConfigManager.YOUTUBE_DL.getAbsolutePath(), "-F",
				vinfo.url);

		CLI y = new CLI(n);
		y.addProcessListener(new ProcessAdapter() {

			@Override
			public void processLineOut(String line) {

				if (DownloadFormat.isFormat(line)) {
					formats.add(new DownloadFormat(line));
				}

			}

			@Override
			public void processStop() {
				vinfo.setFormats(formats);
				for (DownloadProgressListener l : listeners) {
					l.infoComplete(vinfo);
				}
			}

		});
		y.run();
	}

	private void downloadVideo() {
		DownloadFormat f = vinfo.getBestFormat();
		downloadVideo(f);
	}

	private void downloadVideo(DownloadFormat f) {

		final File targetFile = new File(ConfigManager.TEMP_DIR.getAbsolutePath()
				+ ConfigManager.DS + Media.sanitize(vinfo.getTitle()) + "." + f.extension);

		vinfo.setTempFile(targetFile);

		ProcessBuilder n = new ProcessBuilder(ConfigManager.YOUTUBE_DL.getAbsolutePath(), "-f",
				f.formatCode, "-o", targetFile.getAbsolutePath(), vinfo.url);

		CLI y = new CLI(n);
		final long t = System.currentTimeMillis();

		y.addProcessListener(new ProcessAdapter() {

			@Override
			public void processLineOut(String line) {

				if (DownloadInfo.isProgress(line)) {
					DownloadInfo dinf = new DownloadInfo(line);
					vinfo.setSize(dinf.size);
					for (DownloadProgressListener l : listeners) {
						l.downloadProgress(vinfo, dinf);
					}
				}

			}

		});
		for (DownloadProgressListener l : listeners) {
			l.downloadStart(vinfo);
		}

		y.run();

		vinfo.setDownloadTime((System.currentTimeMillis() - t) / 1000.0);
		for (DownloadProgressListener l : listeners) {
			l.downloadComplete(vinfo);
		}
	}
	
	private List<SilenceInfo> silenceInfo() {
		ProcessBuilder n = new ProcessBuilder();

		// just run silence detect to null
		List<String> meta = new ArrayList<String>();
		meta.add(ConfigManager.FFMPEG.getAbsolutePath());
		meta.add("-i");
		meta.add(vinfo.getTempFile().getAbsolutePath());
		meta.add("-af");
		meta.add("silencedetect=n=-50dB:d=1");
		meta.add("-f");
		meta.add("null");
		meta.add("-");
		n.command(meta);

		CLI y = new CLI(n);
		

		final ArrayList<SilenceInfo> silence = new ArrayList<SilenceInfo>();
		
		y.addProcessListener(new ProcessAdapter() {
			@Override
			public void processLineOut(String line) {
				if(SilenceInfo.isSilenceInfo(line)) {
					silence.add(new SilenceInfo(line));
				}
				
			}
			
		});
		y.run();
		
		for (DownloadProgressListener l : listeners) {
			l.silenceComplete(vinfo, silence);
		}
		
		return silence;
	}
	
	private double volumeInfo() {
		ProcessBuilder n = new ProcessBuilder();

		// just run silence detect to null
		List<String> meta = new ArrayList<String>();
		meta.add(ConfigManager.FFMPEG.getAbsolutePath());
		meta.add("-i");
		meta.add(vinfo.getTempFile().getAbsolutePath());
		meta.add("-af");
		meta.add("volumedetect");
		meta.add("-f");
		meta.add("null");
		meta.add("-");
		n.command(meta);

		CLI y = new CLI(n);
		
		VolumeProcess vp = new VolumeProcess();
		
		y.addProcessListener(vp);
		y.run();
		
		double v = vp.getVolume();
		
		for (DownloadProgressListener l : listeners) {
			l.volumeComplete(vinfo, v);
		}
		
		return v;
		
	}
	
	private class VolumeProcess extends ProcessAdapter {
		private double volume = 1.0;
		
		@Override
		public void processLineOut(String line) {
			if(line.contains("max_volume")) {
				String p = line.split(":")[1];
				p = p.split("dB")[0].trim();
				volume = Double.parseDouble(p);
			}
			
		}
		
		@Override
		public void processStop() {
			volume = volume >= 0 ? 1.0 : -volume;
		}
		
		public double getVolume() {
			return volume;
		}
	}

	private void convert() {
		final File targetFile = new File(ConfigManager.TEMP_DIR.getAbsolutePath()
				+ ConfigManager.DS + Media.sanitize(vinfo.getTitle()) + ".mp3");

		
		List<SilenceInfo> silence = silenceInfo();
		
		
		double volume = volumeInfo();
		
		// we got the silence now!		
		double firstSilenceEnd = 0, lastSilenceStart = 0;
		
		boolean cutStart = false, cutEnd = false;
		
		// no silence at all
		if(silence.size() == 0) {
			// skip 
		} else if(silence.size() == 1) {
			// has to be at the end because no end was given
			lastSilenceStart = silence.get(0).getTimeStart();
			cutEnd = true;
		} else {
			// silence at all ends
			for(SilenceInfo s : silence) {
				if(firstSilenceEnd == 0 && s.getTimeEnd() != -1)
					firstSilenceEnd = s.getTimeEnd();
				
				if(s.getTimeStart() != -1)
					lastSilenceStart = s.getTimeStart();
			}
			cutStart = true;
			cutEnd = true;
		}
		
		ProcessBuilder n = new ProcessBuilder();
		
		List<String> meta = new ArrayList<String>();
		meta.add(ConfigManager.FFMPEG.getAbsolutePath());
		meta.add("-i");
		meta.add(vinfo.getTempFile().getAbsolutePath());
		if(cutStart) {
			meta.add("-ss");
			meta.add(firstSilenceEnd + "");
		}
		if(cutEnd) {
			meta.add("-t");
			meta.add((lastSilenceStart - firstSilenceEnd) + "");
		}
		meta.add("-y");
		meta.add("-ab");
		meta.add(ConfigManager.getInstance().getConfig(ConfigKey.AUDIO_BITRATE)+"k");
		if(volume > 1) {
			meta.add("-af");
			meta.add("volume=" + volume + "dB:precision=double");
		}
		meta.add(targetFile.getAbsolutePath());
		n.command(meta);

		CLI y = new CLI(n);

		final long t = System.currentTimeMillis();
		// and finally convert and cut it
		y.run();
		
		vinfo.setConversionTime((System.currentTimeMillis() - t) / 1000.0);
		vinfo.setMp3File(targetFile);
		for (DownloadProgressListener l : listeners) {
			l.convertComplete(vinfo);
		}
	}

	public DownloadManager(String url) {
		this.vinfo = new VideoInfo();
		this.vinfo.url = url;
		this.listeners = new ArrayList<DownloadProgressListener>();
	}

	@Override
	public void run() {
		long t = System.currentTimeMillis();
		getVideoInfo();
		downloadVideo();
		convert();

		for (DownloadProgressListener l : listeners) {
			double time = (System.currentTimeMillis() - t) / 1000.0;
			l.complete(vinfo, time);
		}
	}

	public void addListener(DownloadProgressListener yt) {
		this.listeners.add(yt);
	}

	public void removeListener(DownloadProgressListener yt) {
		this.listeners.remove(yt);
	}

}
