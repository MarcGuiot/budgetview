package org.designup.picsou.gui.components;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.PicsouInit;
import org.designup.picsou.gui.components.dialogs.MonthChooserDialog;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.model.Month;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Arrays;

public class MonthChooserDemo {

  public static void main(String[] args) throws Exception {

    Directory directory = PicsouApplication.createDirectory();
    JFrame frame = new JFrame();
    directory.add(JFrame.class, frame);
    GlobRepository repository = PicsouInit.init(ServerAccess.NULL, directory, true, false).getRepository();
    for (int monthId = 200701; monthId < 200809; monthId = Month.next(monthId)) {
      repository.findOrCreate(Key.create(Month.TYPE, monthId));
    }

    PicsouDialog.FORCE_NONMODAL = true;
    MonthChooserDialog chooser = new MonthChooserDialog("Choose month", frame, directory);
    chooser.show(200806, 200701, 200909, Arrays.asList(200904, 200704), new MonthChooserDialog.Callback() {
      public void processSelection(int monthId) {
        System.out.println("MonthChooserDemo.processSelection: " + monthId);
      }

      public void processNoneSelected() {
        System.out.println("MonthChooserDemo.processNoneSelected");
      }

      public void processCancel() {
        System.out.println("MonthChooserDemo.processCancel");
      }
    });
  }
}
