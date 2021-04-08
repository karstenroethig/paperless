package karstenroethig.paperless.webapp.model.dto.search;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public abstract class AbstractArchivedSearchDto
{
	private boolean showArchived = false;

	public boolean hasParams()
	{
		return showArchived;
	}
}
