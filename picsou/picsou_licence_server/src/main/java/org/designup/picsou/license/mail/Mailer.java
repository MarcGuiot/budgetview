package org.designup.picsou.license.mail;

import org.designup.picsou.license.Lang;
import org.apache.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.*;

public class Mailer {
  static Logger logger = Logger.getLogger("mailer");
  static AtomicLong count = new AtomicLong(0);
  private static final int WAIT_BEFORE_RETRY = 1000;
  private int port = 25;
  private String host = "localhost";
  private String fromAdress = "support@mybudgetview.fr";
  private BlockingQueue<MailToSent> pendingsMail = new LinkedBlockingQueue<MailToSent>();
  private Map<String, Long> currentIdForMail = new ConcurrentHashMap<String, Long>();
  private Thread thread = new ReSendMailThread(pendingsMail);

  public Mailer() {
    thread.start();
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public boolean sendRequestLicence(String lang, String activationCode, final String mail) {
    final String mail1 = mail;
    final String subject1 = Lang.get("resend" + ".license.subject", lang);
    final String content1 = Lang.get("resend" + ".license.message", lang, activationCode, mail);
    MailToSent mailToSent = new SendEmail(subject1, content1, fromAdress, mail1);
    if (mailToSent.sent()) {
      return true;
    }
    add(mailToSent);
    return false;
  }

  public boolean reSendExistingLicenseOnError(String lang, String activationCode, final String mail) {
    MailToSent mailToSent = new SendEmail(Lang.get("resend.error" + ".license.subject", lang),
                                          Lang.get("resend.error" + ".license.message", lang, activationCode, mail),
                                          fromAdress, mail);
    if (mailToSent.sent()) {
      return true;
    }
    add(mailToSent);
    return false;
  }

  public boolean sendNewLicense(String mail, String code, String lang) {
    SendEmail newLicenseMailToSent =
      new SendEmail(Lang.get("new.license.subject", lang), Lang.get("new.license.message", lang, code, mail), fromAdress, mail);
    if (newLicenseMailToSent.sent()) {
      return true;
    }
    add(newLicenseMailToSent);
    return false;
  }

  public boolean sendToSupport(Mailbox mailbox, String fromMail, String title, String content){
    SendEmail supportEmailToSend =
      new SendEmail(title, "declared mail:'" + fromMail + "'\ncontent:\n" + content,
                             "feedback@mybudgetview.fr", mailbox.getEmail());
    if (supportEmailToSend.sent()){
      return true;
    }
    add(supportEmailToSend);
    return false;
  }

  public boolean sendNewMobileAccount(String mail, String lang, String url){
    SendEmail sent  = new SendEmail(Lang.get("mobile.new.subject", lang), Lang.get("mobile.new.message", lang, url, mail),
                                    fromAdress, mail);
    if (sent.sent()){
      return true;
    }
    add(sent);
    return false;
  }

  public boolean sendFromMobileToUseBV(String mailTo, String lang) {
    SendEmail sent  = new SendEmail(Lang.get("mobile.mail.subject", lang), Lang.get("mobile.mail.message", lang),
                                    fromAdress, mailTo);
    if (sent.sent()){
      return true;
    }
    add(sent);
    return false;
  }

  public boolean sendAndroidVersion(String mail, String lang) {
    SendEmail sent  = new SendEmail(Lang.get("mobile.mail.subject", lang), Lang.get("mobile.mail.message", lang),
                                    fromAdress, mail);
    if (sent.sent()){
      return true;
    }
    add(sent);
    return false;
  }



  private void sendMail(String to, String from, String subject, String content) throws MessagingException {

    Properties mailProperties = new Properties();
    mailProperties.setProperty("mail.smtp.host", host);
    mailProperties.setProperty("mail.smtp.port", Integer.toString(port));

    Session session = Session.getDefaultInstance(mailProperties);

    MimeMessage message = new MimeMessage(session);
    message.setFrom(new InternetAddress(from));
    message.setSubject(subject);
    message.setSentDate(new Date());
    message.setText(content, "UTF-8", "html");
    message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
    synchronized (session){
      Transport.send(message);
    }
    logger.info("mail sent : " + to + "  " + subject);
  }

  private void add(MailToSent sent) {
    long current = count.incrementAndGet();
    sent.set(current);
    logger.info("Mail to send : " + sent);
    try {
      currentIdForMail.put(sent.getMail(), sent.current);
      this.pendingsMail.put(sent);
    }
    catch (InterruptedException e) {
      logger.info("Mailer add", e);
    }
  }

  public void stop() {
    thread.interrupt();
    try {
      thread.join();
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  static abstract class MailToSent {
    protected long retryCount;
    protected long current;
    protected String mail;

    public MailToSent(String mail) {
      this.mail = mail;
    }

    public String getMail() {
      return mail;
    }

    abstract boolean sent();

    void inc() {
      retryCount++;
    }

    void set(long current) {
      this.current = current;
    }
  }

  private class ReSendMailThread extends Thread {
    private BlockingQueue<MailToSent> mail;

    public ReSendMailThread(BlockingQueue<MailToSent> mail) {
      this.mail = mail;
      setDaemon(true);
    }

    public void run() {
      try {
        List<MailToSent> mailsToResend = new ArrayList<MailToSent>();
        while (!isInterrupted()) {
          mailsToResend.add(mail.take());  // will block
          mail.drainTo(mailsToResend);     // copy all mail to send
          for (MailToSent mailToSent : mailsToResend) {
            Long lastId = currentIdForMail.get(mailToSent.getMail()); // on recupere le dernier Id du mail qu'on veux envoyer
            if (lastId != null && mailToSent.current >= lastId) {    // si le mail qu'on veux envoyer est plus vieux qu'un autre on ne l'envoie pas.
              if (!mailToSent.sent()) {
                if (mailToSent.retryCount > 10) {
                  logger.error("Message " + mailToSent + " will never been sent.");
                }
                else {
                  mail.put(mailToSent);
                }
              }
              else {
                // on verifie que c'est bien l'ID du mail en cours et non d'un autre qui viendrait d'etre ajouté.
                Long currentRemove = currentIdForMail.remove(mailToSent.getMail());
                if (currentRemove != null && currentRemove > mailToSent.current) {
                  currentIdForMail.put(mailToSent.getMail(), currentRemove);
                }
              }
            }
          }
          mailsToResend.clear();
          Thread.sleep(WAIT_BEFORE_RETRY);
        }
      }
      catch (InterruptedException e) {
        logger.info("ReSendMailThread run", e);
      }
    }
  }

  private class SendEmail extends MailToSent {
    private String fromMail;
    private String title;
    private String content;

    public SendEmail(String title, final String realContent, final String fromMail, final String toMail) {
      super(toMail);
      this.title = title;
      this.content = realContent;
      this.fromMail = fromMail;
    }

    public boolean sent() {
      try {
        inc();
        sendMail(mail, fromMail, title, content);
        return true;
      }
      catch (AddressException badAdress){
        logger.error(toString(), badAdress);
        return true;
      }
      catch (Exception e) {
        logger.warn(toString(), e);
        return false;
      }
    }

    public String toString() {
      return "license server message: " + title + ", content: " + content + " ; " + retryCount + " for " + count;
    }
  }

  public enum Mailbox {
    SUPPORT("support@mybudgetview.fr"),
    ADMIN("admin@mybudgetview.fr");
    private String email;

    Mailbox(String email) {
      this.email = email;
    }

    public String getEmail() {
      return email;
    }
  }
}
