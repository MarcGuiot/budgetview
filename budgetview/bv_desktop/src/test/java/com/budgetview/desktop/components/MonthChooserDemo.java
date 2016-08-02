package com.budgetview.desktop.components;

import com.budgetview.desktop.components.dialogs.MonthChooserDialog;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.client.ServerAccess;
import com.budgetview.desktop.Application;
import com.budgetview.desktop.PicsouInit;
import com.budgetview.model.Month;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Arrays;

public class MonthChooserDemo {

  public static void main(String[] args) throws Exception {

    Directory directory = Application.createDirectory();
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
