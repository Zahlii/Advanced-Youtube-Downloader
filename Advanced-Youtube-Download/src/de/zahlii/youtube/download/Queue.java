package de.zahlii.youtube.download;

import java.util.ArrayList;
import java.util.LinkedList;

import de.zahlii.youtube.download.basic.Logging;
import de.zahlii.youtube.download.step.Step;
import de.zahlii.youtube.download.ui.DownloadFrame.Stage;

public class Queue implements ProgressListener {
	private static Queue instance;

	public static Queue getInstance() {
		if (instance == null) {
			instance = new Queue();
		}

		return instance;
	}

	private final ArrayList<ProgressListener> progressListeners = new ArrayList<>();

	private final LinkedList<QueueEntry> queue = new LinkedList<>();
	private int queueSizeCurrent = 0;
	private int queueSizeTotal = 0;
	private Stage status = Stage.IDLE;

	private Queue() {

	}

	public QueueEntry addDownload(final String webURL) {
		final QueueEntry q = new QueueEntry(webURL);
		addEntry(q);
		return q;
	}

	public void addEntry(final QueueEntry q) {
		q.addListener(this);
		queue.add(q);
		queueSizeTotal++;
		queueSizeCurrent++;
		beginQueue();
	}

	public void addListener(final ProgressListener l) {
		progressListeners.add(l);
	}

	public void beginQueue() {
		if (status == Stage.WORKING)
			return;

		QueueEntry q;
		if ((q = popNextItem()) != null) {
			if (q.getDownloadTempFile() != null) {
				Logging.log("Handling " + q.getDownloadTempFile().getAbsolutePath());
			}
			q.start();
			status = Stage.WORKING;
		}
	}

	public double getQueueProgress() {
		return 1 - queueSizeCurrent / Math.max(1.0, queueSizeTotal);
	}

	@Override
	public void onEntryBegin(final QueueEntry entry) {
		for (final ProgressListener l : progressListeners) {
			l.onEntryBegin(entry);
		}
	}

	@Override
	public void onEntryEnd(final QueueEntry entry) {
		queueSizeCurrent--;
		for (final ProgressListener l : progressListeners) {
			l.onEntryEnd(entry);
		}
		status = Stage.IDLE;
		beginQueue();
	}

	@Override
	public void onEntryStepBegin(final QueueEntry entry, final Step step) {
		for (final ProgressListener l : progressListeners) {
			l.onEntryStepBegin(entry, step);
		}
	}

	@Override
	public void onEntryStepEnd(final QueueEntry entry, final Step step, final long t, final double p) {
		for (final ProgressListener l : progressListeners) {
			l.onEntryStepEnd(entry, step, t, p);
		}
	}

	@Override
	public void onEntryStepProgress(final QueueEntry entry, final Step step, final double progress) {
		for (final ProgressListener l : progressListeners) {
			l.onEntryStepProgress(entry, step, progress);
		}
	}

	public QueueEntry popNextItem() {
		return queue.isEmpty() ? null : queue.removeFirst();
	}

	public void removeDownload(final String webURL) {
		for (final QueueEntry q : queue) {
			if (q.getWebURL().equals(webURL)) {
				queue.remove(q);
				q.removeListener(this);
				queueSizeTotal--;
				queueSizeCurrent--;
				return;
			}
		}
	}

	public void removeListener(final ProgressListener l) {
		progressListeners.remove(l);
	}

}
