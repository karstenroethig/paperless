package karstenroethig.paperless.webapp.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

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
public class DocumentDto extends AbstractDtoId
{
	@NotNull
	@Size(min = 1, max = 255)
	private String title;

	@NotNull
	private DocumentTypeDto documentType;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateOfIssue;

	private ContactDto sender;

	private ContactDto receiver;

	private List<FileAttachmentDto> fileAttachments = new ArrayList<>();

	private List<TagDto> tags = new ArrayList<>();

	private DocumentBoxDto documentBox;

	private String description;

	private LocalDateTime createdDatetime;

	private LocalDateTime updatedDatetime;

	private boolean archived = false;

	public void addFileAttachment(FileAttachmentDto fileAttachment)
	{
		fileAttachments.add(fileAttachment);
	}

	public void addTag(TagDto tag)
	{
		tags.add(tag);
	}
}
