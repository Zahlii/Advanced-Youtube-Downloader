package de.zahlii.youtube.download.step;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.zahlii.youtube.download.QueueEntry;
import de.zahlii.youtube.download.basic.ConfigManager;
import de.zahlii.youtube.download.basic.ConfigManager.ConfigKey;
import de.zahlii.youtube.download.basic.Logging;
import de.zahlii.youtube.download.cli.CLI;

public class StepConvert extends Step {

	public StepConvert(final QueueEntry entry) {
		super(entry, new StepDescriptor("FileConvert", "Converts the downloaded file while applying the defined filters and effects on it"));
	}

	@Override
	public void doStep() {
		// not necessary
		final boolean hasSilenceStart = entry.getStepInfo().containsKey("silence.start") && (boolean) entry.getStepInfo().get("silence.start");
		final boolean hasSilenceEnd = entry.getStepInfo().containsKey("silence.end") && (boolean) entry.getStepInfo().get("silence.end");
		final boolean sameFile = entry.getDownloadTempFile().getName().equals(entry.getConvertTempFile().getName());
		final boolean hasVolume = entry.getStepInfo().containsKey("volume.level");

		if (!hasSilenceStart && !hasSilenceEnd && sameFile && !hasVolume) {
			Logging.log("Skipping conversion");
			entry.getStepInfo().put("skipped", true);
			nextStep();
			return;
		}
		entry.getStepInfo().put("skipped", false);
		File target = entry.getConvertTempFile();
		if (target.getAbsolutePath().equals(entry.getDownloadTempFile().getAbsolutePath())) {

			target = new File(ConfigManager.TEMP_DIR + ConfigManager.DS + entry.getConvertTempFile().getName());

			entry.getStepInfo().put("is_forked", true);
		} else {
			entry.getStepInfo().put("is_forked", false);
		}

		final ProcessBuilder n = new ProcessBuilder();

		final List<String> meta = new ArrayList<String>();
		meta.add(ConfigManager.FFMPEG.getAbsolutePath());
		meta.add("-i");
		meta.add(entry.getDownloadTempFile().getAbsolutePath());
		meta.add("-y");
		if (!entry.isFLAC()) {
			meta.add("-ab");
			meta.add(ConfigManager.getInstance().getConfig(ConfigKey.AUDIO_BITRATE, "320") + "k");
		} else {
			meta.add("-compression_level");
			meta.add("12");
		}

		if (entry.getStepInfo().containsKey("silence.start") && (boolean) entry.getStepInfo().get("silence.start")) {
			meta.add("-ss");
			meta.add((double) entry.getStepInfo().get("silence.start.time") + "");
		}
		if (entry.getStepInfo().containsKey("silence.end") && (boolean) entry.getStepInfo().get("silence.end")) {
			meta.add("-t");
			double s = 0;
			try {
				s = (double) entry.getStepInfo().get("silence.start.time");
			} catch (final NullPointerException e) {

			}
			meta.add((double) entry.getStepInfo().get("silence.end.time") - s + "");
		}

		double v = 0.0;

		if (entry.getStepInfo().containsKey("volume.level") && (v = (double) entry.getStepInfo().get("volume.level")) > 1) {
			meta.add("-af");
			meta.add("volume=" + v + "dB:precision=double");
		}

		meta.add(target.getAbsolutePath());
		entry.setDownloadTempFile(target);
		n.command(meta);

		final CLI y = new CLI(n);
		final FFMPEGTimeProcessListener x = new FFMPEGTimeProcessListener() {
			@Override
			public void onProgress(final double progress) {
				StepConvert.this.reportProgress(progress);
			}
		};
		y.addProcessListener(x);
		y.run();

		nextStep();
	}

	@Override
	public String getStepResults() {
		return "File converted to " + entry.getConvertTempFile().getName() + ".";
	}

}
