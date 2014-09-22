/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 */
package de.ytdownload;

import java.awt.EventQueue;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.pushingpixels.substance.api.skin.SubstanceGraphiteGlassLookAndFeel;

import radams.gracenote.webapi.GracenoteException;
import de.ytdownload.model.Song;
import de.ytdownload.ui.frame.MetaSearchUI;

/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 * 
 */
public class Test {
	/**
	 * TODO ADD METHOD DESCRIPTION
	 * 
	 * @author jfruehau
	 * 
	 * @param args
	 * @throws IOException
	 * @throws GracenoteException
	 */
	public static void main(String[] args) throws IOException, GracenoteException {
		// Download d = new Download("h6sFG7qOd4A");
		// d.start();

		try {
			UIManager.setLookAndFeel(new SubstanceGraphiteGlassLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					File f = new File("E:\\Musik\\filsh\\");
					File[] x = f.listFiles(new FilenameFilter() {

						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".mp3");
						}

					});

					MetaSearchUI ui = new MetaSearchUI();

					for (File s : x) {
						ui.handleMetaSearch(new Song(s));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		

	}
}
