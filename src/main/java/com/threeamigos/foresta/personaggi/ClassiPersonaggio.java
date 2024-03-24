package com.threeamigos.foresta.personaggi;

import java.util.function.Supplier;

public enum ClassiPersonaggio {

	ARPIA(Arpia::new),
	CENTAURO(Centauro::new),
	CHIMERA(Chimera::new),
	CHIMERA_DRAGO(ChimeraDrago::new),
	DRAGO(Drago::new),
	EREMITA(Eremita::new),
	FANTASMA(Fantasma::new),
	FOLLETTO(Folletto::new),
	GARGOYLE(Gargoyle::new),
	GIGANTE(Gigante::new),
	GOBLIN(Goblin::new),
	HOBGOBLIN(Hobgoblin::new),
	IDRA(Idra::new),
	LICH(Lich::new),
	MINOTAURO(Minotauro::new),
	MINOTAURO_GIGANTE(MinotauroGigante::new),
	OMBRA_NERA(OmbraNera::new),
	SCHELETRO(Scheletro::new),
	SPETTRO(Spettro::new),
	SPIRITO(Spirito::new),
	STREGA(Strega::new),
	TITANO(Titano::new),
	TROLL(Troll::new),
	VIVERNA(Viverna::new),

	BARDO(Bardo::new),
	CANTASTORIE(Cantastorie::new),
	ELFA(Elfa::new),
	ELFO(Elfo::new),
	GUERRIERA(Guerriera::new),
	GUERRIERO(Guerriero::new),
	LADRA(Ladra::new),
	LADRO(Ladro::new),
	MAGA(Maga::new),
	MAGO(Mago::new),
	OMBRAFIAMMA(OmbraFiamma::new);

	private Supplier<Personaggio> supplier;
	private int quantitaMassima = 1;

	private ClassiPersonaggio(Supplier<Personaggio> supplier) {
		this.supplier = supplier;
	}

	public Personaggio getIstanza() {
		return supplier.get();
	}
	
	void setQuantitaMassima(int quantitaMassima) {
		this.quantitaMassima = quantitaMassima;
	}

	public int getQuantitaMassima() {
		return quantitaMassima;
	}
}
