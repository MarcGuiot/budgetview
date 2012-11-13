package org.designup.picsou.gui.messages;

import org.designup.picsou.gui.description.Formatting;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class MessagesPanel {
  private final Directory directory;
  private final List<MessageDisplay> displays;

  public MessagesPanel(Directory directory, List<MessageDisplay> displays) {
    this.directory = directory;
    this.displays = displays;
  }

  public JPanel create(){
    SplitsBuilder splitsBuilder = SplitsBuilder.init(directory);
    splitsBuilder.setSource(getClass(), "/layout/messages/messagesPanel.splits");
    Repeat<MessageDisplay> repeat = splitsBuilder
      .addRepeat("messages", displays, new RepeatComponentFactory<MessageDisplay>() {
        public void registerComponents(RepeatCellBuilder cellBuilder, final MessageDisplay item) {
          cellBuilder.add("date", new JLabel(Formatting.toString(item.getDate())));
          JTextArea text = new JTextArea(item.getMessage());
          cellBuilder.add("message", text);
          text.setName("Text " + item.getId());
          final JCheckBox component = new JCheckBox(((String)null), item.isCleared());
          cellBuilder.add("cleared", component);
          component.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
              item.clear(component.isSelected());
            }
          });
        }
      });
    return splitsBuilder.load();
  }
}
