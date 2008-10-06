package org.designup.picsou.gui.categories.columns;

import org.designup.picsou.gui.utils.PicsouColors;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorLocator;
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
  protected Color titleColor;
  protected Color grayedTitleColor;
  protected Color errorTitleColor;
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

  public void colorsChanged(ColorLocator colorLocator) {
    selectedBottomBgColor = colorLocator.get(selectionBackgroundBottomColorId);
    selectedTopBgColor = colorLocator.get(selectionBackgroundTopColorId);
    selectedBorderColor = colorLocator.get(PicsouColors.SELECTION_BG_BORDER);
    bottomBgColor = colorLocator.get(backgroundBottomColorId);
    topBgColor = colorLocator.get(backgroundTopColorId);
    titleColor = colorLocator.get(PicsouColors.CATEGORY_TITLE);
    grayedTitleColor = colorLocator.get(PicsouColors.CATEGORY_TITLE_GRAYED);
    errorTitleColor = colorLocator.get(PicsouColors.CATEGORY_TITLE_ERROR);
  }
}
