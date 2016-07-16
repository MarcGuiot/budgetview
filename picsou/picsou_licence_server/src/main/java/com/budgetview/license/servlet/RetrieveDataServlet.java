package com.budgetview.license.servlet;

import com.budgetview.shared.utils.MobileConstants;
import org.apache.log4j.Logger;
import com.budgetview.license.Lang;
import org.globsframework.utils.Files;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;

public class RetrieveDataServlet extends HttpServlet {
  static Logger logger = Logger.getLogger("RetrieveDataServlet");
  private String root;

  public RetrieveDataServlet(String root, Directory directory) {
    this.root = root;
  }

  protected void action(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
    OutputStream outputStream = httpServletResponse.getOutputStream();
    String parameter = httpServletRequest.getParameter(MobileConstants.HEADER_LANG);
    String lang = URLDecoder.decode(parameter != null ? parameter : "fr", "UTF-8");
    String mail = URLDecoder.decode(httpServletRequest.getParameter(MobileConstants.MAIL), "UTF-8");
    String sha1Mail = URLDecoder.decode(httpServletRequest.getParameter(MobileConstants.CRYPTED_INFO), "UTF-8");
    if (Strings.isNullOrEmpty(mail) || Strings.isNullOrEmpty(sha1Mail)) {
      logger.info("missing mail or key " + mail + " " + sha1Mail);
      httpServletResponse.setHeader(MobileConstants.STATUS, "missing mail or key");
      httpServletResponse.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
      return;
    }
    mail = URLDecoder.decode(mail, "UTF-8");
    sha1Mail = URLDecoder.decode(sha1Mail, "UTF-8");
    logger.info("For " + mail + " sha1 " + sha1Mail);
    String fileName = ReceiveDataServlet.generateDirName(mail);
    File rootDir = new File(root, fileName);
    if (!rootDir.exists()) {
      httpServletResponse.setHeader(MobileConstants.STATUS, Lang.get("mobile.no.account", lang));
      httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
    else {
      if (!ReceiveDataServlet.checkSha1Code(sha1Mail, rootDir)){
        logger.info("bad sha1 code => bad password");
        httpServletResponse.setHeader(MobileConstants.STATUS, Lang.get("mobile.password.invalid", lang));
        httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return;
      }
      File file = new File(rootDir, "data.ser");
      if (file.exists()) {
        InputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
        DataInputStream stream = new DataInputStream(fileInputStream);
        String s = stream.readUTF();
        if (!s.equals(sha1Mail)) {
          logger.info("bad sha1 code in data => bad password");
          httpServletResponse.setHeader(MobileConstants.STATUS, Lang.get("mobile.password.invalid", lang));
          httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
          return;
        }
        httpServletResponse.setHeader(MobileConstants.STATUS, "Ok");
        int majorVersion = stream.readInt();
        int minorVersion = stream.readInt();
        httpServletResponse.setHeader(MobileConstants.MAJOR_VERSION_NAME, Integer.toString(majorVersion));
        httpServletResponse.setHeader(MobileConstants.MINOR_VERSION_NAME, Integer.toString(minorVersion));
        httpServletResponse.setHeader(MobileConstants.MINOR_VERSION_NAME, Integer.toString(minorVersion));
        Files.copyStream(fileInputStream, outputStream);
        fileInputStream.close();
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
      }
      else {
        logger.info("no data");
        httpServletResponse.setHeader(MobileConstants.STATUS, Lang.get("mobile.no.data", lang));
        httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
      }
    }
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      req.setCharacterEncoding("UTF-8");
      resp.setCharacterEncoding("UTF-8");
      action(req, resp);
    }
    catch (Exception e) {
      logger.error("erreur: ", e);
    }
  }

}
