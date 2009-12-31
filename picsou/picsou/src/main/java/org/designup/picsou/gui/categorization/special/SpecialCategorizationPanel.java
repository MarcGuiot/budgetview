package org.designup.picsou.gui.categorization.special;

import org.designup.picsou.gui.categorization.utils.FilteredRepeats;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public interface SpecialCategorizationPanel {
  String getId();

  JPanel loadPanel(GlobRepository repository,
                   Directory directory,
                   FilteredRepeats filteredRepeats,
                   SeriesEditionDialog seriesEditionDialog);

  boolean canBeHidden();
}