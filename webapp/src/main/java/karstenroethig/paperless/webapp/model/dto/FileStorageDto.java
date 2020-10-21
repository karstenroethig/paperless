package karstenroethig.paperless.webapp.model.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class FileStorageDto
{
	@NonNull
	private Long storageId;

	@NonNull
	private String fileKey;
}
