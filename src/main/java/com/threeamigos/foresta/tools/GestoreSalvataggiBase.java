package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;

import java.io.*;

public abstract class GestoreSalvataggiBase extends GestoreSuFile implements InterfacciaGestoreSalvataggi {

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
			Logger.log(e);
			return false;
		}
		return true;
	}
}
