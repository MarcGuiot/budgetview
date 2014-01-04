package org.designup.picsou.license.mail;

import org.apache.log4j.Logger;
import org.designup.picsou.license.Lang;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class Mailer {
  static Logger logger = Logger.getLogger("mailer");
  static AtomicLong count = new AtomicLong(0);
  private static final int WAIT_BEFORE_RETRY = 1000;
  private int port = 587;
  private String host = "ns0.ovh.net";
  private BlockingQueue<MailToSent> pendingsMail = new LinkedBlockingQueue<MailToSent>();
  private Map<String, Long> currentIdForMail = new ConcurrentHashMap<String, Long>();
  private Thread thread = new ReSendMailThread(pendingsMail);

  public Mailer() {
    thread.start();
  }

  public void setPort(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public boolean sendRequestLicence(String lang, String activationCode, final String mail) {
    final String subject1 = Lang.get("resend" + ".license.subject", lang);
    final String content1 = Lang.get("resend" + ".license.message", lang, activationCode, mail);
    MailToSent mailToSent = new SendEmail(Mailbox.SUPPORT, subject1, content1, Mailbox.SUPPORT.getEmail(), mail);
    if (mailToSent.sent()) {
      return true;
    }
    add(mailToSent);
    return false;
  }

  public boolean reSendExistingLicenseOnError(String lang, String activationCode, final String mail) {
    MailToSent mailToSent = new SendEmail(Mailbox.SUPPORT, Lang.get("resend.error" + ".license.subject", lang),
                                          Lang.get("resend.error" + ".license.message", lang, activationCode, mail),
                                          Mailbox.SUPPORT.getEmail(), mail);
    if (mailToSent.sent()) {
      return true;
    }
    add(mailToSent);
    return false;
  }

  public boolean sendNewLicense(String mail, String code, String lang) {
    SendEmail newLicenseMailToSent =
      new SendEmail(Mailbox.SUPPORT, Lang.get("new.license.subject", lang), Lang.get("new.license.message", lang, code, mail),
                    Mailbox.SUPPORT.getEmail(), mail);
    if (newLicenseMailToSent.sent()) {
      return true;
    }
    add(newLicenseMailToSent);
    return false;
  }

  public boolean sendToUs(Mailbox mailbox, String fromMail, String title, String content) {
    SendEmail supportEmailToSend =
      new SendEmail(mailbox, title, "From " + fromMail + ": " + content,
                    fromMail, mailbox.getEmail());
    if (supportEmailToSend.sent()) {
      return true;
    }
    add(supportEmailToSend);
    return false;
  }

  public boolean sendNewMobileAccount(String mail, String lang, String url) {
    SendEmail sent = new SendEmail(Mailbox.SUPPORT, Lang.get("mobile.new.subject", lang), Lang.get("mobile.new.message", lang, url, mail),
                                   Mailbox.SUPPORT.getEmail(), mail);
    if (sent.sent()) {
      return true;
    }
    add(sent);
    return false;
  }

  public boolean sendFromMobileToUseBV(String mailTo, String lang) {
    SendEmail sent = new SendEmail(Mailbox.SUPPORT, Lang.get("mobile.mail.subject", lang), Lang.get("mobile.mail.message", lang),
                                   Mailbox.SUPPORT.getEmail(), mailTo);
    if (sent.sent()) {
      return true;
    }
    add(sent);
    return false;
  }

  public boolean sendAndroidVersion(String mail, String lang) {
    SendEmail sent = new SendEmail(Mailbox.SUPPORT, Lang.get("mobile.mail.download.subject", lang), Lang.get("mobile.mail.download.message", lang),
                                   Mailbox.SUPPORT.getEmail(), mail);
    if (sent.sent()) {
      return true;
    }
    add(sent);
    return false;
  }


  private void sendMail(Mailbox mailbox, String sendTo, String replyTo, String subject, String content) throws MessagingException {

    Properties mailProperties = new Properties();
    mailProperties.setProperty("mail.smtp.host", host);
    mailProperties.setProperty("mail.smtp.port", Integer.toString(port));
    mailProperties.setProperty("mail.smtp.user", mailbox.getEmail());
    mailProperties.setProperty("mail.smtp.auth", "true");

    Session session = Session.getDefaultInstance(mailProperties);

    Transport tr = session.getTransport("smtp");

    tr.connect(host, mailbox.getEmail(), "IrvQh4_Se");

    MimeMessage message = new MimeMessage(session);
    message.setFrom(new InternetAddress(mailbox.getEmail()));
    message.setSubject(subject);
    message.setSentDate(new Date());
    message.setText(content, "UTF-8", "html");
    try {
      InternetAddress address = new InternetAddress(replyTo);
      message.setReplyTo(new Address[]{address});
    }
    catch (AddressException e) {
    }
    message.setRecipient(Message.RecipientType.TO, new InternetAddress(sendTo));
    message.saveChanges();
    synchronized (session) {
      tr.sendMessage(message, message.getAllRecipients());
    }
    tr.close();
    logger.info("mail sent : " + sendTo + "  " + subject);
  }

  private void add(MailToSent sent) {
    long current = count.incrementAndGet();
    sent.set(current);
    logger.info("Mail to send : " + sent);
    try {
      currentIdForMail.put(sent.getSendTo(), sent.current);
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
    protected String sendTo;

    public MailToSent(String sendTo) {
      this.sendTo = sendTo;
    }

    public String getSendTo() {
      return sendTo;
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
            Long lastId = currentIdForMail.get(mailToSent.getSendTo()); // on recupere le dernier Id du mail qu'on veux envoyer
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
                Long currentRemove = currentIdForMail.remove(mailToSent.getSendTo());
                if (currentRemove != null && currentRemove > mailToSent.current) {
                  currentIdForMail.put(mailToSent.getSendTo(), currentRemove);
                }
              }
            }
          }
          mailsToResend.clear();
          Thread.sleep(WAIT_BEFORE_RETRY);
        }
      }
      catch (InterruptedException e) {
        logger.info("ReSendMailThread end ", e);
      }
    }
  }

  private class SendEmail extends MailToSent {
    private String replyTo;
    private Mailbox mailbox;
    private String title;
    private String content;

    public SendEmail(Mailbox mailbox, String title, final String realContent, final String replyTo, final String sendTo) {
      super(sendTo);
      this.mailbox = mailbox;
      this.title = title;
      this.content = realContent;
      this.replyTo = replyTo;
    }

    public boolean sent() {
      try {
        inc();
        sendMail(mailbox, sendTo, replyTo, title, content);
        return true;
      }
      catch (AddressException badAdress) {
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

}
