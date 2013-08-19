package org.designup.picsou.gui.components.images;

import com.jidesoft.swing.ResizableMouseInputAdapter;
import org.designup.picsou.gui.components.JPopupButton;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.*;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Set;

public class GlobImageLabelView implements ChangeSetListener, GlobSelectionListener, Disposable {

  private final StringField field;
  private Key currentKey;
  private boolean autoHide;
  private boolean forcedSelection;

  private final GlobRepository repository;
  private Directory directory;
  private FileStorageService fileStorageService;
  private final SelectionService selectionService;

  private final JLabel label = new JLabel();
  private JPopupMenu popupMenu;
  private JPopupButton popupButton;
  private GlobImageLabelView.BrowseImageAction browseImagesAction;

  public static GlobImageLabelView init(StringField field, GlobRepository repository, Directory directory) {
    return new GlobImageLabelView(field, repository, directory);
  }

  private GlobImageLabelView(StringField field, GlobRepository repository, Directory directory) {
    this.field = field;
    this.repository = repository;
    this.directory = directory;
    this.fileStorageService = directory.get(FileStorageService.class);
    this.selectionService = directory.get(SelectionService.class);

    this.browseImagesAction = new BrowseImageAction();
    setKey(null);

    popupMenu = new JPopupMenu();
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
    selectionService.addListener(this, field.getGlobType());
    updateIcon();
  }

  public GlobImageLabelView setAutoHide(boolean autoHide) {
    this.autoHide = autoHide;
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
    this.browseImagesAction.update();
  }

  public JLabel getLabel() {
    return label;
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList all = selection.getAll(field.getGlobType());
    setKey(all.size() == 1 ? all.getFirst().getKey() : null);
    updateIcon();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (currentKey != null && changeSet.containsChanges(currentKey, field)) {
      updateIcon();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(field.getGlobType())) {
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

    final String iconPath = glob.get(field);
    if (Strings.isNullOrEmpty(iconPath)) {
      label.setIcon(null);
      label.setVisible(!autoHide);
      return;
    }

    label.setVisible(true);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        label.setIcon(fileStorageService.getIcon(iconPath, label.getSize()));
      }
    });
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
          repository.update(currentKey, field, file.getAbsolutePath());
        }
      }
    }
  }

}
