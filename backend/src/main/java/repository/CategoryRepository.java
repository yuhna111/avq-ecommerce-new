package repository;

import domain.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// Bổ sung: extends JpaRepository (CLAUDE.md 3.4); các phương thức VP giữ nguyên,
// Spring Data sinh truy vấn theo tên phương thức.
public interface CategoryRepository extends JpaRepository<Category, Long> {

	public Optional<Category> findBySlug(String slug);
}