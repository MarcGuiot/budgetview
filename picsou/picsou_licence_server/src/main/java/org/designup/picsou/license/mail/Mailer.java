package org.designup.picsou.license.mail;

import org.designup.picsou.licence.Lang;
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

  public void sendRequestLicence(String to, String lang) throws MessagingException {
    try {
      sendMail(to, fromAdress,
               Lang.get("request.license.subject", lang),
               Lang.get("request.license.message", lang));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void sendExistingLicense(Glob licence, String lang) {
    try {
      sendMail(licence.get(License.MAIL), fromAdress, Lang.get("resend.license.subject", lang),
               Lang.get("resend.license.message", lang));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public boolean sendNewLicense(String mail, String code, String lang) {
    try {
      sendMail(mail, fromAdress, Lang.get("new.license.subject", lang),
               Lang.get("new.license.message", lang, code));
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