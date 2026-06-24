package it.uniroma3.siw.torneo_calcio.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileUploadService {

    private static final Logger log = LoggerFactory.getLogger(FileUploadService.class);

    @Value("${azure.storage.connection-string:}")
    private String connectionString;

    @Value("${azure.storage.container:images}")
    private String containerName;

    private BlobContainerClient blobContainerClient;

    /**
     * Se la connection string Azure è configurata, usa Blob Storage.
     * Altrimenti usa il filesystem locale (sviluppo).
     */
    @PostConstruct
    public void init() {
        if (connectionString != null && !connectionString.isBlank()) {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
            blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
            log.warn("FileUploadService: uso Azure Blob Storage (container: {})", containerName);
        } else {
            log.warn("FileUploadService: uso filesystem locale");
        }
    }

    /**
     * Salva il file su Azure Blob Storage (produzione) o su filesystem locale (sviluppo).
     * Restituisce l'URL pubblico del file salvato.
     */
    public String save(MultipartFile file, String subfolder) throws IOException {
        log.info("Upload richiesto — file: {}, vuoto: {}",
                file != null ? file.getOriginalFilename() : "null",
                file == null || file.isEmpty());

        if (file == null || file.isEmpty()) return null;

        // Nome univoco per evitare collisioni
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String filename = subfolder + "/" + UUID.randomUUID() + extension;

        if (blobContainerClient != null) {
            // ── Produzione: Azure Blob Storage ──────────────────────────────
            BlobClient blobClient = blobContainerClient.getBlobClient(filename);
            blobClient.upload(file.getInputStream(), file.getSize(), true);
            String url = blobClient.getBlobUrl();
            log.info("File caricato su Azure Blob: {}", url);
            return url;

        } else {
            // ── Sviluppo locale: filesystem ──────────────────────────────────
            String uploadDir = "src/main/resources/static/uploads/" + subfolder;
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(UUID.randomUUID() + extension);
            Files.copy(file.getInputStream(), filePath);
            return "/uploads/" + subfolder + "/" + filePath.getFileName();
        }
    }
}