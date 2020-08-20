package com.alivc.player;

import android.util.Base64;
import java.lang.reflect.Method;
import java.security.SignatureException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Signature {
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    public Signature() {
    }

    public static String calculateRFC2104HMAC(String data, String key) throws SignatureException {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes());
            String result = encode(rawHmac);
            return result;
        } catch (Exception var6) {
            throw new SignatureException("Failed to generate HMAC : " + var6.getMessage());
        }
    }

    public static String encodeBase64(byte[] input) throws Exception {
        Class clazz = Class.forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64");
        Method mainMethod = clazz.getMethod("encode", byte[].class);
        mainMethod.setAccessible(true);
        Object retObj = mainMethod.invoke((Object)null, input);
        return (String)retObj;
    }

    public static String encode(byte[] bytes) {
        return Base64.encodeToString(bytes, 2);
    }
}
