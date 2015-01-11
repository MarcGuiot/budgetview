package org.designup.picsou.gui.categorization;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.categorization.components.*;
import org.designup.picsou.gui.categorization.reconciliation.ReconciliationWarningPanel;
import org.designup.picsou.gui.components.JPopupButton;
import org.designup.picsou.gui.components.filtering.Filterable;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.signpost.PersistentSignpost;
import org.designup.picsou.gui.signpost.guides.GotoBudgetSignpost;
import org.designup.picsou.gui.signpost.sections.SkipCategorizationPanel;
import org.designup.picsou.gui.transactions.creation.TransactionCreationPanel;
import org.designup.picsou.gui.utils.TableView;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.event.TableModelListener;

public class CategorizationSelector extends View implements TableView, Filterable {
  private Directory parentDirectory;

  private GlobList currentTransactions = new GlobList();
  private CategorizationTableView categorizationTableView;

  private PersistentSignpost signpost;
  private CategorizationLevel categorizationLevel;
  private TransactionCreationPanel transactionCreation;

  public CategorizationSelector(final GlobRepository repository, Directory parentDirectory) {
    super(repository, createLocalDirectory(parentDirectory));
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

  public PersistentSignpost getGotoBudgetSignpost() {
    return signpost;
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/categorization/categorizationSelector.splits",
                                                      repository, directory);

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

    initSelectionListener();
    categorizationTableView.updateTableFilter();

    ReconciliationWarningPanel reconciliationWarningPanel =
      new ReconciliationWarningPanel(this, repository, directory);
    builder.add("reconciliationWarningPanel", reconciliationWarningPanel.getPanel());

    parentBuilder.add("categorizationSelector", builder);
  }

  public void setFilter(GlobMatcher matcher) {
    categorizationTableView.setFilter(matcher);
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
        categorizationTableView.updateSelection();
      }
    }, Transaction.TYPE);
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

  public Directory getDirectory() {
    return directory;
  }

  public CategorizationTableView getTableView() {
    return categorizationTableView;
  }

  public GlobList getCurrentTransactions() {
    return currentTransactions;
  }
}
