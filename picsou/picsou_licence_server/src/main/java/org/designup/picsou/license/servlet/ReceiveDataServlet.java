package org.designup.picsou.license.servlet;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.designup.picsou.gui.config.ConfigService;
import org.globsframework.utils.Files;
import org.globsframework.utils.directory.Directory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class ReceiveDataServlet extends AbstractHttpServlet {
  static Logger logger = Logger.getLogger("ReceiveDataServlet");
  private String root;

  public ReceiveDataServlet(String root, Directory mailer) {
    this.root = root;
  }

  protected void action(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
    InputStream inputStream = httpServletRequest.getInputStream();
    OutputStream outputStream = httpServletResponse.getOutputStream();
    String mail = httpServletRequest.getHeader(ConfigService.HEADER_MAIL);
    String dirName = generateFileName(mail);
    File dir = new File(root, dirName);
    if (!dir.exists()){
      logger.warn("No directory '" + dirName + "' for " + mail);
      outputStream.write("error".getBytes());
    }
    else {
      File file = new File(dir, "data.ser");
      if (!file.delete()){
        logger.error("Can not delete file " + file.getAbsolutePath());
        outputStream.write("error".getBytes());
      }
      else {
        OutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(file));
        Files.copyStream(inputStream, fileOutputStream);
      }
    }
  }

  // generate a file starting with the mail with only ascii character (other a replace with _)
  // to prevent naming clash an digest (md5) is add after
  public static String generateFileName(String mail) {
    String digest = DigestUtils.md5Hex(mail);
    StringBuilder builder = new StringBuilder(mail.length());
    for (int i = 0; i < mail.length(); i++){
      char ch = mail.charAt(i);
      if (Character.isJavaIdentifierPart(ch)){
        builder.append(ch);
      }
      else {
        builder.append("_");
      }
    }
    return builder.toString() + "_" + digest;
  }
}
