package karstenroethig.paperless.webapp.model.dto;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserSearchDto
{
	public static final List<EnabledSearchTypeEnum> ALL_AVAILABLE_ENABLED_SEARCH_TYPES = Stream.of(EnabledSearchTypeEnum.values()).collect(Collectors.toUnmodifiableList());

	public static final List<LockedSearchTypeEnum> ALL_AVAILABLE_LOCKED_SEARCH_TYPES = Stream.of(LockedSearchTypeEnum.values()).collect(Collectors.toUnmodifiableList());

	public static final List<NewRegisteredSearchTypeEnum> ALL_AVAILABLE_NEW_REGISTERED_SEARCH_TYPES = Stream.of(NewRegisteredSearchTypeEnum.values()).collect(Collectors.toUnmodifiableList());

	private String name;

	private EnabledSearchTypeEnum enabledSearchType;

	private LockedSearchTypeEnum lockedSearchType;

	private NewRegisteredSearchTypeEnum newRegisteredSearchType;

	public boolean hasParams()
	{
		return StringUtils.isNotBlank(name)
				|| enabledSearchType != null
				|| lockedSearchType != null
				|| newRegisteredSearchType != null;
	}

	public enum EnabledSearchTypeEnum
	{
		ENABLED,
		DISABLED;
	}

	public enum LockedSearchTypeEnum
	{
		LOCKED,
		UNLOCKED;
	}

	public enum NewRegisteredSearchTypeEnum
	{
		NEW_REGISTERED;
	}
}
