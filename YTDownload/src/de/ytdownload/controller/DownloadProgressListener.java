/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 */
package de.ytdownload.controller;

import java.util.List;

import de.ytdownload.model.DownloadInfo;
import de.ytdownload.model.SilenceInfo;
import de.ytdownload.model.VideoInfo;
import radams.gracenote.webapi.GracenoteMetadata;

/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 * 
 */
public interface DownloadProgressListener {
	public void infoStart(String video);

	public void infoMetaComplete(VideoInfo f);

	public void infoImageComplete(VideoInfo f);

	public void infoComplete(VideoInfo f);

	public void downloadStart(VideoInfo f);

	public void downloadProgress(VideoInfo f, DownloadInfo i);

	public void downloadComplete(VideoInfo f);

	public void convertComplete(VideoInfo f);
	
	public void silenceComplete(VideoInfo f, List<SilenceInfo> s);

	public void metaSearchComplete(VideoInfo v, GracenoteMetadata d);

	public void complete(VideoInfo v, double totalTime);

	public void volumeComplete(VideoInfo vinfo, double volume);
}
