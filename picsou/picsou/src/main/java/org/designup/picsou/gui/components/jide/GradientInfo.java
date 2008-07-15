package org.designup.picsou.gui.components.jide;

import java.awt.*;

/**
 * Containts all information pertaining to a particular gradient.
 */
class GradientInfo {
    GraphicsConfiguration gfxConfig;
    int length;
    Color startColor, endColor;
    boolean isVertical;

    public GradientInfo(GraphicsConfiguration gc, int ln, Color sc, Color ec, boolean v) {
        gfxConfig = gc;
        length = ln;
        startColor = sc;
        endColor = ec;
        isVertical = v;
    }

    boolean isEquivalent(GradientInfo gi) {
        return (gi.gfxConfig.equals(gfxConfig) && gi.length == length && gi.startColor.equals(startColor) && gi.endColor.equals(endColor) && gi.isVertical == isVertical);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GradientInfo)) return false;
        return isEquivalent((GradientInfo) o);
    }

    @Override
    public String toString() {
        return "Direction:" + (isVertical ? "ver" : "hor") + ", Length: " + Integer.toString(length) + ", Color1: " + Integer.toString(startColor.getRGB(), 16) + ", Color2: " + Integer.toString(endColor.getRGB(), 16);
    }
}
