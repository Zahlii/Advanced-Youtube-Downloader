package de.zahlii.youtube.download.step;

import java.util.ArrayList;
import java.util.List;

import de.zahlii.youtube.download.QueueEntry;
import de.zahlii.youtube.download.basic.ConfigManager;
import de.zahlii.youtube.download.cli.CLI;
import de.zahlii.youtube.download.cli.ProcessAdapter;

public class StepSilenceDetect extends Step {
	private final ArrayList<SilenceInfo> silence = new ArrayList<SilenceInfo>();
	
	public StepSilenceDetect(QueueEntry entry) {
		super(entry, new StepDescriptor("SilenceDetect","Detects different phases of silence and cuts them out at the start and end of the clip."));
	}

	@Override
	public void doStep() {
		ProcessBuilder n = new ProcessBuilder();
		
		List<String> meta = new ArrayList<String>();
		meta.add(ConfigManager.FFMPEG.getAbsolutePath());
		meta.add("-i");
		meta.add(entry.getDownloadTempFile().getAbsolutePath());
		meta.add("-af");
		meta.add("silencedetect=n=-50dB:d=1");
		meta.add("-f");
		meta.add("null");
		meta.add("-");
		n.command(meta);

		CLI y = new CLI(n);
		

		
		

		FFMPEGTimeProcessListener x = new FFMPEGTimeProcessListener() {
			@Override
			public void onProgress(double progress) {
				reportProgress(progress);				
			}			
		};
		y.addProcessListener(x);
		y.addProcessListener(new ProcessAdapter() {
			@Override
			public void processLineOut(String line) {
				if(SilenceInfo.isSilenceInfo(line)) {
					SilenceInfo si = new SilenceInfo(line);
					silence.add(si);
				}				
			}
			
		});
		y.run();
		
		// we got the silence now!		
		double firstSilenceEnd = 0, lastSilenceStart = 0;
		
		boolean cutStart = false, cutEnd = false;
		
		// no silence at all
		if(silence.size() == 0) {
			// skip 
		} else if(silence.size() == 1) {
			// has to be at the end because no end was given
			lastSilenceStart = silence.get(0).getTimeStart();
			cutEnd = true;
		} else {
			// silence at all ends
			for(SilenceInfo s : silence) {
				if(firstSilenceEnd == 0 && s.getTimeEnd() != -1)
					firstSilenceEnd = s.getTimeEnd();
				
				if(s.getTimeStart() != -1)
					lastSilenceStart = s.getTimeStart();
			}
			cutStart = true;
		}
		
		entry.getStepInfo().put("silence.start", cutStart);
		
		if(cutStart) {			
			entry.getStepInfo().put("silence.start.time", firstSilenceEnd);
		}
		
		entry.getStepInfo().put("silence.end", cutEnd);
		if(cutEnd) {			
			entry.getStepInfo().put("silence.end.time", lastSilenceStart);
		}
		
		nextStep();
	}

	@Override
	public String getStepResults() {
		String ss = (boolean)entry.getStepInfo().get("silence.start") ? "Silence at Start: " + entry.getStepInfo().get("silence.start.time") : "";
		String se = (boolean)entry.getStepInfo().get("silence.end") ? "Silence at End: " + entry.getStepInfo().get("silence.end.time") : "";
		
		
		String r = "";
		if(ss != "")
			r += ss + (se != "" ? ", " + se  : "");
		
		return r.equals("") ? "No silence to remove." : r+".";
	}


}
