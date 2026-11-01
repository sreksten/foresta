package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Genere;
import com.threeamigos.foresta.testi.Testi;

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
		super(MissionePredefinita.IL_CARTOGRAFO);
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
		return Testi.formatta("missioni", "IlCartografo.t1");
	}

	@Override
	public String getDescrizione() {
		if (RITORNO.equals(getPassoCorrente())) {
			return Testi.formatta("missioni", "IlCartografo.t2", getNomeCitta());
		}
		return Testi.formatta("missioni", "IlCartografo.t3", getNomeCitta(), Lingue.corrente().cardinale(getCaselle(), Genere.FEMMINILE), getCaselleEsplorate());
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
				.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(Testi.formatta("missioni", "IlCartografo.s1", getNomeCitta()))))
				.poi(RITORNO);
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante(Testi.formatta("missioni", "IlCartografo.1"))
				.parlaIlMandante(Testi.formatta("missioni", "IlCartografo.2", Lingue.corrente().cardinale(getCaselle(), Genere.FEMMINILE), getRicompensa()))
				.parlaIlCapo(Testi.formatta("missioni", "IlCartografo.3"))
				.parlaIlMandante(Testi.formatta("missioni", "IlCartografo.4"));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante(Testi.formatta("missioni", "IlCartografo.5"))
				.parlaIlCapo(Testi.formatta("missioni", "IlCartografo.6"));
	}

	@Override
	protected String testoAccettazione() {
		return Testi.formatta("missioni", "IlCartografo.t4", getRicompensa(), Lingue.corrente().cardinale(getCaselle(), Genere.FEMMINILE));
	}

	@Override
	protected String testoRicompensa() {
		return Testi.formatta("missioni", "IlCartografo.t5", getRicompensa());
	}

	/**
	 * Tre monete per zona, più cinque.
	 */
	@Override
	protected int getRicompensaBase() {
		return 3 * getCaselle() + 5;
	}
}
