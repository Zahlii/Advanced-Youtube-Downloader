package de.zahlii.youtube.download;

import de.zahlii.youtube.download.step.Step;

public interface ProgressListener {
	public void onEntryBegin(QueueEntry entry);

	public void onEntryStepBegin(QueueEntry entry, Step step);

	public void onEntryStepProgress(QueueEntry entry, Step step, double progress);

	public void onEntryStepEnd(QueueEntry entry, Step step, long t,
			double progress);

	public void onEntryEnd(QueueEntry entry);
}
