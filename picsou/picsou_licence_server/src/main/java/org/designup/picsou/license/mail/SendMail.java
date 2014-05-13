package org.designup.picsou.license.mail;

import javax.mail.MessagingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SendMail {

  public static void main(String[] args) throws IOException, MessagingException {

    List<String> arguments = new ArrayList<String>(Arrays.asList(args));
    String subject = getSubject(arguments);
    if (arguments.isEmpty()) {
      throw new RuntimeException("Missing address");
    }
    String destination = arguments.get(0);
    if (!(destination.contains("regis") || destination.contains("marc"))) {
      throw new RuntimeException("invalid adress " + destination);
    }
    BufferedReader stream = new BufferedReader(new InputStreamReader(System.in));
    String next;
    StringBuilder content = new StringBuilder();
    while ((next = stream.readLine()) != null) {
      content.append(next);
      content.append("\n");
    }
    sendMail(subject, destination, content.toString());
  }

  static public void sendMail(String subject, String destination, String content) throws MessagingException {
    String mailHost = "ns0.ovh.net";
    int mailPort = 587;
    Mailer mailer = new Mailer();
    mailer.setPort(mailHost, mailPort);


//    String[] addrs = Strings.split(s, ',');
//    for (String addr : addrs) {
    mailer.sendMail(Mailbox.ADMIN, destination, "nobody", subject, content, "UTF-8", "text");
//    }
  }

  static private String getSubject(List<String> arguments) {
    String subject = "none";
    for (Iterator<String> iterator = arguments.iterator(); iterator.hasNext(); ) {
      String argument = iterator.next();
      if (argument.equalsIgnoreCase("-s")) {
        iterator.remove();
        if (iterator.hasNext()) {
          subject = iterator.next();
          iterator.remove();
        }
      }
    }
    return subject;
  }
}
