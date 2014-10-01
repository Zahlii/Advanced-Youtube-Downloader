package de.zahlii.youtube.download.step;

import java.io.File;

import de.zahlii.youtube.download.QueueEntry;
import de.zahlii.youtube.download.basic.ConfigManager;
import de.zahlii.youtube.download.cli.CLI;
import de.zahlii.youtube.download.cli.ProcessAdapter;

public class StepDownload extends Step {

	public StepDownload(final QueueEntry entry) {
		super(entry, new StepDescriptor("FileDownload", "Using yt-download.exe to extract the highest quality video available for this link"));
	}

	@Override
	public void doStep() {
		final ProcessBuilder n = new ProcessBuilder(ConfigManager.YOUTUBE_DL.getAbsolutePath(), "-o", ConfigManager.TEMP_DIR.getAbsolutePath() + ConfigManager.DS + "%(title)s.%(ext)s",
				entry.getWebURL());

		final CLI y = new CLI(n);
		y.addProcessListener(new ProcessAdapter() {
			private int current = 1;
			private int total = 1;

			@Override
			public void processLineOut(final String line) {
				if (DownloadInfo.isProgress(line)) {
					final DownloadInfo dinf = new DownloadInfo(line);
					final double c = dinf.progress / 100.0;
					double base = (double) (current - 1) / (double) total;

					base += c / total;
					StepDownload.this.reportProgress(base);
				}
				// [download] Downloading video #1 of 9
				if (line.contains("Downloading video #")) {
					final String[] p = line.split("video #")[1].split(" of ");
					current = Integer.parseInt(p[0]);
					total = Integer.parseInt(p[1]);
				}
				if (line.contains("[download]") && line.contains("Destination:")) {
					final String p = line.split("Destination:")[1].trim();
					StepDownload.this.entry.addDownloadTempFile(new File(p));
				}
				if (line.contains("[download]") && line.contains("has already been")) {
					String p = line.split("download")[1].substring(2);
					p = p.split("has already been")[0].trim();
					StepDownload.this.entry.addDownloadTempFile(new File(p));
				}
			}
		});
		y.run();

		nextStep();
	}

	@Override
	public String getStepResults() {
		return "Download finished to " + entry.getDownloadTempFile().getName() + ".";
	}

}
