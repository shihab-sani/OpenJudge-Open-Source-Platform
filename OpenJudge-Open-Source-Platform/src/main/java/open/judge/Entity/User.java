package open.judge.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "users", indexes = @Index(columnList = "email", unique = true))
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 191)
    private String email;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(length = 200)
    private String githubUsername;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.CONTRIBUTOR;

    @CreationTimestamp
    private Instant createdAt;
}
