package roomescape.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
}
