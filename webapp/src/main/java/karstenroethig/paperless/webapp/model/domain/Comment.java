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
@Table(name = "comment")
public class Comment extends AbstractEntityId
{
	@ManyToOne
	@JoinColumn(name = "document_id")
	private Document document;

	@Column(name = "text", nullable = true)
	private String text;

	@Column(name = "created_datetime", nullable = false)
	@Type(type = "org.hibernate.type.LocalDateTimeType")
	private LocalDateTime createdDatetime;

	@Column(name = "updated_datetime", nullable = true)
	@Type(type = "org.hibernate.type.LocalDateTimeType")
	private LocalDateTime updatedDatetime;

	@Column(name = "deleted", nullable = false)
	private boolean deleted;

	@ManyToOne
	@JoinColumn(name = "author_id")
	private User author;

	@PreRemove
	private void removeCommentFromDocument()
	{
		Document theDocument = getDocument();
		if (theDocument != null)
			theDocument.removeComment(this);
	}
}
