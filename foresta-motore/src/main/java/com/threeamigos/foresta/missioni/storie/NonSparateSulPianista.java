package com.threeamigos.foresta.missioni.storie;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.intermezzi.ScenaInLocanda;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.personaggi.EquipaggiamentoIniziale;
import com.threeamigos.foresta.strumenti.Misc;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tipi.PersonaggioNoto;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.Passo;

/**
 * Non sparate sul pianista: alla terza visita a una locanda nel bosco, a una visita tranquilla, il locandiere chiede
 * di riportare a casa un bardo che ha preso una sbronza solenne e non si regge in piedi, nella città più vicina. Il
 * bardo viaggia con il gruppo come ospite, ma **non vulnerabile**: è così ubriaco che i mostri non si curano di lui, quindi
 * non viene attaccato e non può morire. Arrivati, sua moglie paga e se lo porta in casa. Se la città viene distrutta, la
 * missione fallisce. Il bardo è il BardoLocanda, lo stesso in tutto il mondo (vedi
 * RegistroPersonaggi). Si fa una volta sola: finita, bene o male, non ne lascia una nuova (poi il bardo chiede un altro favore,
 * vedi IlViaggioDellaMoglie).
 * <ol>
 * <li>INCARICO, a inizio locazione, in una locanda nel bosco alla terza visita: l'intermezzo con il locandiere;</li>
 * <li>ACCETTAZIONE, in locazione, nella stessa locanda: il bardo si unisce al gruppo e la missione si ricorda la
 * città più vicina;</li>
 * <li>VIAGGIO, a inizio locazione, fino alla città;</li>
 * <li>ARRIVO, in locazione, nella città: la moglie paga, e la missione si completa.</li>
 * </ol>
 */
public class NonSparateSulPianista extends MissioneAPassi {

	private static final String BARDO = "BARDO";
	// La terza visita: le visite precedenti alla locanda sono già contate quando si entra
	private static final int VISITE_PRECEDENTI = 2;
	// La paga base, per il primo livello del mondo: si moltiplica per il livello di quando il bardo si affida al gruppo
	private static final int RICOMPENSA = 20;
	private static final String LIVELLO_DELL_INCARICO = "LIVELLO_DELL_INCARICO";
	private static final String INCARICO = "INCARICO";
	private static final String ACCETTAZIONE = "ACCETTAZIONE";
	private static final String VIAGGIO = "VIAGGIO";
	private static final String ARRIVO = "ARRIVO";
	private static final String CITTA = "CITTA";
	private static final String LOCANDA = "LOCANDA";

	public NonSparateSulPianista() {
		super(ClasseMissione.NON_SPARATE_SUL_PIANISTA);
	}

	@Override
	public String getNome() {
		return "Non sparate sul pianista";
	}

	/**
	 * Il nome del bardo: quello del bardo della locanda, che è lo stesso in tutto il mondo (vedi RegistroPersonaggi).
	 */
	public String getBardo() {
		return parametro(BARDO, () -> RegistroPersonaggi.getResidente(PersonaggioNoto.BARDO).getNome());
	}

	@Override
	public String getDescrizione() {
		return getBardo() + " il bardo ha bevuto troppo: riportalo sano e salvo a casa, " + Misc.conPreposizione("in", getNomeCitta())
				+ ", e proteggilo per strada.";
	}

	@Override
	protected boolean aspettaUnaVisitaTranquilla() {
		return true;
	}

	/**
	 * La città dove abita il bardo, la più vicina alla locanda; null finché la missione non è accettata.
	 */
	public TipoLocazione getCitta() {
		String citta = ottieniProprieta(CITTA);
		return citta == null ? null : TipoLocazione.valueOf(citta);
	}

	private String getNomeCitta() {
		return getCitta() == null ? "la sua città" : getCitta().getNomeProprio();
	}

	@Override
	protected String passoIniziale() {
		return INCARICO;
	}

