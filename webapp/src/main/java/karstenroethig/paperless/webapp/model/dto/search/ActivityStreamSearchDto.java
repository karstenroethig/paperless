package karstenroethig.paperless.webapp.model.dto.search;

import org.apache.commons.lang3.StringUtils;

import karstenroethig.paperless.webapp.model.dto.UserDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class ActivityStreamSearchDto
{
	private String entityName;
	private Long entityId;
	private UserDto user;

	public boolean hasParams()
	{
		return StringUtils.isNotBlank(entityName)
				|| entityId != null
				|| (user != null && user.getId() != null);
	}
}
