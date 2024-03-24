package com.threeamigos.foresta.motore.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

interface Serializzabile {

	static final String PIPE = "|";

	void salva(PrintWriter stream) throws IOException;
	
	void leggi(BufferedReader stream) throws IOException;
	
}
