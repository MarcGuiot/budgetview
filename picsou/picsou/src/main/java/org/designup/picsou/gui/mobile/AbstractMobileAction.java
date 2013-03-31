package org.designup.picsou.gui.mobile;

import org.designup.picsou.model.UserPreferences;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;

public abstract class AbstractMobileAction extends AbstractAction {
  protected final GlobRepository repository;
  protected final Directory directory;

  public AbstractMobileAction(String label, GlobRepository repository, Directory directory) {
    super(label);
    this.repository = repository;
    this.directory = directory;
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(UserPreferences.KEY)) {
          updateStatus(repository);
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(UserPreferences.TYPE)) {
          updateStatus(repository);
        }
      }
    });
    updateStatus(repository);
  }

  protected Glob updateStatus(GlobRepository repository) {
    Glob preference = repository.find(UserPreferences.KEY);
    if (preference != null) {
      boolean isEnabled = Strings.isNotEmpty(preference.get(UserPreferences.MAIL_FOR_MOBILE));
      processMobileStatusChange(isEnabled);
    }
    return preference;
  }

  protected abstract void processMobileStatusChange(boolean enabled);
}
