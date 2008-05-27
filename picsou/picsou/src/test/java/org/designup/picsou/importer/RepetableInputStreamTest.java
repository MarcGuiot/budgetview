package org.designup.picsou.importer;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.crossbowlabs.globs.utils.TestUtils;

public class RepetableInputStreamTest extends TestCase{

  public void test() throws Exception {
    ByteArrayInputStream stream = new ByteArrayInputStream("some data".getBytes());
    RepetableInputStream repetableInputStream = new RepetableInputStream(stream);
    byte[] expected = new byte[100];
    int expectedLength = repetableInputStream.read(expected);
    repetableInputStream.reset();
    byte[] reread = new byte[100];
    int effectiveLength = repetableInputStream.read(reread);
    assertEquals(expectedLength, effectiveLength);
    assertEquals(new String(expected, 0, expectedLength), new String(reread, 0, expectedLength));
  }

  public void testBigStream() throws Exception {
    byte[] bigBuffer = new byte[2342];
    bigBuffer[bigBuffer.length -1] = 'a';
    bigBuffer[bigBuffer.length -2] = 'b';
    ByteArrayInputStream stream = new ByteArrayInputStream(bigBuffer);
    RepetableInputStream repetableInputStream = new RepetableInputStream(stream);
    checkContent(bigBuffer, repetableInputStream);
    repetableInputStream.reset();
    checkContent(bigBuffer, repetableInputStream);
  }

  public void testResetWithReader() throws Exception {
    ByteArrayInputStream stream = new ByteArrayInputStream("some data".getBytes());
    InputStreamReader streamReader = new InputStreamReader(stream);
    assertTrue(streamReader.ready());
    RepetableInputStream repetableInputStream = new RepetableInputStream(stream);
    byte[] expected = new byte[100];
    repetableInputStream.read(expected);
    repetableInputStream.reset();
    InputStreamReader newStreamReader = new InputStreamReader(repetableInputStream);
    assertTrue(newStreamReader.ready());
  }

  private void checkContent(byte[] bigBuffer, RepetableInputStream repetableInputStream) throws IOException {
    byte[] expected = new byte[100];
    int expectedLength;
    int total = 0;
    while ((expectedLength = repetableInputStream.read(expected)) != -1){
      total += expectedLength;
    }
    assertEquals('a', expected[total % 100  - 1]);
    assertEquals('b', expected[total % 100 - 2]);
    assertEquals(bigBuffer.length, total);
  }
}
