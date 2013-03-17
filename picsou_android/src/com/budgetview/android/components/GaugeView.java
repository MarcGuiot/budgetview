package com.budgetview.android.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import com.budgetview.shared.gui.gauge.GaugeModel;
import com.budgetview.shared.gui.gauge.GaugeModelListener;
import com.budgetview.shared.gui.gauge.GaugeTextSource;
import org.globsframework.utils.Utils;

public class GaugeView extends View {

  private static final int DEFAULT_BAR_HEIGHT = 12;
  private static final int HORIZONTAL_MARGIN = 2;
  private static final int VERTICAL_MARGIN = 2;
  private static final double FIXED_WIDTH_RATIO = 0.2;
  private static final int ARC_WIDTH = 5;
  private static final int ARC_HEIGHT = 10;

  private GaugeModel model;

  private int borderColor = Color.parseColor("#dedddd");
  private int filledColorTop = Color.parseColor("#5583F3");
  private int filledColorBottom = Color.parseColor("#1A4AD3");
  private int emptyColorTop = Color.parseColor("#d3d3d3");
  private int emptyColorBottom = Color.parseColor("#f4f4f4");
  private int overrunColorTop = Color.parseColor("#154DD8");
  private int overrunColorBottom = Color.parseColor("#091F56");
  private int overrunErrorColorTop = Color.parseColor("#F95D5F");
  private int overrunErrorColorBottom = Color.parseColor("#D41D19");
  private int barHeight;

  public GaugeView(Context context) {
    super(context);
    init();
  }

  public GaugeView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public GaugeView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  private void init() {
    setWillNotDraw(false);
    model = new GaugeModel(new GaugeTextSource() {
      public String getText(String s, String... strings) {
        return "";
      }
    });
    model.addListener(new GaugeModelListener() {
      public void modelUpdated() {
        invalidate();
        postInvalidate();
      }

      public void updateTooltip(String s) {
      }
    });
    barHeight = DEFAULT_BAR_HEIGHT;
  }

  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    setMeasuredDimension(200, 20);
  }

  public GaugeModel getModel() {
    return model;
  }

  protected void onDraw(Canvas canvas) {
    int totalWidth = getWidth() - 1 - 2 * HORIZONTAL_MARGIN;
    int width = getAdjustedWidth(totalWidth);
    int height = getHeight() - 1 - 2 * VERTICAL_MARGIN;
    int minX = HORIZONTAL_MARGIN;

    int barTop = (height - barHeight) / 2 + VERTICAL_MARGIN;
    int barBottom = height - barTop;

    int beginWidth = (int)(width * model.getBeginPercent());
    int fillWidth = (int)(width * model.getFillPercent());
    int emptyWidth = (int)(width * model.getEmptyPercent());

    int overrunWidth = width - fillWidth - emptyWidth - beginWidth;
    int overrunEnd = beginWidth + fillWidth + overrunWidth;
    int overrunStart = beginWidth + fillWidth;

    fillBar(canvas, borderColor, borderColor, minX - 1, width + 2, barTop - 2, barBottom + 2);

    if (model.getEmptyPercent() > 0) {
      fillBar(canvas, emptyColorTop, emptyColorBottom, minX, overrunEnd + emptyWidth, barTop, barBottom);
    }

    if (model.getBeginPercent() > 0) {
      fillBar(canvas, overrunErrorColorTop, overrunErrorColorBottom, minX, beginWidth, barTop, barBottom);
    }

    if (model.getOverrunPercent() > 0) {
      if (model.hasOverrunError()) {
        fillBar(canvas, overrunErrorColorTop, overrunErrorColorBottom, minX, overrunStart + overrunWidth, barTop, barBottom);
      }
      else {
        fillBar(canvas, overrunColorTop, overrunColorBottom, minX, overrunStart + overrunWidth, barTop, barBottom);
      }
    }

    if (model.getFillPercent() > 0) {
      fillBar(canvas, filledColorTop, filledColorBottom, minX, fillWidth, barTop, barBottom);
    }
  }

  private void drawBorder(Canvas canvas, int barX, int barTop, int barWidth) {
    Paint paint = new Paint(borderColor);
    RectF rect = new RectF(barX, barTop, barX + barWidth, barTop + barHeight);
    canvas.drawRoundRect(rect, ARC_WIDTH, ARC_HEIGHT, paint);
  }

  private void fillBar(Canvas canvas, int topColor, int bottomColor, int barX, int barWidth, int barTop, int barBottom) {
    GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                                                     new int[]{topColor, bottomColor});
    gradient.setSize(barWidth, barHeight);
    gradient.setCornerRadius(5);
    gradient.setBounds(barX, barTop, barX + barWidth, barTop + barBottom);
    gradient.draw(canvas);
  }

  private int getAdjustedWidth(int totalWidth) {
    Double value = Utils.max(Math.abs(model.getActualValue()), Math.abs(model.getTargetValue()));
    if (Math.abs(value) < 0.1) {
      return 0;
    }
    int fixedWidth = (int)(FIXED_WIDTH_RATIO * totalWidth);
    return fixedWidth + (int)((totalWidth - fixedWidth));
  }
}
