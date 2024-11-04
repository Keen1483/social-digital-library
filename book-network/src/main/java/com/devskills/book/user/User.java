package com.devskills.book.user;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.devskills.book.book.Book;
import com.devskills.book.history.BookTransactionHistory;
import com.devskills.book.role.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "_user")
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails, Principal {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue
	private Integer id;
	
	private String firstname;
	
	private String lastname;
	
	private LocalDate dateOfBirth;
	
	@Column(unique = true)
	private String email;
	
	private String password;
	
	private boolean accountLocked;
	
	private boolean enabled;
	
	@ManyToMany(fetch = FetchType.EAGER)
	private List<Role> roles;
	
	@OneToMany(mappedBy = "owner")
	private List<Book> books;
	
	@OneToMany(mappedBy = "user")
	private List<BookTransactionHistory> histories;
	
	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdDate;
	
	@LastModifiedDate
	@Column(insertable = false)
	private LocalDateTime lastModifiedDate;

	@Override
	public String getName() {
		return email;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.roles
				.stream()
				.map(role -> new SimpleGrantedAuthority(role.getName()))
				.collect(Collectors.toList());
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return email;
	}
	
	@Override
	public boolean isAccountNonLocked() {
		return !accountLocked;
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public String fullName() {
		return firstname + " " + lastname;
	}

}
