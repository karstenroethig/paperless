package karstenroethig.paperless.webapp.model.dto.backup;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class RestoreDto
{
	@NotNull
	@Size(min = 1)
	private String filePath = "/Users/karsten/develop/workspaces/paperless-workspace/paperless/webapp/target/backups/backup_2021-05-02_12-36.zip";
}
