package org.designup.picsou.gui.signpost.guides;

import org.designup.picsou.gui.signpost.SimpleSignpost;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class SeriesPeriodicitySignpost extends SimpleSignpost {
  public SeriesPeriodicitySignpost(GlobRepository repository, Directory directory) {
    super(Lang.get("signpost.seriesPeriodicity"),
          SignpostStatus.SERIES_PERIODICITY_SHOWN,
          SignpostStatus.CATEGORIZATION_SELECTION_SHOWN,
          repository, directory);
  }
}
