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
import java.util.*;

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

    Page page = getPage(target);
    if (page != null) {
      response.setContentType("text/html;charset=utf-8");
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


    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    response.setContentType("text/html;charset=utf-8");
    baseRequest.setHandled(true);
    dumpPages(target, response.getWriter());

    System.out.println("SiteweaverServer$PageHandler.handle: could not find " + target);
  }

  private void dumpPages(String target, PrintWriter writer) {
    writer.write("<html>" +
                 "<h2>Page not found</h2>\n" +
                 "<p>No page found at <strong>"+ target +"</strong></p>\n");
    writer.write("<h2>Actual ontent</h2>\n" +
                 "<ul>\n");
    SortedSet<String> urls = new TreeSet<String>();
    urls.addAll(pages.keySet());
    for (String url : urls) {
      writer.write("<li><a href="+ url + ">" + url + "</a></li>\n");
    }
    writer.write("</ul>\n" +
                 "</html>");
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
