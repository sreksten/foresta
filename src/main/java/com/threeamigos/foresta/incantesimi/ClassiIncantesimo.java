package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.Dado;

import java.util.function.Supplier;

public enum ClassiIncantesimo {

	ARIA(Aria::new, Comando.ARIA),
	ACQUA(Acqua::new, Comando.ACQUA),
	TERRA(Terra::new, Comando.TERRA),
	FUOCO(Fuoco::new, Comando.FUOCO),
	FULMINE(Fulmine::new, Comando.FULMINE),
	MORTE(Morte::new, Comando.MORTE),
	RESURREZIONE(Resurrezione::new, Comando.RESURREZIONE);

	private final Supplier<Incantesimo> supplier;
	private final Comando comandoDiAttivazione;

	ClassiIncantesimo(Supplier<Incantesimo> supplier, Comando comando) {
		this.supplier = supplier;
		this.comandoDiAttivazione = comando;
	}

	public Incantesimo getIstanza() {
		return supplier.get();
	}

	public Comando getComandoDiAttivazione() {
		return comandoDiAttivazione;
	}

	public static Incantesimo ofComando(Comando comando) {
		for (ClassiIncantesimo corrente : values()) {
			if (comando == corrente.comandoDiAttivazione) {
				return corrente.supplier.get();
			}
		}
		throw new IllegalArgumentException("Comando invalido per incantesimo: " + comando);
	}

	public static ClassiIncantesimo casuale() {
		int ordinale = Dado.tira(ClassiIncantesimo.values().length) - 1;
		for (ClassiIncantesimo corrente : ClassiIncantesimo.values()) {
			if (corrente.ordinal() == ordinale) {
				return corrente;
			}
		}
		throw new IllegalStateException("Errore nella generazione casuale di un incantesimo");
	}
}
