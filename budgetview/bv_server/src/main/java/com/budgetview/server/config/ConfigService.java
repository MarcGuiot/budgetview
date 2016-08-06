package com.budgetview.server.config;

import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.OperationDenied;

import java.io.Console;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigService {

  private Properties properties = new Properties();

  public ConfigService(String... args) throws IOException, InvalidParameter {
    if (args.length == 1) {
      loadProperties(args[0]);
    }
    if (args.length > 1) {
      throw new InvalidParameter("Expecting only one command-line argument: <properties_file_path>");
    }
  }

  private void loadProperties(String propertiesFile) throws IOException {
    try {
      properties.load(new FileInputStream(propertiesFile));
    }
    catch (IOException e) {
      throw new IOException("Could not load properties file: " + propertiesFile, e);
    }
  }

  public String get(String property) {
    return doGet(property, false, null);
  }

  public Integer getInt(String property, Integer defaultValue) {
    String result = doGet(property, true, null);
    if (result == null) {
      return defaultValue;
    }
    return Integer.parseInt(result);
  }

  public String doGet(String property, boolean useDefault, String defaultValue) {
    String result = System.getProperty(property);
    if (result != null) {
      return result;
    }

    result = properties.getProperty(property);
    if (result != null) {
      return result;
    }

    if (useDefault) {
      return defaultValue;
    }

    Console console = System.console();
    if (console == null) {
      throw new OperationDenied("Couldn't get Console instance to input: " + property);
    }

    if (property.contains("pwd") || property.contains("password")) {
      char passwordArray[] = console.readPassword(property + ": ");
      return new String(passwordArray).trim();
    }

    return console.readLine(property + ": ").trim();
  }
}
