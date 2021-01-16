package karstenroethig.paperless.webapp.model.dto;

import java.util.ArrayList;
import java.util.List;

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
@EqualsAndHashCode(callSuper = true, exclude = {"name"})
public class GroupDto extends AbstractDtoId
{
	@NotNull
	@Size(min = 1, max = 255)
	private String name;

	private List<UserDto> members = new ArrayList<>();

	public void addMember(UserDto member)
	{
		members.add(member);
	}
}
