package org.designup.picsou.gui.transactions;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.description.TransactionDateStringifier;
import org.designup.picsou.gui.transactions.split.SplitTransactionAction;
import org.designup.picsou.gui.transactions.shift.ShiftTransactionAction;
import org.designup.picsou.gui.utils.TableView;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.utils.AutoHideOnSelectionPanel;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.gui.views.GlobMultiLineTextView;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
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
  private CardHandler cards;

  public TransactionDetailsView(GlobRepository repository, Directory directory, TableView tableView) {
    super(repository, directory);
    this.tableView = tableView;
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("transactionDetails", createPanelBuilder());
  }

  private GlobsPanelBuilder createPanelBuilder() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/transactionDetails.splits", repository, directory);

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

    builder.add("transactionType",
                addLabel(new GlobListFieldStringifier(Transaction.TRANSACTION_TYPE, "", "") {
                  protected String stringify(Object value) {
                    return Lang.get("transactionType." + TransactionType.getType((Integer)value).getName());
                  }
                }, true));

    builder.add("userLabel",
                GlobMultiLineTextView.init(Transaction.TYPE, repository, directory, new UserLabelStringifier())
                  .setAutoHideIfEmpty(true));

    builder.add("bankDate",
                addLabel(new TransactionDateListStringifier(Transaction.BANK_MONTH, Transaction.BANK_DAY), true)
                  .setAutoHideMatcher(new BankDateVisibilityMatcher()));

    builder.addEditor("noteField", Transaction.NOTE);

    builder.add("splitPanel",
                new AutoHideOnSelectionPanel(Transaction.TYPE, GlobListMatchers.AT_LEAST_ONE,
                                             repository, directory));

    builder.add("shift", new ShiftTransactionAction(repository, directory));

    builder.add("split", new SplitTransactionAction(repository, directory));

    builder.add("originalLabel",
                GlobMultiLineTextView.init(Transaction.TYPE, repository, directory,
                                           new GlobListStringFieldStringifier(Transaction.ORIGINAL_LABEL, "..."))
                  .setAutoHideMatcher(new OriginalLabelVisibilityMatcher()));

    builder.add("hyperlinkHandler", new HyperlinkHandler(directory));

    return builder;
  }

  private void updateCard() {
    GlobList transactions = selectionService.getSelection(Transaction.TYPE);
    if (transactions.isEmpty()) {
      if (tableView.getDisplayedGlobs().isEmpty()) {
        cards.show("noData");
      }
      else {
        cards.show("noSelection");
      }
    }
    else {
      cards.show("selection");
    }

  }

  private GlobLabelView addLabel(GlobListStringifier stringifier, boolean autoHide) {
    return GlobLabelView.init(Transaction.TYPE, repository, directory, stringifier)
      .setAutoHideIfEmpty(autoHide);
  }

  private static class TransactionDateListStringifier implements GlobListStringifier {
    private IntegerField month;
    private IntegerField day;

    private TransactionDateListStringifier(IntegerField month, IntegerField day) {
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
        return TransactionDateStringifier.toString(Month.toYear(monthId),
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

  private static class BankDateVisibilityMatcher implements GlobListMatcher {
    public boolean matches(GlobList list, GlobRepository repository) {
      if (list.size() != 1) {
        return false;
      }
      Glob transaction = list.get(0);
      return !Utils.equal(transaction.get(Transaction.DAY), transaction.get(Transaction.BANK_DAY)) ||
             !Utils.equal(transaction.get(Transaction.MONTH), transaction.get(Transaction.BANK_MONTH));
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
}

