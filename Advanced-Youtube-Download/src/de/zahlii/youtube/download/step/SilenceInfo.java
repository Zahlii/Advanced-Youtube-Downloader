package de.zahlii.youtube.download.step;

public class SilenceInfo {
	private double time_start = -1;
	private double time_end = -1;
	
	public double getTimeStart() {
		return time_start;
	}

	public double getTimeEnd() {
		return time_end;
	}

	public static boolean isSilenceInfo(String line) {
		return line.contains("silencedetect") && (line.contains("silence_start:") || line.contains("silence_end"));
	}
	
	public SilenceInfo(String line) {
		time_start = extract(line, "silence_start:");
		time_end = extract(line, "silence_end:");
		
	}
	
	@Override	
	public String toString() {
		return "silence from " + Math.max(0, time_start) + " to " +  Math.max(0, time_end);
	}
	
	private double extract(String line, String search) {
		double time = -1;
		if(line.contains(search)) {
			String[] p = line.split(search);
			
			String t;
			
			if(line.contains("|")) {
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
