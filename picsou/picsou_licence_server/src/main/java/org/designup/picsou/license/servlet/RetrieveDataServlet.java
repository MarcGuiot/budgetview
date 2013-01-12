package org.designup.picsou.license.servlet;

import com.budgetview.shared.utils.ComCst;
import org.apache.log4j.Logger;
import org.designup.picsou.gui.config.ConfigService;
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
    String mail = httpServletRequest.getParameter(ComCst.MAIL);
    String sha1Mail = httpServletRequest.getParameter(ComCst.CRYPTED_INFO);
    if (Strings.isNullOrEmpty(mail) || Strings.isNullOrEmpty(sha1Mail)){
      logger.info("missing mail or key " + mail + " " + sha1Mail);
      httpServletResponse.setHeader(ComCst.STATUS, "missing mail or key");
      return;
    }
    mail = URLDecoder.decode(mail, "UTF-8");
    sha1Mail = URLDecoder.decode(sha1Mail, "UTF-8");
    logger.info("For " + mail);
    String fileName = ReceiveDataServlet.generateDirName(mail);
    File rootDir = new File(root, fileName);
    if (!rootDir.exists()) {
      httpServletResponse.setHeader(ComCst.STATUS, "No mobile account");
    }
    else {
      File file = new File(rootDir, "data.ser");
      if (file.exists()){
        InputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
        DataInputStream stream = new DataInputStream(fileInputStream);
        String s = stream.readUTF();
        if (!s.equals(sha1Mail)){
          logger.info("bad sha1 code => bad password");
          httpServletResponse.setHeader(ComCst.STATUS, "No match");
          return;
        }
        httpServletResponse.setHeader(ComCst.STATUS, "Ok");
        int majorVersion = stream.readInt();
        int minorVersion = stream.readInt();
        httpServletResponse.setHeader(ComCst.MAJOR_VERSION_NAME, Integer.toString(majorVersion));
        httpServletResponse.setHeader(ComCst.MINOR_VERSION_NAME, Integer.toString(minorVersion));
        httpServletResponse.setHeader(ComCst.MINOR_VERSION_NAME, Integer.toString(minorVersion));
        Files.copyStream(fileInputStream, outputStream);
      }
      else {
        logger.info("no data");
        httpServletResponse.setHeader(ComCst.STATUS, "No data");
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
      e.printStackTrace();
    }
  }

}
