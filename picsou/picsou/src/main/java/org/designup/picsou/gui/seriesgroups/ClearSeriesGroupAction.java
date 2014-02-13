package org.designup.picsou.gui.seriesgroups;

import org.designup.picsou.model.Series;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.actions.SingleGlobAction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

public class ClearSeriesGroupAction extends SingleGlobAction {
  public ClearSeriesGroupAction(Key seriesKey, GlobRepository repository) {
    super(Lang.get("seriesGroup.menu.removeFromGroup"), seriesKey, repository);
  }

  protected void processUpdate(Glob series, GlobRepository repository) {
    setEnabled(series != null && series.get(Series.GROUP) != null);
  }

  protected void processClick(Glob series, GlobRepository repository) {
    repository.update(series.getKey(), Series.GROUP, null);
  }
}
