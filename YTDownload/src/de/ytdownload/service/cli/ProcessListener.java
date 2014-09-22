/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 */
package de.ytdownload.service.cli;

/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 * 
 */
public interface ProcessListener {
	public void processStart();

	public void processStop();

	public String processLineIn(String line);

	public void processLineOut(String line);
}
