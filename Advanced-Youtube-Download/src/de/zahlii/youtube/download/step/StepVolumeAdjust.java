package de.zahlii.youtube.download.step;

import java.util.ArrayList;
import java.util.List;

import de.zahlii.youtube.download.QueueEntry;
import de.zahlii.youtube.download.basic.ConfigManager;
import de.zahlii.youtube.download.cli.CLI;
import de.zahlii.youtube.download.cli.ProcessAdapter;

public class StepVolumeAdjust extends Step {

	public StepVolumeAdjust(QueueEntry entry) {
		super(entry, new StepDescriptor("VolumeAdjust","Normalizes the music so that the peak intensity is at 0.0dB"));
	}

	@Override
	public void doStep() {
		ProcessBuilder n = new ProcessBuilder();
		
		List<String> meta = new ArrayList<String>();
		meta.add(ConfigManager.FFMPEG.getAbsolutePath());
		meta.add("-i");
		meta.add(entry.getDownloadTempFile().getAbsolutePath());
		meta.add("-af");
		meta.add("volumedetect");
		meta.add("-f");
		meta.add("null");
		meta.add("-");
		n.command(meta);

		CLI y = new CLI(n);
	

		FFMPEGTimeProcessListener x = new FFMPEGTimeProcessListener() {
			@Override
			public void onProgress(double progress) {
				reportProgress(progress);				
			}			
		};
		VolumeProcessAdapter vp = new VolumeProcessAdapter();
		y.addProcessListener(x);
		y.addProcessListener(vp);
		y.run();
		
		
		entry.getStepInfo().put("volume.level", vp.getVolume());
		
		nextStep();
		
	}
	
	private class VolumeProcessAdapter extends ProcessAdapter {
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

	@Override
	public String getStepResults() {
		double v = (double)entry.getStepInfo().get("volume.level");		
		return v > 1 ? "Song volume adjusted by " + v+ "dB." : "No volume adjust needed.";
	}

}
