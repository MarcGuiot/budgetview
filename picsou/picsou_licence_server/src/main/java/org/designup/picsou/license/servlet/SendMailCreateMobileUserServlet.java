package org.designup.picsou.license.servlet;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.license.mail.Mailer;
import org.globsframework.utils.directory.Directory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Arrays;

public class SendMailCreateMobileUserServlet extends AbstractHttpServlet {
  static Logger logger = Logger.getLogger("SendMailCreateMobileUserServlet");
  private String root;
  private Mailer mailer;

  public SendMailCreateMobileUserServlet(String root, Directory directory) {
    this.root = root;
    this.mailer = directory.get(Mailer.class);
  }

  protected void action(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
    String lang = httpServletRequest.getHeader(ConfigService.HEADER_LANG);
    String mail = httpServletRequest.getHeader(ConfigService.HEADER_MAIL);
    String codedMail = httpServletRequest.getHeader(ConfigService.CODING);

    byte[] decryptedMail = CreateMobileUserServlet.encryptor.decrypt(Base64.decodeBase64(codedMail.getBytes()));
    if (!Arrays.equals(decryptedMail, mail.getBytes("UTF-8"))) {
      logger.error("Invalid decrypted mail for " + mail + " got " + new String(decryptedMail));
      httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    String dirName = ReceiveDataServlet.generateFileName(mail);
    File dir = new File(root, dirName);
    if (!dir.exists()) {
      URIBuilder builder = new URIBuilder("https://www.mybudgetview.fr:443/createMobileUser");
      builder.addParameter(ConfigService.HEADER_MAIL, mail);
      builder.addParameter(ConfigService.HEADER_LANG, lang);
      builder.addParameter(ConfigService.CODING, codedMail);

      String asciUrl = builder.build().toASCIIString();
      mailer.sendNewMobileAccount(mail, lang, asciUrl);
      httpServletResponse.setHeader(ConfigService.HEADER_IS_VALIDE, "true");
    }
    else {
      String content = "Directory already exist " + dir.getAbsolutePath();
      logger.error(content + " : " + mail);
      httpServletResponse.setHeader(ConfigService.HEADER_IS_VALIDE, "false");
    }
  }
}
