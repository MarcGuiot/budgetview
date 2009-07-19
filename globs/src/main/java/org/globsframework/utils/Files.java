package org.globsframework.utils;

import org.globsframework.utils.exceptions.IOFailure;
import org.globsframework.utils.exceptions.ResourceAccessFailed;
import org.globsframework.utils.exceptions.ItemNotFound;

import java.io.*;
import java.util.Properties;

public class Files {
  private Files() {
  }

  public static String loadFileToString(String fileName) throws IOFailure {
    StringBuilder builder = new StringBuilder();
    FileReader fileReader = null;
    BufferedReader reader = null;
    try {
      fileReader = new FileReader(fileName);
      reader = new BufferedReader(fileReader);
      while (true) {
        String line = reader.readLine();
        if (line == null) {
          break;
        }
        builder.append(line).append(Strings.LINE_SEPARATOR);
      }
      return builder.toString();
    }
    catch (IOException e) {
      throw new IOFailure(e);
    }
    finally {
      try {
        if (reader != null) {
          reader.close();
        }
        else if (fileReader != null) {
          fileReader.close();
        }
      }
      catch (IOException e) {
        throw new IOFailure(e);
      }
    }
  }

  public static String loadFileToString(String fileName, final String charsetName) throws IOFailure, ItemNotFound {
    try {
      return loadStreamToString(new InputStreamReader(new FileInputStream(fileName), charsetName));
    }
    catch (UnsupportedEncodingException e) {
      throw new IOFailure(e);
    }
    catch (FileNotFoundException e) {
      throw new ItemNotFound(e);
    }
  }


  public static String loadStreamToString(InputStream stream, final String charsetName) throws IOFailure {
    try {
      return loadStreamToString(new InputStreamReader(stream, charsetName));
    }
    catch (UnsupportedEncodingException e) {
      throw new IOFailure(e);
    }
  }

  public static String loadStreamToString(Reader input) throws IOFailure {
    try {
      Reader reader = new BufferedReader(input);
      StringBuilder result = new StringBuilder();
      char[] buffer = new char[1024];
      int count = reader.read(buffer);
      while (count != -1) {
        result.append(buffer, 0, count);
        count = reader.read(buffer);
      }
      return result.toString();
    }
    catch (IOException e) {
      throw new IOFailure(e);
    }
  }

  public static String dumpStringToFile(String fileName, String content) throws IOFailure {
    FileWriter writer = null;
    try {
      writer = new FileWriter(fileName);
      writer.append(content);
      writer.close();
      return fileName;
    }
    catch (IOException e) {
      throw new IOFailure(e);
    }
    finally {
      if (writer != null) {
        try {
          writer.close();
        }
        catch (IOException e) {
          throw new IOFailure(e);
        }
      }
    }
  }

  public static void deleteSubtree(File directory) {
    File[] files = directory.listFiles();
    if (files == null) {
      return;
    }
    for (File file : files) {
      if (file.isDirectory()) {
        deleteSubtree(file);
      }
      else {
        file.delete();
      }
    }
    directory.delete();
  }

  public static void copyStreamTofile(InputStream inputStream, String file) throws IOException {
    OutputStream outputStream = new FileOutputStream(file);
    byte[] bytes = new byte[1024];
    int readed;
    while ((readed = inputStream.read(bytes)) != -1) {
      outputStream.write(bytes, 0, readed);
    }
    outputStream.close();
  }

  public static Properties loadProperties(Class refClass, String fileName) throws ResourceAccessFailed {
    InputStream stream = getStream(refClass, fileName);
    Properties properties = new Properties();
    try {
      properties.load(stream);
    }
    catch (IOException e) {
      throw new ResourceAccessFailed("Could not load properties file");
    }
    return properties;
  }

  public static InputStream getStream(Class refClass, String fileName) throws ResourceAccessFailed {
    InputStream stream = refClass.getResourceAsStream(fileName);
    if (stream == null) {
      throw new ResourceAccessFailed("Resource file '" + fileName + "' not found for class: " + refClass.getName());
    }
    return stream;
  }

  public static void createParentDirs(File file) {
    if (file == null) {
      return;
    }
    File parent = file.getParentFile();
    if (parent == null) {
      return;
    }
    parent.mkdirs();
  }
}
