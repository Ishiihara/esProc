package com.raqsoft.dm.sql.simple;

import com.raqsoft.common.RQException;

public class Concat implements IFunction
{
	public String getFormula(String[] params) 
	{
		StringBuffer sb = new StringBuffer();
		for(int i = 0, len = params.length; i < len; i++)
		{
			if(params[i].isEmpty())
			{
				throw new RQException("Concat������������Ϊ��");
			}
			if(i > 0)
			{
				sb.append(",");
			}
			sb.append(params[i]);
		}
		return String.format("concat(%s)", sb.toString());
	}
}