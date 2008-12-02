package org.designup.picsou.gui.transactions.columns;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.description.TransactionSeriesStringifier;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.color.ColorChangeListener;
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
import java.awt.event.ActionEvent;
import java.util.Comparator;

public class TransactionSeriesColumn extends AbstractTransactionEditor implements ColorChangeListener {

  private GlobStringifier seriesStringifier;
  private Glob transaction;

  private HyperlinkButton rendererButton;
  private JPanel rendererPanel;
  private HyperlinkButton editorButton;
  private JPanel editorPanel;
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
    super(view, transactionRendererColors, descriptionService, repository, directory);
    this.tableView = view;

    seriesStringifier = new TransactionSeriesStringifier();

    FontLocator fontLocator = directory.get(FontLocator.class);
    normalFont = fontLocator.get("transactionView.category");
    toCategorizeFont = fontLocator.get("transactionView.category.error");

    rendererButton = createHyperlink();
    rendererPanel = new JPanel();
    rendererPanel.setLayout(new BoxLayout(rendererPanel, BoxLayout.X_AXIS));
    rendererPanel.add(rendererButton);
    rendererPanel.add(Box.createHorizontalGlue());

    editorButton = createHyperlink();
    editorPanel = new JPanel();
    editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.X_AXIS));
    editorPanel.add(editorButton);
    editorPanel.add(Box.createHorizontalGlue());

    colorService = directory.get(ColorService.class);
    colorService.addListener(this);
  }

  public Comparator<Glob> getComparator() {
    return seriesStringifier.getComparator(repository);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    selectedColor = colorLocator.get(PicsouColors.TRANSACTION_SELECTED_TEXT);
    toCategorizeColor = colorLocator.get(PicsouColors.TRANSACTION_ERROR_TEXT);
  }

  private HyperlinkButton createHyperlink() {
    HyperlinkButton button = new HyperlinkButton(new OpenChooserAction());
    button.setOpaque(false);
    button.setAutoHide(false);
    button.setUnderline(false);
    return button;
  }

  protected Component getComponent(final Glob transaction, boolean render) {
    HyperlinkButton button;
    JPanel panel;
    if (render) {
      button = this.rendererButton;
      panel = this.rendererPanel;
    }
    else {
      this.transaction = transaction;
      button = this.editorButton;
      panel = this.editorPanel;
    }
    if (Transaction.isPlanned(transaction) || Transaction.isMirrorTransaction(transaction)) {
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
    return panel;
  }

  private class OpenChooserAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
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
  }

  protected void finalize() throws Throwable {
    super.finalize();
    colorService.removeListener(this);
  }
}
