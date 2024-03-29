package com.budgetview.desktop.transactions;

import com.budgetview.desktop.View;
import com.budgetview.desktop.categorization.actions.CategorizationTableActions;
import com.budgetview.desktop.components.JPopupButton;
import com.budgetview.desktop.description.Formatting;
import com.budgetview.desktop.help.HyperlinkHandler;
import com.budgetview.desktop.utils.TableView;
import com.budgetview.model.Month;
import com.budgetview.model.Transaction;
import com.budgetview.model.TransactionType;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.components.AutoHideOnSelectionPanel;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.views.GlobHtmlView;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.utils.CompositeGlobListStringifier;
import org.globsframework.model.format.utils.GlobListFieldStringifier;
import org.globsframework.model.format.utils.GlobListStringFieldStringifier;
import org.globsframework.model.utils.GlobListMatcher;
import org.globsframework.model.utils.GlobListMatchers;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.util.HashSet;
import java.util.Set;

public class TransactionDetailsView extends View {
  private TableView tableView;
  private CategorizationTableActions actions;
  private CardHandler cards;

  public TransactionDetailsView(GlobRepository repository, Directory directory,
                                TableView tableView,
                                CategorizationTableActions actions) {
    super(repository, directory);
    this.tableView = tableView;
    this.actions = actions;
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("transactionDetails", createPanelBuilder());
  }

  private GlobsPanelBuilder createPanelBuilder() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/categorization/transactionDetails.splits", repository, directory);

    cards = builder.addCardHandler("cards");

    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        updateCard();
      }
    }, Transaction.TYPE);

    tableView.addTableListener(new TableModelListener() {
      public void tableChanged(TableModelEvent e) {
        updateCard();
      }
    });

    builder.add("userLabel",
                GlobLabelView.init(Transaction.TYPE, repository, directory, new UserLabelStringifier())
                  .setAutoHideIfEmpty(true));

    CompositeGlobListStringifier detailsStringifier = new CompositeGlobListStringifier(" - ");
    detailsStringifier.add(new TransactionTypeStringifier());
    detailsStringifier.add(descriptionService.getListStringifier(Transaction.ACCOUNT,
                                                                 "", Lang.get("transaction.details.account.multi")));
    detailsStringifier.add(new DateVisibilityMatcher(Transaction.POSITION_DAY, Transaction.POSITION_MONTH),
                           new TransactionDateListStringifier("transaction.details.bankDate",
                                                              Transaction.POSITION_MONTH, Transaction.POSITION_DAY));
    detailsStringifier.add(new DateVisibilityMatcher(Transaction.BUDGET_DAY, Transaction.BUDGET_MONTH),
                           new TransactionDateListStringifier("transaction.details.budgetDate", Transaction.BUDGET_MONTH, Transaction.BUDGET_DAY));

    builder.add("details", addLabel(detailsStringifier, true));

    builder.addEditor("noteField", Transaction.NOTE);

    builder.add("splitPanel",
                new AutoHideOnSelectionPanel(Transaction.TYPE, GlobListMatchers.AT_LEAST_ONE,
                                             repository, directory));

    builder.add("originalLabel",
                GlobHtmlView.init(Transaction.TYPE, repository, directory,
                                  new GlobListStringFieldStringifier(Transaction.ORIGINAL_LABEL, "..."))
                  .setAutoHideMatcher(new OriginalLabelVisibilityMatcher()));

    builder.add("hyperlinkHandler", new HyperlinkHandler(directory));

    builder.add("transactionActions",
                new JPopupButton(Lang.get("transaction.details.actions"), actions.createEditPopup()));

    return builder;
  }

  private void updateCard() {
    GlobList transactions = selectionService.getSelection(Transaction.TYPE);
    cards.show(transactions.isEmpty() ? "nothingShown" : "selection");
  }

  private GlobLabelView addLabel(GlobListStringifier stringifier, boolean autoHide) {
    return GlobLabelView.init(Transaction.TYPE, repository, directory, stringifier)
      .setAutoHideIfEmpty(autoHide);
  }

  private static class TransactionDateListStringifier implements GlobListStringifier {
    private String prefix;
    private IntegerField month;
    private IntegerField day;

    private TransactionDateListStringifier(String prefixKey, IntegerField month, IntegerField day) {
      this.prefix = Lang.get(prefixKey) + " ";
      this.month = month;
      this.day = day;
    }

    public String toString(GlobList selected, GlobRepository repository) {
      if (selected.isEmpty()) {
        return "";
      }
      Set<Integer> values = new HashSet<Integer>();
      for (Glob transaction : selected) {
        values.add(Month.toInt(transaction.get(month), transaction.get(day)));
      }
      if (values.size() == 1) {
        int value = values.iterator().next();
        int monthId = Month.intToMonthId(value);
        return prefix + Formatting.toString(Month.toYear(monthId),
                                            Month.toMonth(monthId),
                                            Month.intToDay(value));
      }
      return "";
    }
  }

  private static class OriginalLabelVisibilityMatcher implements GlobListMatcher {
    public boolean matches(GlobList list, GlobRepository repository) {
      if (list.size() != 1) {
        return false;
      }
      Glob transaction = list.get(0);
      return Strings.isNotEmpty(transaction.get(Transaction.ORIGINAL_LABEL)) &&
             !Utils.equal(transaction.get(Transaction.LABEL), transaction.get(Transaction.ORIGINAL_LABEL));
    }
  }

  private static class DateVisibilityMatcher implements GlobListMatcher {
    private IntegerField dayField;
    private LinkField monthField;

    private DateVisibilityMatcher(final IntegerField dayField, final LinkField monthField) {
      this.dayField = dayField;
      this.monthField = monthField;
    }

    public boolean matches(GlobList list, GlobRepository repository) {
      if (list.size() != 1) {
        return false;
      }
      Glob transaction = list.get(0);
      return !Utils.equal(transaction.get(Transaction.DAY), transaction.get(dayField)) ||
             !Utils.equal(transaction.get(Transaction.MONTH), transaction.get(monthField));
    }
  }

  private static class UserLabelStringifier implements GlobListStringifier {
    public String toString(GlobList list, GlobRepository repository) {
      if (list.isEmpty()) {
        return "";
      }

      if (list.size() == 1) {
        return list.getFirst().get(Transaction.LABEL);
      }

      Set<String> names = list.getValueSet(Transaction.LABEL);
      if (names.size() > 1) {

        Set<String> anonymizedNames = list.getValueSet(Transaction.LABEL_FOR_CATEGORISATION);
        if (anonymizedNames.size() == 1) {
          String firstName = anonymizedNames.iterator().next();
          if (!Strings.isNullOrEmpty(firstName)) {
            return Lang.get("transaction.details.multilabel.similar", firstName, list.size());
          }
          else {
            return Lang.get("transaction.details.multilabel.different", list.size());

          }
        }

        return Lang.get("transaction.details.multilabel.different", list.size());
      }

      String firstName = names.iterator().next();
      return Lang.get("transaction.details.multilabel.similar", firstName, list.size());
    }
  }

  private static class TransactionTypeStringifier extends GlobListFieldStringifier {
    public TransactionTypeStringifier() {
      super(Transaction.TRANSACTION_TYPE, "", "");
    }

    protected String stringify(Object value) {
      return Lang.get("transactionType." + TransactionType.getType((Integer)value).getName());
    }
  }
}

