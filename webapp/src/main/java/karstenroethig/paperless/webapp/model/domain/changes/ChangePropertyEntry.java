package karstenroethig.paperless.webapp.model.domain.changes;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import karstenroethig.paperless.webapp.model.domain.AbstractEntityId;
import karstenroethig.paperless.webapp.model.enums.ChangePropertyEntryOperationEnum;
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
@Table(name = "change_property_entry")
public class ChangePropertyEntry extends AbstractEntityId
{
	@ManyToOne
	@JoinColumn(name = "change_property_id")
	private ChangeProperty changeProperty;

	@Column(name = "operation", length = 191, nullable = false)
	@Enumerated(EnumType.STRING)
	private ChangePropertyEntryOperationEnum operation;

	@Column(name = "entryText", nullable = false)
	private String entryText;

	@Column(name = "entry_ref", nullable = false)
	private Long entryRef;
}
