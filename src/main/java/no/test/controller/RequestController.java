package no.test.controller;

import no.test.service.WordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static no.test.config.MediaTypes.fromExtension;
import static no.test.service.XcelService.finnAntallRaderFraInputStream;
import static no.test.service.XcelService.finnBytteOrd;
import static no.test.util.ZipPacker.packZip;

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

    @RequestMapping("/flettresources")
    ResponseEntity flettresources() throws IOException {
        return createResponseEntity("testfil.docx", new InputStreamResource(new WordService(finnBytteOrd()).flettDokument()));
    }

    @PostMapping(value = "/flett")
    ResponseEntity flett(final HttpServletRequest request) throws IOException, ServletException {
        final List<Part> xlsxParts = getPartFromRequest(request, "xlsx");
        final int filerSomSkalFlettes = finnAntallRaderFraInputStream(xlsxParts.get(0).getInputStream()) - 1;

        if (filerSomSkalFlettes <= 0){
            LOGGER.warn("Det er for få rader for fletting");
            return null;
        }

        final WordService wordService = new WordService(null);
        final ByteArrayInputStream[] flettedeByteArrayInputStream = new ByteArrayInputStream[filerSomSkalFlettes];

        for (int filNummer = 0; filNummer < filerSomSkalFlettes; filNummer++) {
            final HashMap<String, String> bytteord = finnBytteOrd(xlsxParts.get(0).getInputStream(), filNummer +1);

            final List<Part> docxParts = getPartFromRequest(request, "docx");
            final InputStream docxFileContent = docxParts.get(0).getInputStream();

            wordService.setbytteOrdMap(bytteord);
            flettedeByteArrayInputStream[filNummer] = wordService.flettDokument(docxFileContent);
        }

        return createResponseEntity("FletteResultat.zip", packZip(flettedeByteArrayInputStream));
    }

    private List<Part> getPartFromRequest(final HttpServletRequest request, final String fileType)
            throws IOException, ServletException {
        return request.getParts().stream().filter(part -> fileType.equals(part.getName()) && part.getSize() > 0).collect(
                Collectors.toList());
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
        
