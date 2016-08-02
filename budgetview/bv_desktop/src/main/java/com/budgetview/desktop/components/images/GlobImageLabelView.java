package com.budgetview.desktop.components.images;

import com.budgetview.desktop.components.JPopupButton;
import com.budgetview.model.Picture;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;

public class GlobImageLabelView implements ChangeSetListener, GlobSelectionListener, Disposable {

  private final LinkField link;
  private Key currentKey;
  private boolean autoHide;
  private boolean forcedSelection;
  private boolean enabled = true;

  private final GlobRepository repository;
  private final SelectionService selectionService;

  private final JLabel label = new JLabel();
  private GlobImageActions actions;
  private JPopupMenu popupMenu;
  private JPopupButton popupButton;
  private IconFactory defaultIconFactory = IconFactory.NULL_ICON_FACTORY;

  public static GlobImageLabelView init(Key key, LinkField link, Dimension maxSavedSize, GlobRepository repository, Directory directory) {
    return new GlobImageLabelView(key, link, maxSavedSize, repository, directory);
  }

  public static GlobImageLabelView init(LinkField link, Dimension maxSavedSize, GlobRepository repository, Directory directory) {
    return new GlobImageLabelView(null, link, maxSavedSize, repository, directory);
  }

  private GlobImageLabelView(Key key, LinkField link, Dimension maxSavedSize, GlobRepository repository, Directory directory) {
    this.link = link;
    this.repository = repository;
    this.selectionService = directory.get(SelectionService.class);

    actions = new GlobImageActions(key, link, repository, directory, maxSavedSize);
    setKey(key);

    popupMenu = new JPopupMenu();
    actions.add(popupMenu);

    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    label.addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent e) {
        if (!popupMenu.isShowing()) {
          popupMenu.show(label, e.getX(), e.getY());
        }
      }
    });

    label.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        updateIcon();
      }
    });

    repository.addChangeListener(this);
    if (key != null) {
      forcedSelection = true;
      currentKey = key;
    }
    else {
      forcedSelection = false;
      selectionService.addListener(this, link.getGlobType());
    }
    updateIcon();
  }

  public GlobImageLabelView setAutoHide(boolean autoHide) {
    this.autoHide = autoHide;
    return this;
  }

  public GlobImageLabelView setDefaultIconFactory(IconFactory factory) {
    this.defaultIconFactory = factory;
    updateIcon();
    return this;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    updateIcon();
  }

  public GlobImageLabelView forceKeySelection(Key key) {
    if (!forcedSelection) {
      selectionService.removeListener(this);
    }
    forcedSelection = true;
    setKey(key);
    updateIcon();
    return this;
  }

  private void setKey(Key key) {
    this.currentKey = key;
    this.actions.setKey(key);
  }

  public JLabel getLabel() {
    return label;
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList all = selection.getAll(link.getGlobType());
    setKey(all.size() == 1 ? all.getFirst().getKey() : null);
    updateIcon();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (currentKey != null && changeSet.containsChanges(currentKey, link)) {
      updateIcon();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(link.getGlobType())) {
      updateIcon();
    }
  }

  private void updateIcon() {
    Glob glob = repository.find(currentKey);
    if (glob == null) {
      setDefaultIcon(true);
      return;
    }

    Dimension size = label.getPreferredSize();
    boolean noSize = ((size.height == 0) || (size.width == 0));
    ImageIcon icon = Picture.getIcon(glob, link, repository, size);
    if (icon == null) {
      setDefaultIcon(!noSize);
      return;
    }

    if (!enabled) {
      icon = Picture.toGrayscale(icon);
    }

    label.setIcon(icon);
    label.setVisible(true);
  }

  private void setDefaultIcon(boolean autoHideIfNeeded) {
    label.setIcon(defaultIconFactory.createIcon(label.getPreferredSize()));
    label.setVisible(!autoHideIfNeeded || !autoHide);
  }

  public JPopupButton getPopupButton(String text) {
    if (popupButton == null) {
      popupButton = new JPopupButton(text, popupMenu);
    }
    else {
      popupButton.setText(text);
    }
    return popupButton;
  }

  public void dispose() {
    repository.removeChangeListener(this);
  }
}
