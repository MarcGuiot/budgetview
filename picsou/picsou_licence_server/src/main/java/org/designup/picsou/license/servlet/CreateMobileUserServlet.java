package org.designup.picsou.license.servlet;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.designup.picsou.client.http.MD5PasswordBasedEncryptor;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.license.mail.Mailer;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;

public class CreateMobileUserServlet extends HttpServlet {
  static Logger logger = Logger.getLogger("CreateMobileUserServlet");
  private String root;
  private Mailer mailer;
  public static MD5PasswordBasedEncryptor encryptor =
    new MD5PasswordBasedEncryptor(ConfigService.MOBILE_SALT.getBytes(), ConfigService.SOME_PASSWORD.toCharArray(), 5);

  public CreateMobileUserServlet(String root, Directory directory) {
    this.root = root;
    this.mailer = directory.get(Mailer.class);
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      req.setCharacterEncoding("UTF-8");
      resp.setCharacterEncoding("UTF-8");
      action(req, resp);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected void action(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
    String lang = httpServletRequest.getParameter(ConfigService.HEADER_LANG);
    String mail = URLDecoder.decode(httpServletRequest.getParameter(ConfigService.HEADER_MAIL), "UTF-8");
    String coding = URLDecoder.decode(httpServletRequest.getParameter(ConfigService.CODING), "UTF-8");
    byte[] decryptedMail = encryptor.decrypt(Base64.decodeBase64(coding));

    String baseUrl = "fr".equals(lang) ? "http://www.mybudgetview.fr" : "http://www.mybudgetview.com";

    if (!Arrays.equals(decryptedMail, mail.getBytes("UTF-8"))) {
      httpServletResponse.sendRedirect(baseUrl + "/mobile/invalidCreateUserRequest");
      logger.info("Bap password " + mail);
    }
    else {
      String dirName = ReceiveDataServlet.generateDirName(mail);
      File dir = new File(root, dirName);
      if (!dir.exists()) {
        if (dir.mkdir()) {
          logger.info("created : " + dir.getAbsolutePath());
          httpServletResponse.sendRedirect(baseUrl + "/mobile/account-ok");
        }
        else {
          String content = "Can not create dir " + dir.getAbsolutePath();
          logger.error(content + " : " + mail);
          mailer.sendToSupport(Mailer.Mailbox.ADMIN, mail, "Error fs", content);
          httpServletResponse.sendRedirect(baseUrl + "/mobile/internal-error");
        }
      }
      else {
        httpServletResponse.sendRedirect(baseUrl + "/mobile/account-already-present");
        logger.warn("Duplicate create : " + mail);
      }
    }
  }
}
