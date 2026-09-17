package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;

import java.util.List;

public interface InterfacciaGestoreSalvataggi {

	int NUMERO_MASSIMO = 5;

	interface TestataSalvataggio {
		 String getId();
		 String getDescrizione();
		 GruppoGiocatore getGruppoGiocatore();
	}

	interface Salvataggio extends TestataSalvataggio {
		String getContenuto();
		void setContenuto(String contenuto);
	}

	List<TestataSalvataggio> getSalvataggiDisponibili();

	boolean leggiTestata(String id, ModelloDati modelloDati);

	boolean leggi(String id, ModelloDati modelloDati);

	void salva(String id, String descrizione);

}
