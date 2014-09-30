package de.zahlii.youtube.download.step;

import java.util.ArrayList;
import java.util.List;

import de.zahlii.youtube.download.QueueEntry;
import de.zahlii.youtube.download.basic.ConfigManager;
import de.zahlii.youtube.download.basic.Logging;
import de.zahlii.youtube.download.cli.CLI;
import de.zahlii.youtube.download.cli.ProcessAdapter;

public class StepSilenceDetect extends Step {
	private final ArrayList<SilenceInfo> silence = new ArrayList<SilenceInfo>();

	public StepSilenceDetect(final QueueEntry entry) {
		super(
				entry,
				new StepDescriptor(
						"SilenceDetect",
						"Detects different phases of silence and cuts them out at the start and end of the clip."));
	}

	@Override
	public void doStep() {
		final ProcessBuilder n = new ProcessBuilder();

		final List<String> meta = new ArrayList<String>();
		meta.add(ConfigManager.FFMPEG.getAbsolutePath());
		meta.add("-i");
		meta.add(this.entry.getDownloadTempFile().getAbsolutePath());
		meta.add("-af");
		meta.add("silencedetect=n=-50dB:d=0.5");
		meta.add("-f");
		meta.add("null");
		meta.add("-");
		n.command(meta);

		final CLI y = new CLI(n);

		final FFMPEGTimeProcessListener x = new FFMPEGTimeProcessListener() {
			@Override
			public void onProgress(final double progress) {
				StepSilenceDetect.this.reportProgress(progress);
			}
		};
		y.addProcessListener(x);
		y.addProcessListener(new ProcessAdapter() {
			@Override
			public void processLineOut(final String line) {
				if (SilenceInfo.isSilenceInfo(line)) {
					final SilenceInfo si = new SilenceInfo(line);
					if (si.isValid()) {
						StepSilenceDetect.this.silence.add(si);
					}
				}
			}

		});
		y.run();

		// we got the silence now!
		double firstSilenceEnd = 0, lastSilenceStart = 0;

		boolean cutStart = false, cutEnd = false;

		// no silence at all
		if (this.silence.size() == 0) {
			// skip
		} else if (this.silence.size() == 1) {
			// might be at start or end
			final SilenceInfo f = this.silence.get(0);
			if (f.getTimeStart() == -1) { // only silence_end was given -> is at
											// the start
				cutStart = true;
				firstSilenceEnd = f.getTimeEnd();
			} else {
				lastSilenceStart = this.silence.get(0).getTimeStart();
				cutEnd = true;
			}

		} else {
			// silence at all ends
			final SilenceInfo f = this.silence.get(0);
			final SilenceInfo l = this.silence.get(this.silence.size() - 1);
			firstSilenceEnd = f.getTimeEnd();
			lastSilenceStart = l.getTimeStart();

			cutStart = firstSilenceEnd > 0;
			cutEnd = lastSilenceStart > 0;
		}

		this.entry.getStepInfo().put("silence.start", cutStart);

		if (this.silence.size() > 0) {
			Logging.log("found silence: " + this.silence);
		}
		if (cutStart) {
			this.entry.getStepInfo().put("silence.start.time", firstSilenceEnd);
		}

		this.entry.getStepInfo().put("silence.end", cutEnd);
		if (cutEnd) {
			this.entry.getStepInfo().put("silence.end.time", lastSilenceStart);
		}

		this.nextStep();
	}

	@Override
	public String getStepResults() {
		final String ss = (boolean) this.entry.getStepInfo().get(
				"silence.start") ? "Silence at Start: "
				+ this.entry.getStepInfo().get("silence.start.time") : "";
		final String se = (boolean) this.entry.getStepInfo().get("silence.end") ? "Silence at End: "
				+ this.entry.getStepInfo().get("silence.end.time")
				: "";

		String r = ss;
		r += se.equals("") ? "" : (ss.equals("") ? se : ", " + se);

		return r.equals("") ? "No silence to remove." : r + ".";
	}
}
