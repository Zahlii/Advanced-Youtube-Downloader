package de.zahlii.youtube.download.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import de.zahlii.youtube.download.basic.ConfigManager;
import de.zahlii.youtube.download.basic.Logging;

public class CLI {

	private static void printArgs(final List<String> list) {
		final List<String> copy = new ArrayList<String>(list);
		for (int i = 0, l = copy.size(); i < l; i++) {
			String c = copy.get(i);
			if (c.contains(":" + ConfigManager.DS)) {
				final String[] p = c.split("\\\\");
				final int x = p.length;
				c = p[x - 2] + ConfigManager.DS + p[x - 1];
				copy.remove(i);
				copy.add(i, c);
			}
		}
		Logging.log("executing\n\t" + copy.toString());
	}

	private final ProcessBuilder b;
	private BufferedReader in;
	private final List<ProcessListener> listener;

	private Process p;

	public CLI(final ProcessBuilder b) {
		this.b = b;
		listener = new ArrayList<ProcessListener>();

	}

	public void addProcessListener(final ProcessListener l) {
		listener.add(l);
	}

	public void removeProcessListener(final ProcessListener l) {
		listener.remove(l);
	}

	public void run() {
		printArgs(b.command());
		try {
			b.redirectErrorStream(true);
			p = b.start();
			in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));

			while (processInLine()) {

			}
			for (final ProcessListener l : listener) {
				l.processStop();
			}
		} catch (final IOException e) {
			Logging.log("CLI run failed", e);
		}
	}

	private boolean processInLine() throws IOException {
		String line;

		if ((line = in.readLine()) != null) {
			// Logging.log("[CLI]\t" + line);
			for (final ProcessListener l : listener) {
				l.processLineOut(line);
			}
			return true;
		}
		return false;
	}

}
