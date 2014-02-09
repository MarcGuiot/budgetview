package org.designup.picsou.gui.series.analysis.evolution;

import org.designup.picsou.gui.components.table.AbstractRolloverEditor;
import org.designup.picsou.gui.series.analysis.SeriesChartsColors;
import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.gui.series.view.SeriesWrapperType;
import org.designup.picsou.model.Series;
import org.globsframework.gui.splits.components.EmptyIcon;
import org.globsframework.gui.splits.components.HyperlinkButton;
import org.globsframework.gui.splits.painters.PaintablePanel;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public abstract class SeriesEvolutionEditor extends AbstractRolloverEditor {

  private int offset;
  private SeriesChartsColors colors;

  protected Glob currentSeries;

  private JLabel label = new JLabel();
  private PaintablePanel labelPanel;
  private HyperlinkButton rendererButton;
  private PaintablePanel rendererPanel;
  private HyperlinkButton editorButton;
  private PaintablePanel editorPanel;

  protected int referenceMonthId;
  private static final String TEXT_FOR_WIDTH = "+888888.88";

  private Font smallFont;
  private Font largeFont;

  private Icon noIcon = null;
  private Icon subSeriesIcon = new EmptyIcon(8, 5);

  public SeriesEvolutionEditor(int offset,
                               GlobTableView view,
                               DescriptionService descriptionService,
                               GlobRepository repository,
                               Directory directory,
                               SeriesChartsColors colors) {
    super(view, descriptionService, repository, directory);
    this.offset = offset;
    this.colors = colors;

    labelPanel = initCellPanel(label, true, new PaintablePanel());
    smallFont = label.getFont().deriveFont(Font.PLAIN, 10);
    largeFont = label.getFont().deriveFont(Font.BOLD, 11);
  }

  protected void complete(Action action) {

    rendererButton = createHyperlinkButton(action);
    rendererPanel = initCellPanel(rendererButton, true, new PaintablePanel());

    editorButton = createHyperlinkButton(action);
    editorPanel = initCellPanel(editorButton, true, new PaintablePanel());
  }

  public int getWidth() {
    return label.getFontMetrics(label.getFont()).stringWidth(TEXT_FOR_WIDTH);
  }

  public void setReferenceMonth(Integer monthId) {
    this.referenceMonthId = monthId;
  }

  protected Component getComponent(Glob wrapper, boolean edit) {

    Integer itemId = wrapper.get(SeriesWrapper.ITEM_ID);
    if (SeriesWrapper.isSeries(wrapper) && edit) {
      currentSeries = repository.get(Key.create(Series.TYPE, itemId));
    }
    else {
      currentSeries = null;
    }

    String text = getText(wrapper);
    String description = getDescription(wrapper);

    switch (SeriesWrapperType.get(wrapper)) {
      case BUDGET_AREA:
        label.setFont(largeFont);
        label.setIcon(noIcon);
        label.setText(text);
        label.setToolTipText(description);
        colors.setColors(wrapper, row, offset, referenceMonthId, isSelected, label, labelPanel);
        return labelPanel;

      case SERIES: {
        JButton button = edit ? editorButton : rendererButton;
        label.setFont(largeFont);
        button.setText(text);
        button.setToolTipText(description);
        PaintablePanel panel = edit ? editorPanel : rendererPanel;
        colors.setColors(wrapper, row, offset, referenceMonthId, isSelected, button, panel);
        panel.setBorder(getBorderForSeries(wrapper));
        return panel;
      }

      case SERIES_GROUP: {
        label.setFont(smallFont);
        label.setIcon(noIcon);
        label.setText(text);
        label.setToolTipText(description);
        colors.setColors(wrapper, row, offset, referenceMonthId, isSelected, label, labelPanel);
        return labelPanel;
      }

      case SUB_SERIES:
        label.setFont(smallFont);
        label.setIcon(subSeriesIcon);
        label.setText(text);
        label.setToolTipText(description);
        colors.setColors(wrapper, row, offset, referenceMonthId, isSelected, label, labelPanel);
        return labelPanel;

      case SUMMARY:
        label.setFont(largeFont);
        label.setIcon(noIcon);
        label.setText(text);
        label.setToolTipText(description);
        colors.setColors(wrapper, row, offset, referenceMonthId, isSelected, label, labelPanel);
        return labelPanel;

      default:
        throw new InvalidParameter("Unexpected type: " + SeriesWrapperType.get(wrapper));
    }
  }

  protected abstract Border getBorderForSeries(Glob wrapper);

  protected abstract String getDescription(Glob seriesWrapper);

  protected abstract String getText(Glob seriesWrapper);

  protected HyperlinkButton createHyperlinkButton(Action action) {
    HyperlinkButton button = super.createHyperlinkButton(action);
    final Font font = button.getFont().deriveFont(Font.PLAIN, 10);
    button.setFont(font);
    return button;
  }
}
