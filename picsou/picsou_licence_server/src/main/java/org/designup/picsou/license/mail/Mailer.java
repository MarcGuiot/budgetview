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
    MailToSent mailToSent = new ExistingLicenseMailToSent("resend", mail, lang, activationCode);
    if (mailToSent.sent()) {
      return true;
    }
    add(mailToSent);
    return false;
  }

  public boolean reSendExistingLicenseOnError(String lang, String activationCode, final String mail) {
    MailToSent mailToSent = new ExistingLicenseMailToSent("resend.error", mail, lang, activationCode);
    if (mailToSent.sent()) {
      return true;
    }
    add(mailToSent);
    return false;
  }

  public boolean sendNewLicense(String mail, String code, String lang) {
    NewLicenseMailToSent newLicenseMailToSent = new NewLicenseMailToSent(mail, lang, code);
    if (newLicenseMailToSent.sent()) {
      return true;
    }
    add(newLicenseMailToSent);
    return false;
  }

  public boolean sendToSupport(String fromMail, String title, String content){
    SupportMailToSent supportMailToSent = new SupportMailToSent(fromMail, title, content);
    if (supportMailToSent.sent()){
      return true;
    }
    add(supportMailToSent);
    return false;
  }

  private void sendMail(String to, String from, String subjet, String content) throws MessagingException {

    Properties mailProperties = new Properties();
    mailProperties.setProperty("mail.smtp.host", host);
    mailProperties.setProperty("mail.smtp.port", Integer.toString(port));

    Session session = Session.getDefaultInstance(mailProperties);

    MimeMessage message = new MimeMessage(session);
    Multipart multipart = new MimeMultipart();
    BodyPart text = new MimeBodyPart();
    text.setText(content);
    message.setFrom(new InternetAddress(from));
    message.setSubject(subjet);
    message.setSentDate(new Date());
    message.setContent(content, "text/html");
    message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
    Transport.send(message);
    logger.info("mail sent : " + to + "  " + subjet);
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

  private class NewLicenseMailToSent extends MailToSent {
    private final String lang;
    private final String code;

    public NewLicenseMailToSent(String mail, String lang, String code) {
      super(mail);
      this.lang = lang;
      this.code = code;
    }

    public boolean sent() {
      try {
        inc();
        sendMail(mail, fromAdress, Lang.get("new.license.subject", lang),
                 Lang.get("new.license.message", lang, code, mail),
                 Lang.get("new.license.message.text", lang, code, mail));
        return true;
      }
      catch (Exception e) {
        logger.warn(toString(), e);
        return false;
      }
    }

    public String toString() {
      return "new license for '" + mail + "' code : '" + code + "' retry count : " + retryCount + " for " + count;
    }
  }

  private class ExistingLicenseMailToSent extends MailToSent {
    private final String prefix;
    private final String lang;
    private final String activationCode;

    public ExistingLicenseMailToSent(final String prefix, final String mail, String lang, String activationCode) {
      super(mail);
      this.prefix = prefix;
      this.lang = lang;
      this.activationCode = activationCode;
    }

    boolean sent() {
      try {
        inc();
        sendMail(mail, fromAdress, Lang.get(prefix + ".license.subject", lang),
                 Lang.get(prefix + ".license.message", lang, activationCode, mail),
                 Lang.get(prefix + ".license.message.text", lang, activationCode, mail));
        return true;
      }
      catch (Exception e) {
        logger.warn("Mail not sent : " + toString(), e);
        return false;
      }
    }

    public String toString() {
      return "Existing license for '" + mail + "' code : '" + activationCode + "' retry count : " + retryCount + " for " + count;
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

  private class SupportMailToSent extends MailToSent {
    private String fromMail;
    private String title;
    private String content;

    public SupportMailToSent(String fromMail, String title, String content) {
      super("support@mybudgetview.fr");
      this.title = title;
      this.content = "declared mail :'" + fromMail + "'\ncontent:\n" + content;
      this.fromMail = "feedback@mybudgetview.fr";
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
      return "license server message : " + title + ", content : " + content + " ; " + retryCount + " for " + count;
    }
  }

}
