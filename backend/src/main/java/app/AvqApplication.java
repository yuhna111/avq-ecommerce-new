package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Lớp khởi động. Đặt trong package riêng {@code app} vì Spring không cho dùng default package;
 * các package của thiết kế VP (api, service, infra, repository, domain) được quét tường minh.
 */
@SpringBootApplication(scanBasePackages = {"app", "api", "service", "infra", "repository", "domain"})
@EntityScan("domain")
@EnableJpaRepositories("repository")
public class AvqApplication {

    public static void main(String[] args) {
        SpringApplication.run(AvqApplication.class, args);
    }
}
