package com.budgetview.gui.seriesgroups;

import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.utils.directory.Directory;

public class RenameSeriesGroupDialog extends SeriesGroupNamingDialog {

  private Key groupKey;

  public static void show(Key groupKey, GlobRepository repository, Directory directory) {
    SeriesGroupNamingDialog dialog = new RenameSeriesGroupDialog(groupKey, repository, directory);
    dialog.doShow();
  }

  private RenameSeriesGroupDialog(Key groupKey, GlobRepository parentRepository, Directory directory) {
    super(Lang.get("seriesGroup.rename.title"), Lang.get("seriesGroup.rename.message"),
          parentRepository, directory);
    this.groupKey = groupKey;
  }

  protected Key getGroupKey(LocalGlobRepository localRepository) {
    return groupKey;
  }

  protected void processOk(Key groupKey, LocalGlobRepository localRepository) {
  }
}
