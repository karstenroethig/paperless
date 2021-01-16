package karstenroethig.paperless.webapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import karstenroethig.paperless.webapp.model.domain.DocumentBox;

public interface DocumentBoxRepository extends JpaRepository<DocumentBox,Long>, JpaSpecificationExecutor<DocumentBox>
{
	Optional<DocumentBox> findOneByNameIgnoreCase(String name);
}
