/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 */
package de.ytdownload.model;

/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 * 
 */
public class DownloadFormat {
	public String formatCode;
	public String extension;
	public String description;
	public String resolution;

	public DownloadFormat(String line) {
		String[] parts = line.split("\\s+");
		formatCode = parts[0];
		extension = parts[1];
		resolution = parts[2];

		int l = 3;
		if (parts.length >= 4 && parts[3].equals("only")) {
			resolution += " only";
			l = 4;
		}

		description = "";
		for (; l < parts.length; l++) {
			description += parts[l] + " ";
		}
		description = description.trim();
	}

	public static boolean isFormat(String line) {
		String[] parts = line.split("\\s+");

		if (line.contains("DASH"))
			return true;

		if (line.matches("\\s\\d+x\\d+\\s"))
			return true;

		if (parts[0].equals("ld") || parts[0].equals("standard") || parts[0].equals("hq"))
			return true;

		if (isNumeric(parts[0]) && parts.length >= 3) {
			return true;
		}

		return false;
	}

	@Override
	public String toString() {
		return formatCode + " | " + extension + " | " + resolution + " | " + description;
	}

	public static boolean isNumeric(String str) {
		try {
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
}
