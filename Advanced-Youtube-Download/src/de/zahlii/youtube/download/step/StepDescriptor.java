package de.zahlii.youtube.download.step;

public class StepDescriptor {
	private final String stepDescription;
	private final String stepName;

	public StepDescriptor(final String n, final String d) {
		stepName = n;
		stepDescription = d;
	}

	public String getStepDescription() {
		return stepDescription;
	}

	public String getStepName() {
		return stepName;
	}

}
