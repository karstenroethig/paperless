package karstenroethig.paperless.webapp.model.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import karstenroethig.paperless.webapp.util.FileIconEnum;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class FileAttachmentDto extends AbstractDtoId
{
	private Long documentId;

	@NotNull
	private String name;

	private Long size;

	private String sizeFormatted;

	private String contentType;

	private FileIconEnum icon;

	private String hash;

	private LocalDateTime createdDatetime;

	private LocalDateTime updatedDatetime;

	private FileStorageDto fileStorage;
}
