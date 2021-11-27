package karstenroethig.paperless.webapp.repository.specification;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import io.micrometer.core.instrument.util.StringUtils;
import karstenroethig.paperless.webapp.model.domain.AbstractEntityId_;
import karstenroethig.paperless.webapp.model.domain.changes.ChangeGroup;
import karstenroethig.paperless.webapp.model.domain.changes.ChangeGroup_;
import karstenroethig.paperless.webapp.model.dto.search.ActivityStreamSearchDto;

public class ChangesSpecifications
{
	private ChangesSpecifications() {}

	public static Specification<ChangeGroup> matchesSearchParam(ActivityStreamSearchDto activityStreamSearchDto)
	{
		return (Root<ChangeGroup> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			{
				List<Predicate> restrictions = new ArrayList<>();

				if (StringUtils.isNotBlank(activityStreamSearchDto.getEntityName()))
					restrictions.add(cb.equal(root.get(ChangeGroup_.ENTITY_NAME), activityStreamSearchDto.getEntityName()));

				if (activityStreamSearchDto.getEntityId() != null)
					restrictions.add(cb.equal(root.get(ChangeGroup_.ENTITY_ID), activityStreamSearchDto.getEntityId()));

				if (activityStreamSearchDto.getUser() != null && activityStreamSearchDto.getUser().getId() != null)
					restrictions.add(cb.equal(root.get(ChangeGroup_.createdUser).get(AbstractEntityId_.id), activityStreamSearchDto.getUser().getId()));

				return cb.and(restrictions.toArray(new Predicate[] {}));
			};
	}
}
