package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.motore.Comando;

import java.util.List;

public interface InterfacciaGestoreSalvataggi {

	int NUMERO_MASSIMO = 5;

	List<TestataSalvataggio> getSalvataggiDisponibili();

	boolean leggi(Comando id);

	void salva(Comando id);

}
