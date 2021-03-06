package karstenroethig.paperless.webapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import karstenroethig.paperless.webapp.model.domain.Group;

public interface GroupRepository extends JpaRepository<Group,Long>, JpaSpecificationExecutor<Group>
{
	Optional<Group> findOneByNameIgnoreCase(String name);
}
