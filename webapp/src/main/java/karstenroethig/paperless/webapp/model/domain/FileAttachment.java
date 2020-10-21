package karstenroethig.paperless.webapp.model.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PreRemove;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

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
@Table(name = "file_attachment")
public class FileAttachment extends AbstractEntityId
{
	@JoinColumn(name = "file_storage_id")
	@ManyToOne
	private FileStorage fileStorage;

	@Column(name = "key", length = 255, nullable = false)
	private String key;

	@ManyToOne
	@JoinColumn(name = "document_id")
	private Document document;

	@Column(name = "name", length = 255, nullable = false)
	private String name;

	@Column(name = "file_size", nullable = false)
	private Long size;

	@Column(name = "content_type", length = 255, nullable = true)
	private String contentType;

	@Column(name = "hash", length = 255, nullable = false)
	private String hash;

	@Column(name = "created_datetime", nullable = false)
	@Type(type = "org.hibernate.type.LocalDateTimeType")
	private LocalDateTime createdDatetime;

	@Column(name = "updated_datetime", nullable = false)
	@Type(type = "org.hibernate.type.LocalDateTimeType")
	private LocalDateTime updatedDatetime;

	@PreRemove
	private void removeFileAttachmentFromDocument()
	{
		Document theDocument = getDocument();
		if (theDocument != null)
			theDocument.removeFileAttachment(this);
	}
}
