package org.designup.picsou.gui.seriesgroups;

import org.designup.picsou.model.Series;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DeleteSeriesGroupAction extends AbstractAction {
  private Key seriesGroupKey;
  private GlobRepository repository;

  public DeleteSeriesGroupAction(Key seriesGroupKey, GlobRepository repository) {
    super(Lang.get("seriesGroup.menu.delete"));
    this.seriesGroupKey = seriesGroupKey;
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {
    try {
      repository.startChangeSet();
      for (Glob series : repository.findLinkedTo(repository.get(seriesGroupKey), Series.GROUP)) {
        repository.update(series.getKey(), Series.GROUP, null);
      }
      repository.delete(seriesGroupKey);
    }
    finally {
      repository.completeChangeSet();
    }
  }
}
