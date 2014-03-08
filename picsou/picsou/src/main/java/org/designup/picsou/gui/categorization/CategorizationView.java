package org.designup.picsou.gui.categorization;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.accounts.actions.CreateAccountAction;
import org.designup.picsou.gui.categorization.components.*;
import org.designup.picsou.gui.categorization.components.messages.DynamicMessage;
import org.designup.picsou.gui.categorization.components.messages.NoSeriesMessageFactory;
import org.designup.picsou.gui.categorization.reconciliation.ReconciliationWarningPanel;
import org.designup.picsou.gui.categorization.special.*;
import org.designup.picsou.gui.categorization.utils.FilteredRepeats;
import org.designup.picsou.gui.categorization.utils.SeriesCreationHandler;
import org.designup.picsou.gui.components.JPopupButton;
import org.designup.picsou.gui.components.filtering.Filterable;
import org.designup.picsou.gui.components.layoutconfig.SplitPaneConfig;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.projects.actions.CreateProjectAction;
import org.designup.picsou.gui.series.SeriesEditor;
import org.designup.picsou.gui.signpost.Signpost;
import org.designup.picsou.gui.signpost.guides.CategorizationAreaSignpost;
import org.designup.picsou.gui.signpost.guides.GotoBudgetSignpost;
import org.designup.picsou.gui.signpost.sections.SkipCategorizationPanel;
import org.designup.picsou.gui.transactions.TransactionDetailsView;
import org.designup.picsou.gui.transactions.creation.TransactionCreationPanel;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.gui.utils.TableView;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.actions.DisabledAction;
import org.globsframework.gui.components.ShowHideButton;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;

public class CategorizationView extends View implements TableView, Filterable {

  private Directory parentDirectory;

  private GlobList currentTransactions = new GlobList();
  private CategorizationTableView categorizationTableView;
  private FilteredRepeats seriesRepeat;

  private Signpost signpost;
  private CategorizationLevel categorizationLevel;
  private TransactionCreationPanel transactionCreation;

