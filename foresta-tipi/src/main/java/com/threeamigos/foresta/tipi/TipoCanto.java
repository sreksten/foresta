package com.threeamigos.foresta.tipi;

/**
 * I canti innati del Bardo e della Cantastorie (vedi CantoDelBardo e bardo.md). Sta in tipi, come {@link ClasseIncantesimo},
 * perché è un'enum di dati che il motore usa e che può servire a chi sta sotto (la UI mostra i canti per comando).
 * <p>
 * Il nome e la descrizione di ogni canto non stanno qui ma nel file {@code tipo_canto_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link Testi}).
 */
public enum TipoCanto {

	CURATIVO(Comando.CANTO_CURATIVO),
	IPNOTICO(Comando.CANTO_IPNOTICO),
	DI_VALORE(Comando.CANTO_DI_VALORE),
	// Non si conosce subito: lo insegna il bardo della locanda (vedi CantoDelBardo.isUrloDellaBansheeAppreso)
	URLO_DELLA_BANSHEE(Comando.URLO_DELLA_BANSHEE);

	private final Comando comando;

	TipoCanto(Comando comando) {
		this.comando = comando;
	}

	/**
	 * Il comando con cui il giocatore sceglie questo canto nel menu degli incantesimi
	 */
	public Comando getComando() {
		return comando;
	}

	/**
	 * @return il canto scelto con quel comando, o null se il comando non è di un canto
	 */
	public static TipoCanto ofComando(Comando comando) {
		for (TipoCanto tipo : values()) {
			if (tipo.comando == comando) {
				return tipo;
			}
		}
		return null;
	}

	public String getNome() {
		return Testi.testoOCostante(this, "NOME");
	}

	public String getDescrizione() {
		return Testi.testoOCostante(this, "DESCRIZIONE");
	}
}
