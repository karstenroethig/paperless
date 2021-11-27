package karstenroethig.paperless.webapp.model.dto.changes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import karstenroethig.paperless.webapp.model.dto.UserDto;
import karstenroethig.paperless.webapp.model.enums.ActivityActionEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ActivityGroupDto
{
	private UserDto user;

	private LocalDateTime datetime;

	private ActivityActionEnum action;

	private String entityName;

	private String entityTitle;

	private String entityLink;

	private List<ActivityItemDto> items = new ArrayList<>();

	private List<ActivityPropertyDto> properties = new ArrayList<>();

	public void addItem(ActivityItemDto item)
	{
		this.items.add(item);
	}

	public void addProperty(ActivityPropertyDto property)
	{
		this.properties.add(property);
	}
}
