package karstenroethig.paperless.webapp.model.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

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

@Entity
@Table(name = "document_type")
public class DocumentType extends AbstractEntityId
{
	@Column(name = "name", length = 191, nullable = false)
	private String name;

	@Column(name = "description", nullable = true)
	private String description;

	@Column(name = "archived", nullable = false)
	private boolean archived;
}
