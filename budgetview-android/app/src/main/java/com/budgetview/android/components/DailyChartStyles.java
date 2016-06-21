package com.budgetview.android.components;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;

import com.budgetview.android.R;
import com.budgetview.shared.gui.TextMetrics;

import java.util.HashMap;
import java.util.Map;

public class DailyChartStyles {

    private final Resources resources;
    private Map<String, Paint> paints = new HashMap<String, Paint>();

    public DailyChartStyles(Resources resources) {
        this.resources = resources;
    }

    public Paint getBackgroundPaint() {
        return getPaint("background", R.color.item_background, Paint.Style.FILL);
    }

    public Paint getChartBgPaint() {
        return getPaint("chartBg", "#FFFFFF", Paint.Style.FILL);
    }

    public Paint getChartBorderPaint() {
        return getPaint("chartBorder", "#888888", Paint.Style.STROKE);
    }

    public Paint getColumnDividerPaint() {
        return getPaint("columnDivider", "#cccccc", Paint.Style.STROKE);
    }

    public Paint getLabelPaint() {
        return getPaint("label", "#555555", Paint.Style.STROKE);
    }

    public Paint getScaleOriginLinePaint() {
        return getPaint("scaleOriginLine", "#555555", Paint.Style.STROKE);
    }

    public Paint getScaleLinePaint() {
        return getPaint("scaleLine", "#AAAAAA", Paint.Style.STROKE);
    }

    public Paint getSectionLinePaint() {
        return getPaint("sectionLine", "#555555", Paint.Style.STROKE);
    }

    public Paint getSelectedColumnBorderPaint() {
        return getPaint("selectedColumnBorder", "#555555", Paint.Style.STROKE);
    }

    public Paint getCurrentDayColor() {
        return getPaint("currentDay", "#333333", Paint.Style.STROKE);
    }

    public Paint getInnerLabelColor(Double minValue) {
        return getPaint("innerLabel", "#444444", Paint.Style.STROKE);
    }

    public Paint getCurrentDayAnnotation() {
        return getPaint("currentDay", "#444444", Paint.Style.STROKE);
    }

    public Paint getGraphBackground(boolean positive, boolean current, boolean future) {
        if (positive) {
            if (future) {
                return getPaint("positiveGraphFutureBg", "#99FF99", 100, Paint.Style.FILL);
            } else {
                return getPaint("positiveGraphBg", "#99FF99", 100, Paint.Style.FILL);
            }
        } else {
            if (future) {
                return getPaint("negativeGraphFutureBg", "#FF9999", 100, Paint.Style.FILL);
            } else {
                return getPaint("negativeGraphBg", "#FF9999", 100, Paint.Style.FILL);
            }
        }
    }

    public Paint getGraphLine(boolean positive, boolean current, boolean future) {
        if (positive) {
            return getPaint("positiveGraphLine", "#229922", Paint.Style.STROKE);
        } else {
            return getPaint("negativeGraphLine", "#992222", Paint.Style.STROKE);
        }
    }

    private Paint getPaint(String name, String color, int alpha, Paint.Style style) {
        return doGetPaint(name, Color.parseColor(color), alpha, style);
    }

    private Paint getPaint(String name, String color, Paint.Style style) {
        return doGetPaint(name, Color.parseColor(color), 255, style);
    }

    private Paint getPaint(String name, int color, Paint.Style style) {
        return doGetPaint(name, resources.getColor(color), 255, style);
    }

    private Paint doGetPaint(String name, int colorId, int alpha, Paint.Style style) {
        Paint paint = paints.get(name);
        if (paint == null) {
            paint = new Paint();
            paint.setColor(colorId);
            paint.setAlpha(alpha);
            paint.setStyle(style);
            paints.put(name, paint);
        }
        return paint;
    }

    public TextMetrics getTypefaceMetrics() {
        final Paint paint = new Paint();
        final Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        return new TextMetrics() {
            public int stringWidth(String text) {
                return (int) paint.measureText(text);
            }

            public int getAscent() {
                return (int) fontMetrics.ascent;
            }
        };
    }
}
