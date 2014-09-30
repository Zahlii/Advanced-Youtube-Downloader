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
		super(
				entry,
				new StepDescriptor(
						"FileConvert",
						"Converts the downloaded file while applying the defined filters and effects on it"));
	}

	@Override
	public void doStep() {
		// not necessary
		final boolean hasSilenceStart = this.entry.getStepInfo().containsKey(
				"silence.start")
				&& (boolean) this.entry.getStepInfo().get("silence.start");
		final boolean hasSilenceEnd = this.entry.getStepInfo().containsKey(
				"silence.end")
				&& (boolean) this.entry.getStepInfo().get("silence.end");
		final boolean sameFile = this.entry.getDownloadTempFile().getName()
				.equals(this.entry.getConvertTempFile().getName());
		final boolean hasVolume = this.entry.getStepInfo().containsKey(
				"volume.level");

		if (!hasSilenceStart && !hasSilenceEnd && sameFile && !hasVolume) {
			Logging.log("Skipping conversion");
			this.entry.getStepInfo().put("skipped", true);
			this.nextStep();
			return;
		}
		this.entry.getStepInfo().put("skipped", false);
		File target = this.entry.getConvertTempFile();
		if (target.getAbsolutePath().equals(
				this.entry.getDownloadTempFile().getAbsolutePath())) {

			target = new File(ConfigManager.TEMP_DIR + ConfigManager.DS
					+ this.entry.getConvertTempFile().getName());

			this.entry.getStepInfo().put("is_forked", true);
		} else {
			this.entry.getStepInfo().put("is_forked", false);
		}

		final ProcessBuilder n = new ProcessBuilder();

		final List<String> meta = new ArrayList<String>();
		meta.add(ConfigManager.FFMPEG.getAbsolutePath());
		meta.add("-i");
		meta.add(this.entry.getDownloadTempFile().getAbsolutePath());
		meta.add("-y");
		if (!this.entry.isFLAC()) {
			meta.add("-ab");
			meta.add(ConfigManager.getInstance().getConfig(
					ConfigKey.AUDIO_BITRATE, "320")
					+ "k");
		} else {
			meta.add("-compression_level");
			meta.add("12");
		}

		if (this.entry.getStepInfo().containsKey("silence.start")
				&& (boolean) this.entry.getStepInfo().get("silence.start")) {
			meta.add("-ss");
			meta.add((double) this.entry.getStepInfo()
					.get("silence.start.time") + "");
		}
		if (this.entry.getStepInfo().containsKey("silence.end")
				&& (boolean) this.entry.getStepInfo().get("silence.end")) {
			meta.add("-t");
			double s = 0;
			try {
				s = (double) this.entry.getStepInfo().get("silence.start.time");
			} catch (final NullPointerException e) {

			}
			meta.add(((double) this.entry.getStepInfo().get("silence.end.time") - s)
					+ "");
		}

		double v = 0.0;

		if (this.entry.getStepInfo().containsKey("volume.level")
				&& (v = (double) this.entry.getStepInfo().get("volume.level")) > 1) {
			meta.add("-af");
			meta.add("volume=" + v + "dB:precision=double");
		}

		meta.add(target.getAbsolutePath());
		this.entry.setDownloadTempFile(target);
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

		this.nextStep();
	}

	@Override
	public String getStepResults() {
		return "File converted to " + this.entry.getConvertTempFile().getName()
				+ ".";
	}

}
