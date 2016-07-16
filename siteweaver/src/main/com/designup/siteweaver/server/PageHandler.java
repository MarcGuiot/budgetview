package com.designup.siteweaver.server;

import com.designup.siteweaver.generation.SiteGenerator;
import com.designup.siteweaver.model.*;
import com.designup.siteweaver.server.upload.FileAccess;
import com.designup.siteweaver.server.upload.SiteUploader;
import com.designup.siteweaver.server.utils.FileAccessHtmlLogger;
import com.designup.siteweaver.server.utils.LocalOutput;
import com.designup.siteweaver.xml.SiteParser;
import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageHandler extends AbstractHandler {

  private Site site;
  private Map<String, Page> pages = new HashMap<String, Page>();
  private List<ResourceHandler> resourceHandlerList = new ArrayList<ResourceHandler>();
  private File configFile;
  private FileAccess fileAccess;
  private long lastConfigFileUpdate;

  public PageHandler(File configFile, FileAccess fileAccess) throws Exception {
    this.configFile = configFile;
    this.fileAccess = fileAccess;
    reload();
  }

  private void reload() throws Exception {
    this.site = SiteParser.parse(configFile);
    this.lastConfigFileUpdate = configFile.lastModified();
    reloadPages();
    reloadHandlers();
  }

  private void reloadHandlers() throws Exception {
    for (ResourceHandler handler : resourceHandlerList) {
      handler.stop();
    }
    for (CopySet copySet : site.getCopySets()) {
      ResourceHandler handler = new ResourceHandler();
      String inputDirectory = site.getInputDirectory(copySet);
      handler.setResourceBase(inputDirectory);
      resourceHandlerList.add(handler);
    }
  }

  private void reloadPages() throws IOException {
    this.pages.clear();
    this.site.processPages(new PageFunctor() {
      public void process(Page page) throws Exception {
        pages.put(page.getUrl(), page);
      }
    });
  }


  public void handle(String target, Request request, HttpServletRequest httpServletRequest, HttpServletResponse response) throws IOException, ServletException {
    Request baseRequest = (request instanceof Request) ? (Request)request : HttpConnection.getCurrentConnection().getHttpChannel().getRequest();

    if (configFile.lastModified() > lastConfigFileUpdate) {
      try {
        reload();
      }
      catch (Exception e) {
        response.setContentType("text/html;charset=utf-8");
        e.printStackTrace(new PrintWriter(response.getWriter()));
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        lastConfigFileUpdate = 0;
        return;
      }
    }

    if (target.equalsIgnoreCase("/!dump")) {
      response.setContentType("text/html;charset=utf-8");
      writeSite(response.getWriter());
      response.setStatus(HttpServletResponse.SC_OK);
      baseRequest.setHandled(true);
      return;
    }

    if (target.equalsIgnoreCase("/!diff")) {
      diffAndUpload(response, false, "Diff");
      baseRequest.setHandled(true);
      return;
    }

    if (target.equalsIgnoreCase("/!publish")) {
      diffAndUpload(response, true, "Publish");
      baseRequest.setHandled(true);
      return;
    }

    Page page = getPage(target);
    if (page != null) {
      response.setContentType("text/html;charset=utf-8");
      File file = new File(site.getInputFilePath(page));
      if (!file.exists()) {
        return404(target, response, baseRequest,
                  "No source found for page <strong><code>" + target + "</code></strong> " +
                  "with path URL <strong><code>" + file.getAbsolutePath() + "</code></strong>");
        return;
      }
      SiteGenerator.run(site, page, new LocalOutput(response.getWriter()));
      response.setStatus(HttpServletResponse.SC_OK);
      baseRequest.setHandled(true);
      return;
    }

    for (ResourceHandler handler : resourceHandlerList) {
      handler.handle(target, request, httpServletRequest, response);
      if (baseRequest.isHandled()) {
        return;
      }
    }

    return404(target, response, baseRequest,
              "No page found at URL <strong><code>" + target + "</code></strong>");

    System.out.println("!! PageHandler: could not find " + target);
  }

  private void diffAndUpload(HttpServletResponse response, boolean applyChanges, String title) throws IOException {
    response.setContentType("text/html;charset=utf-8");

    PrintWriter writer = response.getWriter();
    writer.write("<html><body>" +
                 "<h1>" + title + "</h1>");
    FileAccessHtmlLogger logger = new FileAccessHtmlLogger(writer);
    try {
      fileAccess.setApplyChanges(applyChanges);
      fileAccess.addListener(logger);
      SiteUploader uploader = new SiteUploader(site, fileAccess);
      uploader.run();
      response.setStatus(HttpServletResponse.SC_OK);
      logger.complete();
      writer.write("</body></html>");
    }
    catch (IOException e) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      System.out.println("\n\nPageHandler.diffAndUpload: " + e.getMessage());
      e.printStackTrace();
      writer.write("Error: " + e.getMessage() + "</body></html>");
    }
    finally {
      fileAccess.removeListener(logger);
    }
  }

  private void return404(String target, HttpServletResponse response, Request baseRequest, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    response.setContentType("text/html;charset=utf-8");
    baseRequest.setHandled(true);
    write404Page(target, response.getWriter(),
                 message);
  }

  private void writeSite(final PrintWriter writer) {
    writer.write("<html>\n");
    writer.write("<h1>Site content</h1>\n");
    writeRootPage(writer);
    writer.write("<hr/>");
    writer.write("<h1>Files</h1>\n");
    writeFiles(writer);
    writer.write("</html>");
  }

  private void write404Page(String target, PrintWriter writer, String message) {
    writer.write("<html>\n" +
                 "<div style=\"background:#F88;border:solid 1px #F00;color:#FFF;padding:15px;\">\n" +
                 "<h2>404 Page not found</h2>\n" +
                 "<p>" + message + "</p>\n" +
                 "</div>\n");
    writer.write("<h2>Actual content</h2>\n");
    writeRootPage(writer);
    writer.write("</html>");
  }

  private void writeRootPage(PrintWriter writer) {
    writer.write("<ul>\n");
    writePage(site.getRootPage(), writer);
    writer.write("</ul>\n");
  }

  private void writePage(Page page, PrintWriter writer) {
    writer.write("<li><a href=" + page.getUrl() + ">" + page.getTitle() + "</a></li>\n");
    if (page.hasSubPages()) {
      writer.write("<ul>\n");
      for (Page subPage : page.getSubPages()) {
        writePage(subPage, writer);
      }
      writer.write("</ul>\n");
    }
  }

  private Page getPage(String target) {
    return pages.get(target);
  }

  private void writeFiles(final PrintWriter writer) {
    try {
      writer.write("<ul>\n");
      site.processFiles(new FileFunctor() {
        public void process(File inputFile, String targetPath) throws IOException {
          writer.write("<li><a href=file://" + inputFile.getAbsolutePath() + ">" + inputFile.getPath() + "</a> ==> " + targetPath + "</li>\n");
        }
      });
      writer.write("</ul>\n");
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void doStart() throws Exception {
    super.doStart();
    for (ResourceHandler handler : resourceHandlerList) {
      handler.start();
    }
  }

  protected void doStop() throws Exception {
    super.doStop();
    for (ResourceHandler handler : resourceHandlerList) {
      handler.stop();
    }
  }

  public void setServer(Server server) {
    super.setServer(server);
    for (ResourceHandler handler : resourceHandlerList) {
      handler.setServer(server);
    }
  }

  public void destroy() {
    super.destroy();
    for (ResourceHandler handler : resourceHandlerList) {
      handler.destroy();
    }
  }
}
