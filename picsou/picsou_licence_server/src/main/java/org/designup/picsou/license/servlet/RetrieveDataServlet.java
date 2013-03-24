package org.designup.picsou.license.servlet;

import com.budgetview.shared.utils.ComCst;
import org.apache.log4j.Logger;
import org.designup.picsou.license.Lang;
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
    String parameter = httpServletRequest.getParameter(ComCst.HEADER_LANG);
    String lang = URLDecoder.decode(parameter != null ? parameter : "fr", "UTF-8");
    String mail = URLDecoder.decode(httpServletRequest.getParameter(ComCst.MAIL), "UTF-8");
    String sha1Mail = URLDecoder.decode(httpServletRequest.getParameter(ComCst.CRYPTED_INFO), "UTF-8");
    if (Strings.isNullOrEmpty(mail) || Strings.isNullOrEmpty(sha1Mail)) {
      logger.info("missing mail or key " + mail + " " + sha1Mail);
      httpServletResponse.setHeader(ComCst.STATUS, "missing mail or key");
      return;
    }
    mail = URLDecoder.decode(mail, "UTF-8");
    sha1Mail = URLDecoder.decode(sha1Mail, "UTF-8");
    logger.info("For " + mail + " sha1 " + sha1Mail);
    String fileName = ReceiveDataServlet.generateDirName(mail);
    File rootDir = new File(root, fileName);
    if (!rootDir.exists()) {
      httpServletResponse.setHeader(ComCst.STATUS, Lang.get("mobile.no.account", lang));
    }
    else {
      if (!ReceiveDataServlet.checkSha1Code(sha1Mail, rootDir)){
        logger.info("bad sha1 code => bad password");
        httpServletResponse.setHeader(ComCst.STATUS, Lang.get("mobile.password.invalid", lang));
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
          httpServletResponse.setHeader(ComCst.STATUS, Lang.get("mobile.password.invalid", lang));
          httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
          return;
        }
        httpServletResponse.setHeader(ComCst.STATUS, "Ok");
        int majorVersion = stream.readInt();
        int minorVersion = stream.readInt();
        httpServletResponse.setHeader(ComCst.MAJOR_VERSION_NAME, Integer.toString(majorVersion));
        httpServletResponse.setHeader(ComCst.MINOR_VERSION_NAME, Integer.toString(minorVersion));
        httpServletResponse.setHeader(ComCst.MINOR_VERSION_NAME, Integer.toString(minorVersion));
        Files.copyStream(fileInputStream, outputStream);
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
      }
      else {
        logger.info("no data");
        httpServletResponse.setHeader(ComCst.STATUS, Lang.get("mobile.no.data", lang));
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
