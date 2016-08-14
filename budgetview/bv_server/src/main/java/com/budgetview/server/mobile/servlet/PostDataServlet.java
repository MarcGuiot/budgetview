package com.budgetview.server.mobile.servlet;

import com.budgetview.server.license.servlet.AbstractHttpServlet;
import com.budgetview.server.utils.Lang;
import com.budgetview.shared.license.LicenseConstants;
import com.budgetview.shared.encryption.PasswordEncryption;
import com.budgetview.shared.mobile.MobileConstants;
import org.apache.log4j.Logger;
import org.globsframework.utils.Files;
import org.globsframework.utils.Strings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;

public class PostDataServlet extends AbstractHttpServlet {
  public static final String CODE_FILE_NAME = "code.ser";
  public static final String DATA_FILE_NAME = "data.ser";
  static Logger logger = Logger.getLogger("ReceiveDataServlet");
  private String root;

  public PostDataServlet(String root) {
    this.root = root;
  }

  protected void action(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
    InputStream inputStream = httpServletRequest.getInputStream();
    OutputStream outputStream = httpServletResponse.getOutputStream();
    String lang = httpServletRequest.getHeader(MobileConstants.HEADER_LANG);
    String mail = URLDecoder.decode(httpServletRequest.getHeader(LicenseConstants.HEADER_MAIL), "UTF-8");
    String majorVersion = httpServletRequest.getHeader(MobileConstants.MAJOR_VERSION_NAME);
    String minorVersion = httpServletRequest.getHeader(MobileConstants.MINOR_VERSION_NAME);
    String sha1Mail = URLDecoder.decode(httpServletRequest.getHeader(MobileConstants.CRYPTED_INFO), "UTF-8");

    if (Strings.isNullOrEmpty(mail) || Strings.isNullOrEmpty(majorVersion) || Strings.isNullOrEmpty(minorVersion)
        || Strings.isNullOrEmpty(sha1Mail)) {
      logger.error("missing info mail : '" + mail + "' major version : '" + majorVersion + "' minor version '" + minorVersion + "' sha1Mail : '"
                   + sha1Mail + "'");
      httpServletResponse.setHeader(MobileConstants.STATUS, "Missing info");
      return;
    }
    logger.info("receive data from " + mail + " sha1 " + sha1Mail);
    String dirName = generateDirName(mail);
    File dir = new File(root, dirName);
    if (!dir.exists()) {
      logger.warn("No directory '" + dirName + "' for " + mail);
      outputStream.write("error".getBytes());
      httpServletResponse.setHeader(MobileConstants.STATUS, Lang.get("mobile.data.missing.account", lang));
      httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
    else {
      String header = httpServletRequest.getHeader(LicenseConstants.HEADER_PENDING);
      boolean pending = false;
      if (!new File(dir, CODE_FILE_NAME).exists() || (header != null && header.equalsIgnoreCase("true"))) {
        CreateMobileUserServlet.writePendingCode(sha1Mail, dir);
        pending = true;
      }
      if (!pending && !checkSha1Code(sha1Mail, dir)) {
        logger.error("Not a valid password.");
        httpServletResponse.setHeader(MobileConstants.STATUS, Lang.get("mobile.data.invalid.password", lang));
        httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
      }
      else {
        File file = new File(dir, (pending ? CreateMobileUserServlet.PENDING : "") + DATA_FILE_NAME);
        if (file.exists() && !file.delete()) {
          logger.error("Can not delete file " + file.getAbsolutePath());
          httpServletResponse.setHeader(MobileConstants.STATUS, Lang.get("mobile.data.internal", lang));
          httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
        else {
          OutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(file));
          DataOutputStream stream = new DataOutputStream(fileOutputStream);
          stream.writeUTF(sha1Mail);
          stream.writeInt(Integer.parseInt(majorVersion));
          stream.writeInt(Integer.parseInt(minorVersion));
          Files.copyStream(inputStream, fileOutputStream);
          httpServletResponse.setHeader(MobileConstants.STATUS, "OK");
          httpServletResponse.setStatus(HttpServletResponse.SC_OK);
          inputStream.close();
        }
      }
    }
  }

  public static boolean checkSha1Code(String sha1Mail, File dir) {
    return Files.loadFileToString(new File(dir, CODE_FILE_NAME)).trim().equals(sha1Mail);
  }

  // generate a file starting with the mail with only ascii character (other a replace with _)
  // to prevent naming clash an digest (sha1) is add after
  public static String generateDirName(String mail) {
    try {
      String digest = PasswordEncryption.encodeSHA1AndHex(mail.getBytes("UTF-8"));
      StringBuilder builder = new StringBuilder(mail.length());
      for (int i = 0; i < mail.length(); i++) {
        char ch = mail.charAt(i);
        if (Character.isJavaIdentifierPart(ch)) {
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
