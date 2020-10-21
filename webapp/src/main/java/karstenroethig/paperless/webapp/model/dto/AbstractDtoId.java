package karstenroethig.paperless.webapp.model.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
public abstract class AbstractDtoId
{
	@Getter
	@Setter
	protected Long id;
}
