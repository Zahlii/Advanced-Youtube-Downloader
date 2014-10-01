package de.zahlii.youtube.download.step;

import de.zahlii.youtube.download.basic.Logging;

public class DownloadInfo {
	public static boolean isProgress(final String line) {
		return line.startsWith("[download]") && line.contains("ETA") && !line.contains("Unknown");
	}

	public long done;
	public long eta;
	public double progress;
	public long size;

	public long speed;

	public DownloadInfo(final String line) {
		try {
			final String[] parts = line.split("\\s+");
			progress = Double.parseDouble(parts[1].replace("%", ""));
			size = parseSize(parts[3]);
			done = (long) (size * progress);
			speed = parseSize(parts[5]);
			eta = parseTime(parts[7]);
		} catch (final NumberFormatException e) {
			Logging.log("failed getting download progress", e);
		}
	}

	private long parseSize(String string) {
		final String[] sizes = new String[] { "KiB", "MiB", "GiB" };
		long factor = 1;
		for (final String key : sizes) {
			factor *= 1024;
			if (string.contains(key)) {
				string = string.replace(key, "");
				break;
			}
		}
		final double val = Double.parseDouble(string.replace("/s", ""));

		return (long) Math.floor(factor * val);
	}

	private long parseTime(final String string) {
		final String[] parts = string.split(":");
		long sum = 0;
		final int s = parts.length;
		for (int i = 0; i < s; i++) {
			sum += Integer.valueOf(parts[i]) * Math.pow(60, s - i - 1);
		}
		return sum;
	}
}
