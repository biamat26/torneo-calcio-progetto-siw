package it.uniroma3.siw.torneo_calcio.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    /**
     * Salva un file caricato dall'utente nella cartella static/uploads/.
     * Genera un nome univoco per evitare collisioni tra file con lo stesso nome.
     * Restituisce il path relativo da usare come src nelle img HTML.
     *
     * @param file il file caricato dall'utente tramite form
     * @param subfolder la sottocartella (es. "teams", "players", "tournaments")
     * @return il path relativo del file salvato (es. "/uploads/teams/abc123.png")
     * @throws IOException se il salvataggio fallisce
     */
    public String save(MultipartFile file, String subfolder) throws IOException {
        log.info("Upload richiesto — file: {}, vuoto: {}",
                file != null ? file.getOriginalFilename() : "null",
                file == null || file.isEmpty());
        if (file == null || file.isEmpty()) return null;

        // Cartella di destinazione dentro static/
        String uploadDir = "src/main/resources/static/uploads/" + subfolder;
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Nome univoco per evitare collisioni
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String filename = UUID.randomUUID().toString() + extension;

        // Salvataggio
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);

        return "/uploads/" + subfolder + "/" + filename;
    }
}