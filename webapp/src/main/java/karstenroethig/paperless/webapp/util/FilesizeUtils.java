package karstenroethig.paperless.webapp.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class FilesizeUtils
{
	private static final long GB = 1024l * 1024l * 1024l;
	private static final long MB = 1024l * 1024l;
	private static final long KB = 1024l;

	private static final NumberFormat GB_FORMAT = new DecimalFormat("#,###.0#");
	private static final NumberFormat MB_FORMAT = new DecimalFormat("#,###.#");
	private static final NumberFormat LITTLE_SIZE_FORMAT = new DecimalFormat("#,###");

	private FilesizeUtils() {}

	public static String formatFilesize(long bytes)
	{
		if (bytes > GB)
		{
			float gb = (float)bytes / GB;
			return GB_FORMAT.format(gb) + " GB";
		}
		else if (bytes > MB)
		{
			float mb = (float)bytes / MB;
			return MB_FORMAT.format(mb) + " MB";
		}
		else if (bytes > KB)
		{
			float kb = (float)bytes / KB;
			return LITTLE_SIZE_FORMAT.format(kb) + " kB";
		}

		return LITTLE_SIZE_FORMAT.format(bytes) + " Byte(s)";
	}
}
