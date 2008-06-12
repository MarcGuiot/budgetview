package org.designup.picsou.importer.utils;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.designup.picsou.importer.utils.RepeatableInputStream;

public class RepeatableInputStreamTest extends TestCase{

  public void test() throws Exception {
    ByteArrayInputStream stream = new ByteArrayInputStream("some data".getBytes());
    RepeatableInputStream repeatableInputStream = new RepeatableInputStream(stream);
    byte[] expected = new byte[100];
    int expectedLength = repeatableInputStream.read(expected);
    repeatableInputStream.reset();
    byte[] reread = new byte[100];
    int effectiveLength = repeatableInputStream.read(reread);
    assertEquals(expectedLength, effectiveLength);
    assertEquals(new String(expected, 0, expectedLength), new String(reread, 0, expectedLength));
  }

  public void testBigStream() throws Exception {
    byte[] bigBuffer = new byte[2342];
    bigBuffer[bigBuffer.length -1] = 'a';
    bigBuffer[bigBuffer.length -2] = 'b';
    ByteArrayInputStream stream = new ByteArrayInputStream(bigBuffer);
    RepeatableInputStream repeatableInputStream = new RepeatableInputStream(stream);
    checkContent(bigBuffer, repeatableInputStream);
    repeatableInputStream.reset();
    checkContent(bigBuffer, repeatableInputStream);
  }

  public void testResetWithReader() throws Exception {
    ByteArrayInputStream stream = new ByteArrayInputStream("some data".getBytes());
    InputStreamReader streamReader = new InputStreamReader(stream);
    assertTrue(streamReader.ready());
    RepeatableInputStream repeatableInputStream = new RepeatableInputStream(stream);
    byte[] expected = new byte[100];
    repeatableInputStream.read(expected);
    repeatableInputStream.reset();
    InputStreamReader newStreamReader = new InputStreamReader(repeatableInputStream);
    assertTrue(newStreamReader.ready());
  }

  private void checkContent(byte[] bigBuffer, RepeatableInputStream repeatableInputStream) throws IOException {
    byte[] expected = new byte[100];
    int expectedLength;
    int total = 0;
    while ((expectedLength = repeatableInputStream.read(expected)) != -1){
      total += expectedLength;
    }
    assertEquals('a', expected[total % 100  - 1]);
    assertEquals('b', expected[total % 100 - 2]);
    assertEquals(bigBuffer.length, total);
  }
}
