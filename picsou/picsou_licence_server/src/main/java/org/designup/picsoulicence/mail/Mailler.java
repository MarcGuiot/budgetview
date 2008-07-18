package org.designup.picsoulicence.mail;

import org.designup.picsoulicence.model.Licence;
import org.globsframework.model.Glob;

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
  private String fromAdress = "picsou@noreply.com";

  public Mailler() {
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public void sendRequestLicence(String to) throws MessagingException {
    sendMail(to, fromAdress, "picsou", "Bonjour\nCliquez sur le lien suivant pour achetez Picsou.");
  }

  private void sendMail(String to, String adresse, String subjet, String content) throws MessagingException {
    Properties mailProperties = new Properties();
    mailProperties.setProperty("mail.smtp.host", host);
    mailProperties.setProperty("mail.smtp.port", Integer.toString(port));
    Session session = Session.getDefaultInstance(mailProperties);
    MimeMessage message = new MimeMessage(session);
    message.setFrom(new InternetAddress(adresse));
    message.setSubject(subjet);
    message.setSentDate(new Date());
    message.setText(content);
    message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
    Transport.send(message);
  }

  public void sendExistingLicence(Glob licence) {
    try {
      sendMail(licence.get(Licence.MAIL), fromAdress, "picsou",
               "Bonjour\nVous avez demander a ce qu'on vous renvoie votre licence.");
    }
    catch (MessagingException e) {
      e.printStackTrace();
    }
  }
}
