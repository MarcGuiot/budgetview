package org.globsframework.utils.serialization;

import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.IOFailure;

import java.util.Base64;

public class Encoder {
    private Encoder() {
    }

    public static String byteToString(byte[] cryptedBytes) {
        Base64.Encoder encoder = Base64.getEncoder();
        return Strings.removeNewLine(new String(encoder.encode(cryptedBytes)));
    }

    public static byte[] stringToByte(String text) {
        try {
            Base64.Decoder decoder = Base64.getDecoder();
            return decoder.decode(text);
        } catch (Exception e) {
            throw new IOFailure(e);
        }
    }
}
