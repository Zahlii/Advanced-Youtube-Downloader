/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 */
package de.ytdownload.service.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import de.ytdownload.service.Logging;

/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 * 
 */
public class CLI {

	private BufferedReader in;
	private BufferedWriter out;
	private ProcessBuilder b;
	private Process p;
	private List<ProcessListener> listener;

	public CLI(ProcessBuilder b) {
		this.b = b;
		this.listener = new ArrayList<ProcessListener>();

	}

	// @Override
	public void run() {
		try {
			b.redirectErrorStream(true);
			p = b.start();
			in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));

			while (processInLine()) {

			}
			for (ProcessListener l : this.listener) {
				l.processStop();
			}
		} catch (IOException e) {
			Logging.log("CLI run failed", e);
		}
	}

	private void processOutLine(String line) throws IOException {
		String write = null;
		Logging.log("[CLI IN]\t" + line);

		for (ProcessListener l : this.listener) {
			write = l.processLineIn(line);
			break;
		}
		if (write != null)
			out.write(write);
	}

	private boolean processInLine() throws IOException {
		String line;

		if ((line = in.readLine()) != null) {
			Logging.log("[CLI]\t" + line);
			for (ProcessListener l : this.listener) {
				l.processLineOut(line);
			}
			return true;
		}
		return false;
	}

	public void addProcessListener(ProcessListener l) {
		this.listener.add(l);
	}

	public void removeProcessListener(ProcessListener l) {
		this.listener.remove(l);
	}

}
