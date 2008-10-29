package org.designup.picsou.license.mail;

import org.designup.picsou.license.model.License;
import org.globsframework.model.Glob;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

public class Mailer {
  private int port = 25;
  private String host = "localhost";
  private String fromAdress = "picsou@noreply.com";

  public Mailer() {
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public void sendRequestLicense(String to) throws MessagingException {
    sendMail(to, fromAdress, "picsou", "Bonjour\nCliquez sur le lien suivant pour acheter Picsou.");
  }

  private void sendMail(String to, String from, String subjet, String content) throws MessagingException {

    Properties mailProperties = new Properties();
    mailProperties.setProperty("mail.smtp.host", host);
    mailProperties.setProperty("mail.smtp.port", Integer.toString(port));

    Session session = Session.getDefaultInstance(mailProperties);

    MimeMessage message = new MimeMessage(session);
    message.setFrom(new InternetAddress(from));
    message.setSubject(subjet);
    message.setSentDate(new Date());
    message.setText(content);
    message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
    Transport.send(message);
  }

  public void sendExistingLicense(Glob license) {
    try {
      sendMail(license.get(License.MAIL), fromAdress, "picsou",
               "Bonjour\nVous avez demander a ce qu'on vous renvoie votre licence.");
    }
    catch (MessagingException e) {
      e.printStackTrace();
    }
  }

  public boolean sendNewLicense(String mail, String code) {
    try {
      sendMail(mail, fromAdress, "picsou code d'activation",
               "Bonjour\nVoici votre nouveau code d'activation : " + code);
      return true;
    }
    catch (MessagingException e) {
      e.printStackTrace();
      return false;
    }
  }
}
