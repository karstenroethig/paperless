package karstenroethig.paperless.webapp.model.dto.api;

import karstenroethig.paperless.webapp.model.dto.AbstractDtoId;
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
public class TagUsageApiDto extends AbstractDtoId
{
	private Integer usage = 0;
}
