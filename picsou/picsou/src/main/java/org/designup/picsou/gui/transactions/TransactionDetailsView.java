package org.designup.picsou.gui.transactions;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.categorization.CategorizationAction;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.gui.description.TransactionDateStringifier;
import org.designup.picsou.gui.transactions.categorization.CategoryChooserAction;
import org.designup.picsou.gui.transactions.columns.TransactionRendererColors;
import org.designup.picsou.gui.transactions.details.CategorisationHyperlinkButton;
import org.designup.picsou.gui.transactions.split.SplitTransactionAction;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobMultiLineTextEditor;
import org.globsframework.gui.splits.components.HyperlinkButton;
import org.globsframework.gui.utils.AutoHideOnSelectionPanel;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.gui.views.GlobMultiLineTextView;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.format.utils.GlobListFieldStringifier;
import org.globsframework.model.format.utils.GlobListStringFieldStringifier;
import org.globsframework.model.utils.GlobListMatcher;
import org.globsframework.model.utils.GlobListMatchers;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public class TransactionDetailsView extends View {
  public TransactionDetailsView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("transactionDetails", createPanelBuilder());
  }

  private GlobsPanelBuilder createPanelBuilder() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(TransactionDetailsView.class, "/layout/transactionDetails.splits",
                            repository, directory);
    builder.add("transactionType",
                addLabel(new GlobListFieldStringifier(Transaction.TRANSACTION_TYPE, "", "") {
                  protected String stringify(Object value) {
                    return Lang.get("transactionType." + TransactionType.getType((Integer)value).getName());
                  }
                }, true));

    builder.add("userLabel",
                GlobMultiLineTextEditor.init(Transaction.LABEL, repository, directory)
                  .setMultiSelectionText(Lang.get("transaction.details.multilabel"))
                  .setEditable(false));

    builder.add("userDate",
                addLabel(new TransactionDateListStringifier(Transaction.MONTH, Transaction.DAY), true));

    builder.add("amountLabel",
                addLabel(GlobListStringifiers.singularOrPlural(Lang.get("transaction.details.amount.none"),
                                                               Lang.get("transaction.details.amount.singular"),
                                                               Lang.get("transaction.details.amount.plural")), true));
    builder.add("amountValue",
                addLabel(GlobListStringifiers.sum(PicsouDescriptionService.DECIMAL_FORMAT, Transaction.AMOUNT), true));

    builder.add("amountPanel",
                new AutoHideOnSelectionPanel(Transaction.TYPE, GlobListMatchers.AT_LEAST_TWO,
                                             repository, directory));

    builder.add("minimumAmount",
                addLabel(GlobListStringifiers.minimum(Transaction.AMOUNT, PicsouDescriptionService.DECIMAL_FORMAT), true));

    builder.add("maximumAmount",
                addLabel(GlobListStringifiers.maximum(Transaction.AMOUNT, PicsouDescriptionService.DECIMAL_FORMAT), true));

    builder.add("averageAmount",
                addLabel(GlobListStringifiers.average(Transaction.AMOUNT, PicsouDescriptionService.DECIMAL_FORMAT), true));

    builder.add("categoryChooserPanel",
                new AutoHideOnSelectionPanel(Transaction.TYPE, GlobListMatchers.AT_LEAST_ONE,
                                             repository, directory));

    CategoryChooserAction categoryChooserAction = new CategoryChooserAction(new TransactionRendererColors(directory), repository, directory);
    HyperlinkButton categoryChooserLink = new CategorisationHyperlinkButton(categoryChooserAction, repository, directory);
    builder.add("categoryChooserLink", categoryChooserLink);

    builder.add("splitLink", new SplitTransactionAction(repository, directory));

    builder.add("originalLabel",
                GlobMultiLineTextView.init(Transaction.TYPE, repository, directory,
                                           new GlobListStringFieldStringifier(Transaction.ORIGINAL_LABEL, "..."))
                  .setAutoHideMatcher(new OriginalLabelVisibilityMatcher()));

    builder.add("bankDate",
                addLabel(new TransactionDateListStringifier(Transaction.BANK_MONTH, Transaction.BANK_DAY), true)
                  .setAutoHideMatcher(new BankDateVisibilityMatcher()));

    builder.add("categorizeLink", new CategorizationAction(repository, directory, directory.get(JFrame.class)));

    builder.add("transactionSeriesName", addLabel(descriptionService.getListStringifier(Transaction.SERIES), true));
    return builder;
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
      return !Utils.equal(transaction.get(Transaction.LABEL),
                          transaction.get(Transaction.ORIGINAL_LABEL));
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

}

