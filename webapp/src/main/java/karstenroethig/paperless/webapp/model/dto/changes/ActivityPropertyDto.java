package karstenroethig.paperless.webapp.model.dto.changes;

import java.time.LocalDate;
import java.time.LocalDateTime;

import karstenroethig.paperless.webapp.model.enums.ChangePropertyEntryOperationEnum;
import karstenroethig.paperless.webapp.model.enums.ChangePropertyPropertyTypeEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ActivityPropertyDto
{
	private String key;

	private ChangePropertyPropertyTypeEnum type;

	private String valueText;

	private Boolean valueBoolean;

	private LocalDate valueDate;

	private LocalDateTime valueDatetime;

	private Long valueInt;

	private ChangePropertyEntryOperationEnum operation;

	public String createMessageKey(String entityName)
	{
		StringBuilder messageKey = new StringBuilder();
		messageKey.append("activityStream.");
		messageKey.append(entityName);
		messageKey.append(".");
		messageKey.append(key);

		if (type == ChangePropertyPropertyTypeEnum.BOOLEAN)
		{
			messageKey.append(".");
			messageKey.append(valueBoolean != null ? valueBoolean.toString() : null);
		}

		if (operation != null)
		{
			messageKey.append(".");
			messageKey.append(operation.name());
		}

		return messageKey.toString();
	}

	public String getPropertyValue()
	{
		if (type == ChangePropertyPropertyTypeEnum.TEXT
				|| type == ChangePropertyPropertyTypeEnum.REFERENCE
				|| type == ChangePropertyPropertyTypeEnum.REFERENCE_LIST)
			return valueText;

		if (type == ChangePropertyPropertyTypeEnum.BOOLEAN)
			return valueBoolean != null ? valueBoolean.toString() : null;

		if (type == ChangePropertyPropertyTypeEnum.INTEGER)
			return valueInt != null ? valueInt.toString() : null;

		return null;
	}
}
