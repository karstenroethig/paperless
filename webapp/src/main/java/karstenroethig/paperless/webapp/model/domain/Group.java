package karstenroethig.paperless.webapp.model.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.PreRemove;
import javax.persistence.Table;

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

@Entity
@Table(name = "user_group")
public class Group extends AbstractEntityId
{
	@Column(name = "name", length = 255, nullable = false)
	private String name;

	@ManyToMany(mappedBy = "groups", fetch = FetchType.LAZY)
	private List<User> members = new ArrayList<>();

	public void addMember(User member)
	{
		this.members.add(member);
		member.addGroup(this);
	}

	public void removeMember(User member)
	{
		this.members.remove(member);
		member.removeGroup(this);
	}

	@PreRemove
	private void removeGroupFromMembers()
	{
		members.stream().forEach(member -> member.removeGroup(this));
		members.clear();
	}
}
