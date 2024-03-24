package com.threeamigos.foresta.incantesimi;

import java.util.function.Supplier;

import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.tools.Random;

public enum ClassiIncantesimo {

	ARIA(Aria::new, Comando.ARIA),
	ACQUA(Acqua::new, Comando.ACQUA),
	TERRA(Terra::new, Comando.TERRA),
	FUOCO(Fuoco::new, Comando.FUOCO),
	FULMINE(Fulmine::new, Comando.FULMINE),
	MORTE(Morte::new, Comando.MORTE),
	RESURREZIONE(Resurrezione::new, Comando.RESURREZIONE);

	private Supplier<Incantesimo> supplier;
	private Comando comandoDiAttivazione;

	private ClassiIncantesimo(Supplier<Incantesimo> supplier, Comando comando) {
		this.supplier = supplier;
		this.comandoDiAttivazione = comando;
	}

	public Incantesimo getIstanza() {
		return supplier.get();
	}

	public Comando getComandoDiAttivazione() {
		return comandoDiAttivazione;
	}

	public static final Incantesimo ofComando(Comando comando) {
		for (ClassiIncantesimo corrente : values()) {
			if (comando == corrente.comandoDiAttivazione) {
				return corrente.supplier.get();
			}
		}
		throw new IllegalArgumentException();
	}

	public static final ClassiIncantesimo casuale() {
		int ordinale = Random.getInt(ClassiIncantesimo.values().length);
		for (ClassiIncantesimo corrente : ClassiIncantesimo.values()) {
			if (corrente.ordinal() == ordinale) {
				return corrente;
			}
		}
		throw new IllegalArgumentException();
	}
}
