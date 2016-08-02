package com.budgetview.desktop.components.images;

import com.budgetview.desktop.components.dialogs.MessageDialog;
import com.budgetview.desktop.components.dialogs.MessageType;
import com.budgetview.model.Picture;
import com.budgetview.utils.Lang;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidFormat;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class GlobImageActions {

  private Key currentKey;

  private final LinkField link;
  private final GlobRepository repository;
  private final Directory directory;
  private final Dimension maxSavedSize;

  private PasteImageAction pasteImagesAction = new PasteImageAction();
  private BrowseImageAction browseImagesAction = new BrowseImageAction();
  private ClearImageAction clearImageAction = new ClearImageAction();

  public GlobImageActions(Key currentKey, LinkField link, GlobRepository repository, Directory directory, Dimension maxSavedSize) {
    this.link = link;
    this.repository = repository;
    this.directory = directory;
    this.maxSavedSize = maxSavedSize;
    setKey(currentKey);
  }

  public void add(JPopupMenu popupMenu) {
    popupMenu.add(pasteImagesAction);
    popupMenu.add(browseImagesAction);
    popupMenu.add(clearImageAction);
  }

  public void setKey(Key key) {
    this.currentKey = key;
    this.pasteImagesAction.update();
    this.browseImagesAction.update();
    this.clearImageAction.update();
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
      if (!clipboard.isDataFlavorAvailable(flavor)) {
        MessageDialog.show("imageLabel.actions.paste.empty.title",
                           MessageType.INFO, directory,
                           "imageLabel.actions.paste.empty.message");
        return;
      }

      try {
        Image image = (Image)clipboard.getData(flavor);
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

  private class ClearImageAction extends AbstractAction {
    private ClearImageAction() {
      super(Lang.get("imageLabel.actions.clear"));
    }

    public void update() {
      setEnabled(currentKey != null);
    }

    public void actionPerformed(ActionEvent e) {
      repository.setTarget(currentKey, link, null);
    }
  }
}
