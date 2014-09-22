package de.ytdownload.model;

/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 */

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;

import radams.gracenote.webapi.GracenoteMetadata;
import de.ytdownload.service.Logging;

/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 * 
 */
public class Song {
	public Color rowColor;

	private AudioFile song;
	private AudioHeader head;
	private Tag tag;
	private BasicFileAttributes attrib;

	public Song(File songfile) {

		try {
			this.song = AudioFileIO.read(songfile);
			head = song.getAudioHeader();
			tag = song.getTag();
			Path p = Paths.get(this.getFile().getAbsolutePath());
			attrib = Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes();
		} catch (IOException | InvalidAudioFrameException | CannotReadException | TagException
				| ReadOnlyFileException e) {
			Logging.log("failed loading audio file", e);
			head = null;
			tag = null;
			song = null;
		}
	}

	@Override
	public String toString() {
		return getName();
	}

	public BasicFileAttributes getAttrib() {
		return attrib;
	}

	public File getFile() {
		return song.getFile();
	}

	public String getName() {
		return FilenameUtils.getBaseName(song.getFile().getAbsolutePath());
	}

	public boolean isValid() {
		return head != null;
	}

	public AudioHeader getHeader() {
		return head;
	}

	public Tag getTag() {
		return tag;
	}

	public void applyData(GracenoteMetadata data) {
		Map<FieldKey, String> d = data.getTagInfo(this);
		for (Entry<FieldKey, String> e : d.entrySet()) {
			if (!e.getValue().equals(""))
				try {
					tag.setField(e.getKey(), e.getValue());
				} catch (KeyNotFoundException | FieldDataInvalidException | NullPointerException e1) {
					Logging.log("failed loading and setting " + e.getKey(), e1);
				}
		}
		Artwork a = data.loadArtwork();
		if (a != null) {
			tag.deleteArtworkField();
			try {
				tag.addField(a);
			} catch (FieldDataInvalidException e1) {
				Logging.log("failed loading and setting artwork", e1);
			}
		}

	}
}
