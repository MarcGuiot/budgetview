package com.budgetview.desktop.categorization.components;

import com.budgetview.desktop.categorization.special.DeferredCardCategorizationPanel;
import com.budgetview.desktop.categorization.special.HtmlCategorizationPanel;
import com.budgetview.desktop.categorization.special.ShowHidePanelController;
import com.budgetview.desktop.categorization.special.SpecialCategorizationPanel;
import com.budgetview.desktop.categorization.utils.FilteredRepeats;
import com.budgetview.desktop.categorization.utils.SeriesCreationHandler;
import com.budgetview.desktop.description.Labels;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.components.ShowHideButton;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.FieldValue;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Arrays;

public class OtherSeriesChooserPanel {
  private final CreateSeriesActionFactory createSeriesActionFactory;
  private final FilteredRepeats seriesRepeat;
  private final GlobRepository repository;
  private final Directory directory;
  private JPanel panel;

  public OtherSeriesChooserPanel(CreateSeriesActionFactory createSeriesActionFactory,
                                 FilteredRepeats seriesRepeat,
                                 GlobRepository repository, Directory directory) {
    this.createSeriesActionFactory = createSeriesActionFactory;
    this.seriesRepeat = seriesRepeat;
    this.repository = repository;
    this.directory = directory;
  }

  public JPanel getPanel() {
    if (panel == null) {
      createPanel();
    }
    return panel;
  }

  private void createPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(OtherSeriesChooserPanel.class,
                                                      "/layout/categorization/otherSeriesChooserPanel.splits",
                                                      repository, directory);

    builder.add("description", GuiUtils.createReadOnlyHtmlComponent(Labels.getHtmlDescription(BudgetArea.OTHER)));

    java.util.List<SpecialCategorizationPanel> categorizationPanels = Arrays.asList(
      new DeferredCardCategorizationPanel(),
      new HtmlCategorizationPanel("healthReimbursements"),
      new HtmlCategorizationPanel("loans"),
      new HtmlCategorizationPanel("cash"),
      new HtmlCategorizationPanel("exceptionalIncome")
    );

    builder.addRepeat("specialCategorizationPanels",
                      categorizationPanels,
                      new SpecialCategorizationRepeatFactory());

    panel = builder.load();
  }

  public class SpecialCategorizationRepeatFactory implements RepeatComponentFactory<SpecialCategorizationPanel> {
    public void registerComponents(PanelBuilder cellBuilder, SpecialCategorizationPanel categorizationPanel) {

      JPanel blockPanel = new JPanel();
      blockPanel.setName(categorizationPanel.getId());
      cellBuilder.add("specialCaseBlock", blockPanel);

      SeriesCreationHandler handler = new SeriesCreationHandler() {
        public void createSeries(BudgetArea budgetArea, FieldValue... forcedValues) {
          createSeriesActionFactory.createAction(budgetArea, forcedValues).actionPerformed(null);
        }
      };

      JPanel panel = categorizationPanel.loadPanel(repository, directory, seriesRepeat, handler);
      panel.setVisible(false);
      cellBuilder.add("specialCasePanel", panel);

      String label = Lang.get("categorization.specialCases." + categorizationPanel.getId());

      ShowHideButton showHide = new ShowHideButton(panel, label, label);
      categorizationPanel.registerController(new ShowHidePanelController(showHide));
      cellBuilder.add("showHide", showHide);
    }
  }
}
