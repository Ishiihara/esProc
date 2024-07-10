package com.scudata.expression.fn.string;

import javax.crypto.*;
import javax.crypto.spec.*;

import com.scudata.common.Logger;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.expression.IParam;
import com.scudata.expression.fn.CharFunction;
import com.scudata.resources.EngineMessage;

/**
 * aes(input, key, iv; charset) AES����/����
 * parament input	���룬blob����String
 * parament key		���룬������ͳһ
 * parament iv		��ʼ��������������ͳһ
 * parament charset	�ַ����룬���input/key/ivΪ�ַ���ʱʹ�ã�Ĭ��Ϊutf8
 * ѡ��@d	���ܣ�Ĭ��ִ�м���
 * ѡ��@c	CBC(Cipher Block Chaining)ģʽ��Ĭ��ʹ��ECB(Electronic Codebook)ģʽ	
 * ѡ��@n	���ģʽNoPadding��ȱʡPKCS5Padding
 * ���أ�inputΪ�ַ���ʱ�����ַ�����Ϊ����������ʱ���ض���������
 * @author bd
 *
 */
public class AESEncrypt extends CharFunction {	
    // AES��Կ�㷨
    private static final String KEY_ALGORITHM = "AES";
 
    // ����/�����㷨/����ģʽ/��䷽ʽ
    private static final String CIPHER_ALGORITHM_E = "AES/ECB/PKCS5Padding";
    private static final String CIPHER_ALGORITHM_C = "AES/CBC/PKCS5Padding";
    private static final String CIPHER_ALGORITHM_EN = "AES/ECB/NoPadding";
    private static final String CIPHER_ALGORITHM_CN = "AES/CBC/NoPadding";
    
    private static final byte[] DEF_IV = DESEncrypt.encode("1a3b5c7d9e0f2g4h", "utf-8");
     
	protected Object doQuery(Object[] objs) {
		try {
			if (objs==null || objs.length<2){
				MessageManager mm = EngineMessage.get();
				throw new RQException("aes" + mm.getMessage("function.invalidParam"));
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
					throw new RQException("aes(...;charset), charset is invalid." + mm.getMessage("function.invalidParam"));
				}
			}
			if (params == null || params.length < 2){
				MessageManager mm = EngineMessage.get();
				throw new RQException("aes" + mm.getMessage("function.invalidParam"));
			}
			//boolean string = params[0] instanceof String;
			byte[] inputb = DESEncrypt.encode(params[0], charset);
			byte[] keyb = DESEncrypt.encode(params[1], charset);
			byte[] ivb = null;
			if (inputb == null) return null;
			if (keyb == null) return inputb;
			if (params.length > 2) {
				ivb = DESEncrypt.encode(params[2], charset);
			}
			boolean dec = false;
			boolean modeC = false;
			boolean nopad = false;
			boolean autopad = false;
			boolean decStr = false;
			//boolean keyback = false;
			if (option != null) {
				if (option.indexOf('d')>-1) {
					dec = true;
				}
				if (option.indexOf('c')>-1) {
					modeC = true;
				}
				if (option.indexOf('n')>-1) {
					nopad = true;
				}
				if (option.indexOf('a')>-1) {
					autopad = true;
				}
				//if (option.indexOf('k')>-1) {
				//	keyback = true;
				//}
				if (option.indexOf('s')>-1) {
					decStr = true;
				}
			}
			if (nopad) {
				if (inputb.length % 16 != 0 ) {
					if (autopad) {
						MessageManager mm = EngineMessage.get();
						Logger.info("aes(input, key, iv; charset): " + mm.getMessage("encrypt.autopadding", "input", 16));
						inputb = autoPadding(inputb, 16);
					}
					else {
						MessageManager mm = EngineMessage.get();
						throw new RQException("aes(input, key, iv; charset): " + mm.getMessage("encrypt.nopadding", "input", 16));
					}
				}
			}
			if (keyb.length != 16) {
				if (autopad) {
					keyb = padding(keyb, 16);
				}
				else {
					MessageManager mm = EngineMessage.get();
					throw new RQException("aes(input, key, iv; charset): " + mm.getMessage("encrypt.wronglen", "key", 16));
				}
			}
			Cipher ci = null;
			if (modeC) {
				if (ivb == null) {
					MessageManager mm = EngineMessage.get();
					Logger.info("aes(input, key, iv; charset): " + mm.getMessage("encrypt.CBCnoiv"));
					ivb = DEF_IV;
				}
				else if (ivb.length != 16) {
					if (autopad) {
						ivb = padding(keyb, 16);
					}
					else {
						MessageManager mm = EngineMessage.get();
						throw new RQException("aes(input, key, iv; charset): " + mm.getMessage("encrypt.wronglen", "iv", 16));
					}
				}
				if (nopad) {
					ci = Cipher.getInstance(AESEncrypt.CIPHER_ALGORITHM_CN);
				} else {
					ci = Cipher.getInstance(AESEncrypt.CIPHER_ALGORITHM_C);
				}
			} else {
				if (nopad) {
					ci = Cipher.getInstance(AESEncrypt.CIPHER_ALGORITHM_EN);
				} else {
					ci = Cipher.getInstance(AESEncrypt.CIPHER_ALGORITHM_E);
				}
			}
			IvParameterSpec iv = null;
			if (ivb != null) {
				iv = new IvParameterSpec(ivb);
			}
			SecretKeySpec key = new SecretKeySpec(keyb, AESEncrypt.KEY_ALGORITHM);
			byte[] result = null;
			if (dec) {
				if (!modeC) {
					ci.init(Cipher.DECRYPT_MODE, key);
					result = ci.doFinal(inputb);
				}
				else {
					ci.init(Cipher.DECRYPT_MODE, key, iv);
					result = ci.doFinal(inputb);
				}
				if (decStr) {
					return DESEncrypt.encode(result, charset);
				}
			} else {
				if (!modeC) {
					ci.init(Cipher.ENCRYPT_MODE, key);
					result = ci.doFinal(inputb);
				}
				else {
					ci.init(Cipher.ENCRYPT_MODE, key, iv);
					result = ci.doFinal(inputb);
					/*
					byte[] bs = ci.getIV();
					System.out.println();
					for (int i = 0; i < bs.length; i++) {
						System.out.print(bs[i]+",");
					}
					System.out.println();
					if (charset.equalsIgnoreCase("unicodeescape")  ||
							charset.equalsIgnoreCase("unicode escape") ||
							charset.equalsIgnoreCase("unicode-escape") ){
						return CharEncode.utf_unescape(new String(result));
					}else{
						System.out.println(new String(bs, charset));
					}
					*/
				}
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
	
	protected static byte[] padding(byte[] input, int length) {
		byte[] result = new byte[length];
		if (input == null) {
			return result; 
		}
		int len = input.length;
		if (len == length) return input;
		else if (len > length) {
			for (int i = 0; i < length; i++) {
				result[i] = input[i];
			}
		}
		else {
			for (int i = 0; i < len; i++) {
				result[i] = input[i];
			}
		}
		return result;
	}
	
	protected static byte[] autoPadding(byte[] input, int size) {
		byte[] result = new byte[size];
		if (input == null) {
			return result; 
		}
		int len = input.length;
		int length = len/size *size;
		byte diff = (byte) (length + size - len);
		if (len == length) return input;
		else {
			length = length + size;
			result = new byte[length];
			int i = 0;
			for (; i < len; i++) {
				result[i] = input[i];
			}
			for (; i < length; i++) {
				result[i] = diff;
			}
			return result;
		}
	}
}
