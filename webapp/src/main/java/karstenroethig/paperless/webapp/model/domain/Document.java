package karstenroethig.paperless.webapp.model.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
@Table(name = "document")
public class Document extends AbstractEntityIdArchivable
{
	@Column(name = "title", length = 191, nullable = false)
	private String title;

	@ManyToOne(optional = true)
	@JoinColumn(name = "document_type_id")
	private DocumentType documentType;

	@Column(name = "date_of_issue", nullable = true)
	@Type(type = "org.hibernate.type.LocalDateType")
	private LocalDate dateOfIssue;

	@ManyToOne(optional = true)
	@JoinColumn(name = "sender_id")
	private Contact sender;

	@ManyToOne(optional = true)
	@JoinColumn(name = "receiver_id")
	private Contact receiver;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "document")
	private List<FileAttachment> fileAttachments = new ArrayList<>();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "document_tag", joinColumns = { @JoinColumn(name = "document_id") }, inverseJoinColumns = { @JoinColumn(name = "tag_id") })
	private Set<Tag> tags = new HashSet<>();

	@ManyToOne(optional = true)
	@JoinColumn(name = "document_box_id")
	private DocumentBox documentBox;

	@Column(name = "description", nullable = true)
	private String description;

	@Column(name = "created_datetime", nullable = false)
	@Type(type = "org.hibernate.type.LocalDateTimeType")
	private LocalDateTime createdDatetime;

	@Column(name = "updated_datetime", nullable = false)
	@Type(type = "org.hibernate.type.LocalDateTimeType")
	private LocalDateTime updatedDatetime;

	@Column(name = "review_date", nullable = true)
	@Type(type = "org.hibernate.type.LocalDateType")
	private LocalDate reviewDate;

	@Column(name = "deletion_date", nullable = true)
	@Type(type = "org.hibernate.type.LocalDateType")
	private LocalDate deletionDate;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "document")
	private List<Comment> comments = new ArrayList<>();

	public void addTag(Tag tag)
	{
		tags.add(tag);
	}

	public void removeTag(Tag tag)
	{
		tags.remove(tag);
	}

	public void removeFileAttachment(FileAttachment fileAttachment)
	{
		fileAttachment.setDocument(null);
		fileAttachments.remove(fileAttachment);
	}

	public void removeComment(Comment comment)
	{
		comment.setDocument(null);
		comments.remove(comment);
	}
}
