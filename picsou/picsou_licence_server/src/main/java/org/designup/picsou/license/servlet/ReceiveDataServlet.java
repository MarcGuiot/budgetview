package org.designup.picsou.license.servlet;

import com.budgetview.shared.utils.ComCst;
import com.budgetview.shared.utils.Crypt;
import org.apache.log4j.Logger;
import org.designup.picsou.gui.config.ConfigService;
import org.globsframework.utils.Files;
import org.globsframework.utils.Strings;
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
    String majorVersion = httpServletRequest.getHeader(ComCst.MAJOR_VERSION_NAME);
    String minorVersion = httpServletRequest.getHeader(ComCst.MINOR_VERSION_NAME);
    String sha1Mail = httpServletRequest.getHeader(ComCst.CRYPTED_INFO);
    if (Strings.isNullOrEmpty(mail) || Strings.isNullOrEmpty(majorVersion) || Strings.isNullOrEmpty(minorVersion)
       || Strings.isNullOrEmpty(sha1Mail)){
      logger.info("missing info mail : '" + mail + "' major version : '" + majorVersion + "' minor version '" + minorVersion + "' sha1Mail : '"
      + sha1Mail + "'");
      httpServletResponse.setHeader(ComCst.STATUS, "Missing info");
      return;
    }
    logger.info("receive data from " + mail);
    String dirName = generateDirName(mail);
    File dir = new File(root, dirName);
    if (!dir.exists()){
      logger.warn("No directory '" + dirName + "' for " + mail);
      outputStream.write("error".getBytes());
      httpServletResponse.setHeader(ComCst.STATUS, "No mobile account");
    }
    else {
      File file = new File(dir, "data.ser");
      if (file.exists() && !file.delete()){
        logger.error("Can not delete file " + file.getAbsolutePath());
        httpServletResponse.setHeader(ComCst.STATUS, "Can not delete file");
      }
      else {
        OutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(file));
        DataOutputStream stream = new DataOutputStream(fileOutputStream);
        stream.writeUTF(sha1Mail);
        stream.writeInt(Integer.parseInt(majorVersion));
        stream.writeInt(Integer.parseInt(minorVersion));
        Files.copyStream(inputStream, fileOutputStream);
        httpServletResponse.setHeader(ComCst.STATUS, "OK");
      }
    }
  }

  // generate a file starting with the mail with only ascii character (other a replace with _)
  // to prevent naming clash an digest (sha1) is add after
  public static String generateDirName(String mail) {
    try {
      String digest = Crypt.encodeSHA1AndHex(mail.getBytes("UTF-8"));
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
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
