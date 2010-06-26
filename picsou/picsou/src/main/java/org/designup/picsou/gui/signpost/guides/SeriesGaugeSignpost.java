package org.designup.picsou.gui.signpost.guides;

import org.designup.picsou.gui.signpost.SimpleSignpost;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SeriesGaugeSignpost extends SimpleSignpost {
  public SeriesGaugeSignpost(GlobRepository repository, Directory directory) {
    super(Lang.get("signpost.gaugeDescriptions"),
          SignpostStatus.SERIES_GAUGE_SHOWN,
          SignpostStatus.CATEGORIZATION_SELECTION_SHOWN,
          repository, directory);
  }
}