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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageHandler extends AbstractHandler {

  private Site site;
  private Map<String, Page> pages = new HashMap<String, Page>();
  private List<ResourceHandler> resourceHandlerList = new ArrayList<ResourceHandler>();

  public PageHandler(File configFilePath) throws Exception {
    this.site = SiteParser.parse(FileUtils.createEncodedReader(configFilePath.getAbsoluteFile()),
                                 configFilePath.getParent());
    this.pages.clear();
    loadPages();
    for (CopySet copySet : site.getCopySets()) {
      ResourceHandler handler = new ResourceHandler();
      String inputDirectory = URIUtil.canonicalPath(new File(site.getInputDirectory(copySet.getBaseDir())).getAbsolutePath());
      handler.setResourceBase(inputDirectory);
      resourceHandlerList.add(handler);
    }
  }

  private void loadPages() {
    loadPage(this.site.getRootPage());
  }

  private void loadPage(Page page) {
    String fileName = page.getFileName();
    if (!fileName.startsWith("/")) {
      fileName = "/" + fileName;
    }
    pages.put(fileName, page);
    for (Page subPage : page.getSubPages()) {
      loadPage(subPage);
    }
  }

  public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {

    Request baseRequest = (request instanceof Request) ? (Request)request : HttpConnection.getCurrentConnection().getRequest();

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

    System.out.println("SiteweaverServer$PageHandler.handle: could not find " + target);
  }

  private Page getPage(String target) {
    if ("/".equals(target)) {
      target = "/index.html";
    }
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
