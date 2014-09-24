package de.zahlii.youtube.download.basic;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.imageio.ImageIO;

import de.zahlii.youtube.download.basic.net.WebNavigator;

public class Helper {
	public static String getFileID(String webURL) {
		try {
			byte[] bytesOfMessage = webURL.getBytes("UTF-8");

			MessageDigest md = MessageDigest.getInstance("MD5");

			
			byte[] thedigest = md.digest(bytesOfMessage);
			return new String(thedigest, "UTF-8");
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
			return sanitize(webURL);
		}
	}
	
	public static String sanitize(String main) {
		return main.replace("<", "").replace(">", "").replace(":", "").replace("/", "")
				.replace("\\", "").replace("|", "").replace("?", "").replace("*", "")
				.replace("\"", "").replace("\r","").replace("\n","");
	}
	
	public static BufferedImage downloadImage(String url) {
		InputStream iSReader = WebNavigator.getInstance().navigateStream(url);
		try {
			return ImageIO.read(iSReader);
		} catch (IOException e) {
			Logging.log("failed to download artwork",e);
			return new BufferedImage(500,500,BufferedImage.TYPE_4BYTE_ABGR);
		}
	}
}
