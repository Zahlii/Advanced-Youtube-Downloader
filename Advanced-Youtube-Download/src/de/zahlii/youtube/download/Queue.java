package de.zahlii.youtube.download;

import java.util.ArrayList;
import java.util.LinkedList;

import de.zahlii.youtube.download.step.Step;
import de.zahlii.youtube.download.ui.DownloadPanel.Stage;

public class Queue implements ProgressListener {
	private static Queue instance;
	
	private Queue() {
		
	}
	
	public static Queue getInstance() {
		if(instance == null)
			instance = new Queue();
		
		return instance;
	}
	
	private ArrayList<ProgressListener> progressListeners = new ArrayList<>();
	private LinkedList<QueueEntry> queue = new LinkedList<>();
	private Stage status = Stage.IDLE;
	private int queueSizeTotal = 0;
	private int queueSizeCurrent = 0;
	
	public void addListener(ProgressListener l) {
		progressListeners.add(l);
	}
	
	public void removeListener(ProgressListener l) {
		progressListeners.remove(l);
	}
	
	public QueueEntry addDownload(String webURL) {
		QueueEntry q = new QueueEntry(webURL);
		addEntry(q);
		return q;
	}
	
	public void removeDownload(String webURL) {
		for(QueueEntry q : queue) {
			if(q.getWebURL().equals(webURL)) {
				queue.remove(q);
				q.removeListener(this);
				queueSizeTotal--;
				queueSizeCurrent--;
				return;
			}
		}
	}
	
	public QueueEntry popNextItem() {
		return queue.isEmpty() ? null : queue.removeFirst();
	}
	
	public double getQueueProgress() {
		return 1 - (queueSizeCurrent)/Math.max(1.0,queueSizeTotal);
	}
	
	public void beginQueue() {
		if(status == Stage.WORKING)
			return;
		
		
		QueueEntry q;
		if((q = popNextItem()) != null) {
			q.start();
			status = Stage.WORKING;
		}
	}

	@Override
	public void onEntryBegin(QueueEntry entry) {
		for(ProgressListener l : progressListeners) {
			l.onEntryBegin(entry);
		}		
	}

	@Override
	public void onEntryStepBegin(QueueEntry entry, Step step) {
		for(ProgressListener l : progressListeners) {
			l.onEntryStepBegin(entry, step);
		}
	}

	@Override
	public void onEntryStepProgress(QueueEntry entry, Step step, double progress) {
		for(ProgressListener l : progressListeners) {
			l.onEntryStepProgress(entry, step, progress);
		}
	}

	@Override
	public void onEntryStepEnd(QueueEntry entry, Step step, long t, double p) {
		for(ProgressListener l : progressListeners) {
			l.onEntryStepEnd(entry, step, t, p);
		}
	}

	@Override
	public void onEntryEnd(QueueEntry entry) {
		queueSizeCurrent--;
		for(ProgressListener l : progressListeners) {
			l.onEntryEnd(entry);
		}
		status = Stage.IDLE;		
		beginQueue();
	}

	public void addEntry(QueueEntry q) {
		q.addListener(this);
		queue.add(q);
		queueSizeTotal++;
		queueSizeCurrent++;
		beginQueue();
	}
	
}
