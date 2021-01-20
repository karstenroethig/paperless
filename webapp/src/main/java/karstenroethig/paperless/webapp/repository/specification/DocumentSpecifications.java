package karstenroethig.paperless.webapp.repository.specification;

import java.time.LocalDate;
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

import karstenroethig.paperless.webapp.model.domain.AbstractEntityId_;
import karstenroethig.paperless.webapp.model.domain.Document;
import karstenroethig.paperless.webapp.model.domain.Document_;
import karstenroethig.paperless.webapp.model.domain.Tag;
import karstenroethig.paperless.webapp.model.dto.ContactDto;
import karstenroethig.paperless.webapp.model.dto.DocumentBoxDto;
import karstenroethig.paperless.webapp.model.dto.DocumentSearchDto;
import karstenroethig.paperless.webapp.model.dto.DocumentSearchDto.ContactSearchTypeEnum;
import karstenroethig.paperless.webapp.model.dto.DocumentTypeDto;
import karstenroethig.paperless.webapp.model.dto.TagDto;

public class DocumentSpecifications
{
	private DocumentSpecifications() {}

	public static Specification<Document> matchesSearchParam(DocumentSearchDto documentSearchDto)
	{
		return (Root<Document> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			{
				List<Predicate> restrictions = new ArrayList<>();

				addRestrictionsForText(root, cb, restrictions, documentSearchDto.getText());
				addRestrictionsForDocumentType(root, cb, restrictions, documentSearchDto.getDocumentType());
				addRestrictionsForDateOfIssue(root, cb, restrictions, documentSearchDto.getDateOfIssueFrom(), documentSearchDto.getDateOfIssueTo());
				addRestrictionsForContact(root, cb, restrictions, documentSearchDto.getContact(), documentSearchDto.getContactSearchType());
				addRestrictionsForTags(root, query, cb, restrictions, documentSearchDto.getTags());
				addRestrictionsForDocumentBox(root, cb, restrictions, documentSearchDto.getDocumentBox());
				addRestrictionsForReviewDate(root, cb, restrictions, documentSearchDto.getReviewDateFrom(), documentSearchDto.getReviewDateTo());
				addRestrictionsForDeletionDate(root, cb, restrictions, documentSearchDto.getDeletionDateFrom(), documentSearchDto.getDeletionDateTo());
				addRestrictionsForArchived(root, cb, restrictions, documentSearchDto.isShowArchived());

				return cb.and(restrictions.toArray(new Predicate[] {}));
			};
	}

	private static void addRestrictionsForText(Root<Document> root, CriteriaBuilder cb, List<Predicate> restrictions, String text)
	{
		if (StringUtils.isBlank(text))
			return;

		restrictions.add(cb.or(
				cb.like(cb.lower(root.get(Document_.title)), "%" + StringUtils.lowerCase(text) + "%"),
				cb.like(cb.lower(root.get(Document_.description)), "%" + StringUtils.lowerCase(text) + "%")));
	}

	private static void addRestrictionsForDocumentType(Root<Document> root, CriteriaBuilder cb, List<Predicate> restrictions, DocumentTypeDto documentType)
	{
		if (documentType == null || documentType.getId() == null)
			return;

		restrictions.add(cb.equal(root.get(Document_.documentType).get(AbstractEntityId_.id), documentType.getId()));
	}

	private static void addRestrictionsForDateOfIssue(Root<Document> root, CriteriaBuilder cb, List<Predicate> restrictions, LocalDate dateOfIssueFrom, LocalDate dateOfIssueTo)
	{
		if (dateOfIssueFrom != null)
			restrictions.add(cb.greaterThanOrEqualTo(root.get(Document_.dateOfIssue), dateOfIssueFrom));

		if (dateOfIssueTo != null)
			restrictions.add(cb.lessThanOrEqualTo(root.get(Document_.dateOfIssue), dateOfIssueTo));
	}

	private static void addRestrictionsForContact(Root<Document> root, CriteriaBuilder cb, List<Predicate> restrictions, ContactDto contact, ContactSearchTypeEnum contactSearchType)
	{
		if (contact == null || contact.getId() == null)
			return;

		if (contactSearchType == ContactSearchTypeEnum.ONLY_SENDER)
			restrictions.add(cb.equal(root.get(Document_.sender).get(AbstractEntityId_.id), contact.getId()));
		else if (contactSearchType == ContactSearchTypeEnum.ONLY_RECEIVER)
			restrictions.add(cb.equal(root.get(Document_.receiver).get(AbstractEntityId_.id), contact.getId()));
		else
			restrictions.add(cb.or(
					cb.equal(root.get(Document_.sender).get(AbstractEntityId_.id), contact.getId()),
					cb.equal(root.get(Document_.receiver).get(AbstractEntityId_.id), contact.getId())));
	}

	private static void addRestrictionsForTags(Root<Document> root, CriteriaQuery<?> query, CriteriaBuilder cb, List<Predicate> restrictions, List<TagDto> tags)
	{
		if (tags == null || tags.isEmpty())
			return;

		for (TagDto tag : tags)
		{
			Subquery<Long> sub = query.subquery(Long.class);
			Root<Tag> subRoot = sub.from(Tag.class);
			SetJoin<Document, Tag> subTags = root.join(Document_.tags);
			sub.select(subRoot.get(AbstractEntityId_.id));
			sub.where(cb.equal(subTags.get(AbstractEntityId_.id), tag.getId()));

			restrictions.add(cb.exists(sub));
		}
	}

	private static void addRestrictionsForDocumentBox(Root<Document> root, CriteriaBuilder cb, List<Predicate> restrictions, DocumentBoxDto documentBox)
	{
		if (documentBox == null || documentBox.getId() == null)
			return;

		restrictions.add(cb.equal(root.get(Document_.documentBox).get(AbstractEntityId_.id), documentBox.getId()));
	}

	private static void addRestrictionsForReviewDate(Root<Document> root, CriteriaBuilder cb, List<Predicate> restrictions, LocalDate reviewDateFrom, LocalDate reviewDateTo)
	{
		if (reviewDateFrom != null)
			restrictions.add(cb.greaterThanOrEqualTo(root.get(Document_.reviewDate), reviewDateFrom));

		if (reviewDateTo != null)
			restrictions.add(cb.lessThanOrEqualTo(root.get(Document_.reviewDate), reviewDateTo));
	}

	private static void addRestrictionsForDeletionDate(Root<Document> root, CriteriaBuilder cb, List<Predicate> restrictions, LocalDate deletionDateFrom, LocalDate deletionDateTo)
	{
		if (deletionDateFrom != null)
			restrictions.add(cb.greaterThanOrEqualTo(root.get(Document_.deletionDate), deletionDateFrom));

		if (deletionDateTo != null)
			restrictions.add(cb.lessThanOrEqualTo(root.get(Document_.deletionDate), deletionDateTo));
	}

	private static void addRestrictionsForArchived(Root<Document> root, CriteriaBuilder cb, List<Predicate> restrictions, boolean showArchived)
	{
		if (showArchived)
			return;

		restrictions.add(cb.equal(root.get(Document_.archived), Boolean.FALSE));
	}
}
