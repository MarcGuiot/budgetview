package org.globsframework.gui.utils;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class GlobSelectionNodeStyleUpdater<T extends JComponent> implements GlobSelectionListener, Disposable {

  private Key key;
  private SplitsNode<T> node;
  private String styleForSelected;
  private String styleForUnselected;
  private SelectionService selectionService;

  public static <T extends JComponent> GlobSelectionNodeStyleUpdater init(Key key, SplitsNode<T> node, String styleForSelected, String styleForUnselected, Directory directory) {
    return new GlobSelectionNodeStyleUpdater(key, node, styleForSelected, styleForUnselected, directory);
  }

  private GlobSelectionNodeStyleUpdater(Key key, SplitsNode<T> node, String styleForSelected, String styleForUnselected, Directory directory) {
    this.key = key;
    this.node = node;
    this.styleForSelected = styleForSelected;
    this.styleForUnselected = styleForUnselected;
    this.selectionService = directory.get(SelectionService.class);
    this.selectionService.addListener(this, key.getGlobType());
  }

  public void selectionUpdated(GlobSelection selection) {
    boolean selected = selection.getAll(key.getGlobType()).contains(key);
    node.applyStyle(selected ? styleForSelected : styleForUnselected);
  }

  public void dispose() {
    this.selectionService.removeListener(this);
  }
}
