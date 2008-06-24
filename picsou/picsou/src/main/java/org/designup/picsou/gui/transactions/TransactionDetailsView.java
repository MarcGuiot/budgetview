package org.designup.picsou.gui.transactions;

import org.designup.picsou.gui.View;
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
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.components.HyperlinkButton;
import org.globsframework.gui.utils.AutoHideOnSelectionPanel;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.format.utils.GlobListFieldStringifier;
import org.globsframework.model.format.utils.GlobListStringFieldStringifier;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransactionDetailsView extends View implements GlobSelectionListener, ChangeSetListener {
  public TransactionDetailsView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("transactionDetails", createPanel());
  }

  private JPanel createPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(repository, directory);
    builder.add("transactionType",
                addLabel(new GlobListFieldStringifier(Transaction.TRANSACTION_TYPE, "", "") {
                  protected String stringify(Object value) {
                    return Lang.get("transactionType." + TransactionType.getType((Integer)value).getName());
                  }
                }, true));
    builder.add("userLabel",
                addLabel(new GlobListStringFieldStringifier(Transaction.LABEL,
                                                            Lang.get("transaction.details.multilabel")), false));
    builder.add("date",
                addLabel(new TransactionDateListStringifier(), true));

    builder.add("amountLabel",
                addLabel(GlobListStringifiers.singularOrPlural(Lang.get("transaction.details.amount.none"),
                                                               Lang.get("transaction.details.amount.singular"),
                                                               Lang.get("transaction.details.amount.plural")), true));
    builder.add("amountValue",
                addLabel(GlobListStringifiers.sum(Transaction.AMOUNT, PicsouDescriptionService.DECIMAL_FORMAT), true));

    builder.add("amountPanel", new AutoHideOnSelectionPanel(Transaction.TYPE,
                                                            AutoHideOnSelectionPanel.Mode.SHOW_IF_AT_LEAST_TWO,
                                                            directory));

    builder.add("minimumAmount",
                addLabel(GlobListStringifiers.minimum(Transaction.AMOUNT, PicsouDescriptionService.DECIMAL_FORMAT), true));

    builder.add("maximumAmount",
                addLabel(GlobListStringifiers.maximum(Transaction.AMOUNT, PicsouDescriptionService.DECIMAL_FORMAT), true));

    builder.add("averageAmount",
                addLabel(GlobListStringifiers.average(Transaction.AMOUNT, PicsouDescriptionService.DECIMAL_FORMAT), true));

    builder.add("categoryChooserPanel",
                new AutoHideOnSelectionPanel(Transaction.TYPE,
                                             AutoHideOnSelectionPanel.Mode.SHOW_IF_AT_LEAST_ONE,
                                             directory));
    CategoryChooserAction categoryChooserAction = new CategoryChooserAction(new TransactionRendererColors(directory), repository, directory);
    final JButton categoryChooserButton =
      new JButton(categoryChooserAction);
    categoryChooserButton.setText(null);
    builder.add("categoryChooserButton", categoryChooserButton);
    HyperlinkButton categoryChooserLink = new CategorisationHyperlinkButton(categoryChooserAction, repository, directory);
    builder.add("categoryChooserLink", categoryChooserLink);

    builder.add("detailInfo", new AutoHideOnSelectionPanel(Transaction.TYPE,
                                                           AutoHideOnSelectionPanel.Mode.SHOW_IF_ONLY_ONE,
                                                           directory));
    builder.add("splitLink", new SplitTransactionAction(repository, directory));

    builder.add("originalLabel",
                addLabel(new GlobListStringFieldStringifier(Transaction.ORIGINAL_LABEL, ""), true));
    return (JPanel)builder.parse(TransactionDetailsView.class, "/layout/transactionDetails.splits");
  }

  private GlobLabelView addLabel(GlobListStringifier stringifier, boolean autoHide) {
    return GlobLabelView.init(Transaction.TYPE, repository, directory, stringifier)
      .setAutoHide(autoHide);
  }

  public void selectionUpdated(GlobSelection selection) {
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
  }

  public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
  }

  public void colorsChanged(ColorLocator colorLocator) {
  }

  private static class TransactionDateListStringifier implements GlobListStringifier {
    public String toString(GlobList selected) {
      if (selected.isEmpty()) {
        return "";
      }
      Set<Integer> values = new HashSet<Integer>();
      for (Glob transaction : selected) {
        values.add(Month.toInt(transaction.get(Transaction.MONTH), transaction.get(Transaction.DAY)));
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
}

