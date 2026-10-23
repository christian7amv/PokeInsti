package com.ejemplo.pokeinsti.service;

import org.junit.Test;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public final class NdefTextDecoderTest {
    private byte[] payload(String text, boolean utf16) {
        byte[] language = {'e', 's'};
        byte[] value = text.getBytes(utf16 ? StandardCharsets.UTF_16 : StandardCharsets.UTF_8);
        byte[] result = new byte[1 + language.length + value.length];
        result[0] = (byte) ((utf16 ? 0x80 : 0) | language.length);
        System.arraycopy(language, 0, result, 1, language.length);
        System.arraycopy(value, 0, result, 3, value.length);
        return result;
    }
    @Test public void readsUtf8TextWithLanguage() {
        assertEquals("pokeinsti:etiqueta:01", NdefTextDecoder.decode(payload("pokeinsti:etiqueta:01", false)));
    }
    @Test public void readsUtf16Text() {
        assertEquals("pokeinsti:etiqueta:10", NdefTextDecoder.decode(payload("pokeinsti:etiqueta:10", true)));
    }
    @Test public void rejectsTruncatedLanguage() {
        assertThrows(IllegalArgumentException.class, () -> NdefTextDecoder.decode(new byte[]{5,'e'}));
    }
    @Test public void rejectsNonProjectText() {
        assertThrows(IllegalArgumentException.class, () -> NdefTextDecoder.decode(payload("https://example.com", false)));
    }
    @Test public void rejectsMalformedUtf8() {
        assertThrows(IllegalArgumentException.class, () -> NdefTextDecoder.decode(new byte[]{0,(byte)0xC3,(byte)0x28}));
    }
    @Test public void rejectsEmptyAndReservedStatus() {
        assertThrows(IllegalArgumentException.class, () -> NdefTextDecoder.decode(new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> NdefTextDecoder.decode(new byte[]{0x40,'a'}));
    }
}