package de.zahlii.youtube.download.step;

public class StepDescriptor {
	private String stepName;
	private String stepDescription;

	public StepDescriptor(String n, String d) {
		stepName = n;
		stepDescription = d;
	}

	public String getStepName() {
		return stepName;
	}

	public String getStepDescription() {
		return stepDescription;
	}

}
