package karstenroethig.paperless.webapp.model.domain.changes;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import karstenroethig.paperless.webapp.model.domain.AbstractEntityId;
import karstenroethig.paperless.webapp.model.enums.ChangeItemOperationEnum;
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
@Table(name = "change_item")
public class ChangeItem extends AbstractEntityId
{
	@ManyToOne
	@JoinColumn(name = "change_group_id")
	private ChangeGroup changeGroup;

	@Column(name = "operation", length = 191, nullable = false)
	@Enumerated(EnumType.STRING)
	private ChangeItemOperationEnum operation;

	@Column(name = "entity_name", length = 191, nullable = false)
	private String entityName;

	@Column(name = "entity_id", length = 18, nullable = false)
	private Long entityId;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "changeItem")
	private List<ChangeProperty> changeProperties = new ArrayList<>();
}
