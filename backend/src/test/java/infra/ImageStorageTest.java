package infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

/** Bổ sung 17/11: lưu ảnh tải lên, kiểm tra định dạng bằng chữ ký đầu file và giới hạn 5 MB. */
class ImageStorageTest {
	static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0 };
	static final byte[] JPG = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10, 'J', 'F', 'I', 'F', 0, 1 };

	@TempDir Path dir;

	@Test
	void luuAnhPngVaJpg_traVeDuongDanUploads() {
		ImageStorage st = new ImageStorage(dir.toString());
		String png = st.save(new MockMultipartFile("file", "a.png", "image/png", PNG));
		String jpg = st.save(new MockMultipartFile("file", "b.jpeg", "image/jpeg", JPG));
		assertThat(png).startsWith("/uploads/").endsWith(".png");
		assertThat(jpg).endsWith(".jpg");
		assertThat(Files.exists(dir.resolve(png.substring("/uploads/".length())))).isTrue();
	}

	@Test
	void fileGiaDangAnh_biTuChoi() {
		ImageStorage st = new ImageStorage(dir.toString());
		// đuôi .png và Content-Type image/png nhưng nội dung là mã HTML
		byte[] html = "<script>alert(1)</script>".getBytes();
		assertThatThrownBy(() -> st.save(new MockMultipartFile("file", "x.png", "image/png", html)))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("JPEG, PNG");
	}

	@Test
	void fileRongHoacQuaLon_biTuChoi() {
		ImageStorage st = new ImageStorage(dir.toString());
		assertThatThrownBy(() -> st.save(new MockMultipartFile("file", "e.png", "image/png", new byte[0])))
				.isInstanceOf(IllegalArgumentException.class);
		byte[] big = new byte[(int) ImageStorage.MAX_BYTES + 1];
		System.arraycopy(PNG, 0, big, 0, PNG.length);
		assertThatThrownBy(() -> st.save(new MockMultipartFile("file", "big.png", "image/png", big)))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("5 MB");
	}
}
