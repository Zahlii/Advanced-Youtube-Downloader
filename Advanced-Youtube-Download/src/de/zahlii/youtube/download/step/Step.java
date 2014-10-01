package de.zahlii.youtube.download.step;

import java.util.ArrayList;

import de.zahlii.youtube.download.QueueEntry;

/**
 * Represent one step such as downloading, converting, searching data... Used to
 * structure the workflow and make it easier to add/remove steps based on
 * settings.
 * 
 * @author Zahlii
 * 
 */
public abstract class Step {
	protected StepDescriptor stepDescriptor;
	protected QueueEntry entry;

	private final ArrayList<StepListener> stepListeners = new ArrayList<>();

	public Step(final QueueEntry entry, final StepDescriptor descr) {
		this.entry = entry;
		stepDescriptor = descr;
	}

	public void addListener(final StepListener l) {
		stepListeners.add(l);
	}

	/**
	 * This method is responsible for carrying out the action. IMPORTANT: In the
	 * end, nextStep() has to be called in order for the next Step to begin.
	 * This is used so that threaded/asynchronous actions can be carried out.
	 */
	public abstract void doStep();

	public StepDescriptor getStepDescriptor() {
		return stepDescriptor;
	}

	/**
	 * This should return the results of the Step, such as analyzing results or
	 * file paths affected, for representation in the UI.
	 * 
	 * @return
	 */
	public abstract String getStepResults();

	/**
	 * Informs the current queue entry to start the next Step.
	 */
	protected void nextStep() {
		entry.nextStep();
	}

	public void removeListener(final StepListener l) {
		stepListeners.remove(l);
	}

	/**
	 * Can be used by the implementing subclass to inform every listener about
	 * the progress of the current progress, such as download percentage.
	 * 
	 * @param progress
	 *            current progress, ranging within [0,1]
	 */
	protected void reportProgress(final double progress) {
		for (final StepListener l : stepListeners) {
			l.stepProgress(progress);
		}
	}

}
