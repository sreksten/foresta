package com.threeamigos.foresta.tools;

import java.util.List;

public interface InterfacciaGestoreSalvataggi {

	public static final int NUMERO_MASSIMO = 5;

	public interface InterfacciaTestataSalvataggio {

		public String getId();

		public String getDescrizione();

	}

	public interface InterfacciaSalvataggio extends InterfacciaTestataSalvataggio {

		public String getContenuto();

		public void setContenuto(String contenuto);

	}

	public List<InterfacciaTestataSalvataggio> getSalvataggiDisponibili();

	public boolean leggi(String id);

	public void salva(String id, String descrizione);

}
