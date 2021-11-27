package karstenroethig.paperless.webapp.service.impl;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import karstenroethig.paperless.webapp.model.domain.AbstractEntityIdArchivable_;
import karstenroethig.paperless.webapp.model.domain.changes.ChangeGroup;
import karstenroethig.paperless.webapp.model.domain.changes.ChangeItem;
import karstenroethig.paperless.webapp.model.domain.changes.ChangeProperty;
import karstenroethig.paperless.webapp.model.dto.changes.ActivityGroupDto;
import karstenroethig.paperless.webapp.model.dto.changes.ActivityPropertyDto;
import karstenroethig.paperless.webapp.model.dto.search.ActivityStreamSearchDto;
import karstenroethig.paperless.webapp.model.enums.ActivityActionEnum;
import karstenroethig.paperless.webapp.model.enums.ChangeItemOperationEnum;
import karstenroethig.paperless.webapp.model.enums.ChangePropertyEntryOperationEnum;
import karstenroethig.paperless.webapp.model.enums.ChangePropertyPropertyTypeEnum;
import karstenroethig.paperless.webapp.repository.ChangeGroupRepository;
import karstenroethig.paperless.webapp.repository.specification.ChangesSpecifications;

@Service
@Transactional
public class ActivityStreamServiceImpl
{
	private static final Predicate<ChangeProperty> PREDICATE_CHANGE_PROPERTY_EVERYTHING = property -> true;

	private static final Predicate<ChangeProperty> PREDICATE_CHANGE_PROPERTY_IGNORE_UNARCHIVED
		= property -> !(StringUtils.equals(property.getPropertyKey(), AbstractEntityIdArchivable_.ARCHIVED)
			&& property.getNewValueBoolean() != null
			&& !property.getNewValueBoolean());

	@Autowired private UserServiceImpl userService;

	@Autowired private ChangeGroupRepository changeGroupRepository;

	public Page<ActivityGroupDto> findBySearchParams(ActivityStreamSearchDto activityStreamSearchDto, Pageable pageable)
	{
		Page<ChangeGroup> page = changeGroupRepository.findAll(ChangesSpecifications.matchesSearchParam(activityStreamSearchDto), pageable);
		return page.map(this::convertToActivity);
	}

	public Page<ActivityGroupDto> findAll(Pageable pageable)
	{
		Page<ChangeGroup> page = changeGroupRepository.findAll(pageable);
		return page.map(this::convertToActivity);
	}

	private ActivityGroupDto convertToActivity(ChangeGroup changeGroup)
	{
		if (changeGroup == null)
			return null;

		if (StringUtils.equals(changeGroup.getEntityName(), "contact")) // TODO switch to constant variable
			return createActivityGroupForContact(changeGroup);

		if (StringUtils.equals(changeGroup.getEntityName(), "document")) // TODO switch to constant variable
			return createActivityGroupForDocument(changeGroup);

		return createBasicActivityGroup(changeGroup, ActivityActionEnum.UNKNOWN);
	}

	private ActivityGroupDto createActivityGroupForContact(ChangeGroup changeGroup)
	{
		ChangeItem changeItem = changeGroup.getChangeItems().stream().findFirst().orElse(null); // FIXME Was, wenn es mehrere gibt (z. B. Files bei Documenten)?

		ActivityGroupDto activityGroup;

		if (changeItem == null)
			return createBasicActivityGroup(changeGroup, ActivityActionEnum.UNKNOWN);

		else if (changeItem.getOperation() == ChangeItemOperationEnum.CREATE)
		{
			activityGroup = createBasicActivityGroup(changeGroup, ActivityActionEnum.CONTACT_CREATED);
			activityGroup.setProperties(convertToActivity(changeItem.getChangeProperties(), PREDICATE_CHANGE_PROPERTY_IGNORE_UNARCHIVED));
		}

		else if (changeItem.getOperation() == ChangeItemOperationEnum.UPDATE)
		{
			activityGroup = createBasicActivityGroup(changeGroup, ActivityActionEnum.CONTACT_UPDATED);
			activityGroup.setProperties(convertToActivity(changeItem.getChangeProperties()));
		}

		else if (changeItem.getOperation() == ChangeItemOperationEnum.DELETE)
			return createBasicActivityGroup(changeGroup, ActivityActionEnum.CONTACT_DELETED);

		else
			return createBasicActivityGroup(changeGroup, ActivityActionEnum.UNKNOWN);

		return activityGroup;
	}

