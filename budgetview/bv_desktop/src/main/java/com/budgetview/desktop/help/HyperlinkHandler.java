package com.budgetview.desktop.help;

import com.budgetview.desktop.browsing.BrowsingService;
import com.budgetview.desktop.card.NavigationService;
import com.budgetview.desktop.feedback.FeedbackService;
import com.budgetview.desktop.model.Card;
import org.globsframework.utils.directory.Directory;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.util.HashMap;
import java.util.Map;

public class HyperlinkHandler implements HyperlinkListener {
  private HelpService helpService;
  private Directory directory;
  private Map<String, Runnable> actions = new HashMap<String, Runnable>();

  public HyperlinkHandler(Directory directory) {
    this.directory = directory;
    this.helpService = directory.get(HelpService.class);
  }

  public void hyperlinkUpdate(HyperlinkEvent e) {
    if (!HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
      return;
    }

    String href = e.getDescription().trim();
    processLink(href);
  }

  public void registerLinkAction(String href, Runnable action) {
    actions.put(href, action);
  }

  private void processLink(String href) {
    if (href.startsWith("help:")) {
      helpService.show(href.substring(5));
    }
    else if (href.startsWith("card:")) {
      NavigationService navigationService = directory.get(NavigationService.class);
      navigationService.gotoCard(Card.get(href.substring(5)));
    }
    else if (href.startsWith("url:")) {
      BrowsingService browser = directory.get(BrowsingService.class);
      browser.launchBrowser(href.substring(4));
    }
    else if (href.startsWith("contact:")) {
      directory.get(FeedbackService.class).send();
    }
    else if (actions.containsKey(href)) {
      actions.get(href).run();
    }
    else {
      processCustomLink(href);
    }
  }

  protected void processCustomLink(String href) {
  }
}
