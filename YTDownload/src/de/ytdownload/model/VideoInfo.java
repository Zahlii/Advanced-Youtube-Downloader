/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 */
package de.ytdownload.model;

import java.io.File;
import java.util.List;

/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 * 
 */
public class VideoInfo {
	public String url;

	/**
	 * Get the url
	 * 
	 * @author jfruehau
	 * 
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Set the url
	 * 
	 * @author jfruehau
	 * 
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Get the thumbnail
	 * 
	 * @author jfruehau
	 * 
	 * @return the thumbnail
	 */
	public String getThumbnail() {
		return thumbnail;
	}

	/**
	 * Set the thumbnail
	 * 
	 * @author jfruehau
	 * 
	 * @param thumbnail
	 *            the thumbnail to set
	 */
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	/**
	 * Get the title
	 * 
	 * @author jfruehau
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Set the title
	 * 
	 * @author jfruehau
	 * 
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Get the description
	 * 
	 * @author jfruehau
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the description
	 * 
	 * @author jfruehau
	 * 
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Get the formats
	 * 
	 * @author jfruehau
	 * 
	 * @return the formats
	 */
	public List<DownloadFormat> getFormats() {
		return formats;
	}

	/**
	 * Set the formats
	 * 
	 * @author jfruehau
	 * 
	 * @param formats
	 *            the formats to set
	 */
	public void setFormats(List<DownloadFormat> formats) {
		this.formats = formats;
	}

	/**
	 * Get the size
	 * 
	 * @author jfruehau
	 * 
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Set the size
	 * 
	 * @author jfruehau
	 * 
	 * @param size
	 *            the size to set
	 */
	public void setSize(long size) {
		this.size = size;
	}

	/**
	 * Get the downloadTime
	 * 
	 * @author jfruehau
	 * 
	 * @return the downloadTime
	 */
	public double getDownloadTime() {
		return downloadTime;
	}

	/**
	 * Set the downloadTime
	 * 
	 * @author jfruehau
	 * 
	 * @param downloadTime
	 *            the downloadTime to set
	 */
	public void setDownloadTime(double downloadTime) {
		this.downloadTime = downloadTime;
	}

	/**
	 * Get the conversionTime
	 * 
	 * @author jfruehau
	 * 
	 * @return the conversionTime
	 */
	public double getConversionTime() {
		return conversionTime;
	}

	/**
	 * Set the conversionTime
	 * 
	 * @author jfruehau
	 * 
	 * @param conversionTime
	 *            the conversionTime to set
	 */
	public void setConversionTime(double conversionTime) {
		this.conversionTime = conversionTime;
	}

	/**
	 * Get the tempFile
	 * 
	 * @author jfruehau
	 * 
	 * @return the tempFile
	 */
	public File getTempFile() {
		return tempFile;
	}

	/**
	 * Set the tempFile
	 * 
	 * @author jfruehau
	 * 
	 * @param tempFile
	 *            the tempFile to set
	 */
	public void setTempFile(File tempFile) {
		this.tempFile = tempFile;
	}

	/**
	 * Get the mp3File
	 * 
	 * @author jfruehau
	 * 
	 * @return the mp3File
	 */
	public File getMp3File() {
		return mp3File;
	}

	/**
	 * Set the mp3File
	 * 
	 * @author jfruehau
	 * 
	 * @param mp3File
	 *            the mp3File to set
	 */
	public void setMp3File(File mp3File) {
		this.mp3File = mp3File;
	}

	/**
	 * Get the finalFile
	 * 
	 * @author jfruehau
	 * 
	 * @return the finalFile
	 */
	public File getFinalFile() {
		return finalFile;
	}

	/**
	 * Set the finalFile
	 * 
	 * @author jfruehau
	 * 
	 * @param finalFile
	 *            the finalFile to set
	 */
	public void setFinalFile(File finalFile) {
		this.finalFile = finalFile;
	}

	private String thumbnail;
	private String title;
	private String description;
	private List<DownloadFormat> formats;
	private long size = -1;
	private double downloadTime = -1;
	private double conversionTime = -1;
	private File tempFile;
	private File mp3File;
	private File finalFile;

	public DownloadFormat getFormat(String code) {
		for (DownloadFormat f : formats) {
			if (f.formatCode.equals(code))
				return f;
		}
		return null;
	}

	public DownloadFormat getBestFormat() {
		return formats.get(formats.size() - 1);
	}
}
