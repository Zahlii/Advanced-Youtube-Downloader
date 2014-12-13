package de.zahlii.youtube.download.basic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * logging functions
 * 
 * @author Zahlii
 * 
 */
public class Logging {
	private static boolean isSet;
	private static boolean printStackTrace = true;

	/**
	 * 
	 * return the current path
	 * 
	 * @author Zahlii
	 * 
	 * @return
	 */
	public static String getPathName() {
		File f = new File(Logging.class.getProtectionDomain().getCodeSource().getLocation().getPath());

		final String sep = System.getProperty("file.separator");

		if (f.getAbsolutePath().contains(sep + "bin") || f.getAbsolutePath().contains(".jar")) {
			f = f.getParentFile();
		}

		final String p = f.getAbsolutePath().replace("%20", " ");

		if (!isSet) {
			System.out.println("basedir is " + p);
			isSet = true;
		}
		return p;
	}

	/**
	 * 
	 * logs a message with it's timestamp
	 * 
	 * @author Zahlii
	 * 
	 * @param message
	 *            message string
	 */
	public static void log(final String message) {
		final StackTraceElement s = Thread.currentThread().getStackTrace()[2];

		final String cls = s.getClassName();
		final String shortCls = cls.substring(cls.lastIndexOf(".") + 1, cls.length());

		final String red = padRight(shortCls + "." + s.getMethodName() + "(" + s.getLineNumber() + ")", 35);

		final String msg = getTimeStamp() + " - [@ " + red + "] " + message;
		System.err.println(msg);
		appendString("log.txt", msg);
	}

	/**
	 * 
	 * logs a message and an exception
	 * 
	 * @author Zahlii
	 * 
	 * @param message
	 *            message string
	 * @param e
	 *            exception to be logged
	 */
	public static void log(final String message, final Exception e) {
		final StackTraceElement s = Thread.currentThread().getStackTrace()[2];

		final String cls = s.getClassName();
		final String shortCls = cls.substring(cls.lastIndexOf(".") + 1, cls.length());

		final String red = padRight(shortCls + "." + s.getMethodName() + "(" + s.getLineNumber() + ")", 35);
		log(message + "[ " + e.getClass().getSimpleName() + " - " + e.getMessage() + " ] from " + red);

		if (!printStackTrace)
			return;

		appendException("log.txt", e);

	}

	/**
	 * 
	 * logs a message with a given amount of tabs at the beginning
	 * 
	 * @author Zahlii
	 * 
	 * @param message
	 * @param level
	 */
	public static void log(final String message, int level) {
		final StringBuilder sb = new StringBuilder();
		while (level-- > 0) {
			sb.append("\t");
		}
		log(sb.toString() + message);
	}

	public static String padRight(final String s, final int n) {
		return String.format("%1$-" + n + "s", s);
	}

	/**
	 * 
	 * log into a file
	 * 
	 * @author Zahlii
	 * 
	 * @param file
	 * @param e
	 */
	private static void appendException(final String file, final Exception e) {
		final String f = getPathName() + "/" + file;

		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f, true)))) {
			e.printStackTrace(out);
		} catch (final IOException e1) {
			Logging.log("failed writing into " + f, e);
		}
	}

	/**
	 * 
	 * log into a file
	 * 
	 * @author Zahlii
	 * 
	 * @param file
	 * @param text
	 */
	private static void appendString(final String file, final String text) {
		final String f = getPathName() + "/" + file;

		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f, true)))) {
			out.println(text);
		} catch (final IOException e) {
			Logging.log("failed writing into " + f, e);
		}
	}

	/**
	 * 
	 * get the formatted time
	 * 
	 * @author Zahlii
	 * 
	 * @return a timestamp
	 */
	private static String getTimeStamp() {

		final Date d = new Date();
		final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSS dd.MM.yyyy");

		return df.format(d);
	}

}
