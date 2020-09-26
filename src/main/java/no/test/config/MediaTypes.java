package no.test.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;


/**
 * ENUM som fungerer som et MAP for å hente ut media type strenger som passer til kjente Microsoft MS filformat.
 * Ta utgangspunkt i filendelsen av filen du tenker benytte (file-extension) og oversett den til korrekt ENUM verdi
 * via {@link #fromExtension(String extension) fromExtension}.
 * Media type i streng format kan så hentes ut via {@link #getMediaTypeString() getMediaTypeString}.
 * @see #fromExtension
 * @see #getMediaTypeString
 */
public enum MediaTypes {
    XCEL("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    WORD("docx", "application/msword");

    private static final Logger LOGGER = LoggerFactory.getLogger(MediaTypes.class);
    private final String extension;
    private final String mediaType;

    MediaTypes(final String extension, final String mediaType) {
        this.extension = extension;
        this.mediaType = mediaType;
    }

    public String getMediaTypeString() {
        return mediaType;
    }

    private static final Map<String, MediaTypes> STRING_MEDIA_TYPES_HASH_MAP = new HashMap<>();

    static {
        for (final MediaTypes value : values()) {
            STRING_MEDIA_TYPES_HASH_MAP.put(value.extension, value);
        }
    }

    /**
     * Oversetter file-extension til MediaType som samsvarer til file-extension som så kan benyttes til
     * å hente ut streng av mediatype via {@link #getMediaTypeString() getMediaTypeString}.
     * @param extension     File-extension eller filendelse som beskriver satt filtype.
     * @return              MediaTeype som samsvarer til file-extension.
     */
    public static MediaTypes fromExtension(final String extension) {
        final MediaTypes mediaType = STRING_MEDIA_TYPES_HASH_MAP.getOrDefault(extension, null);
        if (mediaType == null) {
            LOGGER.error("Ukjent media type for fil med extension: {}", extension);
        }
        return mediaType;
    }
}