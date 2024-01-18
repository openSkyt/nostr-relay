package org.openskyt.nostrrelay.BIP340_Schnorr;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openskyt.nostrrelay.BIP340_Schnorr.Point;
import org.openskyt.nostrrelay.BIP340_Schnorr.Util;
import org.openskyt.nostrrelay.dto.EventData;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

@Component
public class EventSigValidator {


    public boolean verifyEvent(EventData eventData) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            ObjectMapper objectMapper = new ObjectMapper();
            StringBuilder hash = new StringBuilder();
            ArrayList<Object> fields = new ArrayList<>();

            Collections.addAll(fields, 0, eventData.getPubkey().toLowerCase(), eventData.getCreated_at(),
                    eventData.getKind(), eventData.getTags(), eventData.getContent());

            String eventDataForHash = objectMapper.writeValueAsString(fields);
            byte[] evenDataHash = md.digest(eventDataForHash.getBytes(StandardCharsets.UTF_8)); //hashing eventData wit id set to 0

            for (byte b : evenDataHash) { //convert byte array to hex for comparison wth id(hash)) from client
                hash.append(String.format("%02x", b));
            }

            if (eventData.getId().contentEquals(hash) &&
                    verify(Util.hexToBytes(eventData.getId()),
                            Util.hexToBytes(eventData.getPubkey()),
                            Util.hexToBytes(eventData.getSig()))) { //todo add logic what to do here
                System.out.println("id + hash is valid and signature is valid");
                return true;
            } else {
                System.out.println("hash is not match or signature is not valid");
                return false;
            }

        } catch (Exception e) {
            System.out.println("cant verify event");
        }
        return false;
    }

    //https://code.samourai.io/samouraidev/BIP340_Schnorr
    public boolean verify(byte[] id, byte[] pubkey, byte[] sig) throws Exception {
        if (id.length != 32 || pubkey.length != 32 || sig.length != 64) {
            System.out.println("ID or pubkey or sig is not valid");
            return false;
        }

        Point p = Point.liftX(pubkey);
        if (p == null) {
            return false;
        }
        BigInteger r = Util.bigIntFromBytes(Arrays.copyOfRange(sig, 0, 32));
        BigInteger s = Util.bigIntFromBytes(Arrays.copyOfRange(sig, 32, 64));
        if (r.compareTo(Point.getp()) >= 0 || s.compareTo(Point.getn()) >= 0) {
            return false;
        }
        int len = 32 + pubkey.length + id.length;
        byte[] buf = new byte[len];
        System.arraycopy(sig, 0, buf, 0, 32);
        System.arraycopy(pubkey, 0, buf, 32, pubkey.length);
        System.arraycopy(id, 0, buf, 32 + pubkey.length, id.length);
        BigInteger e = Util.bigIntFromBytes(Point.taggedHash("BIP0340/challenge", buf)).mod(Point.getn());
        Point R = Point.add(Point.mul(Point.getG(), s), Point.mul(p, Point.getn().subtract(e)));
        return R != null && R.hasEvenY() && R.getX().compareTo(r) == 0;

    }
}
