package karstenroethig.paperless.webapp.repository.specification;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import karstenroethig.paperless.webapp.model.domain.AbstractEntityId_;
import karstenroethig.paperless.webapp.model.domain.Group;
import karstenroethig.paperless.webapp.model.domain.User;
import karstenroethig.paperless.webapp.model.domain.User_;
import karstenroethig.paperless.webapp.model.dto.GroupDto;
import karstenroethig.paperless.webapp.model.dto.UserSearchDto;
import karstenroethig.paperless.webapp.model.dto.UserSearchDto.EnabledSearchTypeEnum;
import karstenroethig.paperless.webapp.model.dto.UserSearchDto.LockedSearchTypeEnum;
import karstenroethig.paperless.webapp.model.dto.UserSearchDto.NewRegisteredSearchTypeEnum;

public class UserSpecifications
{
	private UserSpecifications() {}

	public static Specification<User> matchesSearchParam(UserSearchDto userSearchDto)
	{
		return (Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			{
				List<Predicate> restrictions = new ArrayList<>();

				addRestrictionsForName(root, cb, restrictions, userSearchDto.getName());
				addRestrictionsForGroup(root, query, cb, restrictions, userSearchDto.getGroup());
				addRestrictionsForEnabled(root, cb, restrictions, userSearchDto.getEnabledSearchType());
				addRestrictionsForLocked(root, cb, restrictions, userSearchDto.getLockedSearchType());
				addRestrictionsForNewRegistered(root, cb, restrictions, userSearchDto.getNewRegisteredSearchType());
				addRestrictionsForDeleted(root, cb, restrictions);

				return cb.and(restrictions.toArray(new Predicate[] {}));
			};
	}

	private static void addRestrictionsForName(Root<User> root, CriteriaBuilder cb, List<Predicate> restrictions, String name)
	{
		if (StringUtils.isBlank(name))
			return;

		restrictions.add(cb.or(
				cb.like(cb.lower(root.get(User_.username)), "%" + StringUtils.lowerCase(name) + "%"),
				cb.like(cb.lower(root.get(User_.fullName)), "%" + StringUtils.lowerCase(name) + "%")));
	}

	private static void addRestrictionsForGroup(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb, List<Predicate> restrictions, GroupDto group)
	{
		if (group == null || group.getId() == null)
			return;

		Subquery<Long> sub = query.subquery(Long.class);
		Root<Group> subRoot = sub.from(Group.class);
		ListJoin<User, Group> subGroups = root.join(User_.groups);
		sub.select(subRoot.get(AbstractEntityId_.id));
		sub.where(cb.equal(subGroups.get(AbstractEntityId_.id), group.getId()));

		restrictions.add(cb.exists(sub));
	}

	private static void addRestrictionsForEnabled(Root<User> root, CriteriaBuilder cb, List<Predicate> restrictions, EnabledSearchTypeEnum enabledSearchType)
	{
		if (enabledSearchType == null)
			return;

		if (enabledSearchType == EnabledSearchTypeEnum.ENABLED)
			restrictions.add(cb.equal(root.get(User_.enabled), Boolean.TRUE));
		else if (enabledSearchType == EnabledSearchTypeEnum.DISABLED)
			restrictions.add(cb.equal(root.get(User_.enabled), Boolean.FALSE));
		else
			throw new IllegalArgumentException("unknown enabled search type " + enabledSearchType.name());
	}

	private static void addRestrictionsForLocked(Root<User> root, CriteriaBuilder cb, List<Predicate> restrictions, LockedSearchTypeEnum lockedSearchType)
	{
		if (lockedSearchType == null)
			return;

		if (lockedSearchType == LockedSearchTypeEnum.LOCKED)
			restrictions.add(cb.equal(root.get(User_.locked), Boolean.TRUE));
		else if (lockedSearchType == LockedSearchTypeEnum.UNLOCKED)
			restrictions.add(cb.equal(root.get(User_.locked), Boolean.FALSE));
		else
			throw new IllegalArgumentException("unknown locked search type " + lockedSearchType.name());
	}

	private static void addRestrictionsForNewRegistered(Root<User> root, CriteriaBuilder cb, List<Predicate> restrictions, NewRegisteredSearchTypeEnum newRegisteredSearchType)
	{
		if (newRegisteredSearchType == null)
			return;

		if (newRegisteredSearchType == NewRegisteredSearchTypeEnum.NEW_REGISTERED)
			restrictions.add(cb.equal(root.get(User_.newRegisterd), Boolean.TRUE));
		else
			throw new IllegalArgumentException("unknown new registered search type " + newRegisteredSearchType.name());
	}

	private static void addRestrictionsForDeleted(Root<User> root, CriteriaBuilder cb, List<Predicate> restrictions)
	{
		restrictions.add(cb.equal(root.get(User_.deleted), Boolean.FALSE));
	}
}
