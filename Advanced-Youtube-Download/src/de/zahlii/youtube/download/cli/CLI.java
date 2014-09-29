package de.zahlii.youtube.download.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import de.zahlii.youtube.download.basic.Logging;

public class CLI {

	private BufferedReader in;
	private BufferedWriter out;
	private final ProcessBuilder b;
	private Process p;
	private final List<ProcessListener> listener;

	public CLI(final ProcessBuilder b) {
		this.b = b;
		this.listener = new ArrayList<ProcessListener>();

	}

	public void run() {
		try {
			this.b.redirectErrorStream(true);
			this.p = this.b.start();
			this.in = new BufferedReader(new InputStreamReader(
					this.p.getInputStream()));
			this.out = new BufferedWriter(new OutputStreamWriter(
					this.p.getOutputStream()));

			while (this.processInLine()) {

			}
			for (final ProcessListener l : this.listener) {
				l.processStop();
			}
		} catch (final IOException e) {
			Logging.log("CLI run failed", e);
		}
	}

	private void processOutLine(final String line) throws IOException {
		String write = null;
		Logging.log("[CLI IN]\t" + line);

		for (final ProcessListener l : this.listener) {
			write = l.processLineIn(line);
			break;
		}
		if (write != null) {
			this.out.write(write);
		}
	}

	private boolean processInLine() throws IOException {
		String line;

		if ((line = this.in.readLine()) != null) {
			// Logging.log("[CLI]\t" + line);
			for (final ProcessListener l : this.listener) {
				l.processLineOut(line);
			}
			return true;
		}
		return false;
	}

	public void addProcessListener(final ProcessListener l) {
		this.listener.add(l);
	}

	public void removeProcessListener(final ProcessListener l) {
		this.listener.remove(l);
	}

}
