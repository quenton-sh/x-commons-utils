package x.commons.util.mail;

import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.sun.mail.smtp.SMTPTransport;


public class MailUtils {
	
	private static void doSendMail(MailProperties mailProperties, boolean isHtml) throws Exception {
		PasswordAuthenticator authenticator = null;
		Properties properties = mailProperties.getProperties();
		if (mailProperties.needAuthentication()) {
			authenticator = new PasswordAuthenticator(mailProperties.getUserName(),
					mailProperties.getPassword());
		}
		
		Session sendMailSession = Session
				.getDefaultInstance(properties, authenticator);
		Transport transport = null;
		try {
			Message mailMessage = new MimeMessage(sendMailSession);
			
			Address from = null;
			if (mailProperties.getFromNickname() != null) {
				String nick = javax.mail.internet.MimeUtility.encodeText(mailProperties.getFromNickname());
				from = new InternetAddress(nick + " <" + mailProperties.getFromAddress() + ">");
			} else {
				from = new InternetAddress(mailProperties.getFromAddress());
			}
			mailMessage.setFrom(from);
			
			InternetAddress[] toAddrs = InternetAddress.parse(mailProperties.getToAddress(), false);
			mailMessage.setRecipients(Message.RecipientType.TO, toAddrs);
			
			mailMessage.setSubject(mailProperties.getSubject());
			mailMessage.setSentDate(new Date());
			
			if (isHtml) {
				Multipart mainPart = new MimeMultipart();
				BodyPart html = new MimeBodyPart();
				html.setContent(mailProperties.getContent(), "text/html;charset=utf-8");
				mainPart.addBodyPart(html);
				mailMessage.setContent(mainPart);
			} else {
				String mailContent = mailProperties.getContent();
				mailMessage.setText(mailContent);
			}
			
			URLName url = new URLName("smtp", 
					mailProperties.getServerHost(), 
					mailProperties.getServerPort(), "", 
					mailProperties.getUserName(), 
					mailProperties.getPassword());
			transport = new SMTPTransport(sendMailSession, url);
			transport.connect();
			transport.sendMessage(mailMessage, mailMessage.getAllRecipients());
			
		} finally {
			if(transport != null) {
				transport.close();
			}
		}
	}

	/**
	 * 发送文本格式的邮件
	 * @param mailProperties
	 * @throws MailException 
	 */
	public static void sendTextMail(MailProperties mailProperties) throws Exception {
		doSendMail(mailProperties, false);
	}

	/**
	 * 发送HTML格式的邮件
	 * @param mailProperties
	 * @throws MailException 
	 */
	public static void sendHtmlMail(MailProperties mailProperties) throws Exception {
		doSendMail(mailProperties, true);
	}
}