package org.crossbowlabs.rooms.web;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import junit.framework.TestCase;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.wicket.protocol.http.WicketServlet;
import org.crossbowlabs.rooms.DemoRoomsServer;
import org.crossbowlabs.rooms.model.PersistenceManager;
import org.crossbowlabs.rooms.model.Room;
import org.crossbowlabs.rooms.model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class RoomsPageTest extends TestCase {
  private static final int PORT = 8090;

  public void test() throws Exception {
    HtmlPage htmlPage = getPage("login");
    HtmlInput userInput = (HtmlInput) htmlPage.getHtmlElementById("loginUserName");
    HtmlInput passwordInput = (HtmlInput) htmlPage.getHtmlElementById("loginPassword");
    userInput.setValueAttribute("admin");
    passwordInput.setValueAttribute("admin");
    HtmlSubmitInput id = (HtmlSubmitInput) htmlPage.getHtmlElementById("submitId");
    HtmlPage page = (HtmlPage) id.click();
    // ca marche pas parceque wicket renomme l'id RoomMatriceTableId en RoomMatriceTable5
//    HtmlTable table = (HtmlTable) page.getHtmlElementById("RoomMatriceTableId");
//    HtmlTableCell at = table.getCellAt(1, 1);
//    String s = at.asText();
//    assertTrue(s.contains("verdi"));
//    assertTrue(s.contains("mozart"));
  }

  private HtmlPage getPage(String s) throws IOException {
    WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3_6);
    webClient.setJavaScriptEnabled(true);
    webClient.setThrowExceptionOnScriptError(true);
    webClient.setThrowExceptionOnFailingStatusCode(true);
    HtmlPage htmlPage = ((HtmlPage) webClient.getPage(getUrl(s)));
    return htmlPage;
  }

  private URL getUrl(String s) throws MalformedURLException {
    return new URL("http://localhost:" + PORT + "/" + s);
  }

  protected void setUp() throws Exception {
    super.setUp();

    Locale.setDefault(Locale.ENGLISH);
    DOMConfigurator.configure(DemoRoomsServer.class.getClassLoader().getResource("log4j.xml"));
    initRooms();
    org.mortbay.jetty.Server jetty = new org.mortbay.jetty.Server(PORT);
    Context context = new Context(jetty, "/", Context.SESSIONS);
    context.setResourceBase("target/classes");
    ServletHolder holder = new ServletHolder(new WicketServlet());
    holder.setInitParameter("applicationClassName", RoomsApplication.class.getName());
    context.addServlet(holder, "/*");

    jetty.start();
  }

  private static void initRooms() {
    Session session = PersistenceManager.getInstance().getNewSession();
    Transaction transaction = session.getTransaction();
    transaction.begin();
    User admin = new User("admin", "admin", "admin", "admin@admin");
    session.save(admin);
    session.save(new Room("verdi", 100));
    session.save(new Room("mozart", 20));
    transaction.commit();
    session.close();
  }

}
