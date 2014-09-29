package de.zahlii.youtube.download;

import java.util.ArrayList;
import java.util.LinkedList;

import de.zahlii.youtube.download.basic.Logging;
import de.zahlii.youtube.download.step.Step;
import de.zahlii.youtube.download.ui.DownloadFrame.Stage;

public class Queue implements ProgressListener {
	private static Queue instance;

	private Queue() {

	}

	public static Queue getInstance() {
		if (instance == null) {
			instance = new Queue();
		}

		return instance;
	}

	private final ArrayList<ProgressListener> progressListeners = new ArrayList<>();
	private final LinkedList<QueueEntry> queue = new LinkedList<>();
	private Stage status = Stage.IDLE;
	private int queueSizeTotal = 0;
	private int queueSizeCurrent = 0;

	public void addListener(final ProgressListener l) {
		this.progressListeners.add(l);
	}

	public void removeListener(final ProgressListener l) {
		this.progressListeners.remove(l);
	}

	public QueueEntry addDownload(final String webURL) {
		final QueueEntry q = new QueueEntry(webURL);
		this.addEntry(q);
		return q;
	}

	public void removeDownload(final String webURL) {
		for (final QueueEntry q : this.queue) {
			if (q.getWebURL().equals(webURL)) {
				this.queue.remove(q);
				q.removeListener(this);
				this.queueSizeTotal--;
				this.queueSizeCurrent--;
				return;
			}
		}
	}

	public QueueEntry popNextItem() {
		return this.queue.isEmpty() ? null : this.queue.removeFirst();
	}

	public double getQueueProgress() {
		return 1 - (this.queueSizeCurrent) / Math.max(1.0, this.queueSizeTotal);
	}

	public void beginQueue() {
		if (this.status == Stage.WORKING)
			return;

		QueueEntry q;
		if ((q = this.popNextItem()) != null) {
			Logging.log("Handling " + q.getDownloadTempFile().getAbsolutePath());
			q.start();
			this.status = Stage.WORKING;
		}
	}

	@Override
	public void onEntryBegin(final QueueEntry entry) {
		for (final ProgressListener l : this.progressListeners) {
			l.onEntryBegin(entry);
		}
	}

	@Override
	public void onEntryStepBegin(final QueueEntry entry, final Step step) {
		for (final ProgressListener l : this.progressListeners) {
			l.onEntryStepBegin(entry, step);
		}
	}

	@Override
	public void onEntryStepProgress(final QueueEntry entry, final Step step,
			final double progress) {
		for (final ProgressListener l : this.progressListeners) {
			l.onEntryStepProgress(entry, step, progress);
		}
	}

	@Override
	public void onEntryStepEnd(final QueueEntry entry, final Step step,
			final long t, final double p) {
		for (final ProgressListener l : this.progressListeners) {
			l.onEntryStepEnd(entry, step, t, p);
		}
	}

	@Override
	public void onEntryEnd(final QueueEntry entry) {
		this.queueSizeCurrent--;
		for (final ProgressListener l : this.progressListeners) {
			l.onEntryEnd(entry);
		}
		this.status = Stage.IDLE;
		this.beginQueue();
	}

	public void addEntry(final QueueEntry q) {
		q.addListener(this);
		this.queue.add(q);
		this.queueSizeTotal++;
		this.queueSizeCurrent++;
		this.beginQueue();
	}

}
