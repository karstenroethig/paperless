package karstenroethig.paperless.webapp.model.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true, exclude = {"username", "password", "repeatPassword", "fullName", "enabled", "locked", "newRegistered", "deleted", "authorities", "sessionAuthorities", "groups"})
public class UserDto extends AbstractDtoId
{
	@NotNull
	@Size(min = 1, max = 255)
	private String username;

	@Size(max = 255)
	private String password;

	@Size(max = 255)
	private String repeatPassword;

	@NotNull
	@Size(min = 1, max = 255)
	private String fullName;

	private boolean enabled = false;

	private boolean locked = false;

	private boolean newRegistered = false;

	private boolean deleted = false;

	private List<String> authorities = new ArrayList<>();

	private List<String> sessionAuthorities = new ArrayList<>();

	private List<GroupDto> groups = new ArrayList<>();

	public void addAuthority(String authority)
	{
		authorities.add(authority);
	}

	public boolean hasAuthority(String authority)
	{
		return authorities.contains(authority);
	}

	public void addSessionAuthority(String sessionAuthority)
	{
		sessionAuthorities.add(sessionAuthority);
	}

	public boolean hasSessionAuthority(String authority)
	{
		return sessionAuthorities.contains(authority);
	}

	public List<String> getMissingSessionAuthorities()
	{
		return sessionAuthorities.stream()
				.filter(Predicate.not(authorities::contains))
				.collect(Collectors.toList());
	}

	public void addGroup(GroupDto group)
	{
		groups.add(group);
	}
}
