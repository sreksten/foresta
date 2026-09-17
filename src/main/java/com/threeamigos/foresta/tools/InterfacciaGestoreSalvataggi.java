package com.threeamigos.foresta.tools;

import java.util.List;

public interface InterfacciaGestoreSalvataggi {

	int NUMERO_MASSIMO = 5;

	interface InterfacciaTestataSalvataggio {
		 String getId();
		 String getDescrizione();
	}

	interface InterfacciaSalvataggio extends InterfacciaTestataSalvataggio {
		String getContenuto();
		void setContenuto(String contenuto);
	}

	List<InterfacciaTestataSalvataggio> getSalvataggiDisponibili();

	boolean leggi(String id);

	void salva(String id, String descrizione);

}
