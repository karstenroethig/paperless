package karstenroethig.paperless.webapp.repository.specification;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import karstenroethig.paperless.webapp.model.domain.User;
import karstenroethig.paperless.webapp.model.domain.User_;
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

				if (StringUtils.isNotBlank(userSearchDto.getName()))
					restrictions.add(cb.or(
							cb.like(cb.lower(root.get(User_.username)), "%" + StringUtils.lowerCase(userSearchDto.getName()) + "%"),
							cb.like(cb.lower(root.get(User_.fullName)), "%" + StringUtils.lowerCase(userSearchDto.getName()) + "%")));

				if (userSearchDto.getEnabledSearchType() != null)
				{
					if (userSearchDto.getEnabledSearchType() == EnabledSearchTypeEnum.ENABLED)
						restrictions.add(cb.equal(root.get(User_.enabled), Boolean.TRUE));
					else if (userSearchDto.getEnabledSearchType() == EnabledSearchTypeEnum.DISABLED)
						restrictions.add(cb.equal(root.get(User_.enabled), Boolean.FALSE));
					else
						throw new IllegalArgumentException("unknown search type " + userSearchDto.getEnabledSearchType().name());
				}

				if (userSearchDto.getLockedSearchType() != null)
				{
					if (userSearchDto.getLockedSearchType() == LockedSearchTypeEnum.LOCKED)
						restrictions.add(cb.equal(root.get(User_.locked), Boolean.TRUE));
					else if (userSearchDto.getLockedSearchType() == LockedSearchTypeEnum.UNLOCKED)
						restrictions.add(cb.equal(root.get(User_.locked), Boolean.FALSE));
					else
						throw new IllegalArgumentException("unknown search type " + userSearchDto.getLockedSearchType().name());
				}

				if (userSearchDto.getNewRegisteredSearchType() != null)
				{
					if (userSearchDto.getNewRegisteredSearchType() == NewRegisteredSearchTypeEnum.NEW_REGISTERED)
						restrictions.add(cb.equal(root.get(User_.newRegisterd), Boolean.TRUE));
					else
						throw new IllegalArgumentException("unknown search type " + userSearchDto.getNewRegisteredSearchType().name());
				}

				restrictions.add(cb.equal(root.get(User_.deleted), Boolean.FALSE));

				return cb.and(restrictions.toArray(new Predicate[] {}));
			};
	}
}
