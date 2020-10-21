package karstenroethig.paperless.webapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import karstenroethig.paperless.webapp.model.domain.DocumentType;

public interface DocumentTypeRepository extends JpaRepository<DocumentType,Long>, JpaSpecificationExecutor<DocumentType>
{
	DocumentType findOneByNameIgnoreCase(String name);
}
