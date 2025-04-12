package com.designup.siteweaver.utils;

import java.io.*;

public class FileUtils {
  public static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static final String ENCODING = "UTF-8";

  private FileUtils() {
    // Instanceless class!
  }

  public static Reader createEncodedReader(File file) throws IOException {
    return createEncodedReader(file.getAbsolutePath());
  }

  public static Reader createEncodedReader(String fileName) throws FileNotFoundException, UnsupportedEncodingException {
    return new InputStreamReader(new FileInputStream(fileName), ENCODING);
  }

  public static Writer createEncodedWriter(String outputFileName) throws IOException {
    return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileName), ENCODING));
  }

  public static String readerToString(Reader anyReader) throws IOException {
    BufferedReader reader = null;
    StringBuffer buffer;
    try {
      reader = new BufferedReader(anyReader);
      buffer = new StringBuffer();
      String line;
      boolean firstLine = true;
      while ((line = reader.readLine()) != null) {
        if (!firstLine) {
          buffer.append(LINE_SEPARATOR);
        }
        else {
          firstLine = false;
        }
        buffer.append(line);
      }
    }
    finally {
      close(reader);
    }
    return buffer.toString();
  }

  public static void dumpStringToFile(File file, String content) throws IOException {
    prepareDir(file);
    file.createNewFile();

    Writer writer = null;
    try {
      prepareDir(file);
      FileWriter fileWriter = new FileWriter(file);
      writer = new BufferedWriter(fileWriter);
      writer.write(content);
      writer.flush();
    }
    finally {
      close(writer);
    }
  }

  public static void emptyDirectory(File dir) throws Exception {
    mkdirs(dir);
    File[] list = dir.listFiles();
    if (list != null) {
      for (int j = 0; j < list.length; j++) {
        if (list[j].isDirectory()) {
          emptyDirectory(list[j]);
        }
        delete(list[j]);
      }
    }
  }

  public static void copyStreamToFile(InputStream source, File destinationFile) throws IOException {
    OutputStream output = new BufferedOutputStream(new FileOutputStream(destinationFile));
    InputStream input = new BufferedInputStream(source);
    try {
      byte[] buffer = new byte[1024];
      int nbBytesRead;
      while (true) {
        nbBytesRead = input.read(buffer);
        if (nbBytesRead == -1) {
          break;
        }
        output.write(buffer, 0, nbBytesRead);
      }
    }
    finally {
      close(input);
      close(output);
    }
  }

  public static void copyDirectory(File fromDirectory, File toDirectory, boolean forceOverwrite)
    throws IOException {
    if (!toDirectory.exists()) {
      toDirectory.mkdirs();
    }
    for (File fromFile : fromDirectory.listFiles()) {
      File toFile = new File(toDirectory, fromFile.getName());
      if (fromFile.isDirectory()) {
        copyDirectory(fromFile, toFile, forceOverwrite);
      }
      else if (!toFile.exists() ||
               forceOverwrite ||
               (fromFile.lastModified() >= toFile.lastModified()) ||
               (fromFile.length() >= toFile.length())) {
        copyStreamToFile(new FileInputStream(fromFile), toFile);
      }
    }
  }

  public static void prepareDir(File reportFile) {
    if (reportFile == null) {
      return;
    }
    File parentFile = reportFile.getParentFile();
    if (parentFile != null) {
      parentFile.mkdirs();
    }
  }

  public static void mkdirs(File currentDir) throws Exception {
    if (currentDir.exists() || currentDir.mkdirs()) {
      return;
    }
    throw new Exception("Unable to create directory: " + currentDir);
  }

  public static void delete(File file) throws Exception {
    if (!file.exists()) {
      return;
    }
    if (file.isDirectory()) {
      emptyDirectory(file);
    }
    if (!file.delete()) {
      throw new Exception("Unable to delete file: " + file);
    }
  }


  public static void copyFile(String inputFileName, Writer output) throws IOException {

    Reader input = createEncodedReader(inputFileName);
    try {
      int nextChar;
      while (true) {
        nextChar = input.read();
        if (nextChar == -1) {
          break;
        }
        else {
          output.write(nextChar);
        }
      }
    }
    catch (EOFException e) {
      // Reached end of file - nothing special to do.
    }
    finally {
      input.close();
    }
  }

  public static void close(Writer writer) {
    if (writer != null) {
      try {
        writer.close();
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static void close(Reader reader) {
    if (reader != null) {
      try {
        reader.close();
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static void close(InputStream in) {
    if (in != null) {
      try {
        in.close();
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static void close(OutputStream out) {
    if (out != null) {
      try {
        out.flush();
        out.close();
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
