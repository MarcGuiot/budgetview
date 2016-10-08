package com.budgetview.server.license.mail;

import com.budgetview.server.config.ConfigService;

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
      throw new RuntimeException("Invalid address: " + destination);
    }
    BufferedReader stream = new BufferedReader(new InputStreamReader(System.in));
    String next;
    StringBuilder content = new StringBuilder();
    while ((next = stream.readLine()) != null) {
      content.append(next);
      content.append("\n");
    }
    for (String addr : arguments) {
      sendMail(subject, addr, content.toString());
    }
  }

  public static void sendMail(String subject, String destination, String content) throws MessagingException, IOException {

    ConfigService config = ConfigService.build()
      .set("bv.email.host", "ns0.ovh.net")
      .set("bv.email.port", 587)
      .get();

    Mailer mailer = new Mailer(config);
    mailer.sendMail(Mailbox.ADMIN, destination, "nobody", subject, content, "UTF-8", "text");
  }

  private static String getSubject(List<String> arguments) {
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
