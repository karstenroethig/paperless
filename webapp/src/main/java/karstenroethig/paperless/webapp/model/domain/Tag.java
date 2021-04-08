package karstenroethig.paperless.webapp.model.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.PreRemove;
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
@EqualsAndHashCode(callSuper = true, exclude = {"documents"})

@Entity
@Table(name = "tag")
public class Tag extends AbstractEntityId
{
	@Column(name = "name", length = 191, nullable = false)
	private String name;

	@Column(name = "description", nullable = true)
	private String description;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "document_tag", joinColumns = { @JoinColumn(name = "tag_id") }, inverseJoinColumns = { @JoinColumn(name = "document_id") })
	private Set<Document> documents = new HashSet<>();

	@Column(name = "archived", nullable = false)
	private boolean archived;

	@PreRemove
	private void removeTagFromDocuments()
	{
		documents.clear();
	}
}
