package karstenroethig.paperless.webapp.model.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractDtoIdArchivable extends AbstractDtoId
{
	private boolean archived = false;
}
