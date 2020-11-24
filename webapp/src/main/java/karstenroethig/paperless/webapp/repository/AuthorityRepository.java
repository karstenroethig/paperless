package karstenroethig.paperless.webapp.repository;

import org.springframework.data.repository.CrudRepository;

import karstenroethig.paperless.webapp.model.domain.Authority;

public interface AuthorityRepository extends CrudRepository<Authority,Long>
{
	Authority findOneByNameIgnoreCase(String name);
}
