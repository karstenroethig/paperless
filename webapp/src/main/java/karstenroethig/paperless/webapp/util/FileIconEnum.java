package karstenroethig.paperless.webapp.util;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;

import lombok.Getter;

public enum FileIconEnum
{
	WORD("fas fa-file-word file-icon-color-word", "doc", "docx"),
	EXCEL("fas fa-file-excel file-icon-color-excel", "xls", "xlsx"),
	CSV("fas fa-file-csv file-icon-color-excel", "csv"),
	POWERPOINT("fas fa-file-powerpoint file-icon-color-powerpoint", "ppt", "pptx"),
	ARCHIVE("fas fa-file-archive file-icon-color-archive", "zip", "rar", "tar", "gzip", "gz", "7z"),
	PDF("fas fa-file-pdf file-icon-color-powerpoint", "pdf"),
	HTML("fas fa-file-code file-icon-color-other", "htm", "html"),
	TEXT("fas fa-file-alt file-icon-color-other", "txt", "ini", "java", "js", "css"),
	VIDEO("fas fa-file-video file-icon-color-powerpoint", "mov", "mp4", "m4v", "avi", "mpg", "mkv", "3gp", "webm", "wmv"),
	AUDIO("fas fa-file-audio file-icon-color-word", "mp3", "m4a", "wav"),
	IMAGE("fas fa-file-image file-icon-color-excel", "jpg", "jpeg", "gif", "png"),

	OTHER("fas fa-file file-icon-color-other");

	@Getter
	private final String styleClass;

	private final List<String> fileExtensions;

	private FileIconEnum(String styleClass, String... fileExtensions)
	{
		this.styleClass = styleClass;
		this.fileExtensions = fileExtensions != null ? Stream.of(fileExtensions).collect(Collectors.toList()) : Collections.emptyList();
	}

	public static FileIconEnum findFileIcon(String filename)
	{
		for (FileIconEnum fileIcon : values())
		{
			if (fileIcon.fileExtensions.contains(FilenameUtils.getExtension(filename)))
				return fileIcon;
		}

		return OTHER;
	}
}
