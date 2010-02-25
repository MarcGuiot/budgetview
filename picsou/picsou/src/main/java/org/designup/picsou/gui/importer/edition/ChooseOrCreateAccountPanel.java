package org.designup.picsou.gui.importer.edition;

import org.designup.picsou.model.Account;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class ChooseOrCreateAccountPanel {
  private GlobRepository repository;
  private Directory directory;
  private JPanel panel;
  private GlobsPanelBuilder builder;
  private GlobList accountToAccounts = new GlobList();

  public ChooseOrCreateAccountPanel(final GlobRepository repository, final Directory directory,
                                    GlobList newAccounts) {
  }

  public JPanel getPanel() {
    return panel;
  }

  public void dispose() {
    builder.dispose();
  }
}
