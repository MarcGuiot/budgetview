package org.globsframework.gui.splits;

import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatFactory;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;

public class SplitsRepeatDemo {
  public static void main(String[] args) {

    SplitsBuilder builder = SplitsBuilder.init(new ColorService(), IconLocator.NULL);

    builder.addRepeat("repeat1", new RepeatFactory<String>() {
      public void register(RepeatCellBuilder cellBuilder, String item) {
        cellBuilder.add("label1", new JLabel(item));
        cellBuilder.addRepeat("repeat2", new RepeatFactory<String>() {

          public void register(RepeatCellBuilder cellBuilder, String item) {
            cellBuilder.add("label2", new JLabel(item));
          }
        }, getItems(item));
      }
    }, Arrays.asList("a", "b", "c"));

    builder.setSource("<splits>" +
                      "  <repeat ref='repeat1'>" +
                      "    <row>" +
                      "      <label ref='label1'/>" +
                      "      <repeat ref='repeat2'>" +
                      "        <label ref='label2'/>" +
                      "      </repeat>" +
                      "    </row>" +
                      "  </repeat>" +
                      "</splits>");

    JPanel panel = builder.load();
    GuiUtils.show(panel);
  }

  private static java.util.List<String> getItems(String item) {
    if ("a".equals(item)) {
      return Arrays.asList("a1", "a2");
    }
    if ("c".equals(item)) {
      return Arrays.asList("c1");
    }
    return Collections.emptyList();
  }
}
