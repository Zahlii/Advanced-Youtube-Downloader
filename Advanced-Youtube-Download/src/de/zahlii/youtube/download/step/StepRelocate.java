package de.zahlii.youtube.download.step;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.jaudiotagger.tag.FieldKey;

import de.zahlii.youtube.download.QueueEntry;
import de.zahlii.youtube.download.basic.ConfigManager;
import de.zahlii.youtube.download.basic.ConfigManager.ConfigKey;
import de.zahlii.youtube.download.basic.Helper;
import de.zahlii.youtube.download.basic.Logging;

public class StepRelocate extends Step {
	private Map<FieldKey, String> data;
	private File finalFile;

	public StepRelocate(final QueueEntry entry) {
		super(
				entry,
				new StepDescriptor("FileRelocate",
						"Saves the files under the correct name, as specified in the config"));
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doStep() {
		if ((boolean) this.entry.getStepInfo().get("skipped")) {
			Logging.log("Skipping relocate");
			this.nextStep();
			return;
		}
		this.data = (Map<FieldKey, String>) (this.entry.getStepInfo()
				.get("meta.data"));

		final String dirPath = ConfigManager.getInstance().getConfig(
				ConfigKey.DIR_TARGET, ConfigManager.TEMP_DIR.getAbsolutePath());

		if (this.data != null) {
			String convention = ConfigManager.getInstance().getConfig(
					ConfigKey.FILENAME_CONVENTION, "%artist - %title");
			// %album, %title, %artist, %track, %tracktotal, %year

			convention = this.r(convention, "%album", FieldKey.ALBUM);
			convention = this.r(convention, "%title", FieldKey.TITLE);
			convention = this.r(convention, "%artist", FieldKey.ARTIST);
			convention = this.r(convention, "%track", FieldKey.TRACK);
			convention = this
					.r(convention, "%tracktotal", FieldKey.TRACK_TOTAL);
			convention = this.r(convention, "%year", FieldKey.YEAR);

			this.finalFile = new File(dirPath + ConfigManager.DS + convention
					+ this.entry.getExtension());
		} else {
			this.finalFile = new File(dirPath + ConfigManager.DS
					+ this.entry.getDownloadTempFile().getName());
		}

		if (this.finalFile.exists()) {

			final Runnable r = new Runnable() {

				@Override
				public void run() {
					// this is set to true whenever a temporary file has been
					// added for conversion
					final boolean show = (boolean) StepRelocate.this.entry
							.getStepInfo().get("is_forked");

					final int answer;

					if (!show) {
						answer = JOptionPane
								.showConfirmDialog(
										null,
										"The file "
												+ StepRelocate.this.finalFile
														.getAbsolutePath()
												+ " already exists.\nDo you want to overwrite it?",
										"File exists",
										JOptionPane.YES_NO_OPTION,
										JOptionPane.WARNING_MESSAGE);

					} else {
						answer = JOptionPane.OK_OPTION;
					}

					if (answer == JOptionPane.OK_OPTION) {
						FileUtils.deleteQuietly(StepRelocate.this.finalFile);
						try {
							FileUtils.moveFile(StepRelocate.this.entry
									.getConvertTempFile(),
									StepRelocate.this.finalFile);
						} catch (final IOException e) {
							Logging.log("failed to move file to new location",
									e);
						}
						StepRelocate.this.entry
								.setFinalMP3File(StepRelocate.this.finalFile);
					} else {
						// entry.setFinalMP3File(finalFile);
					}

				}

			};
			if (SwingUtilities.isEventDispatchThread()) {
				r.run();
			} else {
				SwingUtilities.invokeLater(r);
			}
		} else {
			try {
				FileUtils.moveFile(this.entry.getConvertTempFile(),
						this.finalFile);
				this.entry.setFinalMP3File(this.finalFile);
			} catch (final IOException e) {
				Logging.log("failed to move file to new location", e);
			}
		}

		this.nextStep();
	}

	private String r(final String d, final String s, final FieldKey r) {
		return d.replace(s, Helper.sanitize(this.data.get(r)));
	}

	@Override
	public String getStepResults() {
		return this.finalFile != null ? "Moved to " + this.finalFile.getName()
				+ "." : "";
	}

}
