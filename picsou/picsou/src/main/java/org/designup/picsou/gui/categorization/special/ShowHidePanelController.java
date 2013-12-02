package org.designup.picsou.gui.categorization.special;

import org.globsframework.gui.components.ShowHideButton;

public class ShowHidePanelController implements SpecialCategorizationPanelController {
  private final ShowHideButton showHide;

  public ShowHidePanelController(ShowHideButton showHide) {
    this.showHide = showHide;
  }

  public void setShown(boolean visible) {
    if (visible) {
      showHide.setShown();
    }
    else {
      showHide.setHidden();
    }
  }

  public void setLocked(boolean locked) {
    if (locked) {
      showHide.lock();
    }
    else {
      showHide.unlock();
    }
  }
}
