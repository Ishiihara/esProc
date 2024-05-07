package com.scudata.dm;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * �ļ��ӿ�
 * @author WangXiaoJun
 *
 */
public interface IFile {
	/**
	 * �����ļ���
	 * @param fileName
	 */
	void setFileName(String fileName);

	/**
	 * ȡ������
	 * @throws IOException
	 * @return InputStream
	 */
	InputStream getInputStream();

	/**
	 * ȡ��������ļ��������򴴽�
	 * @param isAppend boolean �Ƿ�׷��
	 * @return OutputStream
	 */
	OutputStream getOutputStream(boolean isAppend);

	/**
	 * ȡ�ܹ����д����������ļ��������򴴽�
	 * @param isAppend boolean �Ƿ�׷��
	 * @return RandomOutputStream
	 */
	RandomOutputStream getRandomOutputStream(boolean isAppend);

	/**
	 * �����ļ��Ƿ����
	 * @return boolean
	 */
	boolean exists();

	/**
	 * �����ļ���С
	 * @return long
	 */
	long size();

	/**
	 * ��������޸�ʱ��
	 * @return long
	 */
	long lastModified();

	/**
	 * ɾ���ļ��������Ƿ�ɹ�
	 * @return boolean
	 */
	boolean delete();
	
	/**
	 * ɾ���ļ��м������ļ�
	 * @return
	 */
	boolean deleteDir();

	/**
	 * �ƶ��ļ���path��pathֻ���ļ��������
	 * @param path String �ļ������ļ�·����
	 * @param opt String @y	Ŀ���ļ��Ѵ���ʱǿ�и��ƣ�ȱʡ��ʧ�ܣ���@c	����
	 * @return boolean
	 */
	boolean move(String path, String opt);

	/**
	 * ������ʱ�ļ�
	 * @param prefix String
	 * @return String ���ؾ���·���ļ���
	 */
	String createTempFile(String prefix);
	
	/**
	 * ȡ��������ļ����������֧���򷵻�null
	 * @return RandomAccessFile
	 */
	RandomAccessFile getRandomAccessFile();
	
	/**
	 * �����Ƿ������ļ�
	 * @return true�������ļ���false���������ļ�
	 */
	public boolean isCloudFile();
}
