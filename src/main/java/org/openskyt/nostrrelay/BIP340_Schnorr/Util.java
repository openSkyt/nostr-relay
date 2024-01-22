package org.openskyt.nostrrelay.BIP340_Schnorr;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util  {

    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] buf = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            buf[i / 2] = (byte)((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return buf;
    }

    public static BigInteger bigIntFromBytes(byte[] b) {
        return new BigInteger(1, b);
    }

    public static byte[] sha256(byte[] b) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(b);
    }
}
