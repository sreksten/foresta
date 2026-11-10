package com.threeamigos.foresta.missioni.storie;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoLocazione;

/**
 * Gli Schinieri del Pellegrino Scalzo (vedi {@link RecuperoDelSacerdote}). Un pellegrino ha fatto voto di andare scalzo a tutti i santuari
 * della Foresta; i fedeli gli hanno regalato degli schinieri benedetti, che lui non porta («il voto non dice niente degli schinieri, ma non
 * si sa mai») e trascina in un sacco. Si è perso in un bosco: il gruppo lo trova e lo accompagna alla città più vicina, e lui gli lascia
 * gli schinieri, tanto va scalzo. Il pellegrino viaggia come ospite, ma non vulnerabile: i mostri lo lasciano stare.
 * <ol>
 * <li>INCARICO e ACCETTAZIONE (vedi RecuperoDelSacerdote): il bosco sulla mappa;</li>
 * <li>RICERCA, in locazione, nel bosco: il pellegrino si unisce al gruppo;</li>
 * <li>VIAGGIO, a inizio locazione, fino alla città in piedi più vicina: il pellegrino saluta e gli schinieri passano al gruppo.</li>
 * </ol>
 */
public class GliSchinieriDelPellegrino extends RecuperoDelSacerdote {

	/**
	 * La chiave degli schinieri in leggendari.txt
	 */
	public static final String CHIAVE_DEGLI_SCHINIERI = "SCHINIERI_DEL_PELLEGRINO";
	private static final String RICERCA = "RICERCA";
	private static final String VIAGGIO = "VIAGGIO";
	private static final String PELLEGRINO = "PELLEGRINO";
	// Il pellegrino non è un ostaggio: i mostri non lo attaccano
	private static final boolean VULNERABILE = false;

	public GliSchinieriDelPellegrino() {
		super(MissionePredefinita.GLI_SCHINIERI_DEL_PELLEGRINO);
	}

	@Override
	protected String prefisso() {
		return "GLI_SCHINIERI_DEL_PELLEGRINO";
	}

	@Override
	protected String chiaveDelLeggendario() {
		return CHIAVE_DEGLI_SCHINIERI;
	}

	@Override
	protected String chiParlaNellaScena() {
		return "SSCSC";
	}

	@Override
	protected TipoLocazione tipoDelPosto() {
		return TipoLocazione.BOSCO;
	}

	@Override
	protected String passoDopoLAccettazione() {
		return RICERCA;
	}

	@Override
	public String getRicordoDellaLocazione() {
		return testo("_RICORDO", "PARAM_PELLEGRINO", getNomeDelPellegrino());
	}

	/**
	 * Il pellegrino, con il nome che gli ha dato la missione.
	 */
	public String getNomeDelPellegrino() {
		return parametro(PELLEGRINO, () -> ProduttoreDiMissioniCasuale.testoDellaMissione("GLI_SCHINIERI_DEL_PELLEGRINO_NOME"));
	}

	/**
	 * Gli schinieri li lascia il pellegrino, arrivato in città.
	 */
	@Override
	protected Object[] altriValoriDellaConsegna() {
		return new Object[] {"PARAM_PELLEGRINO", getNomeDelPellegrino()};
	}

	@Override
	protected Passo costruisciGliAltriPassi(String id) {
		switch (id) {
			case RICERCA:
				return prendiInScorta(MomentoControllo.IN_LOCAZIONE, () -> Foresta.getPosizioneDelGruppo().equals(getPostoDellaStoria()),
								getNomeDelPellegrino(), VULNERABILE)
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(testo("_TROVATO", "PARAM_PELLEGRINO", getNomeDelPellegrino()))))
						.poi(VIAGGIO);
			case VIAGGIO:
				return scorta(MomentoControllo.PRE_LOCAZIONE, this::cittaPiuVicina)
						.esegui(this::consegnaIlLeggendario)
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	/**
	 * La città ancora in piedi più vicina al gruppo, dove il pellegrino va a riposarsi: se una città cade per strada, si va a un'altra.
	 */
	private CoordinateMD cittaPiuVicina() {
		CoordinateMD qui = Foresta.getPosizioneDelGruppo();
		CoordinateMD piuVicina = null;
		int distanzaMinima = Integer.MAX_VALUE;
		for (TipoLocazione tipo : TipoLocazione.values()) {
			if (tipo.getCategoriaLocazione() != CategoriaLocazione.CITTA || Foresta.isCittaDistrutta(tipo)) {
				continue;
			}
			CoordinateMD citta = Foresta.getCoordinateLocazioneUnica(tipo);
			if (citta == null || qui == null) {
				continue;
			}
			int distanza = Math.abs(citta.getX() - qui.getX()) + Math.abs(citta.getY() - qui.getY());
			if (distanza < distanzaMinima) {
				distanzaMinima = distanza;
				piuVicina = citta;
			}
		}
		return piuVicina;
	}
}
