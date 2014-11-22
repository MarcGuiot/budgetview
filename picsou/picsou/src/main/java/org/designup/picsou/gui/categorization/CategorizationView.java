package org.designup.picsou.gui.categorization;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.accounts.actions.CreateAccountAction;
import org.designup.picsou.gui.categorization.components.*;
import org.designup.picsou.gui.categorization.components.messages.DynamicMessage;
import org.designup.picsou.gui.categorization.components.messages.NoSeriesMessageFactory;
import org.designup.picsou.gui.categorization.special.*;
import org.designup.picsou.gui.categorization.utils.FilteredRepeats;
import org.designup.picsou.gui.categorization.utils.SeriesCreationHandler;
import org.designup.picsou.gui.description.SeriesAndGroupsComparator;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.projects.actions.CreateProjectAction;
import org.designup.picsou.gui.series.SeriesEditor;
import org.designup.picsou.gui.signpost.guides.CategorizationAreaSignpost;
import org.designup.picsou.gui.signpost.sections.SkipCategorizationPanel;
import org.designup.picsou.gui.transactions.TransactionDetailsView;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.actions.DisabledAction;
import org.globsframework.gui.components.ShowHideButton;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobListStringifier;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class CategorizationView extends View {

  private final CategorizationSelectionView selectionView;
  private final GlobRepository repository;
  private FilteredRepeats seriesRepeat;

  public CategorizationView(final CategorizationSelectionView selectionView, GlobRepository repository) {
    super(repository, selectionView.getDirectory());
    this.selectionView = selectionView;
    this.repository = repository;
    this.seriesRepeat = new FilteredRepeats(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/categorization/categorizationView.splits",
                                                      this.repository, directory);

    builder.add("hyperlinkHandler", new HyperlinkHandler(directory));

    builder.addLabel("transactionLabel", Transaction.TYPE, new GlobListStringifier() {
      public String toString(GlobList list, GlobRepository repository1) {
        if (list.isEmpty()) {
          return null;
        }
        if (list.size() > 1) {
          return Lang.get("categorization.many.transactions.label", Integer.toString(list.size()));
        }
        return list.get(0).get(Transaction.LABEL);
      }
    }).setAutoHideIfEmpty(true);

    SkipCategorizationPanel skipPanel = new SkipCategorizationPanel(repository, directory);
    builder.add("skipCategorizationPanel", skipPanel.getPanel());

    CategorizationTableView categorizationTableView = selectionView.getTableView();
    CategorizationSelector selector = new CategorizationSelector(categorizationTableView.getToReconcileMatcher(),
                                                                 categorizationTableView.getColors(),
                                                                 repository, directory);
    selector.registerComponents(builder);

    addSeriesChooser("incomeSeriesChooser", BudgetArea.INCOME, builder);
    addSeriesChooser("recurringSeriesChooser", BudgetArea.RECURRING, builder);
    addSeriesChooser("variableSeriesChooser", BudgetArea.VARIABLE, builder);
    addSeriesChooser("extrasSeriesChooser", BudgetArea.EXTRAS, builder);
    addSeriesChooser("transferSeriesChooser", BudgetArea.TRANSFER, builder);
    addOtherSeriesChooser("otherSeriesChooser", builder);

    TransactionDetailsView transactionDetailsView =
      new TransactionDetailsView(repository, directory, selectionView, categorizationTableView.getActions());
    transactionDetailsView.registerComponents(builder);

    initSelectionListener();

    JPanel budgetAreaSelectionPanel = new JPanel();
    builder.add("budgetAreaSelectionPanel", budgetAreaSelectionPanel);
    CategorizationAreaSignpost areaSignpost = new CategorizationAreaSignpost(repository, directory);
    areaSignpost.attach(budgetAreaSelectionPanel);

    parentBuilder.add("categorizationView", builder);
  }

  private void initSelectionListener() {
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        seriesRepeat.update(selectionView.getCurrentTransactions());
      }
    }, Transaction.TYPE);
  }

  private void addSeriesChooser(String name, BudgetArea budgetArea, GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(CategorizationView.class,
                                                      "/layout/categorization/seriesChooserPanel.splits",
                                                      repository, directory);

    builder.add("hyperlinkHandler", new HyperlinkHandler(directory));

    builder.add("description", GuiUtils.createReadOnlyHtmlComponent(budgetArea.getHtmlDescription()));

    DynamicMessage noSeriesMessage = NoSeriesMessageFactory.create(budgetArea, repository, directory);
    builder.add("noSeriesMessage", noSeriesMessage.getComponent());

    DynamicMessage categorizationMessage = CategorizationMessageFactory.create(budgetArea, repository, directory);
    builder.add("categorizationMessage", categorizationMessage.getComponent());

    DescriptionPanelHandler descriptionHandler = new DescriptionPanelHandler(repository);
    builder.add("descriptionPanel", descriptionHandler.getPanel());
    builder.add("showDescription", descriptionHandler.getShowAction());
    builder.add("hideDescription", descriptionHandler.getHideAction());

    SeriesRepeatPanel repeatPanel = new SeriesRepeatPanel(budgetArea, null, seriesRepeat, selectionView.getCurrentTransactions(), repository, directory);
    builder.add("rootSeriesPanel", repeatPanel.getPanel());

    builder.addRepeat("groupRepeat", SeriesGroup.TYPE,
                      fieldEquals(SeriesGroup.BUDGET_AREA, budgetArea.getId()),
                      new SeriesAndGroupsComparator(repository),
                      new GroupComponentFactory());

    JPanel groupForSeries = new JPanel();
    builder.add("groupCreateEditSeries", groupForSeries);
    builder.add("createSeries", new CreateSeriesAction(budgetArea));
    builder.add("additionalAction", getAdditionalAction(budgetArea));

    parentBuilder.add(name, builder);
  }

  private Action getAdditionalAction(BudgetArea budgetArea) {
    if (BudgetArea.TRANSFER.equals(budgetArea)) {
      CreateAccountAction createAccountAction = new CreateAccountAction(AccountType.SAVINGS, repository, directory);
      createAccountAction.setAccountTypeEditable(false);
      return createAccountAction;
    }
    if (BudgetArea.EXTRAS.equals(budgetArea)) {
      return new CreateProjectAction(repository, directory);
    }
    return new DisabledAction();
  }

  private void addOtherSeriesChooser(String name, GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(CategorizationView.class,
                                                      "/layout/categorization/otherSeriesChooserPanel.splits",
                                                      repository, directory);

    builder.add("description", GuiUtils.createReadOnlyHtmlComponent(BudgetArea.OTHER.getHtmlDescription()));

    java.util.List<SpecialCategorizationPanel> categorizationPanels = Arrays.asList(
      new DeferredCardCategorizationPanel(),
      new InternalTransfersCategorizationPanel(),
      new HtmlCategorizationPanel("healthReimbursements"),
      new HtmlCategorizationPanel("loans"),
      new HtmlCategorizationPanel("cash"),
      new HtmlCategorizationPanel("exceptionalIncome")
    );

    builder.addRepeat("specialCategorizationPanels",
                      categorizationPanels,
                      new SpecialCategorizationRepeatFactory());

    parentBuilder.add(name, builder);
  }

  public class SpecialCategorizationRepeatFactory implements RepeatComponentFactory<SpecialCategorizationPanel> {
    public void registerComponents(PanelBuilder cellBuilder, SpecialCategorizationPanel categorizationPanel) {

      JPanel blockPanel = new JPanel();
      blockPanel.setName(categorizationPanel.getId());
      cellBuilder.add("specialCaseBlock", blockPanel);

      SeriesCreationHandler handler = new SeriesCreationHandler() {
        public void createSeries(BudgetArea budgetArea, FieldValue... forcedValues) {
          CreateSeriesAction action = new CreateSeriesAction(budgetArea, forcedValues);
          action.actionPerformed(null);
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

  private class CreateSeriesAction extends AbstractAction {
    private final BudgetArea budgetArea;
    private FieldValue[] forcedValues;

    public CreateSeriesAction(BudgetArea budgetArea, FieldValue... forcedValues) {
      super(Lang.get("categorization.series.add"));
      this.budgetArea = budgetArea;
      this.forcedValues = forcedValues;
    }

    public void actionPerformed(ActionEvent e) {
      Key key = SeriesEditor.get(directory).showNewSeries(selectionView.getCurrentTransactions(),
                                                          selectionService.getSelection(Month.TYPE),
                                                          budgetArea,
                                                          forcedValues);
      Glob series = repository.find(key);
      if (key != null && series != null) {
        try {
          repository.startChangeSet();
          for (Glob transaction : selectionView.getCurrentTransactions()) {
            if (!categorize(series, transaction)) {
              Glob mirrorSeries = repository.findLinkTarget(series, Series.MIRROR_SERIES);
              if (mirrorSeries != null) {
                categorize(mirrorSeries, transaction);
              }
            }
          }
        }
        finally {
          repository.completeChangeSet();
        }
      }
    }

    private boolean categorize(Glob series, final Glob transaction) {
      boolean matchFound = seriesRepeat.updateAndCheckMatch(series, transaction);
      if (matchFound) {
        return false;
      }
      Integer subSeriesId = SeriesEditor.get(directory).getLastSelectedSubSeriesId();
      if (series.get(Series.TARGET_ACCOUNT) == null) {
        SeriesChooserComponentFactory.updateTargetSeries(transaction, series.getKey(), repository);
      }
      repository.update(transaction.getKey(),
                        value(Transaction.SERIES, series.get(Series.ID)),
                        value(Transaction.SUB_SERIES, subSeriesId),
                        value(Transaction.RECONCILIATION_ANNOTATION_SET, !Transaction.isManuallyCreated(transaction)));
      return true;
    }
  }

  private class GroupComponentFactory implements RepeatComponentFactory<Glob> {
    public void registerComponents(PanelBuilder cellBuilder, Glob group) {

      JLabel groupLabel = getGroupLabel(group, cellBuilder);
      cellBuilder.add("groupLabel", groupLabel);

      BudgetArea budgetArea = BudgetArea.get(group.get(SeriesGroup.BUDGET_AREA));
      final SeriesRepeatPanel panel = new SeriesRepeatPanel(budgetArea, group, seriesRepeat, selectionView.getCurrentTransactions(), repository, directory);
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
