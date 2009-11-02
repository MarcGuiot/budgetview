package org.designup.picsou.license;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.IOException;

public class AddUserToHsql {
  public static void main(String[] args) throws IOException {
    List<String> arguments = new ArrayList<String>();
    arguments.add("-d");
    arguments.add("jdbc:hsqldb:hsql://localhost/picsou");
    arguments.add("-u");
    arguments.add("sa");
    arguments.add("-p");
    arguments.add("");
    arguments.add("user@localhost.fr");
    arguments.addAll(Arrays.asList(args));
    AddUser.main(arguments.toArray(new String[arguments.size()]));
  }
}
