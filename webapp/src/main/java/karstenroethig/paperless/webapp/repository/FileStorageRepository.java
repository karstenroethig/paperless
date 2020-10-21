package karstenroethig.paperless.webapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import karstenroethig.paperless.webapp.model.domain.FileStorage;

public interface FileStorageRepository extends JpaRepository<FileStorage,Long>, JpaSpecificationExecutor<FileStorage>
{
	List<FileStorage> findBySizeLessThanOrderBySizeAsc(Long size);
}
