package com.budgetview.desktop.categorization;

import com.budgetview.desktop.View;
import com.budgetview.desktop.categorization.components.*;
import com.budgetview.desktop.categorization.utils.FilteredRepeats;
import com.budgetview.desktop.help.HyperlinkHandler;
import com.budgetview.desktop.signpost.guides.CategorizationAreaSignpost;
import com.budgetview.desktop.signpost.sections.SkipCategorizationPanel;
import com.budgetview.desktop.transactions.TransactionDetailsView;
import com.budgetview.model.BudgetArea;
import com.budgetview.model.Transaction;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;

import javax.swing.*;

public class CategorizationView extends View {

  private final CategorizationSelector categorizationSelector;
  private final GlobRepository repository;
  private FilteredRepeats seriesRepeat;
  private final CreateSeriesActionFactory createSeriesActionFactory;

  public CategorizationView(final CategorizationSelector categorizationSelector, GlobRepository repository) {
    super(repository, categorizationSelector.getDirectory());
    this.categorizationSelector = categorizationSelector;
    this.repository = repository;
    this.seriesRepeat = new FilteredRepeats(repository, directory);
    this.createSeriesActionFactory = new CreateSeriesActionFactory(seriesRepeat, categorizationSelector, selectionService, repository, directory);
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

    CategorizationTableView categorizationTableView = categorizationSelector.getTableView();
    CategorizationHandler selector = new CategorizationHandler(categorizationTableView, repository, directory);
    selector.registerComponents(builder);

    addSeriesChooser("incomeSeriesChooser", BudgetArea.INCOME, builder);
    addSeriesChooser("recurringSeriesChooser", BudgetArea.RECURRING, builder);
    addSeriesChooser("variableSeriesChooser", BudgetArea.VARIABLE, builder);
    addSeriesChooser("extrasSeriesChooser", BudgetArea.EXTRAS, builder);
    addSeriesChooser("transferSeriesChooser", BudgetArea.TRANSFER, builder);
    addOtherSeriesChooser("otherSeriesChooser", builder);

    TransactionDetailsView transactionDetailsView =
      new TransactionDetailsView(repository, directory, categorizationSelector, categorizationTableView.getActions());
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
        seriesRepeat.update(categorizationSelector.getCurrentTransactions());
      }
    }, Transaction.TYPE);
  }

  private void addSeriesChooser(String name, BudgetArea budgetArea, GlobsPanelBuilder parentBuilder) {
    SeriesChooserPanel seriesChooserPanel = new SeriesChooserPanel(budgetArea, createSeriesActionFactory, seriesRepeat, categorizationSelector, repository, directory);
    parentBuilder.add(name, seriesChooserPanel.getPanel());

  }

  private void addOtherSeriesChooser(String name, GlobsPanelBuilder parentBuilder) {
    OtherSeriesChooserPanel otherSeriesChooserPanel = new OtherSeriesChooserPanel(createSeriesActionFactory, seriesRepeat, repository, directory);
    parentBuilder.add(name, otherSeriesChooserPanel.getPanel());
  }
}
