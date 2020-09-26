package no.test.controller;

import no.test.service.WordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static no.test.config.MediaTypes.fromExtension;
import static no.test.service.XcelService.finnBytteOrd;

@SuppressWarnings({ "unused", "JavaDoc" })
@RestController
public class RequestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestController.class);

    @RequestMapping("/mal")
    ResponseEntity hentMal() {
        return getResponseEntity("dokumentfletting/Dokumentmal_fletteloesning_SYSTEST.docx");
    }

    @RequestMapping("/data")
    public ResponseEntity hentData() {
        return getResponseEntity("dokumentfletting/Mottakere_fiktive_FNR_fletteloesning_SYSTEST.xlsx");
    }

    @RequestMapping("/flett")
    ResponseEntity flett() throws IOException {
        return createResponseEntity("testfil.docx", new WordService(finnBytteOrd()).flettDokument());
    }

    private ResponseEntity getResponseEntity(final String fileName) {
        return createResponseEntity(fileName,
                                    new InputStreamResource(getClass().getClassLoader().getResourceAsStream(fileName)));
    }

    private ResponseEntity createResponseEntity(final String fileName, final InputStreamResource resource) {
        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(getFileExtension(fileName)))
                             .header(HttpHeaders.CONTENT_DISPOSITION,
                                     String.format("attachment; filename=\"%s\"", fileName))
                             .body(resource);
    }

    private String getFileExtension(final String fileName) {
        final int index = fileName.lastIndexOf('.');
        if (index > 0) {
            return fromExtension(fileName.substring(index + 1)).getMediaTypeString();
        }
        LOGGER.error("fileName: [{}}] inneholder ikke noe punktum eller extension.", fileName);
        return null;
    }
}
        
