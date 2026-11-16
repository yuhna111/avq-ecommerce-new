package app;

import infra.ImageStorage;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Phục vụ ảnh đã tải lên tại /uploads/** (đọc từ thư mục của ImageStorage). */
@Configuration
public class WebConfig implements WebMvcConfigurer {
	private final ImageStorage imageStorage;

	public WebConfig(ImageStorage imageStorage) {
		this.imageStorage = imageStorage;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String location = imageStorage.getDirectory().toUri().toString();
		registry.addResourceHandler("/uploads/**").addResourceLocations(location.endsWith("/") ? location : location + "/");
	}
}
