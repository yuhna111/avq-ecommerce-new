package infra;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Lưu ảnh sản phẩm do nhân viên tải lên (bổ sung 17/11, xem vp-changes.md đợt 2).
 * Chỉ nhận JPEG/PNG/WebP tối đa 5 MB; định dạng được kiểm tra bằng chữ ký đầu file (magic bytes),
 * không tin tên file hay Content-Type do trình duyệt gửi. Tên file lưu là UUID nên không ghi đè được file khác.
 * Trả về đường dẫn công khai /uploads/<tên file> để ghi vào product_images.url.
 */
@Component
public class ImageStorage {
	public static final long MAX_BYTES = 5L * 1024 * 1024;
	private final Path dir;

	public ImageStorage(@Value("${app.upload-dir:uploads}") String dir) {
		this.dir = Paths.get(dir).toAbsolutePath().normalize();
	}

	public String save(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("Chưa chọn file ảnh");
		}
		if (file.getSize() > MAX_BYTES) {
			throw new IllegalArgumentException("Ảnh vượt quá 5 MB");
		}
		try {
			String ext;
			try (InputStream in = file.getInputStream()) {
				ext = detectExtension(in.readNBytes(12));
			}
			if (ext == null) {
				throw new IllegalArgumentException("Chỉ nhận ảnh JPEG, PNG hoặc WebP");
			}
			Files.createDirectories(dir);
			String name = UUID.randomUUID() + "." + ext;
			Path target = dir.resolve(name).normalize();
			try (InputStream in = file.getInputStream()) {
				Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
			}
			return "/uploads/" + name;
		} catch (IOException e) {
			throw new IllegalStateException("Không lưu được ảnh", e);
		}
	}

	public Path getDirectory() {
		return dir;
	}

	/** Nhận dạng định dạng theo chữ ký đầu file. */
	private static String detectExtension(byte[] h) {
		if (h.length >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) {
			return "jpg";
		}
		if (h.length >= 8 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G'
				&& h[4] == 0x0D && h[5] == 0x0A && h[6] == 0x1A && h[7] == 0x0A) {
			return "png";
		}
		if (h.length >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
				&& h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') {
			return "webp";
		}
		return null;
	}
}
