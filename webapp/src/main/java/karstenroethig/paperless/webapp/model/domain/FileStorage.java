package karstenroethig.paperless.webapp.model.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

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
@Table(
	name = "file_storage",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"key"})
	}
)
public class FileStorage extends AbstractEntityId
{
	@Column(name = "key", length = 191, nullable = false)
	private String key;

	@Column(name = "file_size", nullable = false)
	private Long size;
}
