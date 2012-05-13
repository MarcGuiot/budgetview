package org.designup.picsou.gui.series.analysis.evolution;

import org.designup.picsou.gui.components.AbstractRolloverEditor;
import org.designup.picsou.gui.series.analysis.SeriesChartsColors;
import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.gui.series.view.SeriesWrapperType;
import org.designup.picsou.model.Series;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.utils.LabelCustomizers;
import org.globsframework.gui.splits.painters.PaintablePanel;
import org.globsframework.gui.splits.components.HyperlinkButton;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Glob;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
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

  public SeriesEvolutionEditor(int offset,
                               GlobTableView view,
                               DescriptionService descriptionService,
                               GlobRepository repository,
                               Directory directory,
                               SeriesChartsColors colors) {
    super(view, descriptionService, repository, directory);
    this.offset = offset;
    this.colors = colors;

    LabelCustomizers.BOLD.process(label);
    labelPanel = initCellPanel(label, true, new PaintablePanel());
  }

  protected void complete(Action action) {

    rendererButton = createHyperlinkButton(action);
    rendererPanel = initCellPanel(rendererButton, true, new PaintablePanel());

    editorButton = createHyperlinkButton(action);
    editorPanel = initCellPanel(editorButton, true, new PaintablePanel());
  }

  public int getWidth(){
    return label.getFontMetrics(label.getFont()).stringWidth(TEXT_FOR_WIDTH);
  }

  public void setReferenceMonth(Integer monthId) {
    this.referenceMonthId = monthId;
  }

  protected Component getComponent(Glob seriesWrapper, boolean edit) {

    Integer itemId = seriesWrapper.get(SeriesWrapper.ITEM_ID);
    if (edit) {
      currentSeries = repository.get(Key.create(Series.TYPE, itemId));
    }

    String text = getText(seriesWrapper);
    String description = getDescription(seriesWrapper);

    switch (SeriesWrapperType.get(seriesWrapper)) {
      case BUDGET_AREA:
        label.setText(text);
        label.setToolTipText(description);
        colors.setColors(seriesWrapper, row, offset, referenceMonthId, isSelected, label, labelPanel);
        return labelPanel;

      case SERIES:
        JButton button = edit ? editorButton : rendererButton;
        button.setText(text);
        button.setToolTipText(description);
        PaintablePanel panel = edit ? editorPanel : rendererPanel;
        colors.setColors(seriesWrapper, row, offset, referenceMonthId, isSelected, button, panel);
        return panel;

      case SUB_SERIES:
        label.setText(text);
        label.setToolTipText(description);
        colors.setColors(seriesWrapper, row, offset, referenceMonthId, isSelected, label, labelPanel);
        return labelPanel;

      case SUMMARY:
        label.setText(text);
        label.setToolTipText(description);
        colors.setColors(seriesWrapper, row, offset, referenceMonthId, isSelected, label, labelPanel);
        return labelPanel;

      default:
        throw new InvalidParameter("Unexpected type: " + SeriesWrapperType.get(seriesWrapper));
    }
  }

  protected abstract String getDescription(Glob seriesWrapper);

  protected abstract String getText(Glob seriesWrapper);

  protected HyperlinkButton createHyperlinkButton(Action action) {
    HyperlinkButton button = super.createHyperlinkButton(action);
    final Font font = button.getFont().deriveFont(Font.PLAIN, 10);
    button.setFont(font);
    return button;
  }
}
