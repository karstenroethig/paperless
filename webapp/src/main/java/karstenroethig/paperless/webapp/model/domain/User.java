package karstenroethig.paperless.webapp.model.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
@Table(name = "user")
public class User extends AbstractEntityId
{
	@Column(name = "username", length = 255, nullable = true)
	private String username;

	@Column(name = "hashed_password", length = 255, nullable = true)
	private String hashedPassword;

	@Column(name = "full_name", length = 255, nullable = true)
	private String fullName;

	@Column(name = "enabled", nullable = false)
	private boolean enabled;

	@Column(name = "locked", nullable = false)
	private boolean locked;

	@Column(name = "failed_login_attempts", nullable = false)
	private Integer failedLoginAttempts;

	@Column(name = "new_registered", nullable = false)
	private boolean newRegisterd;

	@Column(name = "deleted", nullable = false)
	private boolean deleted;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "user_authority", joinColumns = {@JoinColumn(name = "user_id")}, inverseJoinColumns = {@JoinColumn(name = "authority_id")})
	private List<Authority> authorities = new ArrayList<>();

	public void addAuthority(Authority authority)
	{
		this.authorities.add(authority);
	}

	public void removeAuthority(Authority authority)
	{
		this.authorities.remove(authority);
	}
}
