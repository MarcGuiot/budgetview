package com.budgetview.server.license.checkers;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import org.apache.log4j.Logger;
import org.junit.Assert;

import java.util.Iterator;

public class MailServerChecker {

  private Logger logger = Logger.getLogger("MailServerChecker");

  public static final int PORT = 2500;

  protected SimpleSmtpServer mailServer;
  protected Thread mailThread;
  private boolean started;

  public MailServerChecker() {
  }

  public void start() {
    mailServer = new SimpleSmtpServer(PORT);
    mailThread = new Thread() {
      public void run() {
        logger.info("Starting mail server on port 2500");
        mailServer.run();
      }
    };
    mailThread.setDaemon(true);
    mailThread.start();
    started = true;
  }

  public void checkEmpty() throws InterruptedException {
    if (mailServer == null) {
      Assert.fail("Mail server not started");
    }

    synchronized (mailServer) {
      Iterator receivedEmail = mailServer.getReceivedEmail();
      if (receivedEmail.hasNext()) {
        SmtpMessage message = (SmtpMessage)receivedEmail.next();
        Assert.fail("Unexpected message sent to:" + message.getHeaderValue("To") + ": " + message.getBody());
      }
    }
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
          Assert.fail("No mail received");
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
        Assert.fail("No mail received");
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
    logger.info("Stopped mail server");
  }

  public void dispose() throws InterruptedException {
    stop();
  }
}
