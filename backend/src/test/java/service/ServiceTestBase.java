package service;

import app.AvqApplication;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import testsupport.Fixtures;

/**
 * Kiểm thử tích hợp trên CSDL thật avq_db_vp. Mỗi test chạy trong một giao dịch và được rollback.
 * Tắt OrderExpiryScheduler để không chạy ngầm trong lúc kiểm thử.
 */
@SpringBootTest(classes = AvqApplication.class, properties = "app.scheduling.enabled=false")
@Transactional
abstract class ServiceTestBase {
	@Autowired
	protected EntityManager em;
	protected Fixtures fx;

	@BeforeEach
	void initFixtures() {
		fx = new Fixtures(em);
	}

	protected void flushClear() {
		em.flush();
		em.clear();
	}
}
