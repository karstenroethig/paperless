package karstenroethig.paperless.webapp.model.domain;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode

@MappedSuperclass
public abstract class AbstractEntityId
{
	@Column(length = 18, nullable = false, unique = true)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	private Long id;
}
