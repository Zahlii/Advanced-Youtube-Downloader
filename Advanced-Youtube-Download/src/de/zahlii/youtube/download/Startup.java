package de.zahlii.youtube.download;

import java.awt.EventQueue;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.pushingpixels.substance.api.skin.SubstanceGraphiteGlassLookAndFeel;

import de.zahlii.youtube.download.step.Step;

public class Startup {
	public static void main(String[] args) {
		
		try {
			UIManager.setLookAndFeel(new SubstanceGraphiteGlassLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					init();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			
		});
	}
	private static void init() {
		final Queue q = Queue.getInstance();
		q.addDownload("https://www.youtube.com/watch?v=JhY9GOhFwN4");
		q.addListener(new ProgressListener() {

			@Override
			public void onEntryBegin(QueueEntry entry) {
				System.out.println("Starting with " +entry.getWebURL());				
			}

			@Override
			public void onEntryStepBegin(QueueEntry entry, Step step) {
				System.out.println("Starting with step " + step.getStepDescriptor().getStepName() + " of "+entry.getWebURL());
			}

			@Override
			public void onEntryStepProgress(QueueEntry entry, Step step,
					double progress) {
				System.out.println("Progress with step " + step.getStepDescriptor().getStepName() + " (" + progress + ") of "+entry.getWebURL());
			}

			@Override
			public void onEntryStepEnd(QueueEntry entry, Step step, long time, double progress) {
				System.out.println("Step " + step.getStepDescriptor().getStepName() + " of "+entry.getWebURL() + " ended after " + time +"ms with result " + step.getStepResults());
				
			}

			@Override
			public void onEntryEnd(QueueEntry entry) {
				System.out.println("Finished with " +entry.getWebURL() + " total progress "  + q.getQueueProgress());	
			}
			
		});
		q.beginQueue();
	}
}
