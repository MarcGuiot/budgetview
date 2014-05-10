package picsou;

import org.designup.picsou.gui.PicsouApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainInMemory {
  public static void main(String[] args) throws Exception {
    System.setProperty(PicsouApplication.APPNAME + ".data.in.memory", "true");
    List<String> strings = new ArrayList<String>(Arrays.asList(args));
    if (!strings.contains("-l")) {
      strings.addAll(Arrays.asList("-l", "fr"));
    }
    PicsouApplication.main(strings.toArray(new String[strings.size()]));
  }
}
