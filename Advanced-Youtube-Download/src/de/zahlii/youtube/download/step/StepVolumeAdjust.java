package de.zahlii.youtube.download.step;

import java.util.ArrayList;
import java.util.List;

import de.zahlii.youtube.download.QueueEntry;
import de.zahlii.youtube.download.basic.ConfigManager;
import de.zahlii.youtube.download.cli.CLI;
import de.zahlii.youtube.download.cli.ProcessAdapter;

public class StepVolumeAdjust extends Step {

	private class VolumeProcessAdapter extends ProcessAdapter {
		private double volume = 1.0;

		public double getVolume() {
			return volume;
		}

		@Override
		public void processLineOut(final String line) {
			if (line.contains("max_volume")) {
				String p = line.split(":")[1];
				p = p.split("dB")[0].trim();
				volume = Double.parseDouble(p);
			}

		}

		@Override
		public void processStop() {
			volume = volume >= 0 ? 1.0 : -volume;
		}
	}

	public StepVolumeAdjust(final QueueEntry entry) {
		super(entry, new StepDescriptor("VolumeAdjust",
				"Normalizes the music so that the peak intensity is at 0.0dB"));
	}

	@Override
	public void doStep() {
		final ProcessBuilder n = new ProcessBuilder();

		final List<String> meta = new ArrayList<String>();
		meta.add(ConfigManager.FFMPEG.getAbsolutePath());
		meta.add("-i");
		meta.add(entry.getDownloadTempFile().getAbsolutePath());
		meta.add("-af");
		meta.add("volumedetect");
		meta.add("-f");
		meta.add("null");
		meta.add("-");
		n.command(meta);

		final CLI y = new CLI(n);

		final FFMPEGTimeProcessListener x = new FFMPEGTimeProcessListener() {
			@Override
			public void onProgress(final double progress) {
				StepVolumeAdjust.this.reportProgress(progress);
			}
		};
		final VolumeProcessAdapter vp = new VolumeProcessAdapter();
		y.addProcessListener(x);
		y.addProcessListener(vp);
		y.run();

		entry.getStepInfo().put("volume.level", vp.getVolume());

		nextStep();

	}

	@Override
	public String getStepResults() {
		final double v = (double) entry.getStepInfo().get("volume.level");
		return v > 1 ? "Song volume adjusted by " + v + "dB."
				: "No volume adjust needed.";
	}

}
