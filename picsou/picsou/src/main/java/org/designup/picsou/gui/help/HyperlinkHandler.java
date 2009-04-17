package org.designup.picsou.gui.help;

import org.globsframework.utils.directory.Directory;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.browsing.BrowsingService;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.*;
import java.awt.*;

public class HyperlinkHandler implements HyperlinkListener {
  private HelpService helpService;
  private Directory directory;
  private Window owner;

  public HyperlinkHandler(Directory directory) {
    this(directory, directory.get(JFrame.class));
  }

  public HyperlinkHandler(Directory directory, Window owner) {
    this.directory = directory;
    this.owner = owner;
    this.helpService = directory.get(HelpService.class);
  }

  public void hyperlinkUpdate(HyperlinkEvent e) {
    if (!HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
      return;
    }

    String href = e.getDescription().trim();
    if (href.startsWith("help:")) {
      helpService.show(href.substring(5), owner);
    }
    else if (href.startsWith("card:")) {
      NavigationService navigationService = directory.get(NavigationService.class);
      navigationService.gotoCard(Card.get(href.substring(5)));
    }
    else if (href.startsWith("url:")) {
      BrowsingService browser = directory.get(BrowsingService.class);
      browser.launchBrowser(href.substring(4));
    }
    else {
      processLink(href);
    }
  }

  protected void processLink(String href) {
  }
}
