package karstenroethig.paperless.webapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import karstenroethig.paperless.webapp.model.domain.DocumentBox;

public interface DocumentBoxRepository extends JpaRepository<DocumentBox,Long>, JpaSpecificationExecutor<DocumentBox>
{
	DocumentBox findOneByNameIgnoreCase(String name);
}
