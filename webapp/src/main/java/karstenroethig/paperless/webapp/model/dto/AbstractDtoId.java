package karstenroethig.paperless.webapp.model.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public abstract class AbstractDtoId
{
	protected Long id;
}
