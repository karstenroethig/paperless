package karstenroethig.paperless.webapp.repository.specification;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import karstenroethig.paperless.webapp.model.domain.Contact_;
import karstenroethig.paperless.webapp.model.domain.Document;
import karstenroethig.paperless.webapp.model.domain.DocumentBox_;
import karstenroethig.paperless.webapp.model.domain.DocumentType_;
import karstenroethig.paperless.webapp.model.domain.Document_;
import karstenroethig.paperless.webapp.model.domain.Tag;
import karstenroethig.paperless.webapp.model.domain.Tag_;
import karstenroethig.paperless.webapp.model.dto.DocumentSearchDto;
import karstenroethig.paperless.webapp.model.dto.DocumentSearchDto.ContactSearchTypeEnum;
import karstenroethig.paperless.webapp.model.dto.TagDto;

public class DocumentSpecifications
{
	private DocumentSpecifications() {}

	public static Specification<Document> matchesSearchParam(DocumentSearchDto documentSearchDto)
	{
		return (Root<Document> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			{
				List<Predicate> restrictions = new ArrayList<>();

				if (StringUtils.isNotBlank(documentSearchDto.getText()))
					restrictions.add(cb.or(
							cb.like(cb.lower(root.get(Document_.title)), "%" + StringUtils.lowerCase(documentSearchDto.getText()) + "%"),
							cb.like(cb.lower(root.get(Document_.description)), "%" + StringUtils.lowerCase(documentSearchDto.getText()) + "%")));

				if (documentSearchDto.getDocumentType() != null && documentSearchDto.getDocumentType().getId() != null)
					restrictions.add(cb.equal(root.get(Document_.documentType).get(DocumentType_.id), documentSearchDto.getDocumentType().getId()));

				if (documentSearchDto.getDateOfIssueFrom() != null)
					restrictions.add(cb.greaterThanOrEqualTo(root.get(Document_.dateOfIssue), documentSearchDto.getDateOfIssueFrom()));

				if (documentSearchDto.getDateOfIssueTo() != null)
					restrictions.add(cb.lessThanOrEqualTo(root.get(Document_.dateOfIssue), documentSearchDto.getDateOfIssueTo()));

				if (documentSearchDto.getContact() != null && documentSearchDto.getContact().getId() != null)
				{
					if (documentSearchDto.getContactSearchType() == ContactSearchTypeEnum.ONLY_SENDER)
						restrictions.add(cb.equal(root.get(Document_.sender).get(Contact_.id), documentSearchDto.getContact().getId()));
					else if (documentSearchDto.getContactSearchType() == ContactSearchTypeEnum.ONLY_RECEIVER)
						restrictions.add(cb.equal(root.get(Document_.receiver).get(Contact_.id), documentSearchDto.getContact().getId()));
					else
						restrictions.add(cb.or(
								cb.equal(root.get(Document_.sender).get(Contact_.id), documentSearchDto.getContact().getId()),
								cb.equal(root.get(Document_.receiver).get(Contact_.id), documentSearchDto.getContact().getId())));
				}

				if (documentSearchDto.getTags() != null && !documentSearchDto.getTags().isEmpty())
				{
					for (TagDto tag : documentSearchDto.getTags())
					{
						Subquery<Long> sub = query.subquery(Long.class);
						Root<Tag> subRoot = sub.from(Tag.class);
						SetJoin<Document, Tag> subTags = root.join(Document_.tags);
						sub.select(subRoot.get(Tag_.id));
						sub.where(cb.equal(subTags.get(Tag_.id), tag.getId()));

						restrictions.add(cb.exists(sub));
					}
				}

				if (documentSearchDto.getDocumentBox() != null && documentSearchDto.getDocumentBox().getId() != null)
					restrictions.add(cb.equal(root.get(Document_.documentBox).get(DocumentBox_.id), documentSearchDto.getDocumentBox().getId()));

				if (!documentSearchDto.isShowArchived())
					restrictions.add(cb.equal(root.get(Document_.archived), Boolean.FALSE));

				return cb.and(restrictions.toArray(new Predicate[] {}));
			};
	}
}
