package org.uispec4j;

import junit.framework.AssertionFailedError;
import org.uispec4j.utils.AssertionFailureNotDetectedError;
import org.uispec4j.xml.XmlAssert;

import javax.swing.*;

public abstract class SpinnerTestCase extends UIComponentTestCase {
  protected JSpinner jSpinner;
  protected Spinner spinner;

  protected abstract SpinnerModel createSpinnerModel() throws Exception;

  protected abstract Spinner createSpinner(JSpinner jSpinner);

  protected void setUp() throws Exception {
    init();
  }

  public final void testGetComponentTypeName() throws Exception {
    assertEquals("spinner", spinner.getDescriptionTypeName());
  }

  public final void testGetDescription() throws Exception {
    String property = System.getProperty("java.specification.version");
    if ("".equals(property) || "1.6".equals(property) || "1.8".equals(property)) {
      assertEquals("JSpinner name:'marcel'\n" +
                   "  BasicArrowButton name:'Spinner.nextButton'\n" +
                   "  BasicArrowButton name:'Spinner.previousButton'\n" +
                   "  " + getTypeName() + "\n" +
                   "    JFormattedTextField text:'" + getText() + "' name:'Spinner.formattedTextField'", spinner.getDescription());
    }
    else {
      assertEquals("", spinner.getDescription());
    }
  }

  protected abstract String getTypeName();

  protected final UIComponent createComponent() {
    return spinner;
  }

  protected final void checkPreviousValueFails(String wrongPreviousValue) {
    try {
      assertTrue(spinner.previousValueEquals(wrongPreviousValue));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
      assertEquals("No previous value from the start", e.getMessage());
    }
  }

  protected final void checkNextValueFails(String wrongNextValue) {
    try {
      assertTrue(spinner.nextValueEquals(wrongNextValue));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
      assertEquals("No previous value from the end", e.getMessage());
    }
  }

  protected final void init() throws Exception {
    jSpinner = new JSpinner(createSpinnerModel());
    jSpinner.setName("marcel");
    spinner = createSpinner(jSpinner);
  }

  public void testFactory() throws Exception {
    checkFactory(jSpinner, Spinner.class);
  }

  public abstract String getText();
}
