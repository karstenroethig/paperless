package karstenroethig.paperless.webapp.model.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class DocumentSearchDto extends AbstractArchivedSearchDto
{
	public static final List<ContactSearchTypeEnum> ALL_AVAILABLE_CONTACT_SEARCH_TYPES = Stream.of(ContactSearchTypeEnum.values()).collect(Collectors.toUnmodifiableList());

	private String text;

	private DocumentTypeDto documentType;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateOfIssueFrom;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateOfIssueTo;

	private ContactDto contact;

	private ContactSearchTypeEnum contactSearchType;

	private List<TagDto> tags;

	private DocumentBoxDto documentBox;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate reviewDateFrom;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate reviewDateTo;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate deletionDateFrom;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate deletionDateTo;

	@Override
	public boolean hasParams()
	{
		return StringUtils.isNotBlank(text)
				|| (documentType != null && documentType.getId() != null)
				|| dateOfIssueFrom != null
				|| dateOfIssueTo != null
				|| (contact != null && contact.getId() != null)
				|| (tags != null && !tags.isEmpty())
				|| (documentBox != null && documentBox.getId() != null)
				|| reviewDateFrom != null
				|| reviewDateTo != null
				|| deletionDateFrom != null
				|| deletionDateTo != null
				|| super.hasParams();
	}

	public enum ContactSearchTypeEnum
	{
		ONLY_SENDER,
		ONLY_RECEIVER,
		SENDER_OR_RECEIVER;
	}
}
