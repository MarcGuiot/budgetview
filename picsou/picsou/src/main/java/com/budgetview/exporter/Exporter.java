package com.budgetview.exporter;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.exceptions.ResourceAccessFailed;

import java.io.Writer;
import java.io.IOException;

public interface Exporter {

  String getType();

  String getExtension();

  void export(GlobRepository repository, Writer writer) throws ResourceAccessFailed, IOException;
}
