package com.threeamigos.foresta.motore.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Dichiara che un certo oggetto può essere scritto e riletto da un flusso di input/output
 *
 * @author Stefano Reksten
 */
public interface Serializzabile {

	String PIPE = "|";

	void salva(PrintWriter stream) throws IOException;
	
	void leggi(BufferedReader stream) throws IOException;
	
}
