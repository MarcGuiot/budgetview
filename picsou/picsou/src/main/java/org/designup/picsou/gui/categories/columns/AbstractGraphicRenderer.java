package org.designup.picsou.gui.categories.columns;

import org.designup.picsou.gui.utils.PicsouColors;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorSource;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractGraphicRenderer extends JPanel implements ColorChangeListener {
  protected static final int HORIZONTAL_MARGIN = 4;
  protected static final int VERTICAL_MARGIN = 2;

  protected GlobRepository globRepository;
  protected Color bottomBgColor;
  protected Color topBgColor;
  protected Color selectedBottomBgColor;
  protected Color selectedTopBgColor;
  protected Color selectedBorderColor;
  protected Color incomeTextColor;
  protected Color expenseTextColor;
  protected Color titleColor;
  protected Color grayedTitleColor;
  protected Color errorTitleColor;
  protected Color incomeBarLightColor;
  protected Color incomeBarDarkColor;
  protected Color expenseBarLightColor;
  protected Color expenseBarDarkColor;
  protected Font titleFont;
  protected Font valueFont;
  private PicsouColors selectionBackgroundBottomColorId;
  private PicsouColors selectionBackgroundTopColorId;
  private PicsouColors backgroundBottomColorId;
  private PicsouColors backgroundTopColorId;

  protected AbstractGraphicRenderer(GlobRepository globRepository,
                                    Directory directory,
                                    PicsouColors backgroundTop,
                                    PicsouColors backgroundBottom,
                                    PicsouColors selectionBackgroundTop,
                                    PicsouColors selectionBackgroundBottom) {
    this.globRepository = globRepository;
    selectionBackgroundBottomColorId = selectionBackgroundBottom;
    selectionBackgroundTopColorId = selectionBackgroundTop;
    backgroundBottomColorId = backgroundBottom;
    backgroundTopColorId = backgroundTop;

    directory.get(ColorService.class).addListener(this);
    JLabel label = new JLabel();
    Font defaultFont = label.getFont();
    titleFont = defaultFont.deriveFont(Font.BOLD, 11);
    valueFont = defaultFont.deriveFont(Font.PLAIN, 9);
    setOpaque(false);
  }

  public void colorsChanged(ColorSource colorSource) {
    selectedBottomBgColor = colorSource.get(selectionBackgroundBottomColorId);
    selectedTopBgColor = colorSource.get(selectionBackgroundTopColorId);
    selectedBorderColor = colorSource.get(PicsouColors.SELECTION_BG_BORDER);
    bottomBgColor = colorSource.get(backgroundBottomColorId);
    topBgColor = colorSource.get(backgroundTopColorId);
    incomeTextColor = colorSource.get(PicsouColors.INCOME_TEXT);
    expenseTextColor = colorSource.get(PicsouColors.EXPENSE_TEXT);
    titleColor = colorSource.get(PicsouColors.CATEGORY_TITLE);
    grayedTitleColor = colorSource.get(PicsouColors.CATEGORY_TITLE_GRAYED);
    errorTitleColor = colorSource.get(PicsouColors.CATEGORY_TITLE_ERROR);
    incomeBarLightColor = colorSource.get(PicsouColors.INCOME_BAR_LIGHT);
    incomeBarDarkColor = colorSource.get(PicsouColors.INCOME_BAR_DARK);
    expenseBarLightColor = colorSource.get(PicsouColors.EXPENSE_BAR_LIGHT);
    expenseBarDarkColor = colorSource.get(PicsouColors.EXPENSE_BAR_DARK);
  }
}
