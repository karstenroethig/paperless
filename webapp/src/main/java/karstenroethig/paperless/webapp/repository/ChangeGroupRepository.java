package karstenroethig.paperless.webapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import karstenroethig.paperless.webapp.model.domain.changes.ChangeGroup;

public interface ChangeGroupRepository extends JpaRepository<ChangeGroup,Long>, JpaSpecificationExecutor<ChangeGroup>
{
}
