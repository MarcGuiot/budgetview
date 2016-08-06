package com.budgetview.server.license.mail;

import org.apache.log4j.Logger;
import com.budgetview.server.utils.Lang;

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

  private static final int WAIT_BEFORE_RETRY = 1000;

  private static Logger logger = Logger.getLogger("mailer");

  private static AtomicLong count = new AtomicLong(0);

  private String host = "ns0.ovh.net";
  private int port = 587;

  private BlockingQueue<MailToSend> pendingMails = new LinkedBlockingQueue<MailToSend>();
  private Map<String, Long> currentIdForMail = new ConcurrentHashMap<String, Long>();
  private Thread thread = new ReSendMailThread(pendingMails);

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
    return doSend(Mailbox.SUPPORT, subject1, content1, Mailbox.SUPPORT.getEmail(), mail);
  }

  public boolean reSendExistingLicenseOnError(String lang, String activationCode, final String mail) {
    return doSend(Mailbox.SUPPORT, Lang.get("resend.error" + ".license.subject", lang),
                  Lang.get("resend.error" + ".license.message", lang, activationCode, mail),
                  Mailbox.SUPPORT.getEmail(), mail);
  }

  public boolean sendNewLicense(String mail, String code, String lang) {
    return doSend(Mailbox.SUPPORT, Lang.get("new.license.subject", lang), Lang.get("new.license.message", lang, code, mail),
                  Mailbox.SUPPORT.getEmail(), mail);
  }

  public boolean sendToUs(Mailbox mailbox, String fromMail, String title, String content) {
    return doSend(mailbox, title, "From " + fromMail + ": " + content,
                  fromMail, mailbox.getEmail());
  }

  public boolean sendNewMobileAccount(String mail, String lang, String url) {
    return doSend(Mailbox.SUPPORT, Lang.get("mobile.new.subject", lang), Lang.get("mobile.new.message", lang, url, mail),
                  Mailbox.SUPPORT.getEmail(), mail);
  }

  public boolean sendFromMobileToUseBV(String mailTo, String lang) {
    return doSend(Mailbox.SUPPORT, Lang.get("mobile.mail.subject", lang), Lang.get("mobile.mail.message", lang),
                  Mailbox.SUPPORT.getEmail(), mailTo);
  }

  public boolean sendAndroidVersion(String mail, String lang) {
    return doSend(Mailbox.SUPPORT, Lang.get("mobile.mail.download.subject", lang), Lang.get("mobile.mail.download.message", lang),
                  Mailbox.SUPPORT.getEmail(), mail);
  }

  public void sendMail(Mailbox mailbox, String sendTo, String replyTo, String subject, String content,
                       final String charset, final String subtype) throws MessagingException {
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
    message.setText(content, charset, subtype);
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

  private void sendLater(MailToSend email) {
    long current = count.incrementAndGet();
    email.setCount(current);
    logger.info("Mail to send : " + email);
    try {
      currentIdForMail.put(email.getSendTo(), email.current);
      this.pendingMails.put(email);
    }
    catch (InterruptedException e) {
      logger.info("Error storing email: " + email, e);
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

  private static abstract class MailToSend {
    long retryCount;
    long current;
    String sendTo;

    MailToSend(String sendTo) {
      this.sendTo = sendTo;
    }

    String getSendTo() {
      return sendTo;
    }

    abstract boolean send();

    void inc() {
      retryCount++;
    }

    void setCount(long current) {
      this.current = current;
    }

    public String toString() {
      return sendTo;
    }
  }

  private class ReSendMailThread extends Thread {
    private BlockingQueue<MailToSend> mail;

    ReSendMailThread(BlockingQueue<MailToSend> mail) {
      this.mail = mail;
      setDaemon(true);
    }

    public void run() {
      try {
        List<MailToSend> mailsToResend = new ArrayList<MailToSend>();
        while (!isInterrupted()) {
          mailsToResend.add(mail.take());  // will block
          mail.drainTo(mailsToResend);     // copy all mail to send
          for (MailToSend mailToSend : mailsToResend) {
            Long lastId = currentIdForMail.get(mailToSend.getSendTo()); // on recupere le dernier Id du mail qu'on veux envoyer
            if (lastId != null && mailToSend.current >= lastId) {    // si le mail qu'on veux envoyer est plus vieux qu'un autre on ne l'envoie pas.
              if (!mailToSend.send()) {
                if (mailToSend.retryCount > 10) {
                  logger.error("Message " + mailToSend + " will never been sent.");
                }
                else {
                  mail.put(mailToSend);
                }
              }
              else {
                // on verifie que c'est bien l'ID du mail en cours et non d'un autre qui viendrait d'etre ajouté.
                Long currentRemove = currentIdForMail.remove(mailToSend.getSendTo());
                if (currentRemove != null && currentRemove > mailToSend.current) {
                  currentIdForMail.put(mailToSend.getSendTo(), currentRemove);
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

  private boolean doSend(Mailbox mailbox, String title, final String realContent, final String replyTo, final String sendTo) {
    MailToSend mail = new SendEmail(mailbox, title, realContent, replyTo, sendTo);
    if (mail.send()) {
      return true;
    }
    sendLater(mail);
    return false;

  }

  private class SendEmail extends MailToSend {
    private String replyTo;
    private Mailbox mailbox;
    private String title;
    private String content;

    SendEmail(Mailbox mailbox, String title, final String realContent, final String replyTo, final String sendTo) {
      super(sendTo);
      this.mailbox = mailbox;
      this.title = title;
      this.content = realContent;
      this.replyTo = replyTo;
    }

    public boolean send() {
      try {
        inc();
        sendMail(mailbox, sendTo, replyTo, title, content, "UTF-8", "html");
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
