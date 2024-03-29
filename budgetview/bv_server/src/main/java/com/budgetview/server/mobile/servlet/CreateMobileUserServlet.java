package com.budgetview.server.mobile.servlet;

import com.budgetview.server.license.mail.Mailbox;
import com.budgetview.server.license.mail.Mailer;
import com.budgetview.shared.encryption.MD5PasswordBasedEncryptor;
import com.budgetview.shared.license.LicenseConstants;
import com.budgetview.shared.mobile.MobileConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.globsframework.utils.Files;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.util.Arrays;

public class CreateMobileUserServlet extends HttpServlet {
  public static final String PENDING = "pending";
  private static Logger logger = Logger.getLogger("CreateMobileUserServlet");
  private String root;
  private Mailer mailer;
  public static MD5PasswordBasedEncryptor encryptor =
    new MD5PasswordBasedEncryptor(MobileConstants.SALT.getBytes(), LicenseConstants.SOME_PASSWORD.toCharArray(), 5);

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
      logger.error(e);
    }
  }

  protected void action(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
    String lang = httpServletRequest.getParameter(MobileConstants.HEADER_LANG);
    String mail = URLDecoder.decode(httpServletRequest.getParameter(LicenseConstants.HEADER_MAIL_FROM), "UTF-8");
    String coding = URLDecoder.decode(httpServletRequest.getParameter(LicenseConstants.CODING), "UTF-8");
    byte[] decryptedMail = encryptor.decrypt(Base64.decodeBase64(coding));

    String sha1Mail = URLDecoder.decode(httpServletRequest.getParameter(MobileConstants.CRYPTED_INFO), "UTF-8");

    String baseUrl = "fr".equals(lang) ? MobileConstants.WWW_MYBUDGETVIEW_FR : MobileConstants.WWW_MYBUDGETVIEW_COM;

    if (!Arrays.equals(decryptedMail, mail.getBytes("UTF-8"))) {
      httpServletResponse.sendRedirect(baseUrl + "/mobile/invalidCreateUserRequest");
      logger.info("Invalid password " + mail);
    }
    else {
      String dirName = PostDataServlet.generateDirName(mail);
      File dir = new File(root, dirName);
      if (!dir.exists()) {
        if (dir.mkdir()) {
          logger.info("created : " + dir.getAbsolutePath() + " " + sha1Mail);
          writeCode(sha1Mail, dir);
          httpServletResponse.sendRedirect(baseUrl + "/mobile/account-ok");
        }
        else {
          String content = "Cannot create dir " + dir.getAbsolutePath();
          logger.error(content + " : " + mail);
          mailer.sendToUs(Mailbox.ADMIN, mail, "Error fs", content);
          httpServletResponse.sendRedirect(baseUrl + "/mobile/internal-error");
        }
      }
      else {
        boolean hasPending = false;
        File pendingCode = new File(dir, PENDING + PostDataServlet.CODE_FILE_NAME);
        File pendingData = new File(dir, PENDING + PostDataServlet.DATA_FILE_NAME);
        if (pendingCode.exists() && pendingData.exists()) {
          Files.copyFile(pendingCode, new File(dir, PostDataServlet.CODE_FILE_NAME));
          Files.copyFile(pendingData, new File(dir, PostDataServlet.DATA_FILE_NAME));
          hasPending = true;
        }
        pendingCode.delete();
        pendingData.delete();
        if (!PostDataServlet.checkSha1Code(sha1Mail, dir)) {
          Files.deleteSubtreeOnly(dir);
          writeCode(sha1Mail, dir);
          logger.warn("Duplicate create : " + mail + " " + sha1Mail + " delete previous data.");
          httpServletResponse.sendRedirect(baseUrl + "/mobile/account-already-present");
        }
        else {
          if (hasPending) {
            logger.info("created with pending : " + dir.getAbsolutePath() + " " + sha1Mail);
            httpServletResponse.sendRedirect(baseUrl + "/mobile/account-ok");
          }else {
            logger.warn("Duplicate create : " + mail + " " + sha1Mail + " with same code.");
            httpServletResponse.sendRedirect(baseUrl + "/mobile/account-already-present");
          }
        }
        mailer.sendAndroidVersion(mail, lang);
      }
    }
    httpServletResponse.setStatus(HttpServletResponse.SC_FOUND);
  }

  public static void writeCode(String sha1Mail, File dir) throws IOException {
    Writer writer = new BufferedWriter(new FileWriter(new File(dir, PostDataServlet.CODE_FILE_NAME)));
    writer.append(sha1Mail);
    writer.close();
  }

  public static void writePendingCode(String sha1Mail, File dir) throws IOException {
    FileWriter writer = new FileWriter(new File(dir, PENDING + PostDataServlet.CODE_FILE_NAME));
    writer.append(sha1Mail);
    writer.close();
  }
}
