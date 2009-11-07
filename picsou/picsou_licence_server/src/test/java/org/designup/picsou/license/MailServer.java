package org.designup.picsou.license;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;

import java.util.Iterator;

public class MailServer {
  SimpleSmtpServer mailServer;
  private Thread mailThread;

  public static void main(String[] args) throws InterruptedException {
     new MailServer();

  }

  public MailServer() throws InterruptedException {
    mailServer = new SimpleSmtpServer(5000);
    mailThread = new Thread() {
      public void run() {
        mailServer.run();
      }
    };
    checkMail();
  }

  void checkMail() throws InterruptedException {
    synchronized (mailServer) {
      Iterator receivedEmail = mailServer.getReceivedEmail();
      while (true) {
        if (receivedEmail.hasNext()) {
          SmtpMessage mail = (SmtpMessage)receivedEmail.next();
          receivedEmail.remove();
          System.out.println("receive Mail " + mail.toString());
        }
        if (!receivedEmail.hasNext()) {
          mailServer.wait(800);
        }
        receivedEmail = mailServer.getReceivedEmail();
      }
    }
  }
}
