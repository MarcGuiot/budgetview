package org.globsframework.gui.views.utils;

import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.metamodel.fields.StringField;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class LabelCustomizers {
  public static final AligmentCustomizer ALIGN_LEFT = new AligmentCustomizer(JLabel.LEFT);
  public static final AligmentCustomizer ALIGN_CENTER = new AligmentCustomizer(JLabel.CENTER);
  public static final AligmentCustomizer ALIGN_RIGHT = new AligmentCustomizer(JLabel.RIGHT);
  public static final StaticLabelCustomizer BOLD = createCustomizer(Font.BOLD);
  public static final StaticLabelCustomizer PLAIN = createCustomizer(Font.PLAIN);
  public static final StaticLabelCustomizer ITALIC = createCustomizer(Font.ITALIC);

  public static LabelCustomizer chain(Iterable<LabelCustomizer> customizers) {
    final java.util.List<LabelCustomizer> reducedList = new ArrayList<LabelCustomizer>();
    for (LabelCustomizer customizer : customizers) {
      if (customizer != LabelCustomizer.NO_OP) {
        reducedList.add(customizer);
      }
    }
    return new LabelCustomizer() {
      public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
        for (LabelCustomizer customizer : reducedList) {
          customizer.process(label, glob, isSelected, hasFocus, row, column);
        }
      }
    };
  }

  public static LabelCustomizer chain(final LabelCustomizer... customizers) {
    return chain(Arrays.asList(customizers));
  }

  private static StaticLabelCustomizer createCustomizer(final int style) {
    return new StaticLabelCustomizer() {
      private Font boldFont;

      public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
        if (boldFont == null) {
          Font font = label.getFont();
          boldFont = font.deriveFont(font.getStyle() ^ style);
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

  public interface FontCustomizer extends LabelCustomizer{
    Font getFont();
  }

  public static FontCustomizer font(final Font font) {
    return new FontCustomizer() {
      public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
        label.setFont(font);
      }

      public Font getFont() {
        return font;
      }
    };
  }

  public static FontCustomizer fontSize(FontCustomizer customizer, float newSize) {
    final Font font = customizer.getFont().deriveFont(newSize);
    return new FontCustomizer() {
      public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
        label.setFont(font);
      }

      public Font getFont() {
        return font;
      }
    };
  }

  public static FontCustomizer fontSize(float newSize) {
    JLabel defaultLabel = new JLabel();
    final Font font = defaultLabel.getFont().deriveFont(newSize);
    return new FontCustomizer() {
      public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
        label.setFont(font);
      }

      public Font getFont() {
        return font;
      }
    };
  }

  public static LabelCustomizer autoTooltip() {
    return new LabelCustomizer() {
      public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
        label.setToolTipText(label.getText());
      }
    };
  }

  public static LabelCustomizer fieldTooltip(final StringField field) {
    return new LabelCustomizer() {
      public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
        label.setToolTipText(glob.get(field));
      }
    };
  }

  public static LabelCustomizer tooltip(final GlobStringifier stringifier, final GlobRepository repository) {
    return new LabelCustomizer() {
      public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
        label.setToolTipText(stringifier.toString(glob, repository));
      }
    };
  }

  private static class AligmentCustomizer extends StaticLabelCustomizer {
    private int alignment;

    public AligmentCustomizer(int alignment) {
      this.alignment = alignment;
    }

    public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
      label.setHorizontalAlignment(alignment);
    }
  }
}
