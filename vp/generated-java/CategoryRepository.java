package repository;

import domain.Category;

public interface CategoryRepository {

	public Optional<Category> findBySlug(String slug);
}