package com.budgetview.license.servlet;

import com.budgetview.http.HttpBudgetViewConstants;
import com.budgetview.license.mail.Mailbox;
import com.budgetview.license.mail.Mailer;
import com.budgetview.shared.utils.MobileConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.globsframework.utils.Files;
import org.globsframework.utils.directory.Directory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLDecoder;
import java.util.Arrays;

public class DeleteMobileUserServlet extends AbstractHttpServlet {
  static Logger logger = Logger.getLogger("DeleteMobileUserServlet");
  private String root;
  private final Mailer mailer;

  public DeleteMobileUserServlet(String root, Directory directory) {
    this.root = root;
    mailer = directory.get(Mailer.class);
  }

  protected void action(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
    String lang = httpServletRequest.getHeader(MobileConstants.HEADER_LANG);
    String mail = URLDecoder.decode(httpServletRequest.getHeader(HttpBudgetViewConstants.HEADER_MAIL), "UTF-8");
    String codedMail = httpServletRequest.getHeader(HttpBudgetViewConstants.CODING);
    String sha1Mail = httpServletRequest.getHeader(MobileConstants.CRYPTED_INFO);
    logger.info("receive delete for " + mail + " " + sha1Mail);

    byte[] decryptedMail = CreateMobileUserServlet.encryptor.decrypt(Base64.decodeBase64(URLDecoder.decode(codedMail, "UTF-8").getBytes()));
    if (!Arrays.equals(decryptedMail, mail.getBytes("UTF-8"))) {
      logger.error("Invalid decrypted mail for " + mail + " got " + new String(decryptedMail));
      httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    String dirName = PostDataServlet.generateDirName(mail);
    File dir = new File(root, dirName);
    if (dir.exists()) {
      if (!PostDataServlet.checkSha1Code(sha1Mail, dir)){
        logger.info("bad sha1 code");
        httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
      }
      else {
        if (!Files.deleteWithSubtree(dir)){
          logger.info("Directory found " + dir.getAbsolutePath() + " : " + mail + " but unable to delete it");
          mailer.sendToUs(Mailbox.ADMIN, mail, "Cannot delete directory",
                          "delete the directory '" + dir.getAbsolutePath() + "'");
        } else {
          logger.info("Directory found " + dir.getAbsolutePath() + " : " + mail + " and deleted.");
        }
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
      }
    }
    else {
      logger.info("directory " + dir.getAbsolutePath() + " not found.");
      httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
  }
}
