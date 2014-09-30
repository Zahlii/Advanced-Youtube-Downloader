package de.zahlii.youtube.download.step;

public class SilenceInfo {
	private double time_start = -1;
	private double time_end = -1;

	public double getTimeStart() {
		return this.time_start;
	}

	public double getTimeEnd() {
		return this.time_end;
	}

	public static boolean isSilenceInfo(final String line) {
		return line.contains("silencedetect")
				&& (line.contains("silence_start:") || line
						.contains("silence_end"));
	}

	public SilenceInfo(final String line) {
		this.time_start = this.extract(line, "silence_start:");
		this.time_end = this.extract(line, "silence_end:");

	}

	public boolean isValid() {
		return this.time_start >= 0 || this.time_end >= 0;
	}

	@Override
	public String toString() {
		return "silence from " + Math.max(0, this.time_start) + " to "
				+ Math.max(0, this.time_end);
	}

	private double extract(final String line, final String search) {
		double time = -1;
		if (line.contains(search)) {
			String[] p = line.split(search);

			String t;

			if (line.contains("|")) {
				p = p[1].split("\\|");
				t = p[0].trim();
			} else {
				t = p[1].trim();
			}
			time = Double.parseDouble(t.trim());
		}
		return time;
	}
}
