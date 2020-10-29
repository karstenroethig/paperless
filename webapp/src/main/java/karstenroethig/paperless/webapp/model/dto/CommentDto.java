package karstenroethig.paperless.webapp.model.dto;

import java.time.LocalDateTime;

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
@EqualsAndHashCode(callSuper = true)
public class CommentDto extends AbstractDtoId
{
	@NotNull
	private DocumentDto document;

	@NotNull
	@Size(min = 1)
	private String text;

	private LocalDateTime createdDatetime;

	private LocalDateTime updatedDatetime;

	private boolean deleted = false;
}
