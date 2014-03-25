package x.commons.util.mail;

import javax.mail.*;

class PasswordAuthenticator extends Authenticator {
	
	private String userName = null;
	private String password = null;

	PasswordAuthenticator(String username, String password) {
		this.userName = username;
		this.password = password;
	}

	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(userName, password);
	}
}