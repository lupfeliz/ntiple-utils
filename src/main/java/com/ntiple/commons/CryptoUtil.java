/**
 * @File        : CryptoUtil.java
 * @Version     : $Rev$
 * @Author      : 정재백
 * @History     : 2024-03-31 최초 작성
 * @Description : 암호화 관련 유틸
 **/
package com.ntiple.commons;

import static com.ntiple.commons.Constants.AES;
import static com.ntiple.commons.Constants.AES_CBC_PKCS5Padding;
import static com.ntiple.commons.Constants.PBKDF2WithHmacSHA1;
import static com.ntiple.commons.Constants.RSA;
import static com.ntiple.commons.Constants.UTF8;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtil {

  public static class RSA {
    public static String decrypt(int keytype, String keystr, String ciphertext) throws Exception {
      return decrypt(key(keytype, keystr), ciphertext);
    }

    public static String decrypt(Key key, String ciphertext) throws Exception {
      String ret = "";
      byte[] buf = null;
      buf = cipher(key, Cipher.DECRYPT_MODE).doFinal(Base64.getDecoder().decode(ciphertext));
      ret = new String(buf, UTF8);
      return ret;
    }

    public static String encrypt(int keytype, String keystr, String plaintext) throws Exception {
      return encrypt(key(keytype, keystr), plaintext);
    }

    public static String encrypt(Key key, String plaintext) throws Exception {
      String ret = "";
      byte[] buf = null;
      buf = cipher(key, Cipher.ENCRYPT_MODE).doFinal(plaintext.getBytes(UTF8));
      ret = new String(Base64.getEncoder().encode(buf), UTF8);
      return ret;
    }

    public static Key key(int keytype, String keystr) throws Exception {
      Key key = null;
      if (keytype == 0) {
        key = KeyFactory.getInstance(RSA)
          .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keystr)));
      } else {
        key = KeyFactory.getInstance(RSA)
          .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(keystr)));
      }
      return key;
    }

    public static Cipher cipher(Key key, int mode) throws Exception {
      Cipher cipher = Cipher.getInstance(RSA);
      cipher.init(mode, key);
      return cipher;
    }

    public static String[] generateKeyStrs(int keysize) throws Exception {
      String[] ret = new String[] { "", "" };
      Key[] keys = generateKeys(keysize);
      ret[0] = Base64.getEncoder().encodeToString(keys[0].getEncoded());
      ret[1] = Base64.getEncoder().encodeToString(keys[1].getEncoded());
      return ret;
    }

    public static Key[] generateKeys(int keysize) throws Exception {
      Key[] ret = new Key[] { null, null };
      KeyPairGenerator gen = KeyPairGenerator.getInstance(RSA);
      gen.initialize(keysize, new SecureRandom());
      KeyPair pair = gen.genKeyPair();
      ret[0] = pair.getPrivate();
      ret[1] = pair.getPublic();
      return ret;
    }
  }

  public static class AES {
    public static String decrypt(String keystr, String ciphertext) throws Exception { return decrypt(new SecretKeySpec(Base64.getDecoder().decode(keystr), AES), null, ciphertext); }
    public static String decrypt(String keystr, byte[] iv, String ciphertext) throws Exception { return decrypt(new SecretKeySpec(Base64.getDecoder().decode(keystr), AES), iv, ciphertext); }
    public static String decrypt(Key key, String ciphertext) throws Exception { return decrypt(key, null, ciphertext); }
    public static String decrypt(Key key, byte[] iv, String ciphertext) throws Exception {
      String ret = "";
      byte[] buf = null;
      buf = cipher(key, iv, Cipher.DECRYPT_MODE).doFinal(Base64.getDecoder().decode(ciphertext));
      ret = new String(buf, UTF8);
      return ret;
    }

    public static String encrypt(String keystr, String plaintext) throws Exception { return encrypt(new SecretKeySpec(Base64.getDecoder().decode(keystr), AES), null, plaintext); }
    public static String encrypt(String keystr, byte[] iv, String plaintext) throws Exception { return encrypt(new SecretKeySpec(Base64.getDecoder().decode(keystr), AES), iv, plaintext); }
    public static String encrypt(Key key, String plaintext) throws Exception { return encrypt(key, null, plaintext); }
    public static String encrypt(Key key, byte[] iv, String plaintext) throws Exception {
      String ret = "";
      byte[] buf = null;
      buf = cipher(key, iv, Cipher.ENCRYPT_MODE).doFinal(plaintext.getBytes());
      ret = new String(Base64.getEncoder().encode(buf), UTF8);
      return ret;
    }

    public static Cipher cipher(Key key, byte[] iv, int mode) throws Exception {
      IvParameterSpec ivspec = null;
      if (iv != null)  { ivspec = new IvParameterSpec(iv); }
      return cipher(key, ivspec, mode);
    }

    public static Cipher cipher(Key key, IvParameterSpec ivspec, int mode) throws Exception {
      /** AES_CBC_PKCS5Padding 은 16byte 단위로 처리 */
      if (ivspec == null) { ivspec = new IvParameterSpec(new byte[16]); }
      Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5Padding);
      cipher.init(mode, key, ivspec);
      return cipher;
    }

    public static String generateKeyStr(String passphrase, String salt, int iterations, int keysize) throws Exception {
      String ret = "";
      ret = Base64.getEncoder().encodeToString(generateKey(passphrase, salt, iterations, keysize).getEncoded());
      return ret;
    }

    public static Key generateKey(String passphrase, String salt, int iterations, int keysize) throws Exception {
      Key ret = null;
      SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2WithHmacSHA1);
      byte[] saltbuf = new byte[0];
      if (salt != null && !"".equals(salt)) { saltbuf = salt.getBytes(UTF8); }
      KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), saltbuf, iterations, keysize);
      ret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), AES);
      return ret;
    }
  }
}
