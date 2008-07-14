package org.designup.picsoulicence.mail;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

public class Mailler {
  private int port = 25;
  private String host = "localhost";

  public Mailler() {
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public void sendRequestLicence(String to) throws MessagingException {
    Properties mailProperties = new Properties();
    mailProperties.setProperty("mail.smtp.host", host);
    mailProperties.setProperty("mail.smtp.port", Integer.toString(port));
    Session session = Session.getDefaultInstance(mailProperties);
    MimeMessage message = new MimeMessage(session);
    message.setFrom(new InternetAddress("picsou@noreply.com"));
    message.setSubject("new user");
    message.setSentDate(new Date());
    message.setText("Bonjour\nCliquez sur le lien suivant pour achetez Picsou.");
    message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
    Transport.send(message);
  }
}
