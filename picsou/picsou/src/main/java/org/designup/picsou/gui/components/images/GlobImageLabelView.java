package org.designup.picsou.gui.components.images;

import org.designup.picsou.gui.components.JPopupButton;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.components.dialogs.MessageType;
import org.designup.picsou.gui.projects.components.DefaultPictureIcon;
import org.designup.picsou.model.Picture;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidFormat;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Set;

public class GlobImageLabelView implements ChangeSetListener, GlobSelectionListener, Disposable {

  private final LinkField link;
  private Key currentKey;
  private final Dimension maxSavedSize;
  private boolean autoHide;
  private boolean forcedSelection;

  private final GlobRepository repository;
  private Directory directory;
  private final SelectionService selectionService;

  private final JLabel label = new JLabel();
  private JPopupMenu popupMenu;
  private JPopupButton popupButton;
  private GlobImageLabelView.PasteImageAction pasteImagesAction;
  private GlobImageLabelView.BrowseImageAction browseImagesAction;
  private IconFactory defaultIconFactory = IconFactory.NULL_ICON_FACTORY;

  public static GlobImageLabelView init(LinkField link, Dimension maxSavedSize, GlobRepository repository, Directory directory) {
    return new GlobImageLabelView(link, maxSavedSize, repository, directory);
  }

  private GlobImageLabelView(LinkField link, Dimension maxSavedSize, GlobRepository repository, Directory directory) {
    this.link = link;
    this.maxSavedSize = maxSavedSize;
    this.repository = repository;
    this.directory = directory;
    this.selectionService = directory.get(SelectionService.class);

    this.pasteImagesAction = new PasteImageAction();
    this.browseImagesAction = new BrowseImageAction();
    setKey(null);

    popupMenu = new JPopupMenu();
    popupMenu.add(pasteImagesAction);
    popupMenu.add(browseImagesAction);

    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    label.addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent e) {
        if (!popupMenu.isShowing()) {
          popupMenu.show(label, e.getX(), e.getY());
        }
      }
    });

    repository.addChangeListener(this);
    selectionService.addListener(this, link.getGlobType());
    updateIcon();
  }

  public GlobImageLabelView setAutoHide(boolean autoHide) {
    this.autoHide = autoHide;
    return this;
  }

  public GlobImageLabelView setDefaultIconFactory(IconFactory factory) {
    defaultIconFactory = factory;
    return this;
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
    this.pasteImagesAction.update();
    this.browseImagesAction.update();
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
      label.setIcon(null);
      label.setVisible(!autoHide);
      return;
    }

    Icon icon = Picture.getIcon(glob, link, repository, label.getPreferredSize());
    if (icon == null) {
      label.setIcon(autoHide ? null : defaultIconFactory.createIcon(label.getPreferredSize()));
      label.setVisible(!autoHide);
      return;
    }

    label.setIcon(icon);
    label.setVisible(true);
  }

  public void dispose() {
    repository.removeChangeListener(this);
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

  private class BrowseImageAction extends AbstractAction {
    private BrowseImageAction() {
      super(Lang.get("imageLabel.actions.browse"));
    }

    public void update() {
      setEnabled(currentKey != null);
    }

    public void actionPerformed(ActionEvent e) {
      JFileChooser chooser = new JFileChooser();
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      int returnVal = chooser.showOpenDialog(directory.get(JFrame.class));
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File file = chooser.getSelectedFile();
        if (file.exists()) {
          try {
            Picture.setIcon(currentKey, link, repository, file.getAbsolutePath(), maxSavedSize);
          }
          catch (InvalidFormat invalidFormat) {
            MessageDialog.show("imageLabel.error.title",
                               MessageType.ERROR, directory,
                               "imageLabel.error.invalidFormat");
          }
        }
      }
    }
  }

  private class PasteImageAction extends AbstractAction {
    private PasteImageAction() {
      super(Lang.get("imageLabel.actions.paste"));
    }

    public void update() {
      setEnabled(currentKey != null);
    }

    public void actionPerformed(ActionEvent e) {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      DataFlavor flavor = DataFlavor.imageFlavor;
      if (clipboard.isDataFlavorAvailable(flavor)) {
        try {
          Image image = (Image) clipboard.getData(flavor);
          Picture.setIcon(currentKey, link, repository, image, maxSavedSize);
        }
        catch (UnsupportedFlavorException exception) {
          MessageDialog.show("imageLabel.error.title",
                             MessageType.ERROR, directory,
                             "imageLabel.error.invalidFormat");
        }
        catch (IOException exception) {
          MessageDialog.show("imageLabel.error.title",
                             MessageType.ERROR, directory,
                             "imageLabel.error.invalidFormat");
        }
      }
    }
  }

}
