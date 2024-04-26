package com.scudata.server.odbc;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.scudata.app.config.ConfigConsts;
import com.scudata.app.config.ConfigWriter;
import com.scudata.common.Logger;
import com.scudata.common.ScudataLogger;
import com.scudata.common.StringUtils;
import com.scudata.common.ScudataLogger.FileHandler;
import com.scudata.parallel.UnitClient;
import com.scudata.parallel.UnitContext;
import com.scudata.parallel.XmlUtil;
import com.scudata.resources.ParallelMessage;
import com.scudata.server.ConnectionProxyManager;
import com.scudata.server.unit.UnitServer;

/**
 * ODBC������������
 */
public class OdbcContext extends ConfigWriter {
	public static final String ODBC_CONFIG_FILE = "OdbcServer.xml";

	private String host = UnitContext.getDefaultHost();//"127.0.0.1";
	private int port = 8501, timeOut = 2; // ��ʱ�ļ����ʱ�䣬СʱΪ��λ��0Ϊ����鳬ʱ

	// Connection
	private int conMax = 10;
	private int conTimeOut = 2;// ���Ӵ��ʱ�䣬СʱΪ��λ��0Ϊ����鳬ʱ
	private int conPeriod = 5; // �����������ʱ�ļ����ڵ�ʱ������0Ϊ�������ڡ��ļ��Լ��α����Ĺ���ʱ��,��λ��
	private boolean autoStart=false;
	
	private List<User> users = null;

	/**
	 * ����odbc������������
	 */
	public OdbcContext(){
		try {
			InputStream inputStream = UnitContext.getUnitInputStream(ODBC_CONFIG_FILE);
			if (inputStream != null) {
				load(inputStream);
			}
		}catch (Exception x) {
			x.printStackTrace();
		}
	}

