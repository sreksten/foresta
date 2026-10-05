package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tools.Misc;

/**
 * Il cartografo: in città un cartografo vuole sapere che cosa c'è nella foresta, e paga il gruppo perché esplori un
 * certo numero di caselle mai visitate (vedi {@link MissioneAPassi#esplora}) e torni a raccontargliele. Il numero si
 * pesca quando l'incarico si offre; la missione si ripete.
 */
public class IlCartografo extends IncaricoInCitta {

	private static final String CASELLE = "CASELLE";
	public static final int CASELLE_MINIME = 6;
	public static final int CASELLE_MASSIME = 10;
	private static final String ESPLORAZIONE = "ESPLORAZIONE";

	public IlCartografo() {
		super(ClasseMissione.IL_CARTOGRAFO);
	}

	/**
	 * Quante caselle nuove vuole il cartografo.
	 */
	public int getCaselle() {
		return Integer.parseInt(parametro(CASELLE, () -> String.valueOf(Dado.tira(CASELLE_MINIME, CASELLE_MASSIME))));
	}

	@Override
	protected void allIncarico() {
		getCaselle();
	}

	@Override
	public String getNome() {
		return "Il cartografo";
	}

	@Override
	public String getDescrizione() {
		if (RITORNO.equals(getPassoCorrente())) {
			return "Hai esplorato abbastanza: torna dal cartografo di " + getNomeCitta() + " a raccontargli che cosa hai visto.";
		}
		return "Il cartografo di " + getNomeCitta() + " vuole sapere che cosa c'è in " + Misc.getCardinaleF(getCaselle())
				+ " zone della foresta dove non sei mai stato. Finora: " + getCaselleEsplorate() + ".";
	}

	@Override
	protected String primoPassoDelCompito() {
		return ESPLORAZIONE;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		if (!ESPLORAZIONE.equals(id)) {
			throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
		return esplora(getCaselle())
				.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo("Avete visto abbastanza foresta da riempire una mappa: il cartografo di "
						+ getNomeCitta() + " vi aspetta.")))
				.poi(RITORNO);
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante("Siete esploratori? Io disegno mappe, ma nella foresta non ci metto piede da anni.")
				.parlaIlMandante("Andate dove nessuno di voi è mai stato, " + Misc.getCardinaleF(getCaselle())
						+ " zone nuove, e tornate a raccontarmele. Vi pago " + getRicompensa() + " monete.")
				.parlaIlCapo("E se ci perdiamo?")
				.parlaIlMandante("Allora la mappa vi servirà ancora di più.");
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante("Raccontate, raccontate! Una palude qui, delle rovine là...")
				.parlaIlCapo("E un sacco di mostri dappertutto. Quelli segnateli bene.");
	}

	@Override
	protected String testoAccettazione() {
		return "Il cartografo pagherà " + getRicompensa() + " monete per " + Misc.getCardinaleF(getCaselle())
				+ " zone della foresta mai visitate.";
	}

	@Override
	protected String testoRicompensa() {
		return "Il cartografo ricopia tutto sulla sua mappa e paga le " + getRicompensa() + " monete promesse.";
	}

	/**
	 * Tre monete per zona, più cinque.
	 */
	@Override
	protected int getRicompensa() {
		return 3 * getCaselle() + 5;
	}
}
