package org.designup.picsou.license.servlet;

import org.designup.picsou.gui.config.ConfigService;
import org.globsframework.utils.Files;
import org.globsframework.utils.directory.Directory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class RetrieveDataServlet extends AbstractHttpServlet {
  private String root;

  public RetrieveDataServlet(String root, Directory directory) {
    this.root = root;
  }

  protected void action(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
    InputStream inputStream = httpServletRequest.getInputStream();
    OutputStream outputStream = httpServletResponse.getOutputStream();
    String mail = httpServletRequest.getHeader(ConfigService.HEADER_MAIL);
    String fileName = ReceiveDataServlet.generateFileName(mail);
    File file = new File(root, fileName);
    if (!file.exists()){

    }
    else {
      InputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
      Files.copyStream(fileInputStream, outputStream);
    }
  }
}
