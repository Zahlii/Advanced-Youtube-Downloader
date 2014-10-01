package de.zahlii.youtube.download.step;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.zahlii.youtube.download.cli.ProcessAdapter;

public abstract class FFMPEGTimeProcessListener extends ProcessAdapter {
	private double timeTotal;

	public abstract void onProgress(double progress);

	@Override
	public void processLineOut(final String line) {
		if (line.contains("Duration:")) {
			timeTotal = getTime(line);
		}
		if (line.contains("time=")) {
			final double time = getTime(line);
			onProgress(time / timeTotal);
		}
	}

	private double getTime(final String line) {
		final Pattern p = Pattern.compile("(\\d{2,2}):(\\d{2,2}):(\\d{2,2})\\.(\\d{2,2})");
		final Matcher m = p.matcher(line);

		double r = 0;
		if (m.find()) {
			final int hrs = Integer.parseInt(m.group(1));
			final int mins = Integer.parseInt(m.group(2));
			final int secs = Integer.parseInt(m.group(3));
			final int ms = Integer.parseInt(m.group(4));

			r = hrs * 3600 * 1000 + mins * 60 * 1000 + secs * 1000 + ms;
		}
		return r;
	}
}
