package org.designup.picsou.gui.signpost.guides;

import org.designup.picsou.gui.signpost.SimpleSignpost;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class SeriesAmountSignpost extends SimpleSignpost {
  public SeriesAmountSignpost(GlobRepository repository, Directory directory) {
    super(Lang.get("signpost.seriesAmount"),
          SignpostStatus.SERIES_AMOUNT_SHOWN,
          SignpostStatus.CATEGORIZATION_SELECTION_SHOWN,
          repository, directory);
  }
}