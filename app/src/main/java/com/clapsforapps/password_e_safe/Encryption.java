/*
 * Copyright (c) 2015, The Codefather. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 3 along with this work.
 */

package com.clapsforapps.password_e_safe;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by User on 29/08/2015.
 */
public class Encryption {

    public static final String AES_ALGORITHM = "AES";
    private static final String MD5_ALGORITHM = "MD5";
    private static final String SHA1_ALGORITHM = "SHA-256";

    public static final byte[] DEFAULT_KEY_VALUE =
            new byte[] { 'k', '5', 'd', 'I', '%', 'z', '3',
                    'H', '@', 'r', 'f','7', 'A', 'u', 'e', 'h' };

    public static String encryptAES(String data, SecretKey key) throws Exception {
        //Key key = new SecretKeySpec(keyValue, AES_ALGORITHM);
        Cipher c = Cipher.getInstance(AES_ALGORITHM);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(data.getBytes());
        String encryptedValue = Base64.encodeToString(encVal, Base64.DEFAULT);
        return encryptedValue;
    }

    public static String decryptAES(String encryptedData, SecretKey key) throws Exception {
        //Key key = new SecretKeySpec(keyValue, AES_ALGORITHM);
        StringBuilder sb = new StringBuilder();
        for(byte b : key.getEncoded())
            sb.append((char)b);
        //Log.wtf("Key", sb.toString());
        Cipher c = Cipher.getInstance(AES_ALGORITHM);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decValue = c.doFinal(Base64.decode(encryptedData, Base64.DEFAULT));
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }

    public static String encryptMD5(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance(MD5_ALGORITHM);
        md5.update(password.getBytes(), 0, password.length());
        StringBuilder sb = new StringBuilder();
        for(byte b : md5.digest())
            sb.append(String.format("%02x", b&0xff));
        return sb.toString();
    }

    public static SecretKey generateKey(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Number of PBKDF2 hardening rounds to use. Larger values increase
        // computation time. You should select a value that causes computation
        // to take >100ms.
        //final int iterations = 1000;
        // Generate a 256-bit key
        //final int outputKeyLength = 256;
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(1);
        byte[] salt = sr.generateSeed(256);
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(new PBEKeySpec(password.toCharArray(), salt, 1000, 256));
    }

    public static SecretKey encryptSHA1(String password) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance(SHA1_ALGORITHM);
        sha1.update(password.getBytes(), 0, password.length());
        StringBuilder sb = new StringBuilder();
        for(byte b : sha1.digest())
            sb.append(String.format("%02x", b&0xff));
        //Log.wtf("SHA-1 key", sb.toString().substring(0,32));
        return new SecretKeySpec(sb.toString().substring(0,32).getBytes(), AES_ALGORITHM);
    }

}
