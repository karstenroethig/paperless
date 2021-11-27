package karstenroethig.paperless.webapp.model.domain;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)

@MappedSuperclass
public abstract class AbstractEntityIdArchivable extends AbstractEntityId
{
	@Column(name = "archived", nullable = false)
	private boolean archived;
}
