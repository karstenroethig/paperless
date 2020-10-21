package karstenroethig.paperless.webapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import karstenroethig.paperless.webapp.model.domain.Tag;

public interface TagRepository extends JpaRepository<Tag,Long>, JpaSpecificationExecutor<Tag>
{
	Tag findOneByNameIgnoreCase(String name);
}
