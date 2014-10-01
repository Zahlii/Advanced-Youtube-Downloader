package de.zahlii.youtube.download.step;

public class StepDescriptor {
	private final String stepName;
	private final String stepDescription;

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
