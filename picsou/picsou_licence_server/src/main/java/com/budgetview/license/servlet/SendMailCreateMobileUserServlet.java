package com.budgetview.license.servlet;

import com.budgetview.gui.config.ConfigService;
import com.budgetview.shared.utils.ComCst;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import com.budgetview.license.mail.Mailer;
import org.globsframework.utils.directory.Directory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;

public class SendMailCreateMobileUserServlet extends AbstractHttpServlet {
  static Logger logger = Logger.getLogger("SendMailCreateMobileUserServlet");
  private String root;
  private Integer sslPort;
  private Integer port;
  private Mailer mailer;

  public SendMailCreateMobileUserServlet(String root, Directory directory, Integer sslPort, Integer port) {
    this.root = root;
    this.sslPort = sslPort;
    this.port = port;
    this.mailer = directory.get(Mailer.class);
  }

  protected void action(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
    String lang = httpServletRequest.getHeader(ComCst.HEADER_LANG);
    String mail = URLDecoder.decode(httpServletRequest.getHeader(ConfigService.HEADER_MAIL), "UTF-8");
    String codedMail = httpServletRequest.getHeader(ConfigService.CODING);
    String sha1Mail = httpServletRequest.getHeader(ComCst.CRYPTED_INFO);

    byte[] decryptedMail = CreateMobileUserServlet.encryptor.decrypt(Base64.decodeBase64(URLDecoder.decode(codedMail, "UTF-8").getBytes()));
    if (!Arrays.equals(decryptedMail, mail.getBytes("UTF-8"))) {
      logger.error("Invalid decrypted mail for " + mail + " got " + new String(decryptedMail));
      httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    String dirName = ReceiveDataServlet.generateDirName(mail);
    File dir = new File(root, dirName);
    if (dir.exists()) {
      String content = "Directory already exist " + dir.getAbsolutePath();
      logger.info(content + " : " + mail);
    }
    else {
      String content = "Directory " + dir.getAbsolutePath();
      boolean mkdir = dir.mkdir();
      logger.info(content + (mkdir? " created " : " not created"));
    }
    URIBuilder builder = new URIBuilder("http://www.mybudgetview.fr:" + port+ LicenseServer.CREATE_MOBILE_USER);
    builder.addParameter(ConfigService.HEADER_MAIL, URLEncoder.encode(mail, "UTF-8"));
    builder.addParameter(ComCst.HEADER_LANG, lang);
    builder.addParameter(ConfigService.CODING, codedMail);
    builder.addParameter(ComCst.CRYPTED_INFO, sha1Mail);

    String asciiUrl = builder.build().toASCIIString();
    mailer.sendNewMobileAccount(mail, lang, asciiUrl);
    httpServletResponse.setHeader(ConfigService.HEADER_IS_VALIDE, "true");
    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
  }
}
