package com.budgetview.gui.categorization.special;

import com.budgetview.gui.categorization.utils.FilteredRepeats;
import com.budgetview.gui.categorization.utils.SeriesCreationHandler;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public interface SpecialCategorizationPanel {
  String getId();

  JPanel loadPanel(GlobRepository repository,
                   Directory directory,
                   FilteredRepeats filteredRepeats,
                   SeriesCreationHandler seriesCreationHandler);

  void registerController(SpecialCategorizationPanelController controller);
}
