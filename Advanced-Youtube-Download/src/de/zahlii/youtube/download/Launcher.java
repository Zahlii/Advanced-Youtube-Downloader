package de.zahlii.youtube.download;

import static java.lang.System.out;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileFilter;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.pushingpixels.substance.api.skin.SubstanceGraphiteGlassLookAndFeel;

import de.zahlii.youtube.download.basic.ConfigManager;
import de.zahlii.youtube.download.basic.Logging;
import de.zahlii.youtube.download.ui.DownloadFrame;

public class Launcher {
	private static String[] args;
	private static DownloadFrame mainFrame;
	private static boolean isDefault;

	public static void main(final String[] args) {
		Launcher.args = args;
		if (hasFlag("-h")) {
			out.println("Usage: adytd [URL]");
			out.println();
			out.println("Where:");
			out.println("URL\tcan be any web URL or shortened version such as youtube id");

			out.println();
			out.println("-h\t\tdisplay help options");
			out.println("-f [File]\tUsed to improve local files. File can be any path to a File, or a path containing wildcards such as *.mp3 for the filename");
			out.println("-g\t\trun a gracenote search");
			out.println("-s\t\tremove silence");
			out.println("-v\t\tapply replaygain/peak normalization");
			out.println();
			out.println("Default is:");
			out.println("URL\t\t-g -s -v");
			out.println("File\t\t-g -s -v");
			System.exit(0);
		}
		Launcher.launch();
		Launcher.parseArgs();
	}

	private static void parseArgs() {
		isDefault = !(hasFlag("-g") || hasFlag("-s") || hasFlag("-v"));
		if (!isDefault) {
			QueueEntry.setEnableGracenote(hasFlag("-g"));
			QueueEntry.setEnableSilence(hasFlag("-s"));
			QueueEntry.setEnableVolume(hasFlag("-v"));
		} else {
			QueueEntry.setEnableGracenote(true);
			QueueEntry.setEnableSilence(true);
			QueueEntry.setEnableVolume(true);
		}
		if (args.length == 0) {
			// default behaviour
		} else if (args.length == 1) {
			Queue.getInstance().addDownload(args[0]);
		} else {

			final String file;
			if ((file = getFlag("-f")) != null) {
				// wildcards
				final File f = new File(file);

				final FileFilter filter = new FileFilter() {
					@Override
					public boolean accept(final File arg0) {
						final String t = file.replace("*", ".*").replace(
								ConfigManager.DS, "\\" + ConfigManager.DS);
						if (arg0.getAbsolutePath().matches(t))
							return true;

						return false;
					}

				};
				if (file.contains("*")) {
					File root = f.getParentFile();
					while (!root.isDirectory()) {
						root = root.getParentFile();
					}
					out.println("Scanning " + root.getAbsolutePath());
					handleFiles(root.listFiles(filter));
				} else if (f.isDirectory()) {
					handleFiles(f.listFiles(new FileFilter() {

						@Override
						public boolean accept(final File arg0) {
							return !arg0.isDirectory();
						}

					}));
				} else {
					if (!(f.exists() && f.canWrite())) {
						out.println("File " + f.getAbsolutePath()
								+ " does not exist or can't be written");
						System.exit(0);
					}
					handleFiles(new File[] { f });
				}
			}
		}
	}

	private static void handleFiles(final File[] files) {

		out.println("Handling " + files.length + " files.");
		for (final File f : files) {
			if (!f.canWrite()) {
				out.println("Skipping " + f.getAbsolutePath()
						+ " because it can't be written");
				continue;
			}

			Queue.getInstance().addEntry(new QueueEntry(f));
		}
	}

	public static boolean hasFlag(final String f) {
		for (final String a : args) {
			if (f.equals(a) || a.contains(f))
				return true;
		}
		return false;
	}

	public static String getFlag(final String f) {
		for (int i = 0, l = args.length; i < l; i++) {
			final String a = args[i];
			if (f.equals(a) || a.contains(f) && (i + 1) < l)
				return args[i + 1];
		}
		return null;
	}

	private static void launch() {
		try {
			UIManager.setLookAndFeel(new SubstanceGraphiteGlassLookAndFeel());
		} catch (final UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					mainFrame = new DownloadFrame();
				} catch (final Exception e) {
					Logging.log("failed to start program", e);
				}
			}
		});
	}
}
