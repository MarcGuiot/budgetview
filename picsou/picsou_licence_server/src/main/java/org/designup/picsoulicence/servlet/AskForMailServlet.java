package org.designup.picsoulicence.servlet;

import org.designup.picsoulicence.mail.Mailler;
import org.globsframework.utils.directory.Directory;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AskForMailServlet extends HttpServlet {
  private Mailler mailler;

  public AskForMailServlet(Directory directory) {
    mailler = directory.get(Mailler.class);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String mailTo = req.getHeader("mailTo");
    try {
      mailler.sendRequestLicence(mailTo);
    }
    catch (MessagingException e) {
      resp.setStatus(HttpServletResponse.SC_OK);
    }
  }
}
