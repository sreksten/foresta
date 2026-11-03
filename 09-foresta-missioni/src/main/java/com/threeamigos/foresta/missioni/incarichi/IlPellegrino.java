package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.testi.RegoleDellaLingua;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Preposizione;

/**
 * In città una donna chiede di accompagnare suo fratello, un pellegrino, fino a un tempio nella foresta: la
 * missione rivendica il tempio e lo segna sulla mappa, il pellegrino viaggia con il gruppo come ospite (vedi
 * {@link MissioneAPassi#prendiInScorta}) e se ne separa arrivati al tempio; poi si torna dalla sorella a riscuotere
 * (vedi IncaricoInCitta). Il tempio è sicuro: all'arrivo niente avversari né oggetti a caso (vedi
 * RegistroMissioni.sopprimiContenutoLocazione).
 */
public class IlPellegrino extends IncaricoInCitta {

	private static final String PELLEGRINO = "PELLEGRINO";
	private static final int RICOMPENSA = 25;
	private static final String META = "META";
	private static final String PARTENZA = "PARTENZA";
	private static final String VIAGGIO = "VIAGGIO";

	public IlPellegrino() {
		super(MissionePredefinita.IL_PELLEGRINO);
	}

	/**
	 * Il nome del pellegrino, da missioni.txt: la missione si ripete con altri pellegrini.
	 */
	public String getPellegrino() {
		return parametro(PELLEGRINO, ProduttoreDiMissioniCasuale::nomePellegrino);
	}

	@Override
	protected void allIncarico() {
		getPellegrino();
	}

	@Override
	protected String creaIlNome() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_PELLEGRINO_TITOLO");
	}

	@Override
	public String getDescrizione() {
		return descrizioneDalModello();
	}

	@Override
	protected String produzioneDellaDescrizione(String passo) {
		return RITORNO.equals(passo) ? "IL_PELLEGRINO_DESCRIZIONE_RITORNO" : "IL_PELLEGRINO_DESCRIZIONE_VIAGGIO";
	}

	@Override
	protected Object[] valoriStabiliDellaDescrizione() {
		return valori();
	}

	/**
	 * Tutti i segnaposto dei testi della missione: il pellegrino, il tempio nelle sue forme (il nome vero solo dopo averlo trovato), la
	 * città e le monete.
	 */
	private Object[] valori() {
		RegoleDellaLingua lingua = Lingue.corrente();
		String tempio = getNomeDelTempio();
		return new Object[] {
				"PARAM_PELLEGRINO", getPellegrino(),
				"PARAM_A_TEMPIO", lingua.conPreposizione(Preposizione.A, tempio),
				"PARAM_ARRIVO_TEMPIO", lingua.conPreposizione(Preposizione.ARRIVO, tempio),
				"PARAM_TEMPIO_MAIUSC", lingua.inizialeMaiuscola(tempio),
				"PARAM_CITTA", getNomeCitta(),
				"PARAM_MONETE", getRicompensa()};
	}

	@Override
	public String getRicordoDellaLocazione() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_PELLEGRINO_RICORDO", valori());
	}

	/**
	 * Il tempio della meta, o null finché la missione non l'ha trovato.
	 */
	public CoordinateMD getTempio() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	/**
	 * Il nome del tempio della meta, con l'articolo; "il tempio" finché la missione non l'ha trovato.
	 */
	private String getNomeDelTempio() {
		return getTempio() == null ? ProduttoreDiMissioniCasuale.testoDellaMissione("IL_PELLEGRINO_TEMPIO_GENERICO") : Foresta.nomeDelTempio(getTempio());
	}

	private boolean nelTempio() {
		return getTempio() != null && getTempio().equals(Foresta.getPosizioneDelGruppo());
	}

	@Override
	protected String primoPassoDelCompito() {
		return META;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		switch (id) {
			case META:
				return cercaLocazione(MomentoControllo.IN_LOCAZIONE, TipoLocazione.TEMPIO)
						.esegui(() -> {
							Foresta.setLocazioneConosciuta(getTempio());
							BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_PELLEGRINO_TEMPIO_SEGNATO", valori())));
						})
						.poi(PARTENZA);
			case PARTENZA:
				return prendiInScorta(MomentoControllo.IN_LOCAZIONE, this::nellaCitta, getPellegrino()).poi(VIAGGIO);
			case VIAGGIO:
				return scorta(MomentoControllo.PRE_LOCAZIONE, this::getTempio)
						.aOgniControllo(() -> {
							if (nelTempio()) {
								RegistroMissioni.sopprimiContenutoLocazione(getTempio());
							}
						})
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_PELLEGRINO_ARRIVATO", valori()))))
						.poi(RITORNO);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_PELLEGRINO_INCARICO_1", valori()))
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_PELLEGRINO_INCARICO_2", valori()))
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_PELLEGRINO_INCARICO_3", valori()))
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_PELLEGRINO_INCARICO_4", valori()));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_PELLEGRINO_RINGRAZIAMENTO_1", valori()))
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_PELLEGRINO_RINGRAZIAMENTO_2", valori()))
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_PELLEGRINO_RINGRAZIAMENTO_3", valori()));
	}

	@Override
	protected String testoAccettazione() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_PELLEGRINO_ACCETTAZIONE", valori());
	}

	@Override
	protected String testoRicompensa() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_PELLEGRINO_RICOMPENSA", valori());
	}

	@Override
	protected int getRicompensaBase() {
		return RICOMPENSA;
	}
}
