package org.designup.picsou.licence.mail;

import org.designup.picsou.licence.Lang;
import org.designup.picsou.licence.model.License;
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

  public void sendRequestLicence(String to, String lang) throws MessagingException {
    try {
      sendMail(to, fromAdress,
               Lang.get("request.licence.subject", lang),
               Lang.get("request.licence.message", lang));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void sendExistingLicence(Glob licence, String lang) {
    try {
      sendMail(licence.get(License.MAIL), fromAdress, Lang.get("resend.licence.subject", lang),
               Lang.get("resend.licence.message", lang));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public boolean sendNewLicense(String mail, String code, String lang) {
    try {
      sendMail(mail, fromAdress, Lang.get("new.licence.subject", lang),
               Lang.get("new.licence.message", lang, code));
      return true;
    }
    catch (Exception e) {
      e.printStackTrace();
      return false;
    }
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
}
