package com.budgetview.server.mobile.servlet;

import com.budgetview.server.license.mail.Mailer;
import com.budgetview.server.license.servlet.AbstractHttpServlet;
import com.budgetview.shared.license.LicenseConstants;
import com.budgetview.shared.mobile.MobileConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
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
  private Integer port;
  private Mailer mailer;

  public SendMailCreateMobileUserServlet(String root, Directory directory, Integer port) {
    this.root = root;
    this.port = port;
    this.mailer = directory.get(Mailer.class);
  }

  protected void action(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
    String lang = httpServletRequest.getHeader(MobileConstants.HEADER_LANG);
    String mail = URLDecoder.decode(httpServletRequest.getHeader(LicenseConstants.HEADER_MAIL), "UTF-8");
    String codedMail = httpServletRequest.getHeader(LicenseConstants.CODING);
    String sha1Mail = httpServletRequest.getHeader(MobileConstants.CRYPTED_INFO);

    byte[] decryptedMail = CreateMobileUserServlet.encryptor.decrypt(Base64.decodeBase64(URLDecoder.decode(codedMail, "UTF-8").getBytes()));
    if (!Arrays.equals(decryptedMail, mail.getBytes("UTF-8"))) {
      logger.error("Invalid decrypted mail for " + mail + " got " + new String(decryptedMail));
      httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    String dirName = PostDataServlet.generateDirName(mail);
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
    URIBuilder builder = new URIBuilder("http://www.mybudgetview.fr:" + port + MobileConstants.CREATE_MOBILE_USER);
    builder.addParameter(LicenseConstants.HEADER_MAIL, URLEncoder.encode(mail, "UTF-8"));
    builder.addParameter(MobileConstants.HEADER_LANG, lang);
    builder.addParameter(LicenseConstants.CODING, codedMail);
    builder.addParameter(MobileConstants.CRYPTED_INFO, sha1Mail);

    String asciiUrl = builder.build().toASCIIString();
    mailer.sendNewMobileAccount(mail, lang, asciiUrl);
    httpServletResponse.setHeader(LicenseConstants.HEADER_IS_VALID, "true");
    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
  }
}
