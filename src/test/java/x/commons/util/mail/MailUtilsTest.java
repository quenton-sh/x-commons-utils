package x.commons.util.mail;

import org.junit.Test;

import x.commons.util.mail.MailProperties;
import x.commons.util.mail.MailUtils;

public class MailUtilsTest {
	
	@Test
	public void sendTextMail() throws Exception {
		MailProperties props = new MailProperties();
		props.setAuthentication(true);
		props.setContent("<html><body><h3><font color=\"red\">测试-邮件内容</font></h3></body></html>");
		props.setFromAddress("mailutils@yeah.net");
		props.setServerHost("smtp.yeah.net");
		props.setSubject("测试邮件-text");
		props.setToAddress("mailutils@yeah.net");
		props.setUserName("mailutils@yeah.net");
		props.setPassword("mailutils123");
		MailUtils.sendTextMail(props);
		
		props.setEncryptProtocol(EncryptProtocol.SSL);
		props.setSubject("测试邮件-text-SSL");
		MailUtils.sendTextMail(props);
		
		props.setEncryptProtocol(EncryptProtocol.TSL);
		props.setSubject("测试邮件-text-TSL");
		MailUtils.sendTextMail(props);
	}
	
	@Test
	public void sendHtmlMail() throws Exception {
		MailProperties props = new MailProperties();
		props.setAuthentication(true);
		props.setContent("<html><body><h3><font color=\"red\">测试-邮件内容</font></h3></body></html>");
		props.setFromAddress("mailutils@yeah.net");
		props.setFromNickname("测试邮件发件人");
		props.setServerHost("smtp.yeah.net");
		props.setSubject("测试邮件-html");
		props.setToAddress("mailutils@yeah.net");
		props.setUserName("mailutils@yeah.net");
		props.setPassword("mailutils123");
//		MailUtils.sendHtmlMail(props);
		
		props.setEncryptProtocol(EncryptProtocol.SSL);
		props.setSubject("测试邮件-html-SSL");
		MailUtils.sendHtmlMail(props);
		
		props.setEncryptProtocol(EncryptProtocol.TSL);
		props.setSubject("测试邮件-html-TSL");
		MailUtils.sendHtmlMail(props);
	}
}
