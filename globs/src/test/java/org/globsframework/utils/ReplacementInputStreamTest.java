package org.globsframework.utils;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

public class ReplacementInputStreamTest extends TestCase {

  public void testReplace() throws Exception {
    ReplacementInputStream stream = new ReplacementInputStream(new ByteArrayInputStream("somm data to toto".getBytes()));
    stream.replace("somm data", "some data");
    stream.replace("somm tata", "somm titi");
    stream.replace("toto", "titi");
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    String s = reader.readLine();
//    assertEquals("some data to titi", s);
  }
}
