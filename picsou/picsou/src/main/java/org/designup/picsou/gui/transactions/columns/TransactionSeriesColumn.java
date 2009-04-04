package org.designup.picsou.gui.transactions.columns;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.HyperlinkTableColumn;
import org.designup.picsou.gui.description.TransactionSeriesStringifier;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.components.HyperlinkButton;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;

public class TransactionSeriesColumn extends HyperlinkTableColumn {

  protected TransactionRendererColors rendererColors;
  private GlobRepository repository;

  private GlobStringifier seriesStringifier;
  private Glob transaction;

  private GlobTableView tableView;

  private ColorService colorService;

  private Color selectedColor;
  private Color toCategorizeColor;
  private Font normalFont;
  private Font toCategorizeFont;

  public TransactionSeriesColumn(GlobTableView view,
                                 TransactionRendererColors transactionRendererColors,
                                 DescriptionService descriptionService,
                                 GlobRepository repository,
                                 Directory directory) {
    super(view, descriptionService, repository, directory);
    this.tableView = view;
    this.rendererColors = transactionRendererColors;
    this.repository = repository;

    seriesStringifier = new TransactionSeriesStringifier();

    FontLocator fontLocator = directory.get(FontLocator.class);
    normalFont = fontLocator.get("transactionView.category");
    toCategorizeFont = fontLocator.get("transactionView.category.error");

    colorService = directory.get(ColorService.class);
  }

  public String getName() {
    return Lang.get("series");
  }

  public Comparator<Glob> getComparator() {
    return seriesStringifier.getComparator(repository);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    super.colorsChanged(colorLocator);
    selectedColor = colorLocator.get(PicsouColors.TRANSACTION_SELECTED_TEXT);
    toCategorizeColor = colorLocator.get(PicsouColors.TRANSACTION_ERROR_TEXT);
  }

  protected void updateComponent(HyperlinkButton button, JPanel panel, Glob transaction, boolean render) {
    if (!render) {
      this.transaction = transaction;
    }

    if (Transaction.isPlanned(transaction)
        || Transaction.isMirrorTransaction(transaction)
        || Transaction.isCreatedBySeries(transaction)) {
      button.setEnabled(false);
      rendererColors.setForeground(button, isSelected, transaction, true);
      button.setDisabledColor(isSelected ? rendererColors.getTransactionSelectedTextColor() : rendererColors.getTransactionPlannedTextColor());
      button.setFont(normalFont);
      button.setUnderline(false);
      button.setText(seriesStringifier.toString(transaction, repository));
      button.setToolTipText(null);
    }
    else if (!Series.UNCATEGORIZED_SERIES_ID.equals(transaction.get(Transaction.SERIES))) {
      button.setEnabled(true);
      rendererColors.setForeground(button, isSelected, transaction, true);
      button.setFont(normalFont);
      button.setUnderline(false);
      button.setText(seriesStringifier.toString(transaction, repository));
      button.setToolTipText(Lang.get("transaction.categorizationLink.tooltip"));
    }
    else {
      button.setEnabled(true);
      button.setForeground(isSelected ? selectedColor : toCategorizeColor);
      button.setFont(toCategorizeFont);
      button.setUnderline(true);
      button.setText(Lang.get("category.assignement.required"));
      button.setToolTipText(Lang.get("transaction.categorizationLink.tooltip"));
    }

    rendererColors.setBackground(panel, transaction, isSelected, row);
  }

  public GlobStringifier getStringifier() {
    return seriesStringifier;
  }

  protected void processClick() {
    if (Transaction.isPlanned(transaction)) {
      return;
    }

    tableView.getComponent().requestFocus();
    selectTransactionIfNeeded(transaction);

    GlobList list = tableView.getCurrentSelection();
    if (list.isEmpty()) {
      return;
    }
    directory.get(NavigationService.class).gotoCategorization(list);
  }

  private void selectTransactionIfNeeded(Glob transaction) {
    GlobList selection = tableView.getCurrentSelection();
    if (!selection.contains(transaction)) {
      tableView.select(transaction);
    }
  }

  protected void finalize() throws Throwable {
    super.finalize();
    colorService.removeListener(this);
  }
}
