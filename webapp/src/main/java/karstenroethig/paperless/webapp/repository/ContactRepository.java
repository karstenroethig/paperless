package karstenroethig.paperless.webapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import karstenroethig.paperless.webapp.model.domain.Contact;

public interface ContactRepository extends JpaRepository<Contact,Long>, JpaSpecificationExecutor<Contact>
{
	Contact findOneByNameIgnoreCase(String name);
}
