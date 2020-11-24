package karstenroethig.paperless.webapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import karstenroethig.paperless.webapp.model.domain.User;

public interface UserRepository extends JpaRepository<User,Long>, JpaSpecificationExecutor<User>
{
	Optional<User> findOneByUsernameIgnoreCase(String username);
}
