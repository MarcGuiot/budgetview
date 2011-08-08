package org.designup.picsou.gui.components.highlighting;


import net.sourceforge.htmlunit.corejs.javascript.Node;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.List;

public abstract class HighlightUpdater implements GlobSelectionListener, Disposable {

  private Key targetKey;
  private HighlightingService highlightingService;

  public HighlightUpdater(Key targetKey,
                          Directory directory) {
    this.targetKey = targetKey;
    this.highlightingService = directory.get(HighlightingService.class);
    highlightingService.addListener(this, targetKey.getGlobType());
  }

  public void selectionUpdated(GlobSelection selection) {
    List<Key> selectedKeys = selection.getAll(targetKey.getGlobType()).getKeyList();
    setHighlighted(selectedKeys.contains(targetKey));
  }

  protected abstract void setHighlighted(boolean contains);

  public void dispose() {
    highlightingService.removeListener(this);
  }
}
