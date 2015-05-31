package org.designup.picsou.gui.transactions.columns;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.table.HyperlinkTableColumn;
import org.designup.picsou.gui.description.stringifiers.TransactionSeriesStringifier;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.color.ColorLocator;
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

    this.seriesStringifier = new TransactionSeriesStringifier();

    FontLocator fontLocator = directory.get(FontLocator.class);
    this.normalFont = fontLocator.get("transactionView.category");
    this.toCategorizeFont = fontLocator.get("transactionView.category.error");
  }

  public String getName() {
    return Lang.get("series");
  }

  public Comparator<Glob> getComparator() {
    return seriesStringifier.getComparator(repository);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    super.colorsChanged(colorLocator);
    selectedColor = colorLocator.get("transactionTable.text.selected");
    toCategorizeColor = colorLocator.get("transaction.error.text");
  }

  protected void updateComponent(JButton jButton, JPanel panel, Glob transaction, boolean edit) {
    super.updateComponent(jButton, panel, transaction, edit);

    if (edit) {
      this.transaction = transaction;
    }

    HyperlinkButton button = (HyperlinkButton) jButton;

    if (Transaction.isPlanned(transaction) || Transaction.isMirrorTransaction(transaction)) {
      button.setEnabled(false);
      button.setDisabledColor(isSelected ? rendererColors.getTransactionSelectedTextColor() : rendererColors.getTransactionPlannedTextColor());
      setSeriesText(button, transaction);
    }
    else if (!Series.UNCATEGORIZED_SERIES_ID.equals(transaction.get(Transaction.SERIES))) {
      button.setEnabled(true);
      setSeriesText(button, transaction);
    }
    else {
      button.setEnabled(true);
      button.setForeground(isSelected ? selectedColor : toCategorizeColor);
      button.setFont(toCategorizeFont);
      button.setUnderline(true);
      button.setText(Lang.get("categorization.required"));
      button.setToolTipText(Lang.get("transaction.categorizationLink.tooltip"));
    }

    rendererColors.setBackground(panel, transaction, isSelected, row);
  }

  private void setSeriesText(HyperlinkButton button, Glob transaction) {
    rendererColors.setForeground(button, isSelected, transaction, TransactionRendererColors.Mode.DEFAULT, true);
    button.setFont(normalFont);
    button.setUnderline(false);
    String seriesText = getSeriesText(transaction);
    button.setText(seriesText);
    button.setToolTipText(getTooltipText(transaction, seriesText));
  }

  private String getSeriesText(Glob transaction) {
    return seriesStringifier.toString(transaction, repository);
  }

  private String getTooltipText(Glob transaction, String seriesText) {
    Glob series = repository.findLinkTarget(transaction, Transaction.SERIES);
    if ((series != null) && !Series.UNCATEGORIZED_SERIES_ID.equals(series.get(Series.ID))) {
      String description = series.get(Series.DESCRIPTION);
      return "<html>" + seriesText + ":<br>" + description + "</html>";
    }
    return seriesText;
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
    directory.get(NavigationService.class).gotoCategorization(list, false);
  }

  private void selectTransactionIfNeeded(Glob transaction) {
    GlobList selection = tableView.getCurrentSelection();
    if (!selection.contains(transaction)) {
      tableView.select(transaction);
    }
  }
}
