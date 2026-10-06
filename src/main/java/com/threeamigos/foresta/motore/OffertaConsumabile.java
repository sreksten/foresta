package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.interfacce.VistaOffertaConsumabile;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoConsumabile;

/**
 * Una voce del listino dell'alchimista (vedi OfferteAlchimista).
 */
final class OffertaConsumabile implements VistaOffertaConsumabile {

	private final TipoConsumabile tipo;
	private final ClasseIncantesimo classeIncantesimo;
	private final VistaPersonaggio personaggio;
	private final String nome;
	private final String descrizione;
	private final int costo;

	OffertaConsumabile(TipoConsumabile tipo, ClasseIncantesimo classeIncantesimo, VistaPersonaggio personaggio,
					   String nome, String descrizione, int costo) {
		this.tipo = tipo;
		this.classeIncantesimo = classeIncantesimo;
		this.personaggio = personaggio;
		this.nome = nome;
		this.descrizione = descrizione;
		this.costo = costo;
	}

	@Override
	public TipoConsumabile getTipo() {
		return tipo;
	}

	@Override
	public ClasseIncantesimo getClasseIncantesimo() {
		return classeIncantesimo;
	}

	@Override
	public VistaPersonaggio getPersonaggio() {
		return personaggio;
	}

	@Override
	public String getNome() {
		return nome;
	}

	@Override
	public String getDescrizione() {
		return descrizione;
	}

	@Override
	public int getCosto() {
		return costo;
	}
}
