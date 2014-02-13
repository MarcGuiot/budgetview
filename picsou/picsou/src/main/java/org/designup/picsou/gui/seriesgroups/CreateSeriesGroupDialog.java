package org.designup.picsou.gui.seriesgroups;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesGroup;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.utils.directory.Directory;

import static org.globsframework.model.FieldValue.value;

public class CreateSeriesGroupDialog extends SeriesGroupNamingDialog {

  public static void show(Key seriesKey, GlobRepository repository, Directory directory) {
    SeriesGroupNamingDialog dialog = new CreateSeriesGroupDialog(seriesKey, repository, directory);
    dialog.doShow();
  }

  private Key seriesKey;

  private CreateSeriesGroupDialog(Key seriesKey, GlobRepository parentRepository, Directory directory) {
    super(Lang.get("seriesGroup.creation.title"), Lang.get("seriesGroup.creation.message"), parentRepository, directory);
    this.seriesKey = seriesKey;
  }

  protected void processOk(Key groupKey, LocalGlobRepository localRepository) {
    localRepository.update(seriesKey, Series.GROUP, groupKey.get(SeriesGroup.ID));
  }

  protected Key getGroupKey(LocalGlobRepository localRepository) {
    BudgetArea budgetArea = Series.getBudgetArea(localRepository.get(seriesKey));
    return localRepository.create(SeriesGroup.TYPE,
                                  value(SeriesGroup.BUDGET_AREA, budgetArea.getId())).getKey();
  }
}
