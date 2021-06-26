package karstenroethig.paperless.webapp.util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileTypeUtils
{
	private static final byte[] FILE_SIGNATURE_XML = new byte[] {0x3C, 0x3F, 0x78, 0x6D, 0x6C, 0x20};

	private static final byte[] FILE_SIGNATURE_ZIP = new byte[] {0x50, 0x4B, 0x03, 0x04};
	private static final byte[] FILE_SIGNATURE_ZIP_EMPTY_ARCHIVE = new byte[] {0x50, 0x4B, 0x05, 0x06};
	private static final byte[] FILE_SIGNATURE_ZIP_SPANNED_ARCHIVE = new byte[] {0x50, 0x4B, 0x07, 0x08};

	private FileTypeUtils() {}

	public static boolean isXmlFile(Path filePath)
	{
		byte[] fileSignature = readFileSignature(filePath, FILE_SIGNATURE_XML.length);

		return Arrays.compare(FILE_SIGNATURE_XML, fileSignature) == 0;
	}

	public static boolean isZipArchive(Path filePath)
	{
		byte[] fileSignature = readFileSignature(filePath, FILE_SIGNATURE_ZIP.length);

		return Arrays.compare(FILE_SIGNATURE_ZIP, fileSignature) == 0
				|| Arrays.compare(FILE_SIGNATURE_ZIP_EMPTY_ARCHIVE, fileSignature) == 0
				|| Arrays.compare(FILE_SIGNATURE_ZIP_SPANNED_ARCHIVE, fileSignature) == 0;
	}

	private static byte[] readFileSignature(Path filePath, int lengthOfSignature)
	{
		byte[] fileSignature = new byte[lengthOfSignature];

		try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r"))
		{
			raf.read(fileSignature);
		}
		catch (IOException ex)
		{
			log.error("Error on reading file signature", ex);
			return null;
		}

		return fileSignature;
	}
}
