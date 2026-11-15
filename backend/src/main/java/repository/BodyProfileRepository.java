package repository;

import domain.BodyProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// Bổ sung: extends JpaRepository (CLAUDE.md 3.4); các phương thức VP giữ nguyên,
// Spring Data sinh truy vấn theo tên phương thức.
public interface BodyProfileRepository extends JpaRepository<BodyProfile, Long> {

	public Optional<BodyProfile> findByCustomerId(Long customerId);
}