package org.designup.picsou.gui.transactions.columns;

import org.designup.picsou.gui.categorization.CategorizationDialog;
import org.designup.picsou.gui.description.TransactionSeriesStringifier;
import org.designup.picsou.gui.utils.PicsouColors;
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
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.Strings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class TransactionSeriesColumn extends AbstractTransactionEditor implements ColorChangeListener {
  private GlobStringifier seriesStringifier;
  private HyperlinkButton rendererButton;
  private JPanel rendererPanel;
  private HyperlinkButton editorButton;
  private JPanel editorPanel;
  private GlobTableView tableView;
  private Glob transaction;
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

    directory.get(ColorService.class).addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    selectedColor = colorLocator.get(PicsouColors.TRANSACTION_SELECTED_TEXT);
    toCategorizeColor = colorLocator.get(PicsouColors.TRANSACTION_ERROR_TEXT);
  }

  private HyperlinkButton createHyperlink() {
    HyperlinkButton button = new HyperlinkButton(new OpenChooserAction());
    button.setOpaque(false);
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
    if (transaction.get(Transaction.SERIES) != null) {
      rendererColors.setForeground(button, isSelected, transaction);
      button.setFont(normalFont);
      button.setUnderline(false);
      button.setText(seriesStringifier.toString(transaction, repository));
    }
    else {
      button.setForeground(isSelected ? selectedColor : toCategorizeColor);
      button.setFont(toCategorizeFont);
      button.setUnderline(true);
      button.setText(Lang.get("category.assignement.required"));
    }
    rendererColors.setBackground(panel, isSelected, row);
    return panel;
  }

  private class OpenChooserAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      tableView.getComponent().requestFocus();
      selectTransactionIfNeeded(transaction);

      CategorizationDialog dialog = directory.get(CategorizationDialog.class);
      dialog.show(getTransactionList(transaction), true, false);
    }

    private GlobList getTransactionList(Glob transaction) {
      GlobList selection = tableView.getCurrentSelection();
      if (selection.size() > 1) {
        return selection;
      }
      else if ((transaction.get(Transaction.SERIES) != null) ||
               (Strings.isNullOrEmpty(transaction.get(Transaction.LABEL_FOR_CATEGORISATION)))) {
        return new GlobList(transaction);
      }
      else {
        return tableView.getGlobs()
          .filter(and(fieldEquals(Transaction.LABEL_FOR_CATEGORISATION,
                                  transaction.get(Transaction.LABEL_FOR_CATEGORISATION)),
                      isNull(Transaction.SERIES)),
                  repository);
      }
    }

    private void selectTransactionIfNeeded(Glob transaction) {
      GlobList selection = tableView.getCurrentSelection();
      if (!selection.contains(transaction)) {
        tableView.select(transaction);
      }
    }
  }
}
