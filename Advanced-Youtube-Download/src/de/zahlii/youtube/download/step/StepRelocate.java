package de.zahlii.youtube.download.step;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.swing.JOptionPane;

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
	
	public StepRelocate(QueueEntry entry) {
		super(entry, new StepDescriptor("FileRelocate","Saves the files under the correct name, as specified in the config"));
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doStep() {
		data = (Map<FieldKey, String>)( entry.getStepInfo().get("meta.data"));
		
		
		
		String convention = ConfigManager.getInstance().getConfig(ConfigKey.FILENAME_CONVENTION,"%artist - %title");
		// %album, %title, %artist, %track, %tracktotal, %year
		
		convention = r(convention, "%album" , FieldKey.ALBUM);
		convention = r(convention, "%title" , FieldKey.TITLE);
		convention = r(convention, "%artist" , FieldKey.ARTIST);
		convention = r(convention, "%track" , FieldKey.TRACK);
		convention = r(convention, "%tracktotal" , FieldKey.TRACK_TOTAL);
		convention = r(convention, "%year" , FieldKey.YEAR);
		
		String dirPath = ConfigManager.getInstance().getConfig(ConfigKey.DIR_TARGET,ConfigManager.TEMP_DIR.getAbsolutePath());
		
		finalFile = new File(dirPath + ConfigManager.DS + convention + entry.getExtension());

		try {
			if(finalFile.exists() && !finalFile.equals(entry.getConvertTempFile())) {
				int answer = JOptionPane.showConfirmDialog(null,
						"The file "+ finalFile.getAbsolutePath() + " already exists.\nDo you want to overwrite it?",
						"File exists",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE
						);
				
				if(answer == JOptionPane.OK_OPTION) {
					FileUtils.deleteQuietly(finalFile);
					FileUtils.moveFile(entry.getConvertTempFile(), finalFile);
					entry.setFinalMP3File(finalFile);
				} else {
					//entry.setFinalMP3File(finalFile);
				}
			} else {
				FileUtils.moveFile(entry.getConvertTempFile(), finalFile);
				entry.setFinalMP3File(finalFile);
			}
			
		} catch (IOException e) {
			Logging.log("failed to move file to new location",e);
		}
		
		nextStep();
	}
	
	private String r(String d, String s, FieldKey r) {
		return d.replace(s, Helper.sanitize(data.get(r)));
	}

	@Override
	public String getStepResults() {
		return "Moved to " + finalFile.getName()+".";
	}

}
