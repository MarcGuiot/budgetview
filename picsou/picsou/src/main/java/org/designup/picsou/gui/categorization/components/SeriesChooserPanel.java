package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.gui.accounts.actions.CreateAccountAction;
import org.designup.picsou.gui.categorization.CategorizationSelector;
import org.designup.picsou.gui.categorization.components.messages.DynamicMessage;
import org.designup.picsou.gui.categorization.components.messages.NoSeriesMessageFactory;
import org.designup.picsou.gui.categorization.utils.FilteredRepeats;
import org.designup.picsou.gui.description.SeriesAndGroupsComparator;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.projects.actions.CreateProjectAction;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.SeriesGroup;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.actions.DisabledAction;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class SeriesChooserPanel {
  private final BudgetArea budgetArea;
  private CreateSeriesActionFactory createSeriesActionFactory;
  private final FilteredRepeats seriesRepeats;
  private final CategorizationSelector categorizationSelector;
  private JEditorPane categorizationMessage;
  private final GlobRepository repository;
  private final Directory directory;
  private JPanel panel;

  public SeriesChooserPanel(BudgetArea budgetArea, CreateSeriesActionFactory createSeriesActionFactory,
                            FilteredRepeats seriesRepeats, CategorizationSelector categorizationSelector,
                            GlobRepository repository, Directory directory) {
    this.budgetArea = budgetArea;
    this.createSeriesActionFactory = createSeriesActionFactory;
    this.seriesRepeats = seriesRepeats;
    this.categorizationSelector = categorizationSelector;
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
    GlobsPanelBuilder builder = new GlobsPanelBuilder(SeriesChooserPanel.class,
                                                      "/layout/categorization/seriesChooserPanel.splits",
                                                      repository, directory);

    builder.add("hyperlinkHandler", new HyperlinkHandler(directory));

    builder.add("description", GuiUtils.createReadOnlyHtmlComponent(budgetArea.getHtmlDescription()));

    DynamicMessage noSeriesMessage = NoSeriesMessageFactory.create(budgetArea, repository, directory);
    builder.add("noSeriesMessage", noSeriesMessage.getComponent());

    DescriptionPanelHandler descriptionHandler = new DescriptionPanelHandler(repository);
    builder.add("descriptionPanel", descriptionHandler.getPanel());
    builder.add("showDescription", descriptionHandler.getShowAction());
    builder.add("hideDescription", descriptionHandler.getHideAction());

    SeriesRepeatPanel repeatPanel = new SeriesRepeatPanel(budgetArea, null, seriesRepeats, categorizationSelector.getCurrentTransactions(), repository, directory);
    builder.add("rootSeriesPanel", repeatPanel.getPanel());

    builder.addRepeat("groupRepeat", SeriesGroup.TYPE,
                      fieldEquals(SeriesGroup.BUDGET_AREA, budgetArea.getId()),
                      new SeriesAndGroupsComparator(repository),
                      new GroupComponentFactory());

    categorizationMessage = GuiUtils.createReadOnlyHtmlComponent();
    builder.add("categorizationMessage", categorizationMessage);
    categorizationMessage.setText("");
    categorizationMessage.setVisible(false);

    JPanel groupForSeries = new JPanel();
    builder.add("groupCreateEditSeries", groupForSeries);
    builder.add("createSeries", createSeriesActionFactory.createAction(budgetArea));
    builder.add("additionalAction", getAdditionalAction(budgetArea));

    panel = builder.load();
  }

  private Action getAdditionalAction(BudgetArea budgetArea) {
    if (BudgetArea.EXTRAS.equals(budgetArea)) {
      CreateProjectAction createProject = new CreateProjectAction(repository, directory);
      createProject.setAutoHide();
      return createProject;
    }
    return new DisabledAction();
  }

  private class GroupComponentFactory implements RepeatComponentFactory<Glob> {
    public void registerComponents(PanelBuilder cellBuilder, Glob group) {

      JLabel groupLabel = getGroupLabel(group, cellBuilder);
      cellBuilder.add("groupLabel", groupLabel);

      BudgetArea budgetArea = BudgetArea.get(group.get(SeriesGroup.BUDGET_AREA));
      final SeriesRepeatPanel panel = new SeriesRepeatPanel(budgetArea, group, seriesRepeats, categorizationSelector.getCurrentTransactions(), repository, directory);
      cellBuilder.add("seriesPanel", panel.getPanel());
      panel.addAutoHide(groupLabel);
      cellBuilder.addDisposable(panel);
    }

    private JLabel getGroupLabel(Glob group, PanelBuilder cellBuilder) {
      if (group == null) {
        JLabel emptyLabel = new JLabel();
        emptyLabel.setVisible(false);
        return emptyLabel;
      }

      GlobLabelView labelView = GlobLabelView.init(SeriesGroup.TYPE, repository, directory)
        .forceSelection(group.getKey());
      cellBuilder.addDisposable(labelView);
      return labelView.getComponent();
    }
  }

}
