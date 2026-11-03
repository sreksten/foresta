package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.IncontroDiMissione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * In città c'è una taglia sul capo di una banda di hobgoblin che si nasconde in un bosco: la missione rivendica il
 * bosco, lo segna sulla mappa e ci mette la banda (vedi {@link MissioneAPassi#combatti}). Sconfitta tutta la banda,
 * si torna a riscuotere (vedi IncaricoInCitta). Il nome del capo viene da missioni.txt: la missione si ripete con
 * altri capi.
 */
public class LaTagliaSullaBanda extends IncaricoInCitta {

	private static final String CAPOBANDA = "CAPOBANDA";
	public static final int HOBGOBLIN = 3;
	private static final int RICOMPENSA = 30;
	private static final String COVO = "COVO";
	private static final String CACCIA = "CACCIA";

	public LaTagliaSullaBanda() {
		super(MissionePredefinita.LA_TAGLIA_SULLA_BANDA);
	}

	/**
	 * Il nome del capobanda, da missioni.txt.
	 */
	public String getCapobanda() {
		return parametro(CAPOBANDA, ProduttoreDiMissioniCasuale::nomeCapobanda);
	}

	/**
	 * La banda di hobgoblin, con il suo capo.
	 */
	public IncontroDiMissione getBanda() {
		return IncontroDiMissione.di(TipoPersonaggio.HOBGOBLIN, HOBGOBLIN).conCapo(getCapobanda());
	}

	@Override
	protected void allIncarico() {
		getCapobanda();
	}

	@Override
	protected String creaIlNome() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("LA_TAGLIA_SULLA_BANDA_TITOLO", valori());
	}

	@Override
	public String getDescrizione() {
		return descrizioneDalModello();
	}

	@Override
	protected String produzioneDellaDescrizione(String passo) {
		return RITORNO.equals(passo) ? "LA_TAGLIA_SULLA_BANDA_DESCRIZIONE_RITORNO" : "LA_TAGLIA_SULLA_BANDA_DESCRIZIONE_CACCIA";
	}

	@Override
	protected Object[] valoriStabiliDellaDescrizione() {
		return valori();
	}

	/**
	 * Tutti i segnaposto dei testi della missione: il capo della banda, la città e le monete.
	 */
	private Object[] valori() {
		return new Object[] {"PARAM_CAPOBANDA", getCapobanda(), "PARAM_CITTA", getNomeCitta(), "PARAM_MONETE", getRicompensa()};
	}

	@Override
	public String getRicordoDellaLocazione() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("LA_TAGLIA_SULLA_BANDA_RICORDO", valori());
	}

	/**
	 * Il bosco della banda, o null finché la missione non l'ha trovato.
	 */
	public CoordinateMD getCovo() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	@Override
	protected String primoPassoDelCompito() {
		return COVO;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		switch (id) {
			case COVO:
				return cercaLocazione(MomentoControllo.IN_LOCAZIONE, TipoLocazione.BOSCO)
						.esegui(() -> {
							Foresta.setLocazioneConosciuta(getCovo());
							BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_TAGLIA_SULLA_BANDA_COVO_SEGNATO", valori())));
						})
						.poi(CACCIA);
			case CACCIA:
				return combatti(this::getCovo, getBanda())
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_TAGLIA_SULLA_BANDA_SCONFITTA", valori()))))
						.poi(RITORNO);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		return ScenaInCitta.conCapitano()
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_TAGLIA_SULLA_BANDA_INCARICO_1", valori()))
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_TAGLIA_SULLA_BANDA_INCARICO_2", valori()))
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_TAGLIA_SULLA_BANDA_INCARICO_3", valori()));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conCapitano()
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_TAGLIA_SULLA_BANDA_RINGRAZIAMENTO_1", valori()))
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_TAGLIA_SULLA_BANDA_RINGRAZIAMENTO_2", valori()))
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_TAGLIA_SULLA_BANDA_RINGRAZIAMENTO_3", valori()));
	}

	@Override
	protected String testoAccettazione() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("LA_TAGLIA_SULLA_BANDA_ACCETTAZIONE", valori());
	}

	@Override
	protected String testoRicompensa() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("LA_TAGLIA_SULLA_BANDA_RICOMPENSA", valori());
	}

	@Override
	protected int getRicompensaBase() {
		return RICOMPENSA;
	}
}
