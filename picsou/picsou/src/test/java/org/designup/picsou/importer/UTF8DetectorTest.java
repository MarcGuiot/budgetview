package org.designup.picsou.importer;

import junit.framework.TestCase;

public class UTF8DetectorTest extends TestCase {

  public void testUUTF8Find() throws Exception {
    byte[] bytes = "some éà@".getBytes("UTF-8");
    UTF8Detector.Coder coder = UTF8Detector.first;
    for (byte b : bytes) {
      coder = coder.push(b);
    }
    assertNotSame(UTF8Detector.undef, coder);
  }

  public void testUndef() throws Exception {
    byte[] bytes = "some éà@".getBytes("ISO-8859-1");
    UTF8Detector.Coder coder = UTF8Detector.first;
    for (byte b : bytes) {
      coder = coder.push(b);
    }
    assertSame(UTF8Detector.undef, coder);
  }

  public void testMayBe() throws Exception {
    byte[] bytes = "some".getBytes("UTF-8");
    UTF8Detector.Coder coder = UTF8Detector.first;
    for (byte b : bytes) {
      coder = coder.push(b);
    }
    assertNotSame(UTF8Detector.undef, coder);
  }
}
