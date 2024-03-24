package com.threeamigos.foresta.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;

public abstract class GestoreSalvataggiBase implements InterfacciaGestoreSalvataggi {

	public int getCardinalita() {
		return NUMERO_MASSIMO;
	}
	
	protected abstract void salva(InterfacciaGestoreSalvataggi.InterfacciaSalvataggio salvataggio);
	
	protected abstract InterfacciaGestoreSalvataggi.InterfacciaSalvataggio recuperaSalvataggio(String id);
	
	public final void salva(String id, String descrizione) {
		try {
			StringWriter out = new StringWriter();
			PrintWriter writer = new PrintWriter(out);
			ModelloDati.getIstanza().salva(writer);
			writer.flush();
			Salvataggio salvataggio = new Salvataggio();
			salvataggio.setId(id);
			salvataggio.setNome(descrizione);
			salvataggio.setContenuto(out.toString());
			salva(salvataggio);
		} catch (Exception e) {
			Logger.log(e);
		}
	}
	
	public boolean leggi(String id) {
		InterfacciaGestoreSalvataggi.InterfacciaSalvataggio salvataggio = recuperaSalvataggio(id);
		if (salvataggio == null) {
			return false;
		}
		try {
			ModelloDati.getIstanza().leggi(new BufferedReader(new StringReader(salvataggio.getContenuto())));
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}
