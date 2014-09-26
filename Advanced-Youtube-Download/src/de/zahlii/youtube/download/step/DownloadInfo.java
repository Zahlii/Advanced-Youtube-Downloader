package de.zahlii.youtube.download.step;

import de.zahlii.youtube.download.basic.Logging;

public class DownloadInfo {
	public double progress;
	public long size;
	public long done;
	public long speed;
	public long eta;

	public DownloadInfo(String line) {
		try {
			String[] parts = line.split("\\s+");
			progress = Double.parseDouble(parts[1].replace("%", ""));
			size = parseSize(parts[3]);
			done = (long) (size * progress);
			speed = parseSize(parts[5]);
			eta = parseTime(parts[7]);
		} catch (NumberFormatException e) {
			Logging.log("failed getting download progress", e);
		}
	}

	private long parseTime(String string) {
		String[] parts = string.split(":");
		long sum = 0;
		int s = parts.length;
		for (int i = 0; i < s; i++) {
			sum += Integer.valueOf(parts[i]) * Math.pow(60, s - i - 1);
		}
		return sum;
	}

	private long parseSize(String string) {
		String[] sizes = new String[] { "KiB", "MiB", "GiB" };
		long factor = 1;
		for (String key : sizes) {
			factor *= 1024;
			if (string.contains(key)) {
				string = string.replace(key, "");
				break;
			}
		}
		double val = Double.parseDouble(string.replace("/s", ""));

		return (long) Math.floor(factor * val);
	}

	public static boolean isProgress(String line) {
		return line.startsWith("[download]") && line.contains("ETA")
				&& !line.contains("Unknown");
	}
}
