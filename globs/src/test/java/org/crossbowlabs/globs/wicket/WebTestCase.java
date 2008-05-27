package org.crossbowlabs.globs.wicket;

import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import junit.framework.TestCase;
import org.crossbowlabs.globs.metamodel.DummyObject;
import org.crossbowlabs.globs.model.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class WebTestCase extends TestCase {
  protected GlobChecker checker;
  protected DummyWebServer server;
  protected GlobRepository repository;
  protected DummyChangeSetListener changeSetListener;
  private List collectedAlerts = new ArrayList();

  protected void setUp() throws Exception {
    Locale.setDefault(Locale.US);
    repository = GlobRepositoryBuilder.createEmpty();
    changeSetListener = new DummyChangeSetListener();
    checker = new GlobChecker();
    server = new DummyWebServer();
    server.start();
    DummyApplication.reset(repository);
    DummyPage.reset();
  }

  protected void tearDown() throws Exception {
    if (!collectedAlerts.isEmpty()) {
      fail("Uncaught alerts: " + collectedAlerts);
    }

    server.stop();
    repository = null;
    server = null;
    DummyApplication.reset(null);
    DummyPage.reset();
  }

  protected String getComponentId() {
    return DummyPage.COMPONENT_ID;
  }

  protected void assertNoMessages(HtmlElement element) throws Exception {
    assertNoMessages(element.getPage());
  }

  protected void assertNoMessages(HtmlPage page) throws Exception {
    HtmlElement element = page.getHtmlElementById(DummyPage.FEEDBACK_COMPONENT_ID);
    assertFalse(element.getAllHtmlChildElements().hasNext());
  }

  protected void assertAlert(String alert) {
    if (!collectedAlerts.remove(alert)) {
      fail("Alert not found: " + alert);
    }
  }

  protected void checkMessages(HtmlPage page, String... messages) throws Exception {
    HtmlElement element = page.getHtmlElementById(DummyPage.FEEDBACK_COMPONENT_ID);
    String displayedMessages = element.asText();
    for (String message : messages) {
      if (!displayedMessages.contains(message)) {
        fail("Message '" + message + "' not found. Actual messages: " + displayedMessages);
      }
    }
  }

  public void runBare() throws Throwable {
    try {
      super.runBare();
    }
    catch (FailingHttpStatusCodeException e) {
      System.out.println("WebTestCase.runBare - received unexpected status " + e.getStatusCode() + "\n" +
                         e.getResponse().getContentAsString());
      throw e;
    }
  }

  protected HtmlPage renderPage(ComponentFactory factory) throws Exception {
    DummyPage.setFactory(factory);

    final WebClient webClient = new WebClient();
    webClient.setRedirectEnabled(true);
    webClient.setJavaScriptEnabled(true);
    webClient.setThrowExceptionOnScriptError(true);
    webClient.setThrowExceptionOnFailingStatusCode(true);

    // experimental - avoids user-specified timeouts but does not work with all AJAX requests
    webClient.setAjaxController(new NicelyResynchronizingAjaxController());

    CollectingAlertHandler handler = new CollectingAlertHandler(collectedAlerts);
    webClient.setAlertHandler(handler);

    final URL url = new URL(server.getUrl(""));
    return (HtmlPage)webClient.getPage(url);
  }

  protected HtmlElement renderComponent(ComponentFactory factory) throws Exception {
    HtmlPage page = renderPage(factory);
    return page.getHtmlElementById(DummyPage.COMPONENT_ID);
  }

  protected void checkRendereringError(String message, ComponentFactory factory) throws Exception {
    DummyPage.setFactory(factory);

    final WebClient webClient = new WebClient();
    webClient.setThrowExceptionOnFailingStatusCode(true);
    final URL url = new URL(server.getUrl(""));
    try {
      webClient.getPage(url);
      fail("Error '" + message + "' did not happen");
    }
    catch (FailingHttpStatusCodeException e) {
      assertEquals(500, e.getStatusCode());
      String pageContent = e.getResponse().getContentAsString().replace("&#039;", "'");
      if (!pageContent.contains(message)) {
        fail("Message '" + message + "' not found - actual page content is: " + pageContent);
      }
    }
  }

  protected void sleep(long millis) {
    try {
      Thread.sleep(millis);
    }
    catch (InterruptedException e) {
      throw new Error(e);
    }
  }

  protected void dumpPage(HtmlPage page) {
    System.out.println(page.asXml());
  }

  protected Glob getDummyObject(int i) {
    return repository.get(Key.create(DummyObject.TYPE, i));
  }

}
