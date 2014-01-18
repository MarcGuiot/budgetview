package org.designup.picsou.gui.series.utils;

import org.designup.picsou.gui.seriesgroups.DeleteSeriesGroupAction;
import org.designup.picsou.gui.seriesgroups.SeriesGroupMenu;
import org.globsframework.gui.splits.utils.DisposableGroup;
import org.globsframework.gui.utils.DisposablePopupMenuFactory;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class SeriesGroupPopupFactory implements DisposablePopupMenuFactory {

  private Glob seriesGroup;
  private GlobRepository repository;
  private Directory directory;
  private JPopupMenu menu;
  private DisposableGroup disposables = new DisposableGroup();

  public SeriesGroupPopupFactory(Glob seriesGroup,
                                 GlobRepository repository,
                                 Directory directory) {
    this.seriesGroup = seriesGroup;
    this.repository = repository;
    this.directory = directory;
  }

  public JPopupMenu createPopup() {
    if (menu == null) {
      menu = new JPopupMenu();
      menu.add(new DeleteSeriesGroupAction(seriesGroup.getKey(), repository));
    }
    return menu;
  }

  public void dispose() {
    disposables.dispose();
  }
}
