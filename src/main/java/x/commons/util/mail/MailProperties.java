package x.commons.util.mail;

import java.util.Properties;

public class MailProperties {

	// 邮件服务器地址
	private String serverHost;
	
	// SMTP端口
	private int serverPort = -1;
	
	// 邮件发送者的地址
	private String fromAddress;
	
	// 发件人昵称
	private String fromNickname;
	
	// 邮件接收者的地址
	private String toAddress;
	
	// 登陆邮件发送服务器的用户名和密码
	private String userName;
	private String password;
	
	// 是否需要身份验证
	private boolean authentication = false;
	
	// 邮件主题
	private String subject;
	
	// 邮件的文本内容
	private String content;
	
	// 加密协议
	private EncryptProtocol encryptProtocol = EncryptProtocol.NONE;

	/**
	 * 获得邮件会话属性
	 */
	Properties getProperties() {
		Properties p = new Properties();
		p.put("mail.smtp.host", this.getServerHost());
		p.put("mail.smtp.port", "" + this.getServerPort());
		p.put("mail.smtp.auth", this.needAuthentication() ? "true" : "false");
		if (this.encryptProtocol == EncryptProtocol.SSL) {
			p.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			p.put("mail.smtp.socketFactory.port", "" + this.getServerPort());
		} else if (this.encryptProtocol == EncryptProtocol.TSL) {
			p.put("mail.smtp.starttls.enable", "true");
		}
		return p;
	}

	public String getServerHost() {
		return serverHost;
	}

	/**
	 * 设置邮件服务器地址
	 * @param serverHost 域名或IP地址
	 */
	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}

	public int getServerPort() {
		if (this.serverPort != -1) {
			return this.serverPort;
		} else {
			if (this.encryptProtocol == EncryptProtocol.SSL) {
				return 465;
			} else if (this.encryptProtocol == EncryptProtocol.TSL) {
				return 587;
			} else {
				return 25;
			}
		}
	}

	/**
	 * 设置SMTP端口
	 * @param serverPort
	 */
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public boolean needAuthentication() {
		return authentication;
	}

	/**
	 * 设置邮件服务器是否需要身份验证
	 * @param authentication
	 */
	public void setAuthentication(boolean authentication) {
		this.authentication = authentication;
	}
	
	
	public String getPassword() {
		return password;
	}

	/**
	 * 设置身份验证的密码
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getUserName() {
		return userName;
	}

	/**
	 * 设置身份验证的用户名
	 * @param userName
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getFromAddress() {
		return fromAddress;
	}

	/**
	 * 设置发件人地址
	 * @param fromAddress
	 */
	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}
	
	public String getFromNickname() {
		return fromNickname;
	}
	
	/**
	 * 设置发件人昵称
	 * @param nickname
	 */
	public void setFromNickname(String nickname) {
		this.fromNickname = nickname;
	}

	public String getToAddress() {
		return toAddress;
	}

	/**
	 * 设置收件人地址，多个地址之间用逗号分隔
	 * @param toAddress
	 */
	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}

	public String getSubject() {
		return subject;
	}

	/**
	 * 设置邮件标题
	 * @param subject
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	/**
	 * 设置邮件正文内容
	 * @param textContent
	 */
	public void setContent(String textContent) {
		this.content = textContent;
	}

	public EncryptProtocol getEncryptProtocol() {
		return encryptProtocol;
	}

	/**
	 * 设置加密协议
	 * @param encryptProtocol
	 */
	public void setEncryptProtocol(EncryptProtocol encryptProtocol) {
		this.encryptProtocol = encryptProtocol;
	}
	
}
