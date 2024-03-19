package com.scudata.dm;

import java.io.File;

/**
 * �Ǳ���File������࣬��Զ���ļ�ӳ��Ϊ����File��
 * @author LW
 *
 */
public class NonLocalFile extends File {
	private static final long serialVersionUID = 1L;

	private FileObject fo;
	
	public NonLocalFile(String pathname, FileObject fo) {
		super(pathname);
		this.fo = fo;
	}

	public String getAbsolutePath() {
        return fo.getFileName();
    }
	
	public String getName() {
		return fo.getFileName();
	}
	
	public boolean exists() {
		return fo.isExists();
	}
	
	public boolean delete() {
		return fo.delete();
	}
}
