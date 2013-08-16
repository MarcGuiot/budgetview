package org.designup.picsou.gui.components.images;

import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.*;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;

public class ImageLabel implements ChangeSetListener, Disposable {

  private final Key key;
  private final StringField field;
  private final GlobRepository repository;
  private final JLabel label = new JLabel();
  private FileStorageService fileStorageService;
  private boolean autoHide;

  public ImageLabel(Key key, StringField field, GlobRepository repository, Directory directory, boolean autoHide) {
    this.key = key;
    this.field = field;
    this.repository = repository;
    this.fileStorageService = directory.get(FileStorageService.class);
    repository.addChangeListener(this);
    updateIcon();
    this.autoHide = autoHide;
  }

  public JLabel getLabel() {
    return label;
  }

  private void updateIcon() {
    Glob glob = repository.find(key);
    if (glob == null) {
      label.setIcon(null);
      label.setVisible(!autoHide);
      return;
    }

    String iconPath = glob.get(field);
    if (Strings.isNullOrEmpty(iconPath)) {
      label.setIcon(null);
      label.setVisible(!autoHide);
      return;
    }

    label.setVisible(true);
    label.setIcon(fileStorageService.getIcon(iconPath, label.getSize()));
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    updateIcon();
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    updateIcon();
  }

  public void dispose() {
    repository.removeChangeListener(this);
  }
}
