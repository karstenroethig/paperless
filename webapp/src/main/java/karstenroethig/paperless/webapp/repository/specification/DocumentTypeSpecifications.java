package karstenroethig.paperless.webapp.repository.specification;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import karstenroethig.paperless.webapp.model.domain.DocumentType;
import karstenroethig.paperless.webapp.model.domain.DocumentType_;
import karstenroethig.paperless.webapp.model.dto.DocumentTypeSearchDto;

public class DocumentTypeSpecifications
{
	private DocumentTypeSpecifications() {}

	public static Specification<DocumentType> matchesSearchParam(DocumentTypeSearchDto documentTypeSearchDto)
	{
		return (Root<DocumentType> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			{
				List<Predicate> restrictions = new ArrayList<>();

				if (!documentTypeSearchDto.isShowArchived())
					restrictions.add(cb.equal(root.get(DocumentType_.archived), Boolean.FALSE));

				return cb.and(restrictions.toArray(new Predicate[] {}));
			};
	}
}
