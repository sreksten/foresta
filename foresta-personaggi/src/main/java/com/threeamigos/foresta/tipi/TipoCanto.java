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

	CURATIVO,
	IPNOTICO,
	DI_VALORE,
	// Questo non è subito disponibile. Lo insegna il Bardo della locanda una volta che è stato portato a casa
	// (missione NonSparateSulPianista completata) e che sua moglie è stata scortata (missione IlViaggioDellaMoglie
	// completata). Quando si torna in una locanda, il Bardo insegna il canto.
	// (vedi CantoDelBardo.isUrloDellaBansheeAppreso)
	URLO_DELLA_BANSHEE;

	public String getNome() {
		return Testi.testoOCostante(this, "NOME");
	}

	public String getDescrizione() {
		return Testi.testoOCostante(this, "DESCRIZIONE");
	}

	/**
	 * I punti di MAGIA che costa cantarlo.
	 */
	public int getCostoLancio() {
		switch (this) {
			case CURATIVO:
				return CostantiCanti.CANTO_CURATIVO_COSTO_LANCIO;
			case IPNOTICO:
				return CostantiCanti.CANTO_IPNOTICO_COSTO_LANCIO;
			case DI_VALORE:
				return CostantiCanti.CANTO_DI_VALORE_COSTO_LANCIO;
			case URLO_DELLA_BANSHEE:
				return CostantiCanti.CANTO_URLO_DELLA_BANSHEE_COSTO_LANCIO;
			default:
				throw new IllegalStateException("Costo non previsto per " + this);
		}
	}
}