	/**
	 * ��������
	 * @param is �����ļ�������
	 * @throws Exception
	 */
	public void load(InputStream is) throws Exception {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document xmlDocument = docBuilder.parse(is);
		NodeList nl = xmlDocument.getChildNodes();
		Node root = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeName().equalsIgnoreCase("Server")) {
				root = n;
			}
		}
		if (root == null) {
			throw new Exception(ParallelMessage.get().getMessage("UnitConfig.errorxml"));
		}

		// Server ����
		String buf = XmlUtil.getAttribute(root, "host");
		if (StringUtils.isValidString(buf)) {
			host = buf;
		}

		buf = XmlUtil.getAttribute(root, "port");
		if (StringUtils.isValidString(buf)) {
			port = Integer.parseInt(buf);
		}

		buf = XmlUtil.getAttribute(root, "autostart");
		if (StringUtils.isValidString(buf)) {
			autoStart = Boolean.parseBoolean(buf);
		}

		// �̶������־������̨�� �� start.home/nodes/[ip_port]/log Ŀ¼��
		String home = UnitServer.getHome();
		String file = "odbc/" + UnitClient.getHostPath(host) + "_" + port + "/log/log.txt";
		File f = new File(home, file);
		File fp = f.getParentFile();
		if (!fp.exists()) {
			fp.mkdirs();
		}
		String logFile = f.getAbsolutePath();
		FileHandler lfh = ScudataLogger.newFileHandler(logFile);
		ScudataLogger.addFileHandler(lfh);
		
		buf = XmlUtil.getAttribute(root, "timeout");
		if (StringUtils.isValidString(buf)) {
			timeOut = Integer.parseInt(buf);
		}

		Node conNode = XmlUtil.findSonNode(root, "Connection");
		Node subNode = XmlUtil.findSonNode(conNode, "Max");
		buf = XmlUtil.getNodeValue(subNode);
		if (StringUtils.isValidString(buf)) {
			int t = Integer.parseInt(buf);
			if (t > 0)
				conMax = t;
		}

		subNode = XmlUtil.findSonNode(conNode, "Timeout");
		buf = XmlUtil.getNodeValue(subNode);
		if (StringUtils.isValidString(buf)) {
			int t = Integer.parseInt(buf);
			if (t > 0)
				conTimeOut = t;
		}

		subNode = XmlUtil.findSonNode(conNode, "Period");
		buf = XmlUtil.getNodeValue(subNode);
		if (StringUtils.isValidString(buf)) {
			int t = Integer.parseInt(buf);
			if (t > 0)
				conPeriod = t;
		}

		Node usersNode = XmlUtil.findSonNode(root, "Users");
		NodeList userList = usersNode.getChildNodes();

		users = new ArrayList<User>();
		for (int i = 0, size = userList.getLength(); i < size; i++) {
			Node xmlNode = userList.item(i);
			if (!(xmlNode.getNodeName().equalsIgnoreCase("User")))
				continue;
			User user = new User();
			buf = XmlUtil.getAttribute(xmlNode, "name");
			user.name = buf;

			buf = XmlUtil.getAttribute(xmlNode, "password");
			user.password = buf;

			buf = XmlUtil.getAttribute(xmlNode, "admin");
			if (StringUtils.isValidString(buf)) {
				user.admin = Boolean.parseBoolean(buf);
			}

			users.add(user);
		}

	}

	/**
	 * �������õ������
	 * @param out �����
	 * @throws SAXException
	 */
	public void save(OutputStream out) throws SAXException {
		Result resultxml = new StreamResult(out);
		handler.setResult(resultxml);
		level = 0;
		handler.startDocument();
		// ���ø��ڵ�Ͱ汾
		handler.startElement("", "", "Server", getAttributesImpl(new String[] {
				ConfigConsts.VERSION, "1", "host", host, "port", port + "","autostart", autoStart + "",
				"timeout", timeOut + ""})); 

		level = 1;
		startElement("Connection", null);
		level = 2;
		writeAttribute("Max", conMax + "");
		writeAttribute("Timeout", conTimeOut + "");
		writeAttribute("Period", conPeriod + "");
		level = 1;
		endElement("Connection");

		startElement("Users", null);
		if (users != null) {
			for (int i = 0, size = users.size(); i < size; i++) {
				level = 2;
				User u = users.get(i);
				startElement(
						"User",
						getAttributesImpl(new String[] { "name", u.name,
								"password", u.password, "admin", u.admin + "" }));
				endElement("User");
			}
			level = 1;
			endElement("Users");
		} else {
			endEmptyElement("Users");
		}

		handler.endElement("", "", "Server");
		// �ĵ�����,ͬ��������
		handler.endDocument();
	}

	/**
	 * ��ȡ������IP
	 * @return IP��ַ
	 */
	public String getHost() {
		return host;
	}

	/**
	 * ��ȡ�˿ں�
	 * @return �˿ں�
	 */
	public int getPort() {
		return port;
	}

	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * ���ö˿ں�
	 * @param port �˿�
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * �����Ƿ��Զ�����
	 * @param as �Ƿ��Զ�����
	 */
	public void setAutoStart(boolean as) {
		this.autoStart = as;
	}

	/**
	 * ��ȡ�������Ƿ��Զ�������
	 * @return �Ƿ�������
	 */
	public boolean isAutoStart() {
		return autoStart;
	}
	
	/**
	 * ��ȡ���ӳ�ʱ��ʱ��
	 * @return ��ʱʱ��
	 */
	public int getTimeOut() {
		return timeOut;
	}

	/**
	 * ������ʱ�ļ����ʱ�䣬СʱΪ��λ��0Ϊ����鳬ʱ
	 * @param timeout ��ʱʱ��
	 */
	public void setTimeOut(int timeout) {
		this.timeOut = timeout;
	}

	/**
	 * ��ȡ���������Ŀ
	 * @return ���������
	 */
	public int getConMax() {
		return conMax;
	}

	/**
	 * �������������
	 * @param max ���������
	 */
	public void setConMax(int max) {
		this.conMax = max;
	}

	/**
	 * ��ȡ���ӳ�ʱ��ʱ��
	 * @return ���ӳ�ʱ��ʱ��
	 */
	public int getConTimeOut() {
		return conTimeOut;
	}
	/**
	 * �������Ӵ��ʱ�䣬СʱΪ��λ��0Ϊ����鳬ʱ
	 * @param cto ʱ��
	 */
	public void setConTimeOut(int cto) {
		this.conTimeOut = cto;
	}

	/**
	 * ��ȡ��鳬ʱ���
	 * @return ��ʱ����� 
	 */
	public int getConPeriod() {
		return this.conPeriod;
	}

	/**
	 * ���ü����������ʱ�ļ����ڵ�ʱ������0Ϊ�������ڡ�
	 * �ļ��Լ��α����Ĺ���ʱ��,��λ��
	 */
	public void setConPeriod(int period) {
		this.conPeriod = period;
	}

	/**
	 * ��ȡ�û��б�
	 * @return �û��б�
	 */
	public List<User> getUserList() {
		return users;
	}

	/**
	 * �����û��б�
	 * @param users �û��б�
	 */
	public void setUserList(List<User> users) {
		this.users = users;
	}

	/**
	 * ʵ��toString�ӿ�
	 */
	public String toString() {
		return host + ":" + port;
	}

	/**
	 * ����û��Ƿ����
	 * @param user �û���
	 * @return  ����ʱ����true�����򷵻�false
	 */
	public boolean isUserExist(String user) {
		if (users == null) {
			return true;
		}
		for (User u : users) {
			if (u.getName().equalsIgnoreCase(user))
				return true;
		}
		return false;
	}

	/**
	 * У���û��Ϸ���
	 * @param user �û���
	 * @param password ����
	 * @return У��ͨ������true�����򷵻�false
	 * @throws Exception
	 */
	public boolean checkUser(String user, String password) throws Exception{
		ConnectionProxyManager cpm = ConnectionProxyManager.getInstance(); 
		if(cpm.size()>=conMax){
			throw new Exception("Exceed server's max connections, login user:"+user);
		}
		int size = users.size();
		for (int i = 0; i < size; i++) {
			User u = users.get(i);
			if (u.getName().equalsIgnoreCase(user)) {
				if (u.getPassword().equals(password)) {
					return true;
				} else {
					throw new Exception("Invalid password.");
				}
			}
		}
		throw new Exception("Invalid user name.");
	}

	// ����1��ʾ��ȷ����������
	public static class User {
		private String name = null;
		private String password = null;
		private boolean admin = false;

		public User() {
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public boolean isAdmin() {
			return admin;
		}

		public void setAdmin(boolean admin) {
			this.admin = admin;
		}
	}
}