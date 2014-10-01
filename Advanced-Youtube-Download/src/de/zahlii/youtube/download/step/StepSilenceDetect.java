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
		meta.add(entry.getDownloadTempFile().getAbsolutePath());
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
						silence.add(si);
					}
				}
			}

		});
		y.run();

		// we got the silence now!
		double firstSilenceEnd = 0, lastSilenceStart = 0;

		boolean cutStart = false, cutEnd = false;

		// no silence at all
		if (silence.size() == 0) {
			// skip
		} else if (silence.size() == 1) {
			// might be at start or end
			final SilenceInfo f = silence.get(0);
			if (f.getTimeStart() == -1) { // only silence_end was given -> is at
											// the start
				cutStart = true;
				firstSilenceEnd = f.getTimeEnd();
			} else {
				lastSilenceStart = silence.get(0).getTimeStart();
				cutEnd = true;
			}

		} else {
			// silence at all ends
			final SilenceInfo f = silence.get(0);
			final SilenceInfo l = silence.get(silence.size() - 1);
			firstSilenceEnd = f.getTimeEnd();
			lastSilenceStart = l.getTimeStart();

			cutStart = firstSilenceEnd > 0;
			cutEnd = lastSilenceStart > 0;
		}

		entry.getStepInfo().put("silence.start", cutStart);

		if (silence.size() > 0) {
			Logging.log("found silence: " + silence);
		}
		if (cutStart) {
			entry.getStepInfo().put("silence.start.time", firstSilenceEnd);
		}

		entry.getStepInfo().put("silence.end", cutEnd);
		if (cutEnd) {
			entry.getStepInfo().put("silence.end.time", lastSilenceStart);
		}

		nextStep();
	}

	@Override
	public String getStepResults() {
		final String ss = (boolean) entry.getStepInfo().get("silence.start") ? "Silence at Start: "
				+ entry.getStepInfo().get("silence.start.time")
				: "";
		final String se = (boolean) entry.getStepInfo().get("silence.end") ? "Silence at End: "
				+ entry.getStepInfo().get("silence.end.time")
				: "";

		String r = ss;
		r += se.equals("") ? "" : ss.equals("") ? se : ", " + se;

		return r.equals("") ? "No silence to remove." : r + ".";
	}
}
