package de.ytdownload.controller;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;

import radams.gracenote.webapi.GracenoteMetadata;
import de.ytdownload.controller.ConfigManager.ConfigKey;
import de.ytdownload.model.Song;
import de.ytdownload.service.Logging;
import de.ytdownload.ui.frame.InfoFrame;

/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 */

/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 * 
 */
public class SongManager {
	private static SongManager instance;
	private List<Song> files;

	private File startingDir = new File(ConfigManager.getInstance().getConfig(ConfigKey.DIR_TARGET));

	private SongManager() {
		files = new ArrayList<Song>();
		updateFileList();
	}

	public void setWorkingDirectory(File newdir) {
		if (!newdir.isDirectory())
			return;

		this.startingDir = newdir;
		updateFileList();
	}

	public File getWorkingDirectory() {
		return startingDir;
	}

	public void updateFileList() {
		/*
		(new Thread() {
			@Override
			public void run() {
				File[] mp3files = startingDir.listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						return (name.contains(".mp3") || name.contains(".m4a"));
					}

				});

				files.clear();

				for (File f : mp3files) {
					files.add(new Song(f));
				}
			}
		}).start();
		*/
	}

	public List<Song> getSongs() {
		return files;
	}

	public static SongManager getInstance() {
		if (instance == null)
			instance = new SongManager();

		return instance;
	}

	public void safeFile(Song s, GracenoteMetadata data) {
		try {
			AudioFile f = AudioFileIO.read(s.getFile());
			Map<FieldKey, String> d = data.getTagInfo(s);
			for (Entry<FieldKey, String> e : d.entrySet()) {
				if (!e.getValue().equals(""))
					f.getTag().setField(e.getKey(), e.getValue());
			}
			Artwork a = data.loadArtwork();
			if (a != null) {
				f.getTag().deleteArtworkField();
				f.getTag().addField(a);
			}
			AudioFileIO.write(f);

			Files.move(
					Paths.get(s.getFile().getAbsolutePath()),
					Paths.get(startingDir.getAbsolutePath() + ConfigManager.DS
							+ data.getNewFileName(s)), REPLACE_EXISTING);
		} catch (CannotReadException | IOException | TagException | ReadOnlyFileException
				| InvalidAudioFrameException | CannotWriteException e) {
			Logging.log("failed safing song file", e);
		}
	}

	/**
	 * TODO ADD METHOD DESCRIPTION
	 * 
	 * @author jfruehau
	 * 
	 * @param s
	 * @param f
	 */
	public void safeFile(Song s, InfoFrame fr) {
		try {
			AudioFile f = AudioFileIO.read(s.getFile());

			Map<FieldKey, String> data = new HashMap<FieldKey, String>();
			data.put(FieldKey.ARTIST, fr.getArtist());
			data.put(FieldKey.ALBUM_ARTIST, fr.getAlbumArtist());
			data.put(FieldKey.CONDUCTOR, fr.getArtist());
			data.put(FieldKey.ALBUM, fr.getAlbum());
			data.put(FieldKey.TITLE, fr.getSongtitle());
			data.put(FieldKey.TRACK, fr.getTrack());
			data.put(FieldKey.TRACK_TOTAL, fr.getTrackCount());
			data.put(FieldKey.YEAR, fr.getYear());
			data.put(FieldKey.MOOD, fr.getMood());

			data.put(FieldKey.GENRE, fr.getGenre());
			data.put(FieldKey.TEMPO, fr.getTempo());
			data.put(FieldKey.COMMENT, s.getName());

			for (Entry<FieldKey, String> e : data.entrySet()) {
				if (!e.getValue().equals(""))
					f.getTag().setField(e.getKey(), e.getValue());
			}
			Artwork a = fr.loadArtWork();
			if (a != null) {
				f.getTag().deleteArtworkField();
				f.getTag().addField(a);
			}
			AudioFileIO.write(f);

			Files.move(
					Paths.get(s.getFile().getAbsolutePath()),
					Paths.get(startingDir.getAbsolutePath() + ConfigManager.DS
							+ fr.getNewFileName(s)), REPLACE_EXISTING);
		} catch (CannotReadException | IOException | TagException | ReadOnlyFileException
				| InvalidAudioFrameException | CannotWriteException e) {
			Logging.log("failed safing song file", e);
		}

	}
}
