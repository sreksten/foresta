package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.intermezzi.ScenaInLocanda;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.Bardo;
import com.threeamigos.foresta.personaggi.EquipaggiamentoIniziale;
import com.threeamigos.foresta.tools.Misc;

/**
 * Non sparate sul pianista: alla terza visita a una locanda nel bosco, a una visita tranquilla, il locandiere chiede
 * di riportare a casa il bardo Ugolino, che ha preso una sbronza solenne e non si regge in piedi, nella città più
 * vicina. Ugolino viaggia con il gruppo come ospite vulnerabile: arrivati, sua moglie paga e se lo porta in casa. Se
 * per strada muore, o la città viene distrutta, la missione fallisce.
 * <ol>
 * <li>INCARICO, a inizio locazione, in una locanda nel bosco alla terza visita: l'intermezzo con il locandiere;</li>
 * <li>ACCETTAZIONE, in locazione, nella stessa locanda: Ugolino si unisce al gruppo e la missione si ricorda la
 * città più vicina;</li>
 * <li>VIAGGIO, a inizio locazione, fino alla città;</li>
 * <li>ARRIVO, in locazione, nella città: la moglie paga, e la missione si completa.</li>
 * </ol>
 */
public class NonSparateSulPianista extends MissioneAPassi {

	public static final String UGOLINO = "Ugolino";
	// La terza visita: le visite precedenti alla locanda sono già contate quando si entra
	private static final int VISITE_PRECEDENTI = 2;
	private static final int RICOMPENSA = 20;
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

	@Override
	public String getDescrizione() {
		return "Ugolino il bardo ha bevuto troppo: riportalo sano e salvo a casa, " + Misc.conPreposizione("in", getNomeCitta())
				+ ", e proteggilo per strada.";
	}

	@Override
	protected boolean aspettaUnaVisitaTranquilla() {
		return true;
	}

	/**
	 * La città dove abita Ugolino, la più vicina alla locanda; null finché la missione non è accettata.
	 */
	public ClassiLocazione getCitta() {
		String citta = ottieniProprieta(CITTA);
		return citta == null ? null : ClassiLocazione.valueOf(citta);
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
							CoordinateMD locanda = gruppo.getCoordinate();
							aggiungiProprieta(LOCANDA, locanda.getX() + "," + locanda.getY());
						})
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, () -> scenaDelLocandiere().getPagine())
						.poi(ACCETTAZIONE);
			case ACCETTAZIONE:
				return prendiInScorta(MomentoControllo.IN_LOCAZIONE, this::nellaLocanda,
								() -> new Bardo(UGOLINO, EquipaggiamentoIniziale.livelloCasualeDalMondo()), true)
						.esegui(() -> {
							aggiungiProprieta(CITTA, cittaPiuVicina().name());
							BusEventi.pubblica(new NotificaTestoParagrafo("Ugolino il bardo, che non si regge in piedi, si unisce al gruppo: va riportato a casa, "
									+ Misc.conPreposizione("in", getNomeCitta()) + "."));
							attivaMissione();
						})
						.poi(VIAGGIO);
			case VIAGGIO:
				return scorta(MomentoControllo.PRE_LOCAZIONE, () -> Foresta.getCoordinateLocazioneUnica(getCitta()),
								() -> "Ugolino il bardo non ce l'ha fatta: a casa non arriverà mai. Il locandiere non ve lo perdonerà.")
						.falliscoSe(this::isCittaDistrutta, () -> getNomeCitta() + " è stata distrutta: Ugolino non ha più una casa a cui tornare.")
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, () -> scenaDellaMoglie().getPagine())
						.poi(ARRIVO);
			case ARRIVO:
				return ricompensa(MomentoControllo.IN_LOCAZIONE, RICOMPENSA,
								() -> "La moglie di Ugolino paga " + RICOMPENSA + " monete e trascina il marito in casa per un orecchio.")
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	private ScenaInLocanda scenaDelLocandiere() {
		return ScenaInLocanda.conLocandiere()
				.parlaIlLocandiere("Ah, siete voi! Proprio voi cercavo. Vedete quello là, abbracciato al liuto sotto il tavolo?")
				.parlaIlLocandiere("È Ugolino, il bardo. Stasera ha preso una sbronza solenne e non si regge in piedi.")
				.parlaIlLocandiere("Abita " + Misc.conPreposizione("in", cittaPiuVicina().getNomeProprio())
						+ ", non è lontano, ma da solo non ci arriva vivo. Me lo riportereste a casa?")
				.parlaIlCapo("Purché non canti per tutto il viaggio.")
				.parlaIlLocandiere("Questo non ve lo posso promettere.");
	}

	private ScenaInCitta scenaDellaMoglie() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante("Ugolino! Di nuovo in queste condizioni!")
				.parlaIlCapo("Lo abbiamo trovato in una locanda, abbracciato al suo liuto.")
				.parlaIlMandante("Grazie, viandanti. A lui ci penso io.");
	}

	private boolean isTerzaVisitaAUnaLocanda() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		if (gruppo.getClasseLocazioneCorrente() != ClassiLocazione.LOCANDA) {
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
	private ClassiLocazione cittaPiuVicina() {
		CoordinateMD qui = GruppoGiocatore.getIstanza().getCoordinate();
		ClassiLocazione piuVicina = null;
		int distanzaMinima = Integer.MAX_VALUE;
		for (ClassiLocazione classe : ClassiLocazione.values()) {
			if (classe.getTipoLocazione() != ClassiLocazione.TipoLocazione.CITTA || LineaTemporale.isCittaDistrutta(classe)) {
				continue;
			}
			CoordinateMD citta = Foresta.getCoordinateLocazioneUnica(classe);
			if (citta == null) {
				continue;
			}
			int distanza = Math.abs(citta.getX() - qui.getX()) + Math.abs(citta.getY() - qui.getY());
			if (distanza < distanzaMinima) {
				distanzaMinima = distanza;
				piuVicina = classe;
			}
		}
		return piuVicina;
	}
}
