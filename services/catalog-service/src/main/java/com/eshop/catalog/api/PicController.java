package com.eshop.catalog.api;

import com.eshop.catalog.infrastructure.CatalogItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Serves catalog item product images from the classpath ({@code src/main/resources/pics/}).
 *
 * <p>Base path: {@code /api/v1/catalog/items}
 *
 * <p>The image file name is looked up from the {@code CatalogItem.pictureFileName} column.
 * MIME type is inferred from the file extension.
 */
@RestController
@RequestMapping("/api/v1/catalog/items")
@RequiredArgsConstructor
@Slf4j
public class PicController {

    private final CatalogItemRepository catalogItemRepository;

    /**
     * Returns the product image for the given catalog item id.
     *
     * <p>Status codes:
     * <ul>
     *   <li>{@code 400 Bad Request} — if {@code catalogItemId} is &le; 0.</li>
     *   <li>{@code 404 Not Found} — if the catalog item does not exist or the image file is missing.</li>
     *   <li>{@code 200 OK} — with the image bytes and a content-type matching the file extension.</li>
     * </ul>
     *
     * @param catalogItemId the catalog item id
     * @return the image as a Spring {@link Resource}
     */
    @GetMapping("/{catalogItemId}/pic")
    public ResponseEntity<Resource> getPic(@PathVariable int catalogItemId) {
        if (catalogItemId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        var itemOpt = catalogItemRepository.findById(catalogItemId);
        if (itemOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String pictureFileName = itemOpt.get().getPictureFileName();
        if (pictureFileName == null || pictureFileName.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new ClassPathResource("pics/" + pictureFileName);
        if (!resource.exists()) {
            log.warn("Image file not found for catalog item {}: pics/{}", catalogItemId, pictureFileName);
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = detectMediaType(pictureFileName);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }

    /**
     * Infers the HTTP {@link MediaType} from the file extension.
     * Defaults to {@link MediaType#APPLICATION_OCTET_STREAM} for unknown extensions.
     *
     * @param fileName the file name (with extension)
     * @return the corresponding {@link MediaType}
     */
    private MediaType detectMediaType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if (lower.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        if (lower.endsWith(".svg")) return MediaType.parseMediaType("image/svg+xml");
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
