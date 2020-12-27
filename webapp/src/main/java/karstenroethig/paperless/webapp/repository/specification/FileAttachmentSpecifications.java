package karstenroethig.paperless.webapp.repository.specification;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import karstenroethig.paperless.webapp.model.domain.AbstractEntityId_;
import karstenroethig.paperless.webapp.model.domain.FileAttachment;
import karstenroethig.paperless.webapp.model.domain.FileAttachment_;
import karstenroethig.paperless.webapp.model.dto.DocumentDto;

public class FileAttachmentSpecifications
{
	private FileAttachmentSpecifications() {}

	public static Specification<FileAttachment> matchesDocument(DocumentDto document)
	{
		return (Root<FileAttachment> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			{
				List<Predicate> restrictions = new ArrayList<>();

				if (document != null && document.getId() != null)
					restrictions.add(cb.equal(root.get(FileAttachment_.document).get(AbstractEntityId_.id), document.getId()));

				return cb.and(restrictions.toArray(new Predicate[] {}));
			};
	}
}
