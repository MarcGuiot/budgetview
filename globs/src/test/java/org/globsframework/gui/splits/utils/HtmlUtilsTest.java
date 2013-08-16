package org.globsframework.gui.splits.utils;

import junit.framework.TestCase;

public class HtmlUtilsTest extends TestCase {
  public void testCleanup() throws Exception {
    assertEquals("Les femmes & les enfants d'abord !",
                 HtmlUtils.cleanup("<html>" +
                                   "<body>" +
                                   "<p> Les <b>femmes</b><br>" +
                                   "    &amp; les <b>enfants</b></p>\n" +
                                   "<p style='margin-top:4px'>d&apos;abord&nbsp;!</p>\n" +
                                   "<body></html>"));
  }

  public void testCleanupWithDefaultJEditorPaneText() throws Exception {
    assertEquals("",
                 HtmlUtils.cleanup("<html>\n" +
                                   "  <head>\n" +
                                   "\n" +
                                   "  </head>\n" +
                                   "  <body>\n" +
                                   "    \n" +
                                   "  </body>\n" +
                                   "</html>"));
  }
}
