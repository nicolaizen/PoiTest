package no.test.service;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public final class WordService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WordService.class);

    private HashMap<String, String> bytteOrdMap;

    public WordService(final HashMap<String, String> bytteOrdMap) {
        setbytteOrdMap(bytteOrdMap);
    }

    public void setbytteOrdMap(final HashMap<String, String> bytteOrdMap) {
        this.bytteOrdMap = bytteOrdMap;
    }

    public ByteArrayInputStream flettDokument(final InputStream is) throws IOException {
        final XWPFDocument document = openData(is);
        flettTabellerAvDoc(document);
        flettTextParagraphs(document);
        flettTextBox(document);
        flettHeadere(document);
        return dokumentTilByteArrayInputStream(document);
    }

    public ByteArrayInputStream flettDokument() throws IOException {
        final XWPFDocument document = openData();
        flettTabellerAvDoc(document);
        flettTextParagraphs(document);
        flettTextBox(document);
        flettHeadere(document);
        return dokumentTilByteArrayInputStream(document);
    }

    private ByteArrayInputStream dokumentTilByteArrayInputStream(final XWPFDocument document) {
        try {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.write(byteArrayOutputStream);
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        } catch (final IOException e) {
            LOGGER.error("Fikk ikke skrevet document til byteArrayOutputStream. Exception: {}", e.getMessage());
            return null;
        }
    }

    public XWPFDocument openData(final InputStream is) throws IOException {
        return new XWPFDocument(is);
    }

    public XWPFDocument openData() throws IOException {
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        final String filename = "dokumentfletting/Dokumentmal_fletteloesning_SYSTEST.docx";
        return openData(classloader.getResourceAsStream(filename));
    }

    private void flettTabellerAvDoc(final XWPFDocument doc) {
        doc.getTables().forEach(this::flettRaderAvTabell);
    }

    private void flettRaderAvTabell(final XWPFTable table) {
        table.getRows().forEach(this::flettCellerAvRad);
    }

    private void flettCellerAvRad(final XWPFTableRow row) {
        row.getTableCells().forEach(this::flettParagraphAvCelle);
    }

    @SuppressWarnings("CodeBlock2Expr")
    private void flettParagraphAvCelle(final XWPFTableCell cell) {
        finnBetydeligeBytteOrdICelle(cell).forEach(ordPar -> {
            cell.getParagraphs().forEach(paragraph -> {
                flettRunAvParagraph(paragraph, ordPar.getKey());
            });
        });
    }

    private Set<Map.Entry<String, String>> finnBetydeligeBytteOrdICelle(final XWPFTableCell cell) {
        return bytteOrdMap.entrySet()
                          .stream()
                          .filter(ordPar -> cell.getText().contains(ordPar.getKey()))
                          .collect(Collectors.toSet());
    }

    private void flettRunAvParagraph(final XWPFParagraph paragraph, final String ordSomSkalByttesUt) {
        for (final XWPFRun run : paragraph.getRuns()) {
            if (!run.text().contains(ordSomSkalByttesUt)) {
                continue;
            }
            run.setText(bytteOrdMap.entrySet()
                                   .stream()
                                   .filter(ordPar -> ordPar.getKey()
                                                           .contains(ordSomSkalByttesUt))
                                   .map(Map.Entry::getValue)
                                   .findFirst().orElse(null), 0);
        }
    }

    private void flettTextParagraphs(final XWPFDocument document) {
        final List<XWPFParagraph> paragraphs = document.getParagraphs();
        for (final XWPFParagraph paragraph : paragraphs) {
            replaceParagraph(paragraph);
        }
        paragraphs.iterator().forEachRemaining(paragraph -> LOGGER.info(paragraph.getParagraphText()));
    }

    private void replaceParagraph(final XWPFParagraph paragraph) {
        final List<IRunElement> runs = paragraph.getIRuns();
        final Iterator<IRunElement> iterator = runs.iterator();

        for (int index = 0; iterator.hasNext(); index++) {
            final IRunElement elem = iterator.next();
            if (bytteOrdMap.containsKey(elem.toString())) {
                final XWPFRun run = paragraph.getRuns().get(index);
                run.setText(bytteOrdMap.get(elem.toString()), 0);
            }
        }
    }

    public void flettTextBox(final XWPFDocument document) {
        for (final XWPFParagraph paragraph : document.getParagraphs()) {
            final XmlCursor cursor = paragraph.getCTP().newCursor();
            cursor.selectPath(
                    "declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' .//*/w:txbxContent/w:p/w:r");

            while (cursor.hasNextSelection()) {
                cursor.toNextSelection();
                final XmlObject xml = cursor.getObject();
                try {
                    final XWPFRun run = new XWPFRun(CTR.Factory.parse(xml.xmlText()), (IRunBody) paragraph);
                    final String text = run.getText(0);

                    for (final Map.Entry<String, String> ordPar : bytteOrdMap.entrySet()) {
                        if (text != null && text.contains(ordPar.getKey())) {
                            run.setText(text.replace(ordPar.getKey(), ordPar.getValue()), 0);
                        }
                    }

                    xml.set(run.getCTR());
                } catch (final XmlException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void flettHeadere(final XWPFDocument document) {
        for (final XWPFHeader header : document.getHeaderList()) {
            for (final XWPFParagraph paragraph : header.getParagraphs()) {
                replaceParagraph(paragraph);
            }
        }
    }
}
