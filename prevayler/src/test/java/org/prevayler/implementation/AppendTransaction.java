package org.prevayler.implementation;

import org.prevayler.Transaction;

import java.util.Date;

public class AppendTransaction implements Transaction {

	private static final long serialVersionUID = -3830205386199825379L;
	public String toAdd;

	private AppendTransaction() {
		// Skaringa requires a default constructor, but XStream does not.
	}

	public AppendTransaction(String toAdd) {
		this.toAdd = toAdd;
	}

	public void executeOn(Object prevalentSystem, Date executionTime) {
		((StringBuffer)prevalentSystem).append(toAdd);
	}

}
