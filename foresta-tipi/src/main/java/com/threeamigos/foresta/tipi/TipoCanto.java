package com.threeamigos.foresta.tipi;

/**
 * I canti innati del Bardo e della Cantastorie, simili alle abilità di Maghi e Sacerdoti (anche se queste possono
 * essere lanciate anche da chi possiede una pergamena equivalente).
 * <p>
 * Similmente a {@link TipoIncantesimo}, è associato a un {@link Comando} usato dalla UI per richiederne il lancio
 * al motore.
 * <p>
 * Il nome e la descrizione di ogni canto non stanno qui ma nel file {@code tipo_canto_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link Testi}).
 * <p>
 * Vedi anche CantoDelBardo.
 *
 * @author Stefano Reksten
 */
public enum TipoCanto {

	CURATIVO(Comando.CANTO_CURATIVO),
	IPNOTICO(Comando.CANTO_IPNOTICO),
	DI_VALORE(Comando.CANTO_DI_VALORE),
	// Questo non è subito disponibile. Lo insegna il Bardo della locanda una volta che è stato portato a casa
	// (missione NonSparateSulPianista completata) e che sua moglie è stata scortata (missione IlViaggioDellaMoglie
	// completata). Quando si torna in una locanda, il Bardo insegna il canto.
	// (vedi CantoDelBardo.isUrloDellaBansheeAppreso)
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
