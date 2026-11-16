package repository;

import domain.AiInferenceLog;
import org.springframework.data.jpa.repository.JpaRepository;

// Thêm theo VP (Hình 4.9, bổ sung 16/11). Không khai báo phương thức riêng:
// lưu bằng save(...) có sẵn của JpaRepository (CLAUDE.md 3.4).
public interface AiInferenceLogRepository extends JpaRepository<AiInferenceLog, Long> {
}
