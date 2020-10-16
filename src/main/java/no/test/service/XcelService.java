package no.test.service;

import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;

public final class XcelService {
    private static final Logger LOGGER = LoggerFactory.getLogger(XcelService.class);

    public static HashMap<String, String> finnBytteOrd(final InputStream is, final int rownum) {
        final XSSFWorkbook workbook = openData(is);
        return finnBytteOrdFraWorkbook(workbook, rownum);
    }

    public static HashMap<String, String> finnBytteOrd() {
        final XSSFWorkbook workbook = openData();
        return finnBytteOrdFraWorkbook(workbook);
    }

    public static int finnAntallRaderFraInputStream(final InputStream is) {
        return openData(is).getSheetAt(0).getPhysicalNumberOfRows();
    }

    private static HashMap<String, String> finnBytteOrdFraWorkbook(final XSSFWorkbook workbook) {
        return finnBytteOrdFraWorkbook(workbook, 2);
    }

    private static HashMap<String, String> finnBytteOrdFraWorkbook(final XSSFWorkbook workbook, final int rownum) {
        LOGGER.info("Antall sider funnet i xlsx er: " + workbook.getNumberOfSheets());

        final XSSFSheet sheet = workbook.getSheetAt(0);

        final XSSFRow headerRow = sheet.getRow(0);
        final XSSFRow variableRow = sheet.getRow(rownum);

        LOGGER.info("Antall rader funnet på første side i xlsx er: " + sheet.getPhysicalNumberOfRows());
        LOGGER.info("Antall celler funnet i første rad i xlsx er: " + headerRow.getPhysicalNumberOfCells());

        final HashMap<String, String> bytteOrdMap = new HashMap<>();

        for (int i = 0; i < headerRow.getPhysicalNumberOfCells() && i < variableRow.getPhysicalNumberOfCells(); i++) {
            bytteOrdMap.put(String.format("«%s»", cellToString(headerRow.getCell(i))), cellToString(variableRow.getCell(i)));
            bytteOrdMap.put(String.format("<%s>", cellToString(headerRow.getCell(i))), cellToString(variableRow.getCell(i)));
        }
        return bytteOrdMap;
    }

    private static String cellToString(final XSSFCell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getRichStringCellValue().getString();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return new BigDecimal(cell.toString()).toPlainString();
                }
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case _NONE:
            case BLANK:
            case ERROR:
                break;
        }
        return null;
    }

    private static XSSFWorkbook openData(final InputStream is) {
        try {
            return new XSSFWorkbook(is);
        } catch (final IOException e) {
            LOGGER.error("Fikk ikke gjort om FileInputStream til XSSFWorkbook. Trace: {}", e.getMessage());
            return null;
        }
    }

    private static XSSFWorkbook openData() {
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        final String filename = "dokumentfletting/Mottakere_fiktive_FNR_fletteloesning_SYSTEST.xlsx";
        final InputStream is = classloader.getResourceAsStream(filename);
        return openData(is);
    }
}
