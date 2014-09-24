package de.zahlii.youtube.download.cli;

public interface ProcessListener {
	public void processStart();

	public void processStop();

	public String processLineIn(String line);

	public void processLineOut(String line);
}
