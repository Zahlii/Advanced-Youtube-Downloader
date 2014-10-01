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
		if ((boolean) entry.getStepInfo().get("skipped")) {
			Logging.log("Skipping relocate");
			nextStep();
			return;
		}
		data = (Map<FieldKey, String>) entry.getStepInfo().get("meta.data");

		final String dirPath = ConfigManager.getInstance().getConfig(
				ConfigKey.DIR_TARGET, ConfigManager.TEMP_DIR.getAbsolutePath());

		if (data != null) {
			String convention = ConfigManager.getInstance().getConfig(
					ConfigKey.FILENAME_CONVENTION, "%artist - %title");
			// %album, %title, %artist, %track, %tracktotal, %year

			convention = r(convention, "%album", FieldKey.ALBUM);
			convention = r(convention, "%title", FieldKey.TITLE);
			convention = r(convention, "%artist", FieldKey.ARTIST);
			convention = r(convention, "%track", FieldKey.TRACK);
			convention = r(convention, "%tracktotal", FieldKey.TRACK_TOTAL);
			convention = r(convention, "%year", FieldKey.YEAR);

			finalFile = new File(dirPath + ConfigManager.DS + convention
					+ entry.getExtension());
		} else {
			finalFile = new File(dirPath + ConfigManager.DS
					+ entry.getDownloadTempFile().getName());
		}

		if (finalFile.exists()) {

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
												+ finalFile.getAbsolutePath()
												+ " already exists.\nDo you want to overwrite it?",
										"File exists",
										JOptionPane.YES_NO_OPTION,
										JOptionPane.WARNING_MESSAGE);

					} else {
						answer = JOptionPane.OK_OPTION;
					}

					if (answer == JOptionPane.OK_OPTION) {
						FileUtils.deleteQuietly(finalFile);
						try {
							FileUtils.moveFile(StepRelocate.this.entry
									.getConvertTempFile(), finalFile);
						} catch (final IOException e) {
							Logging.log("failed to move file to new location",
									e);
						}
						StepRelocate.this.entry.setFinalMP3File(finalFile);
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
				FileUtils.moveFile(entry.getConvertTempFile(), finalFile);
				entry.setFinalMP3File(finalFile);
			} catch (final IOException e) {
				Logging.log("failed to move file to new location", e);
			}
		}

		nextStep();
	}

	@Override
	public String getStepResults() {
		return finalFile != null ? "Moved to " + finalFile.getName() + "." : "";
	}

	private String r(final String d, final String s, final FieldKey r) {
		return d.replace(s, Helper.sanitize(data.get(r)));
	}

}
