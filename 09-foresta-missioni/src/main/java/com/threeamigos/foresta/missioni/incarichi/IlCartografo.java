package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Genere;

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
	protected String creaIlNome() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_CARTOGRAFO_TITOLO");
	}

	@Override
	public String getDescrizione() {
		return descrizioneDalModello();
	}

	@Override
	protected String produzioneDellaDescrizione(String passo) {
		return RITORNO.equals(passo) ? "IL_CARTOGRAFO_DESCRIZIONE_RITORNO" : "IL_CARTOGRAFO_DESCRIZIONE_ESPLORAZIONE";
	}

	@Override
	protected Object[] valoriStabiliDellaDescrizione() {
		return valori();
	}

	@Override
	protected Object[] valoriDellaDescrizione() {
		return new Object[] {"PARAM_ESPLORATE", getCaselleEsplorate()};
	}

	/**
	 * Tutti i segnaposto dei testi della missione che non cambiano mentre dura: la città, le zone da esplorare e le monete.
	 */
	private Object[] valori() {
		return new Object[] {"PARAM_CITTA", getNomeCitta(), "PARAM_CASELLE", Lingue.corrente().cardinale(getCaselle(), Genere.FEMMINILE), "PARAM_MONETE", getRicompensa()};
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
				.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_CARTOGRAFO_ESPLORATO", valori()))))
				.poi(RITORNO);
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_CARTOGRAFO_INCARICO_1", valori()))
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_CARTOGRAFO_INCARICO_2", valori()))
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_CARTOGRAFO_INCARICO_3", valori()))
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_CARTOGRAFO_INCARICO_4", valori()));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_CARTOGRAFO_RINGRAZIAMENTO_1", valori()))
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_CARTOGRAFO_RINGRAZIAMENTO_2", valori()));
	}

	@Override
	protected String testoAccettazione() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_CARTOGRAFO_ACCETTAZIONE", valori());
	}

	@Override
	protected String testoRicompensa() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_CARTOGRAFO_RICOMPENSA", valori());
	}

	/**
	 * Tre monete per zona, più cinque.
	 */
	@Override
	protected int getRicompensaBase() {
		return 3 * getCaselle() + 5;
	}
}