	@Override
	protected Passo costruisciPasso(String id) {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		switch (id) {
			case INCARICO:
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, () -> isTerzaVisitaAUnaLocanda() && cittaPiuVicina() != null && isVisitaTranquilla())
						.esegui(() -> {
							getBardo();
							CoordinateMD locanda = gruppo.getCoordinate();
							aggiungiProprieta(LOCANDA, locanda.getX() + "," + locanda.getY());
						})
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, () -> scenaDelLocandiere().getPagine())
						.poi(ACCETTAZIONE);
			case ACCETTAZIONE:
				return prendiInScorta(MomentoControllo.IN_LOCAZIONE, this::nellaLocanda,
								() -> RegistroPersonaggi.costruisciPersonaggio(RegistroPersonaggi.getResidente(PersonaggioNoto.BARDO),
										EquipaggiamentoIniziale.livelloCasualeDalMondo()), false)
						.esegui(() -> {
							aggiungiProprieta(CITTA, cittaPiuVicina().name());
							aggiungiProprieta(LIVELLO_DELL_INCARICO, String.valueOf(Statistiche.getLivello()));
							BusEventi.pubblica(new NotificaTestoParagrafo(getBardo() + " il bardo, che non si regge in piedi, si unisce al gruppo: va riportato a casa, "
									+ Misc.conPreposizione("in", getNomeCitta()) + ". È così ubriaco che i mostri non si curano nemmeno di lui."));
							attivaMissione();
						})
						.poi(VIAGGIO);
			case VIAGGIO:
				return scorta(MomentoControllo.PRE_LOCAZIONE, () -> Foresta.getCoordinateLocazioneUnica(getCitta()), false)
						.falliscoSe(this::isCittaDistrutta, () -> getNomeCitta() + " è stata distrutta: " + getBardo() + " non ha più una casa a cui tornare.")
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, () -> scenaDellaMoglie().getPagine())
						.poi(ARRIVO);
			case ARRIVO:
				return ricompensa(MomentoControllo.IN_LOCAZIONE, getRicompensa(),
								() -> RegistroPersonaggi.getResidente(PersonaggioNoto.MOGLIE_DEL_BARDO).getNome() + ", la moglie di " + getBardo() + ", paga "
										+ getRicompensa() + " monete e trascina il marito in casa per un orecchio.")
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	/**
	 * Quanto paga la moglie del bardo: la paga base per il livello del mondo di quando il bardo si è affidato al gruppo.
	 */
	private int getRicompensa() {
		String livello = ottieniProprieta(LIVELLO_DELL_INCARICO);
		return RICOMPENSA * (livello != null ? Integer.parseInt(livello) : Statistiche.getLivello());
	}

	private ScenaInLocanda scenaDelLocandiere() {
		return ScenaInLocanda.conLocandiere()
				.parlaIlLocandiere("Ah, siete voi! Proprio voi cercavo. Vedete quello là, abbracciato al liuto sotto il tavolo?")
				.parlaIlLocandiere("È " + getBardo() + ", il bardo. Stasera ha preso una sbronza solenne e non si regge in piedi.")
				.parlaIlLocandiere("Abita " + Misc.conPreposizione("in", cittaPiuVicina().getNomeProprio())
						+ ", non è lontano, ma da solo non ci arriva vivo. Me lo riportereste a casa?")
				.parlaIlCapo("Purché non canti per tutto il viaggio.")
				.parlaIlLocandiere("Questo non ve lo posso promettere.");
	}

	private ScenaInCitta scenaDellaMoglie() {
		// Il bardo (il BardoLocanda, con il nome che ha in tutto il mondo) è ancora nel gruppo, si congeda a fine missione:
		// la scena lo mette dopo il capo, rivolto verso la moglie
		return ScenaInCitta.conMoglieDelBardo()
				.parlaIlMandante(getBardo() + "! Di nuovo in queste condizioni!")
				.parlaIlCapo("Lo abbiamo trovato in una locanda, abbracciato al suo liuto.")
				.parlaIlMandante("Grazie, viandanti. A lui ci penso io.")
				.parlaIlMandante(getBardo() + ", non ti basteranno tre Pozioni della Salute grandi quando avrò finito con te!")
				.parlaLOspite("Qualcuno ha un boccale di Sglord?")
				.parlaIlCapo("(Non vorrei essere nei suoi panni.)");
	}

	private boolean isTerzaVisitaAUnaLocanda() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		if (gruppo.getTipoLocazioneCorrente() != TipoLocazione.LOCANDA) {
			return false;
		}
		String visite = Foresta.getLocazioneMD(gruppo.getCoordinate()).ottieniProprieta(Locanda.LOCANDA_VISITE);
		return visite != null && Integer.parseInt(visite) >= VISITE_PRECEDENTI;
	}

	private boolean nellaLocanda() {
		CoordinateMD qui = GruppoGiocatore.getIstanza().getCoordinate();
		return (qui.getX() + "," + qui.getY()).equals(ottieniProprieta(LOCANDA));
	}

	private boolean isCittaDistrutta() {
		return getCitta() != null && LineaTemporale.isCittaDistrutta(getCitta());
	}

	/**
	 * La città ancora in piedi più vicina al gruppo, o null se non ce n'è più nessuna.
	 */
	private TipoLocazione cittaPiuVicina() {
		CoordinateMD qui = GruppoGiocatore.getIstanza().getCoordinate();
		TipoLocazione piuVicina = null;
		int distanzaMinima = Integer.MAX_VALUE;
		for (TipoLocazione tipo : TipoLocazione.values()) {
			if (tipo.getCategoria() != CategoriaLocazione.CITTA || LineaTemporale.isCittaDistrutta(tipo)) {
				continue;
			}
			CoordinateMD citta = Foresta.getCoordinateLocazioneUnica(tipo);
			if (citta == null) {
				continue;
			}
			int distanza = Math.abs(citta.getX() - qui.getX()) + Math.abs(citta.getY() - qui.getY());
			if (distanza < distanzaMinima) {
				distanzaMinima = distanza;
				piuVicina = tipo;
			}
		}
		if (piuVicina != null) {
			Foresta.setLocazioneConosciuta(Foresta.getCoordinateLocazioneUnica(piuVicina));
		}
		return piuVicina;
	}
}
