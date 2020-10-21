package karstenroethig.paperless.webapp.repository.specification;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import karstenroethig.paperless.webapp.model.domain.DocumentBox;
import karstenroethig.paperless.webapp.model.domain.DocumentBox_;
import karstenroethig.paperless.webapp.model.dto.DocumentBoxSearchDto;

public class DocumentBoxSpecifications
{
	private DocumentBoxSpecifications() {}

	public static Specification<DocumentBox> matchesSearchParam(DocumentBoxSearchDto documentBoxSearchDto)
	{
		return (Root<DocumentBox> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			{
				List<Predicate> restrictions = new ArrayList<>();

				if (!documentBoxSearchDto.isShowArchived())
					restrictions.add(cb.equal(root.get(DocumentBox_.archived), Boolean.FALSE));

				return cb.and(restrictions.toArray(new Predicate[] {}));
			};
	}
}
