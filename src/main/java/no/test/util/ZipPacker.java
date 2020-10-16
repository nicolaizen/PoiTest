package no.test.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ZipPacker {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZipPacker.class);

    public static InputStreamResource packZip(final ByteArrayInputStream[] fileInputStreams) {

        final byte[] buffer = new byte[1024];
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ZipOutputStream zos = new ZipOutputStream(out);

        try {
            LOGGER.info("fileInputStreams.length: " + fileInputStreams.length);
            for (int i=0; i < fileInputStreams.length; i++) {
                final ByteArrayInputStream fis = fileInputStreams[i];

                zos.putNextEntry(new ZipEntry(String.format("<%s>.docx", i)));

                int length = fis.read(buffer);
                while (length > 0) {
                    zos.write(buffer, 0, length);
                    length = fis.read(buffer);
                }
                zos.closeEntry();
                fis.close();
            }
            zos.close();

        }
        catch (final IOException ioe) {
            System.out.println("Error creating zip file: " + ioe);
        }
        return new InputStreamResource(new ByteArrayInputStream(out.toByteArray()));
    }
}