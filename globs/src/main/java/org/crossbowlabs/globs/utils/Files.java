package org.crossbowlabs.globs.utils;

import org.crossbowlabs.globs.utils.exceptions.IOFailure;

import java.io.*;

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
      while (reader.ready()) {
        String line = reader.readLine();
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

  public static String loadStreamToString(InputStream stream) throws IOFailure {
    try {
      Reader reader = new BufferedReader(new InputStreamReader(stream));
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

  public static void dumpStringToFile(String fileName, String content) throws IOFailure {
    FileWriter writer = null;
    try {
      writer = new FileWriter(fileName);
      writer.append(content);
      writer.close();
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
    while ((readed = inputStream.read(bytes)) != -1){
      outputStream.write(bytes, 0, readed);
    }
    outputStream.close();
  }
}
