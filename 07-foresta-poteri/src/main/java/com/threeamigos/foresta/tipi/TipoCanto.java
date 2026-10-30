package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.testi.Testi;

/**
 * I canti innati del Bardo e della Cantastorie, simili alle abilità di Maghi e Sacerdoti (anche se queste possono
 * essere lanciate anche da chi possiede una pergamena equivalente).
 * <p>
 * Il comando con cui la UI ne chiede il lancio al motore lo dà {@link ComandiAssociati}.
 * <p>
 * Il nome e la descrizione di ogni canto non stanno qui ma nel file {@code tipo_canto_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link Testi}).
 * <p>
 * Vedi anche CantoDelBardo.
 *
 * @author Stefano Reksten
 */
public enum TipoCanto {

	CURATIVO(CostantiCanti.CANTO_CURATIVO_COSTO_LANCIO, CostantiCanti.CANTO_CURATIVO_RICARICA),
	IPNOTICO(CostantiCanti.CANTO_IPNOTICO_COSTO_LANCIO, CostantiCanti.CANTO_IPNOTICO_RICARICA),
	DI_VALORE(CostantiCanti.CANTO_DI_VALORE_COSTO_LANCIO, CostantiCanti.CANTO_DI_VALORE_RICARICA),
	// Questo non è subito disponibile. Lo insegna il Bardo della locanda una volta che è stato portato a casa
	// (missione NonSparateSulPianista completata) e che sua moglie è stata scortata (missione IlViaggioDellaMoglie
	// completata). Quando si torna in una locanda, il Bardo insegna il canto.
	// (vedi CantoDelBardo.isUrloDellaBansheeAppreso)
	URLO_DELLA_BANSHEE(CostantiCanti.CANTO_URLO_DELLA_BANSHEE_COSTO_LANCIO, CostantiCanti.CANTO_URLO_DELLA_BANSHEE_RICARICA);

	private final int costoLancio;
	private final int ricarica;

	TipoCanto(int costoLancio, int ricarica) {
		this.costoLancio = costoLancio;
		this.ricarica = ricarica;
	}

	public String getNome() {
		return Testi.testoOCostante(this, "NOME");
	}

	/**
	 * L'azione che intona questo canto: ha il suo nome preceduto da CANTO_, tranne l'Urlo della Banshee.
	 */
	public TipoAzione getTipoAzione() {
		return this == URLO_DELLA_BANSHEE ? TipoAzione.URLO_DELLA_BANSHEE : TipoAzione.valueOf("CANTO_" + name());
	}

	public String getDescrizione() {
		return Testi.testoOCostante(this, "DESCRIZIONE");
	}

	/**
	 * I punti di Magia che costa cantarlo.
	 */
	public int getCostoLancio() {
		return costoLancio;
	}

	/**
	 * Quanti turni passano prima di poterlo cantare di nuovo.
	 */
	public int getRicarica() {
		return ricarica;
	}
}
