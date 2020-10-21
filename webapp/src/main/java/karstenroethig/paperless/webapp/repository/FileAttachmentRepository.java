package karstenroethig.paperless.webapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import karstenroethig.paperless.webapp.model.domain.FileAttachment;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment,Long>, JpaSpecificationExecutor<FileAttachment>
{
}
