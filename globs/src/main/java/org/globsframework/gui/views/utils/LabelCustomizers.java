package org.globsframework.gui.views.utils;

import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class LabelCustomizers {

  public static LabelCustomizer chain(final LabelCustomizer... customizers) {
    return new LabelCustomizer() {
      public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
        for (LabelCustomizer customizer : customizers) {
          customizer.process(label, glob, isSelected, hasFocus, row, column);
        }
      }
    };
  }

  public static LabelCustomizer alignLeft() {
    return new AligmentCustomizer(JLabel.LEFT);
  }

  public static LabelCustomizer alignCenter() {
    return new AligmentCustomizer(JLabel.CENTER);
  }

  public static LabelCustomizer alignRight() {
    return new AligmentCustomizer(JLabel.RIGHT);
  }

  public static LabelCustomizer bold() {
    return new LabelCustomizer() {
      private Font boldFont;

      public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
        if (boldFont == null) {
          Font font = label.getFont();
          boldFont = font.deriveFont(font.getStyle() ^ Font.BOLD);
        }
        label.setFont(boldFont);
      }
    };
  }

  public static LabelCustomizer margin(int top, int left, int bottom, int right) {
    final Border border = BorderFactory.createEmptyBorder(top, left, bottom, right);
    return new LabelCustomizer() {
      public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
        label.setBorder(border);
      }
    };
  }

  public static LabelCustomizer stringifier(final GlobStringifier stringifier, final GlobRepository repository) {
    return new LabelCustomizer() {
      public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
        String value = stringifier.toString(glob, repository);
        label.setText(value);
      }
    };
  }

  private static class AligmentCustomizer implements LabelCustomizer {
    private int alignment;

    public AligmentCustomizer(int alignment) {
      this.alignment = alignment;
    }

    public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
      label.setHorizontalAlignment(alignment);
    }
  }
}
