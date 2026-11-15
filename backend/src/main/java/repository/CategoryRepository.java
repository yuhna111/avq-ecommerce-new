package repository;

import domain.Category;
import java.util.Optional;

public interface CategoryRepository {

	public Optional<Category> findBySlug(String slug);
}