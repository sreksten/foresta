package com.threeamigos.foresta.tools;

import java.util.List;

public interface InterfacciaGestoreSalvataggi {

	int NUMERO_MASSIMO = 5;

	interface TestataSalvataggio {
		 String getId();
		 String getDescrizione();
	}

	interface Salvataggio extends TestataSalvataggio {
		String getContenuto();
		void setContenuto(String contenuto);
	}

	List<TestataSalvataggio> getSalvataggiDisponibili();

	boolean leggi(String id);

	void salva(String id, String descrizione);

}
