package com.cloud.gateway.config;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;


/**
 *
 * @author:胡立涛
 * @description: TODO RSA加密
 * @date: 2022/3/30
 * @param:
 * @return:
 */
public class RSAHelper {

     final Key pubKey;


    private  final Cipher pubDecryptCipher;

    public static Key keySpecFromByte(byte[] key, boolean pubOrPri) throws Exception {
        KeyFactory rsaKeyFactory = KeyFactory.getInstance("RSA");
        KeySpec keySpec = new X509EncodedKeySpec(key);
        try {
            if(pubOrPri)
                return rsaKeyFactory.generatePublic(keySpec);
            else
                return rsaKeyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            if(!e.getMessage().contains("supported for RSA"))
                throw e;
            keySpec = new PKCS8EncodedKeySpec(key);
            if(pubOrPri)
                return rsaKeyFactory.generatePublic(keySpec);
            else
                return rsaKeyFactory.generatePrivate(keySpec);
        }
    }

    public RSAHelper(String hexPubKey) throws Exception {
        try {
            byte[] pubKey = Hex.decodeHex(hexPubKey);
            this.pubKey = keySpecFromByte(pubKey,true);
            pubDecryptCipher = Cipher.getInstance("RSA");
            pubDecryptCipher.init(Cipher.DECRYPT_MODE, this.pubKey);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }
    public RSAHelper(byte[] pubKey)throws Exception {
        try {
            this.pubKey = keySpecFromByte(pubKey,true);
            pubDecryptCipher = Cipher.getInstance("RSA");
            pubDecryptCipher.init(Cipher.DECRYPT_MODE, this.pubKey);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }


    /**
     *
     * @param data Data must not be longer than 128 bytes
     */
    public byte[] decryptByPubKey(byte[] data) throws Exception {
        try {
            return pubDecryptCipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    private byte[] crypt(byte[] input,Cipher cipher, int MAX_ENCRYPT_BLOCK) throws Exception {
        int offSet = 0;
        byte[] cache = {};
        try(ByteArrayOutputStream bops = new ByteArrayOutputStream()) {
            int inputLength = input.length;
            while (inputLength - offSet > 0) {
                if (inputLength - offSet > MAX_ENCRYPT_BLOCK) {
                    cache = cipher.doFinal(input, offSet, MAX_ENCRYPT_BLOCK);
                    offSet += MAX_ENCRYPT_BLOCK;
                } else {
                    cache = cipher.doFinal(input, offSet, inputLength - offSet);
                    offSet = inputLength;
                }
                bops.write(cache);
            }
            return bops.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    public String getHexPubKey(){
        return Hex.encodeHexString(pubKey.getEncoded(), false);
    }


    public String getBase64PubKey(){
        return  Base64.getEncoder().encodeToString(pubKey.getEncoded());
    }


    @Override
    public String toString() {
        return  "hexPubKey:  "+getHexPubKey()+"\n"+
                "Base64PubKey:  "+getBase64PubKey()+"\n";
    }
}
