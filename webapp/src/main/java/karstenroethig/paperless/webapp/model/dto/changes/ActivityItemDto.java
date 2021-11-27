package karstenroethig.paperless.webapp.model.dto.changes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ActivityItemDto
{
	private String entityName;

	private String entityLink;
}
