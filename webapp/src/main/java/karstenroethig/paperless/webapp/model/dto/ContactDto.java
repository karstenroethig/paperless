package karstenroethig.paperless.webapp.model.dto;

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
@EqualsAndHashCode(callSuper = true)
public class ContactDto extends AbstractDtoId
{
	@NotNull
	@Size(min = 1, max = 255)
	private String name;

	private boolean archived = false;
}
