package karstenroethig.paperless.webapp.model.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class FileAttachmentUploadDto
{
	@NotNull
	private DocumentDto document;

	@NotEmpty
	private MultipartFile[] files;
}
