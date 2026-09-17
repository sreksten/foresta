package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;

import java.io.*;

public abstract class GestoreSalvataggiBase extends GestoreSuFile implements InterfacciaGestoreSalvataggi {

	protected abstract void salva(Salvataggio salvataggio);
	
	protected abstract Salvataggio recuperaSalvataggio(String id);
	
	public final void salva(String id, String descrizione) {
		try {
			StringWriter out = new StringWriter();
			PrintWriter writer = new PrintWriter(out);
			ModelloDati.getIstanza().salva(writer);
			writer.flush();
			SalvataggioImpl salvataggio = new SalvataggioImpl();
			salvataggio.setId(id);
			salvataggio.setNome(descrizione);
			salvataggio.setContenuto(out.toString());
			salva(salvataggio);
		} catch (Exception e) {
			Logger.log(e);
		}
	}

	public boolean leggiTestata(String id, ModelloDati modelloDati) {
		Salvataggio salvataggio = recuperaSalvataggio(id);
		if (salvataggio == null) {
			return false;
		}
		try {
			modelloDati.leggiTestata(new BufferedReader(new StringReader(salvataggio.getContenuto())));
		} catch (IOException e) {
			Logger.log(e);
			return false;
		}
		return true;
	}

	public boolean leggi(String id, ModelloDati modelloDati) {
		Salvataggio salvataggio = recuperaSalvataggio(id);
		if (salvataggio == null) {
			return false;
		}
		try {
			modelloDati.leggi(new BufferedReader(new StringReader(salvataggio.getContenuto())));
		} catch (IOException e) {
			Logger.log(e);
			return false;
		}
		return true;
	}
}