  public CategorizationView(final GlobRepository repository, Directory parentDirectory) {
    super(repository, createLocalDirectory(parentDirectory));
    this.seriesRepeat = new FilteredRepeats(repository, directory);
    this.parentDirectory = parentDirectory;
    parentDirectory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        selectionService.select(selection.getAll(Month.TYPE), Month.TYPE);
        categorizationTableView.updateTableFilter();
      }
    }, Month.TYPE);

    categorizationLevel = new CategorizationLevel(repository, directory);

    this.signpost = new GotoBudgetSignpost(categorizationLevel, repository, parentDirectory);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("categorizationView", createPanelBuilder());
  }

  public void setFilter(GlobMatcher matcher) {
    categorizationTableView.setFilter(matcher);
  }

  public Signpost getGotoBudgetSignpost() {
    return signpost;
  }

  private GlobsPanelBuilder createPanelBuilder() {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/categorization/categorizationView.splits",
                                                      this.repository, directory);

    builder.add("hyperlinkHandler", new HyperlinkHandler(directory));

    CategorizationGaugePanel gauge = new CategorizationGaugePanel(categorizationLevel, repository, parentDirectory);
    builder.add("gaugePanel", gauge.getPanel());

    categorizationTableView = new CategorizationTableView(currentTransactions, repository, directory);
    categorizationTableView.registerComponents(builder);

    transactionCreation = new TransactionCreationPanel(repository, directory, parentDirectory);
    transactionCreation.registerComponents(builder);

    builder.add("actionsMenu", new JPopupButton(Lang.get("budgetView.actions"),
                                                new CategorizationTablePopupFactory(categorizationTableView.getTable(),
                                                                                    transactionCreation,
                                                                                    categorizationTableView.getActions(),
                                                                                    repository, directory)));

    builder.addLabel("transactionLabel", Transaction.TYPE, new GlobListStringifier() {
      public String toString(GlobList list, GlobRepository repository) {
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

    CategorizationSelector selector = new CategorizationSelector(categorizationTableView.getToReconcileMatcher(),
                                                                 categorizationTableView.getColors(),
                                                                 repository, directory);
    selector.registerComponents(builder);

    addSeriesChooser("incomeSeriesChooser", BudgetArea.INCOME, builder);
    addSeriesChooser("recurringSeriesChooser", BudgetArea.RECURRING, builder);
    addSeriesChooser("variableSeriesChooser", BudgetArea.VARIABLE, builder);
    addSeriesChooser("extrasSeriesChooser", BudgetArea.EXTRAS, builder);
    addSeriesChooser("savingsSeriesChooser", BudgetArea.SAVINGS, builder);
    addOtherSeriesChooser("otherSeriesChooser", builder);

    TransactionDetailsView transactionDetailsView =
      new TransactionDetailsView(repository, directory, this, categorizationTableView.getActions());
    transactionDetailsView.registerComponents(builder);

    initSelectionListener();
    categorizationTableView.updateTableFilter();

    JPanel budgetAreaSelectionPanel = new JPanel();
    builder.add("budgetAreaSelectionPanel", budgetAreaSelectionPanel);
    CategorizationAreaSignpost areaSignpost = new CategorizationAreaSignpost(repository, directory);
    areaSignpost.attach(budgetAreaSelectionPanel);

    ReconciliationWarningPanel reconciliationWarningPanel =
      new ReconciliationWarningPanel(this, repository, directory);
    builder.add("reconciliationWarningPanel", reconciliationWarningPanel.getPanel());

    builder.add("categorizationSplit", SplitPaneConfig.create(directory, LayoutConfig.CATEGORIZATION_HORIZONTAL));

    return builder;
  }

  public void addTableListener(TableModelListener listener) {
    categorizationTableView.addTableListener(listener);
  }

  public GlobList getDisplayedGlobs() {
    return categorizationTableView.getDisplayedGlobs();
  }

  private void initSelectionListener() {
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        currentTransactions.clear();
        currentTransactions.addAll(selection.getAll(Transaction.TYPE));
        Set<Integer> months = currentTransactions.getValueSet(Transaction.BUDGET_MONTH);
        seriesRepeat.update(months, currentTransactions);
        categorizationTableView.updateSelection();
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

    seriesRepeat.addRepeat(budgetArea, builder, Matchers.seriesCategorizationFilter(budgetArea.getId()));

    JPanel groupForSeries = new JPanel();
    builder.add("groupCreateEditSeries", groupForSeries);
    builder.add("createSeries", new CreateSeriesAction(budgetArea));
    builder.add("additionalAction", getAdditionalAction(budgetArea));

    parentBuilder.add(name, builder);
  }

  private Action getAdditionalAction(BudgetArea budgetArea) {
    if (BudgetArea.SAVINGS.equals(budgetArea)) {
      CreateAccountAction createAccountAction = new CreateAccountAction(AccountType.SAVINGS, repository, directory);
      createAccountAction.setAccountTypeEditable(false);
      return createAccountAction;
    }
    if (BudgetArea.EXTRAS.equals(budgetArea)) {
      return new CreateProjectAction(directory);
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

  public void highlightTransactionCreation() {
    transactionCreation.showTip();
  }

  public void reset() {
    categorizationTableView.reset();
  }

  public void setFilteringMode(CategorizationFilteringMode mode) {
    categorizationTableView.setFilteringMode(mode);
  }

  public class SpecialCategorizationRepeatFactory implements RepeatComponentFactory<SpecialCategorizationPanel> {
    public void registerComponents(RepeatCellBuilder cellBuilder, SpecialCategorizationPanel categorizationPanel) {

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

  private static Directory createLocalDirectory(Directory directory) {
    Directory localDirectory = new DefaultDirectory(directory);
    SelectionService selectionService = new SelectionService();
    localDirectory.add(selectionService);
    return localDirectory;
  }

  public void show(GlobList transactions, boolean forceShowUncategorized) {
    categorizationTableView.show(transactions, forceShowUncategorized);
  }

  public void showWithMode(CategorizationFilteringMode filteringMode) {
    categorizationTableView.showWithMode(filteringMode);
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
      Key key = SeriesEditor.get(directory).showNewSeries(currentTransactions,
                                                          selectionService.getSelection(Month.TYPE),
                                                          budgetArea,
                                                          forcedValues);
      Glob series = repository.find(key);
      if (key != null && series != null) {
        try {
          repository.startChangeSet();
          for (Glob transaction : currentTransactions) {
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
      repository.update(transaction.getKey(),
                        value(Transaction.SERIES, series.get(Series.ID)),
                        value(Transaction.SUB_SERIES, subSeriesId),
                        value(Transaction.RECONCILIATION_ANNOTATION_SET, !Transaction.isManuallyCreated(transaction)));
      return true;
    }
  }
}
