package org.designup.picsou.license.checkers;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import junit.framework.Assert;

import java.util.Iterator;

public class MailServerChecker {
  protected SimpleSmtpServer mailServer;
  protected Thread mailThread;
  private boolean started;

  public MailServerChecker() {
  }

  public void start() {
    mailServer = new SimpleSmtpServer(2500);
    mailThread = new Thread() {
      public void run() {
        mailServer.run();
      }
    };
    mailThread.setDaemon(true);
    mailThread.start();
    started = true;
  }

  public Email checkReceivedMail(String mailTo) throws InterruptedException {
    if (mailServer == null) {
      Assert.fail("Mail server not started");
    }

    long end = System.currentTimeMillis() + 4000;
    synchronized (mailServer) {
      Iterator receivedEmail = mailServer.getReceivedEmail();
      while (!receivedEmail.hasNext()) {
        mailServer.wait(800);
        if (System.currentTimeMillis() > end) {
          Assert.fail("no mail received");
        }
        receivedEmail = mailServer.getReceivedEmail();
      }
      if (receivedEmail.hasNext()) {
        SmtpMessage message = (SmtpMessage)receivedEmail.next();
        Assert.assertEquals(mailTo, message.getHeaderValue("To"));
        receivedEmail.remove();
        return new Email(message);
      }
      else {
        Assert.fail("no mail received");
      }
    }
    return null;
  }

  public void stop() throws InterruptedException {
    if (mailServer != null) {
      if (started) {
        try {
          mailServer.stop();
        }
        catch (Exception e) {
        }
        started = false;
      }
    }
    mailServer = null;
    if (mailThread != null) {
      mailThread.join();
    }
    mailThread = null;
  }

  public void dispose() throws InterruptedException {
    stop();
  }
}
