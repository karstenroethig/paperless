package karstenroethig.paperless.webapp.model.domain.changes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import karstenroethig.paperless.webapp.model.domain.AbstractEntityId;
import karstenroethig.paperless.webapp.model.domain.User;
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
@Table(name = "change_group")
public class ChangeGroup extends AbstractEntityId
{
	@Column(name = "entity_name", length = 191, nullable = false)
	private String entityName;

	@Column(name = "entity_id", length = 18, nullable = false)
	private Long entityId;

	@Column(name = "entity_title", length = 191, nullable = false)
	private String entityTitle;

	@Column(name = "created_datetime", nullable = false)
	@Type(type = "org.hibernate.type.LocalDateTimeType")
	private LocalDateTime createdDatetime;

	@ManyToOne
	@JoinColumn(name = "created_user_id")
	private User createdUser;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "changeGroup")
	private List<ChangeItem> changeItems = new ArrayList<>();
}
