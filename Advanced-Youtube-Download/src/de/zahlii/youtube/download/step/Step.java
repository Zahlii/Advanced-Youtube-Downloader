package de.zahlii.youtube.download.step;

import java.util.ArrayList;

import de.zahlii.youtube.download.QueueEntry;

public abstract class Step {
	protected StepDescriptor stepDescriptor;
	protected QueueEntry entry;
	
	private ArrayList<StepListener> stepListeners = new ArrayList<>();
	
	public Step(QueueEntry entry, StepDescriptor descr) {
		this.entry = entry;
		this.stepDescriptor = descr;
	}
	
	public abstract void doStep();
	
	public StepDescriptor getStepDescriptor() {
		return stepDescriptor;
	}
	
	public void addListener(StepListener l) {
		stepListeners.add(l);
	}
	
	public void removeListener(StepListener l) {
		stepListeners.remove(l);
	}
	
	protected void nextStep() {
		entry.nextStep();
	}
	
	protected void reportProgress(double progress) {
		for(StepListener l : stepListeners) {
			l.stepProgress(progress);
		}
	}
	
	public abstract String getStepResults();
	
}
