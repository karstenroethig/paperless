package karstenroethig.paperless.webapp.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import karstenroethig.paperless.webapp.model.domain.Authority;

public interface AuthorityRepository extends CrudRepository<Authority,Long>
{
	Optional<Authority> findOneByNameIgnoreCase(String name);
}
