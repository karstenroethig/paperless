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
	@Size(min = 1, max = 191)
	private String title;

	@NotNull
	private DocumentTypeDto documentType;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateOfIssue;

	private ContactDto sender;

	private ContactDto receiver;

	private List<TagDto> tags = new ArrayList<>();

	private DocumentBoxDto documentBox;

	private String description;

	private LocalDateTime createdDatetime;

	private LocalDateTime updatedDatetime;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate reviewDate;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate deletionDate;

	private boolean archived = false;

	public void addTag(TagDto tag)
	{
		tags.add(tag);
	}
}
