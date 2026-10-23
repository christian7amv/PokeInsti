package com.ejemplo.pokeinsti.service;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

public final class NdefTextDecoder {
    private NdefTextDecoder() {}
    public static String decode(byte[] payload) {
        if (payload == null || payload.length == 0) throw new IllegalArgumentException("Registro vacío");
        int status = payload[0] & 0xff;
        int languageSize = status & 0x3f;
        if ((status & 0x40) != 0 || payload.length <= 1 + languageSize)
            throw new IllegalArgumentException("Registro NDEF incompleto");
        Charset charset = (status & 0x80) == 0 ? StandardCharsets.UTF_8 : StandardCharsets.UTF_16;
        try {
            String text = charset.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(payload, 1 + languageSize, payload.length - 1 - languageSize))
                    .toString().trim();
            if (!text.matches("pokeinsti:etiqueta:[0-9]{2,6}"))
                throw new IllegalArgumentException("El texto no es un código PokéInsti");
            return text;
        } catch (CharacterCodingException error) {
            throw new IllegalArgumentException("Codificación de texto inválida", error);
        }
    }
}