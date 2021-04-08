package karstenroethig.paperless.webapp.repository.specification;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import karstenroethig.paperless.webapp.model.domain.Tag;
import karstenroethig.paperless.webapp.model.domain.Tag_;
import karstenroethig.paperless.webapp.model.dto.search.TagSearchDto;

public class TagSpecifications
{
	private TagSpecifications() {}

	public static Specification<Tag> matchesSearchParam(TagSearchDto tagSearchDto)
	{
		return (Root<Tag> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			{
				List<Predicate> restrictions = new ArrayList<>();

				if (!tagSearchDto.isShowArchived())
					restrictions.add(cb.equal(root.get(Tag_.archived), Boolean.FALSE));

				return cb.and(restrictions.toArray(new Predicate[] {}));
			};
	}
}
