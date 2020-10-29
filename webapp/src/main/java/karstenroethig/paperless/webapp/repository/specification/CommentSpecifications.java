package karstenroethig.paperless.webapp.repository.specification;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import karstenroethig.paperless.webapp.model.domain.Comment;
import karstenroethig.paperless.webapp.model.domain.Comment_;
import karstenroethig.paperless.webapp.model.domain.Document_;
import karstenroethig.paperless.webapp.model.dto.DocumentDto;

public class CommentSpecifications
{
	private CommentSpecifications() {}

	public static Specification<Comment> matchesDocument(DocumentDto document)
	{
		return (Root<Comment> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			{
				List<Predicate> restrictions = new ArrayList<>();

				if (document != null && document.getId() != null)
					restrictions.add(cb.equal(root.get(Comment_.document).get(Document_.id), document.getId()));

				return cb.and(restrictions.toArray(new Predicate[] {}));
			};
	}
}
