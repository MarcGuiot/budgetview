package org.designup.picsou.gui.help;

import org.globsframework.utils.directory.Directory;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class HyperlinkHandler implements HyperlinkListener {
  private HelpService helpService;

  public HyperlinkHandler(Directory directory) {
    this.helpService = directory.get(HelpService.class);
  }

  public void hyperlinkUpdate(HyperlinkEvent e) {
    if (!HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
      return;
    }
    String description = e.getDescription().trim();
    if (description.startsWith("page:")) {
      helpService.show(description.substring(5));
    }
  }
}
