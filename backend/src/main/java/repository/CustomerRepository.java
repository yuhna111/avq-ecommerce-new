package repository;

import domain.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Bổ sung: extends JpaRepository (CLAUDE.md 3.4); các phương thức VP giữ nguyên.
public interface CustomerRepository extends JpaRepository<Customer, Long> {

	// Một tham số identifier so với cả email lẫn phone (BR01: đăng nhập bằng email HOẶC SĐT).
	// Tên này không suy ra được truy vấn 1 tham số theo quy ước Spring Data nên dùng @Query.
	@Query("select c from Customer c where c.email = :identifier or c.phone = :identifier")
	public Optional<Customer> findByEmailOrPhone(@Param("identifier") String identifier);

	public boolean existsByEmail(String email);

	public boolean existsByPhone(String phone);
}
