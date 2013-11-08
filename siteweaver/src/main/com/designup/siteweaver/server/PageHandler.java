package com.designup.siteweaver.server;

import com.designup.siteweaver.generation.SiteGenerator;
import com.designup.siteweaver.model.CopySet;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;
import com.designup.siteweaver.utils.FileUtils;
import com.designup.siteweaver.xml.SiteParser;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.util.URIUtil;

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
  private File configFilePath;
  private long lastConfigFileUpdate;

  public PageHandler(File configFilePath) throws Exception {
    this.configFilePath = configFilePath;
    reload();
  }

  private void reload() throws Exception {
    this.site = SiteParser.parse(FileUtils.createEncodedReader(configFilePath.getAbsoluteFile()),
                                 configFilePath.getParent());
    this.lastConfigFileUpdate = configFilePath.lastModified();
    reloadPages();
    reloadHandlers();
  }

  private void reloadHandlers() throws Exception {
    for (ResourceHandler handler : resourceHandlerList) {
      handler.stop();
    }
    for (CopySet copySet : site.getCopySets()) {
      ResourceHandler handler = new ResourceHandler();
      String inputDirectory = URIUtil.canonicalPath(new File(site.getInputDirectory(copySet.getBaseDir())).getAbsolutePath());
      handler.setResourceBase(inputDirectory);
      resourceHandlerList.add(handler);
    }
  }

  private void reloadPages() {
    this.pages.clear();
    loadPage(this.site.getRootPage());
  }

  private void loadPage(Page page) {
    pages.put(page.getUrl(), page);
    for (Page subPage : page.getSubPages()) {
      loadPage(subPage);
    }
  }

  public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {

    Request baseRequest = (request instanceof Request) ? (Request)request : HttpConnection.getCurrentConnection().getRequest();

    if (configFilePath.lastModified() > lastConfigFileUpdate) {
      try {
        System.out.println("PageHandler.handle: reloading config file");
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

    if (target.equalsIgnoreCase("/dump")) {
      response.setContentType("text/html;charset=utf-8");
      writeSite(response.getWriter());
      response.setStatus(HttpServletResponse.SC_OK);
      baseRequest.setHandled(true);
      return;
    }

    Page page = getPage(target);
    if (page != null) {
      response.setContentType("text/html;charset=utf-8");
      File file = new File(site.getAbsoluteFileName(page));
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
      handler.handle(target, request, response, dispatch);
      if (baseRequest.isHandled()) {
        return;
      }
    }

    return404(target, response, baseRequest,
              "No page found at URL <strong><code>" + target + "</code></strong>");

    System.out.println("SiteweaverServer$PageHandler.handle: could not find " + target);
  }

  private void return404(String target, HttpServletResponse response, Request baseRequest, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    response.setContentType("text/html;charset=utf-8");
    baseRequest.setHandled(true);
    write404Page(target, response.getWriter(),
                 message);
  }

  private void writeSite(PrintWriter writer) {
    writer.write("<html>\n");
    writer.write("<h1>Site content</h1>\n");
    writeRootPage(writer);
    writer.write("</html>");
  }

  private void write404Page(String target, PrintWriter writer, String message) {
    writer.write("<html>\n" +
                 "<div style=\"background:#F88;border:solid 1px #F00;color:#FFF;padding:15px;\">\n" +
                 "<h2>404 Page not found</h2>\n" +
                 "<p>"+ message +"</p>\n" +
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
