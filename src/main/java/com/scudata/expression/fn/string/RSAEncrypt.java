package com.scudata.expression.fn.string;

import javax.crypto.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import com.scudata.common.Logger;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Sequence;
import com.scudata.expression.IParam;
import com.scudata.expression.fn.CharFunction;
import com.scudata.resources.EngineMessage;

/**
 * rsa(input, m:e; charset) RSA����/����
 * parament input	���룬blob����String
 * parament modulus	ģ���ַ������ߴ�������û��eʱΪ�ַ������߶���������
 * parament e		ָ�����ַ������ߴ�����
 * parament charset	�ַ����룬���inputΪ�ַ���ʱʹ�ã�Ĭ��Ϊutf8
 * ѡ��@d	���ܣ�Ĭ��ִ�м���
 * ���أ�inputΪ�ַ���ʱ�����ַ�����Ϊ����������ʱ���ض���������
 * @author bd
 *
 */
public class RSAEncrypt extends CharFunction {	
    // AES��Կ�㷨
    private static final String KEY_ALGORITHM = "RSA";
 
    // ������Կ
    public static KeyPair generateKeyPair(int n) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyGen.initialize(n, new SecureRandom());
        return keyGen.genKeyPair();
    }
    
	protected Object doQuery(Object[] objs) {
		try {
			boolean dec = false;
			boolean createKey = false;
			boolean modulus = false;
			boolean blob = false;
			boolean decStr = false;
			if (option != null) {
				if (option.indexOf('k')>-1) {
					createKey = true;
				}
				if (option.indexOf('d')>-1) {
					dec = true;
				}
				if (option.indexOf('m')>-1) {
					modulus = true;
				}
				if (option.indexOf('b')>-1) {
					blob = true;
				}
				if (option.indexOf('s')>-1) {
					decStr = true;
				}
			}
			if (createKey) {
				int n = 2048;
				if (objs!=null && objs.length>0){
					Object setn = objs[0];
					if (setn instanceof Number) {
						n = ((Number) setn).intValue();
					}
					else if (setn != null) {
						n = Integer.valueOf(setn.toString());
					}
				}
				if (n < 512) {
					MessageManager mm = EngineMessage.get();
					Logger.info("rsa@k(n): " + mm.getMessage("encrypt.keylen", 512));
					n = 512;
				}
				//@k ѡ��������Կ��
				KeyPair kp = RSAEncrypt.generateKeyPair(n);
				RSAPublicKey pubKey = (RSAPublicKey) kp.getPublic();
				RSAPrivateKey priKey = (RSAPrivateKey)kp.getPrivate();
				Sequence result = null;
				if (modulus) {
					result = new Sequence(3);
					// ���� modulus/exponentģʽ��Կ
					if (blob) {
						result.add(pubKey.getModulus().toByteArray());
						result.add(pubKey.getPublicExponent().toByteArray());
						result.add(priKey.getPrivateExponent().toByteArray());
					}
					else {
						result.add(pubKey.getModulus().toString());
						result.add(pubKey.getPublicExponent().toString());
						result.add(priKey.getPrivateExponent().toString());
					}
				}
				else {
					result = new Sequence(2);
					result.add(pubKey.getEncoded());
					result.add(priKey.getEncoded());
				}
				return result;
			}
			if (objs==null || objs.length<2){
				MessageManager mm = EngineMessage.get();
				throw new RQException("rsa" + mm.getMessage("function.invalidParam"));
			}
			Object[] params = objs;
			String charset = "utf-8";
			if (param.getType() == IParam.Semicolon) {
				Object obj = objs[0];
				if (obj instanceof Object[]) {
					params = (Object[])obj;
				}
				else {
					params = new Object[1];
					params[0] = obj;
				}
				//aes(...;charset)�����
				if (objs[1] instanceof String) {
					charset = (String) objs[1];
				}
				else {
					MessageManager mm = EngineMessage.get();
					throw new RQException("rsa(...;charset), charset is invalid." + mm.getMessage("function.invalidParam"));
				}
			}
			if (params == null || params.length < 2){
				MessageManager mm = EngineMessage.get();
				throw new RQException("rsa" + mm.getMessage("function.invalidParam"));
			}
			//boolean string = params[0] instanceof String;
			byte[] inputb = DESEncrypt.encode(params[0], charset);
			Object objM = null, objE = null;
			BigInteger bigM = null, bigE = null;
			Object sub = params[1];
			byte[] mb = null;
			if (sub instanceof Object[]) {
				Object[] subs = (Object[]) sub;
				if (subs == null || subs.length < 1) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("rsa" + mm.getMessage("function.invalidParam"));
				}
				objM = subs[0];
				if (subs.length > 1) {
					objE = subs[1];
				}
			}
			else {
				objM = sub;
			}
			if (objE != null) {
				bigM = RSAEncrypt.getBigInteger(objM);
				bigE = RSAEncrypt.getBigInteger(objE);
			}
			else {
				// ֻ��m
				mb = DESEncrypt.encode(objM, charset);
			}
			Cipher ci = Cipher.getInstance(RSAEncrypt.KEY_ALGORITHM);
			KeyFactory kf = KeyFactory.getInstance(KEY_ALGORITHM);
			KeySpec keySpec = null;
			if (bigE == null) {
				keySpec = new X509EncodedKeySpec(mb);
			}
			else {
				keySpec = new RSAPublicKeySpec(bigM, bigE);
			}
			byte[] result = null;
			if (dec) {
				if (bigE != null) {
					keySpec = new RSAPrivateKeySpec(bigM, bigE);
				}
				else {
					keySpec = new PKCS8EncodedKeySpec(mb);
				}
				PrivateKey key = kf.generatePrivate(keySpec);
				ci.init(Cipher.DECRYPT_MODE, key);
				result = ci.doFinal(inputb);
				if (decStr) {
					return DESEncrypt.encode(result, charset);
				}
			} else {
				PublicKey key = kf.generatePublic(keySpec);
				ci.init(Cipher.ENCRYPT_MODE, key);
				result = ci.doFinal(inputb);
			}
			/*
			if (string) {			
				if (charset.equalsIgnoreCase("unicodeescape")  ||
						charset.equalsIgnoreCase("unicode escape") ||
						charset.equalsIgnoreCase("unicode-escape") ){
					return CharEncode.utf_unescape(new String(result));
				}else{
					return new String(result, charset);
				}
			}
			*/
			return result;
		} catch (Exception e) {
			Logger.error(e.getMessage());
		}
		
		return null;
	}
	
	public static BigInteger getBigInteger(Object obj) {
		if (obj == null) return null;
		if (obj instanceof BigInteger) {
			return (BigInteger) obj;
		}
		else if (obj instanceof byte[]) {
			return new BigInteger(1, (byte[])obj);
		}
		else if (obj instanceof BigDecimal) {
			return ((BigDecimal) obj).toBigInteger();
		}
		else if (obj instanceof String) {
			return new BigInteger((String) obj);
		}
		else if (obj instanceof Number) {
			return new BigInteger(Long.toString(((Number) obj).longValue()));
		}
		MessageManager mm = EngineMessage.get();
		throw new RQException("rsa" + mm.getMessage("function.paramTypeError"));
	}
}
