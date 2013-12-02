package org.designup.picsou.gui.categorization.special;

import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.categorization.CategorizationView;
import org.designup.picsou.gui.categorization.components.SeriesChooserComponentFactory;
import org.designup.picsou.gui.categorization.utils.FilteredRepeats;
import org.designup.picsou.gui.categorization.utils.SeriesCreationHandler;
import org.designup.picsou.gui.description.stringifiers.SeriesNameComparator;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.components.GlobRepeat;
import org.globsframework.gui.components.GlobRepeatListener;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class DeferredCardCategorizationPanel implements SpecialCategorizationPanel {
  private SpecialCategorizationPanelController controller;
  private GlobRepository repository;
  private Directory directory;
  private JEditorPane message;
  private GlobRepeat repeat;
  private String currentMsg = null;

  public DeferredCardCategorizationPanel() {
  }

  public String getId() {
    return "deferredCard";
  }

  public JPanel loadPanel(final GlobRepository repository,
                          final Directory directory,
                          FilteredRepeats filteredRepeats,
                          SeriesCreationHandler seriesCreationHandler) {
    this.repository = repository;

    registerUpdater();

    BudgetArea budgetArea = BudgetArea.OTHER;

    GlobsPanelBuilder panelBuilder =
      new GlobsPanelBuilder(CategorizationView.class,
                            "/layout/categorization/specialCategorizationPanels/deferredCardCategorizationPanel.splits",
                            repository, directory);

    HyperlinkHandler handler = new HyperlinkHandler(directory);
    handler.registerLinkAction("importDeferredCardAccount", new Runnable() {
      public void run() {
        ImportFileAction action = ImportFileAction.init("", repository, directory, null);
        action.actionPerformed(null);
      }
    });
    panelBuilder.add("hyperlinkHandler", handler);

    message = GuiUtils.createReadOnlyHtmlComponent();
    panelBuilder.add("message", message);

    JRadioButton invisibleRadio = new JRadioButton("noDeferredCard");
    panelBuilder.add("invisibleToggle", invisibleRadio);

    Matchers.CategorizationFilter filter = Matchers.deferredCardCategorizationFilter();
    repeat = panelBuilder.addRepeat("seriesRepeat",
                                    Series.TYPE,
                                    filter,
                                    SeriesNameComparator.INSTANCE,
                                    new SeriesChooserComponentFactory(budgetArea, invisibleRadio,
                                                                      repository,
                                                                      directory));
    filteredRepeats.add(filter, repeat);

    repeat.addListener(new GlobRepeatListener() {
      public void listChanged(GlobList currentList) {
        updateDisplay();
      }
    });

    return panelBuilder.load();
  }

  private void registerUpdater() {
    repository.addChangeListener(new TypeChangeSetListener(Series.TYPE) {
      protected void update(GlobRepository repository) {
        updateDisplay();
      }
    });
  }

  public void registerController(SpecialCategorizationPanelController controller) {
    this.controller = controller;
    updateDisplay();
  }

  private void updateDisplay() {
    if (repository.contains(Series.TYPE, Matchers.deferredCardSeries())) {
      if (repeat.isEmpty()) {
        updateMessage("categorization.specialCases.deferredCard.invalidSelection");
      }
      else {
        controller.setShown(true);
        updateMessage("categorization.specialCases.deferredCard.select");
      }
    }
    else {
      updateMessage("categorization.specialCases.deferredCard.noaccount");
    }
  }

  private void updateMessage(String key) {
    if (!key.equals(currentMsg)) {
      currentMsg = key;
      message.setText(Lang.get(key));
      GuiUtils.revalidate(message);
    }
  }
}
