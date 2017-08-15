package com.budgetview.desktop.categorization.special;

import com.budgetview.desktop.actions.ImportFileAction;
import com.budgetview.desktop.categorization.CategorizationView;
import com.budgetview.desktop.categorization.utils.CategorizationMatchers;
import com.budgetview.desktop.categorization.utils.FilteredRepeats;
import com.budgetview.desktop.categorization.utils.SeriesCreationHandler;
import com.budgetview.desktop.help.HyperlinkHandler;
import com.budgetview.desktop.series.utils.SeriesMatchers;
import com.budgetview.model.Series;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.Functor;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class DeferredCardCategorizationPanel implements SpecialCategorizationPanel {
  private SpecialCategorizationPanelController controller;
  private GlobRepository repository;
  private JEditorPane message;
  private FilteredRepeats.Handler repeatHandler;
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

    repeatHandler = filteredRepeats.addRepeat(BudgetArea.OTHER, panelBuilder,
                                              CategorizationMatchers.deferredCardCategorizationFilter());
    repeatHandler.addListener(new Functor() {
      public void run() throws Exception {
        updateDisplay();
      }
    });

    return panelBuilder.load();
  }

  private void registerUpdater() {
    repository.addChangeListener(new TypeChangeSetListener(Series.TYPE) {
      public void update(GlobRepository repository) {
        updateDisplay();
      }
    });
  }

  public void registerController(SpecialCategorizationPanelController controller) {
    this.controller = controller;
    updateDisplay();
  }

  private void updateDisplay() {
    if (repository.contains(Series.TYPE, SeriesMatchers.deferredCardSeries())) {
      if (repeatHandler.isEmpty()) {
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
