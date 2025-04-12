package com.designup.siteweaver.html;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * HTML parsing utility which retrieves given HTML tags in a file.
 */
public class HtmlParser {

  private static final int BUFFER_SIZE = 5000;
  private char[] buffer = new char[BUFFER_SIZE];
  private int bufferPos = 0;

  private static final int TEMP_BUFFER_SIZE = 3000;
  private char[] tempBuffer = new char[TEMP_BUFFER_SIZE];
  private int tempBufferPos = 0;

  private Reader inputReader;
  private Writer parsedTextWriter;

  private HtmlTag currentTag;
  private String attributeName;
  private String attributeValue;

  public HtmlParser(Reader reader) {
    inputReader = reader;
  }

  public void close() {
    try {
      inputReader.close();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns the next given tag in the file. The HTML code found before
   * the tag is written in a given Writer.
   */
  public HtmlTag findNextTag(String tagName, Writer parsedTextWriter) {
    this.parsedTextWriter = parsedTextWriter;
    currentTag = new HtmlTag();
    bufferPos = 0;

    try {
      while (seekChar('<')) {
        if (parseTag(tagName)) {
          return currentTag;
        }
      }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    return null;
  }

  private char getNextChar() throws IOException {
    int readInt = inputReader.read();
    if (readInt == -1) {
      throw new EOFException();
    }
    if (bufferPos >= BUFFER_SIZE) {
      throw new RuntimeException("Buffer overflow");
    }
    buffer[bufferPos++] = (char)readInt;
    return (char)readInt;
  }

  private boolean seekChar(char targetChar) throws IOException {
    try {
      while (true) {
        int readInt = inputReader.read();
        if (readInt == -1) {
          throw new EOFException();
        }
        if (bufferPos >= (BUFFER_SIZE - 1)) {
          flushBufferUpToCurrentChar();
        }
        buffer[bufferPos++] = (char)readInt;
        char nextChar = (char)readInt;
        if (nextChar == targetChar) {
          flushBufferUpToCurrentChar();
          return true;
        }
      }
    }
    catch (EOFException e) {
      flushWholeBuffer();
    }
    return false;
  }

  private boolean parseTag(String tagName) throws IOException {

    try {
      // Skip the first whitespaces
      char currentChar = getNextChar();
      currentChar = skipWhitespace(currentChar);

      // Make sure that the current char is a letter or a digit
      if (!Character.isLetterOrDigit(currentChar) && (currentChar != '/')) {
        if (currentChar == '<') {
          flushBufferUpToCurrentChar();
          return parseTag(tagName);
        }
        return false;
      }

      // Read the remainder of the name into a temporary buffer
      tempBufferPos = 0;
      tempBuffer[tempBufferPos++] = currentChar;
      while (true) {
        currentChar = getNextChar();
        if (Character.isLetterOrDigit(currentChar)) {
          tempBuffer[tempBufferPos++] = currentChar;
        }
        else {
          if (isTempBufferEqualTo(tagName)) {
            currentTag.setName(getTempBufferAsString());
            break;
          }
          else {
            flushBufferUpToCurrentChar();
            if (currentChar == '<') {
              return parseTag(tagName);
            }
            return false;
          }
        }
      }

      // Read the tag attributes
      while (true) {

        currentChar = skipWhitespace(currentChar);

        // Check for the end of the tag
        if (currentChar == '>') {
          return true;
        }

        // Read the next attribute name into a temporary buffer
        tempBufferPos = 0;
        tempBuffer[tempBufferPos++] = currentChar;
        while (true) {
          currentChar = getNextChar();
          if (Character.isLetterOrDigit(currentChar)) {
            tempBuffer[tempBufferPos++] = currentChar;
          }
          else {
            attributeName = getTempBufferAsString();
            break;
          }
        }

        currentChar = skipWhitespace(currentChar);

        // Make sure that an '=' is placed before the value, and skip it
        if (currentChar == '=') {
          currentChar = getNextChar();
        }
        else {
          flushBufferUpToCurrentChar();
          if (currentChar == '<') {
            return parseTag(tagName);
          }
          return false;
        }

        currentChar = skipWhitespace(currentChar);

        // Make sure that the value starts with an '"' and skip it
        if (currentChar == '"') {
          currentChar = getNextChar();
        }
        else {
          flushBufferUpToCurrentChar();
          if (currentChar == '<') {
            return parseTag(tagName);
          }
          return false;
        }

        // Read the next chars up to the following '"'
        tempBufferPos = 0;
        do {
          tempBuffer[tempBufferPos++] = currentChar;
          currentChar = getNextChar();
        }
        while (currentChar != '"');

        // Skip the ending '"'
        currentChar = getNextChar();

        attributeValue = getTempBufferAsString();
        currentTag.addAttribute(attributeName, attributeValue);
      }
    }
    catch (Exception e) {
      flushWholeBuffer();
      return false;
    }
  }

  private char skipWhitespace(char current_char) throws IOException {
    while (Character.isWhitespace(current_char)) {
      current_char = getNextChar();
    }
    return current_char;
  }

  private void flushBufferUpToCurrentChar()
    throws IOException {

    if (bufferPos > 1) {

      // Flush all characters (minus the last) into the writer
      if (parsedTextWriter != null) {
        parsedTextWriter.write(buffer, 0, bufferPos - 1);
      }

      // Move the remaining char to the beginning of the buffer
      buffer[0] = buffer[bufferPos - 1];
    }

    // Update the current position
    bufferPos = 1;
  }

  private void flushWholeBuffer()
    throws IOException {
    if (parsedTextWriter != null) {
      parsedTextWriter.write(buffer, 0, bufferPos);
    }
    bufferPos = 0;
  }

  private String getTempBufferAsString() {
    return new String(tempBuffer, 0, tempBufferPos);
  }

  private boolean isTempBufferEqualTo(String target) {
    String lowerCaseTarget = target.toLowerCase();
    String temp = new String(tempBuffer, 0, tempBufferPos);
    return lowerCaseTarget.equals(temp.toLowerCase());
  }
}