	// FIXME Methode verallgemeinern!
	private ActivityGroupDto createActivityGroupForDocument(ChangeGroup changeGroup)
	{
		ChangeItem changeItem = changeGroup.getChangeItems().stream().findFirst().orElse(null);

		ActivityGroupDto activityGroup;

		if (changeItem == null)
			return createBasicActivityGroup(changeGroup, ActivityActionEnum.UNKNOWN);

		else if (changeItem.getOperation() == ChangeItemOperationEnum.CREATE)
		{
			activityGroup = createBasicActivityGroup(changeGroup, ActivityActionEnum.DOCUMENT_CREATED);
			activityGroup.setProperties(convertToActivity(changeItem.getChangeProperties(), PREDICATE_CHANGE_PROPERTY_IGNORE_UNARCHIVED));
		}

		else if (changeItem.getOperation() == ChangeItemOperationEnum.UPDATE)
		{
			activityGroup = createBasicActivityGroup(changeGroup, ActivityActionEnum.DOCUMENT_UPDATED);
			activityGroup.setProperties(convertToActivity(changeItem.getChangeProperties()));
		}

		else if (changeItem.getOperation() == ChangeItemOperationEnum.DELETE)
			return createBasicActivityGroup(changeGroup, ActivityActionEnum.DOCUMENT_DELETED);

		else
			return createBasicActivityGroup(changeGroup, ActivityActionEnum.UNKNOWN);

		return activityGroup;
	}

	private ActivityGroupDto createBasicActivityGroup(ChangeGroup changeGroup, ActivityActionEnum action)
	{
		return createBasicActivityGroup(changeGroup, action, false);
	}

	private ActivityGroupDto createBasicActivityGroup(ChangeGroup changeGroup, ActivityActionEnum action, boolean createLink)
	{
		ActivityGroupDto activityGroup = new ActivityGroupDto();

		activityGroup.setUser(userService.transform(changeGroup.getCreatedUser()));
		activityGroup.setDatetime(changeGroup.getCreatedDatetime());
		activityGroup.setAction(action);
		activityGroup.setEntityName(changeGroup.getEntityName());
		activityGroup.setEntityTitle(changeGroup.getEntityTitle());

		if (createLink)
			activityGroup.setEntityLink(String.format("/%s/show/%s", changeGroup.getEntityName(), changeGroup.getEntityId()));

		return activityGroup;
	}

	private List<ActivityPropertyDto> convertToActivity(List<ChangeProperty> changeProperties)
	{
		return convertToActivity(changeProperties, PREDICATE_CHANGE_PROPERTY_EVERYTHING);
	}

	private List<ActivityPropertyDto> convertToActivity(List<ChangeProperty> changeProperties, Predicate<ChangeProperty> filter)
	{
		return changeProperties.stream()
			.filter(filter)
			.map(this::convertToActivity)
			.collect(Collectors.toList());
	}

	private ActivityPropertyDto convertToActivity(ChangeProperty changeProperty)
	{
		if (changeProperty == null)
			return null;

		ActivityPropertyDto activityProperty = new ActivityPropertyDto();

		activityProperty.setKey(changeProperty.getPropertyKey());
		activityProperty.setType(changeProperty.getPropertyType());

		if (changeProperty.getPropertyType() == ChangePropertyPropertyTypeEnum.TEXT
				|| changeProperty.getPropertyType() == ChangePropertyPropertyTypeEnum.REFERENCE)
			activityProperty.setValueText(changeProperty.getNewValueText());
		else if (changeProperty.getPropertyType() == ChangePropertyPropertyTypeEnum.BOOLEAN)
			activityProperty.setValueBoolean(changeProperty.getNewValueBoolean());
		else if (changeProperty.getPropertyType() == ChangePropertyPropertyTypeEnum.DATE)
			activityProperty.setValueDate(changeProperty.getNewValueDate());
		else if (changeProperty.getPropertyType() == ChangePropertyPropertyTypeEnum.DATETIME)
			activityProperty.setValueDatetime(changeProperty.getNewValueDatetime());
		else if (changeProperty.getPropertyType() == ChangePropertyPropertyTypeEnum.INTEGER)
			activityProperty.setValueInt(changeProperty.getNewValueInt());
		else if (changeProperty.getPropertyType() == ChangePropertyPropertyTypeEnum.REFERENCE_LIST)
		{
			// TODO Hier weiter
			// --> Rückgabewert der Methode ist eine Liste
			// --> Beim Aufrufer wird sie mittels flatMap eingebunden

			activityProperty.setValueText(
				changeProperty.getEntries().stream()
					.filter(entry -> entry.getOperation() == ChangePropertyEntryOperationEnum.ADD)
					.map(entry -> String.format("\"%s\"", entry.getEntryText()))
					.collect(Collectors.joining("; "))
			);
			activityProperty.setOperation(ChangePropertyEntryOperationEnum.ADD);

//			ActivityPropertyDto activityPropertyAdd = new ActivityPropertyDto();
//			activityPropertyAdd.setKey(changeProperty.getPropertyKey());
//			activityPropertyAdd.setType(changeProperty.getPropertyType());
//			activityPropertyAdd.setValueText(null);
		}

		// TODO REFERENCE_LIST -> cretae two new instances (ADD, REMOVE)
		// TODO activityProperty.setOpertaion(changePropertyEntry.getOperation());

		return activityProperty;
	}
}
