package de.zahlii.youtube.download.cli;

public interface ProcessListener {
	public String processLineIn(String line);

	public void processLineOut(String line);

	public void processStart();

	public void processStop();
}
