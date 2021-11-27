package karstenroethig.paperless.webapp.model.domain.changes;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

import org.hibernate.annotations.Type;

import karstenroethig.paperless.webapp.model.domain.AbstractEntityId;
import karstenroethig.paperless.webapp.model.enums.ChangePropertyPropertyTypeEnum;
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
@Table(name = "change_property")
public class ChangeProperty extends AbstractEntityId
{
	@ManyToOne
	@JoinColumn(name = "change_item_id")
	private ChangeItem changeItem;

	@Column(name = "property_key", length = 191, nullable = false)
	private String propertyKey;

	@Column(name = "property_type", length = 191, nullable = false)
	@Enumerated(EnumType.STRING)
	private ChangePropertyPropertyTypeEnum propertyType;

	@Column(name = "old_value_text", nullable = true)
	private String oldValueText;

	@Column(name = "new_value_text", nullable = true)
	private String newValueText;

	@Column(name = "old_value_boolean", nullable = true)
	private Boolean oldValueBoolean;

	@Column(name = "new_value_boolean", nullable = true)
	private Boolean newValueBoolean;

	@Column(name = "old_value_date", nullable = true)
	@Type(type = "org.hibernate.type.LocalDateType")
	private LocalDate oldValueDate;

	@Column(name = "new_value_date", nullable = true)
	@Type(type = "org.hibernate.type.LocalDateType")
	private LocalDate newValueDate;

	@Column(name = "old_value_datetime", nullable = true)
	@Type(type = "org.hibernate.type.LocalDateTimeType")
	private LocalDateTime oldValueDatetime;

	@Column(name = "new_value_datetime", nullable = true)
	@Type(type = "org.hibernate.type.LocalDateTimeType")
	private LocalDateTime newValueDatetime;

	@Column(name = "old_value_int", nullable = true)
	private Long oldValueInt;

	@Column(name = "new_value_int", nullable = true)
	private Long newValueInt;

	@Column(name = "old_value_ref", nullable = true)
	private Long oldValueRef;

	@Column(name = "new_value_ref", nullable = true)
	private Long newValueRef;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "changeProperty")
	private List<ChangePropertyEntry> entries = new ArrayList<>();
}
