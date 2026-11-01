package com.threeamigos.foresta.missioni.storie;

import com.threeamigos.foresta.tipi.TipoIntermezzo;
import com.threeamigos.foresta.intermezzi.Locande;
import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.intermezzi.ScenaInLocanda;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.mondo.RegistroPersonaggi;
import com.threeamigos.foresta.personaggi.EquipaggiamentoIniziale;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.PersonaggioNoto;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Preposizione;
import com.threeamigos.foresta.testi.Testi;

/**
 * Non sparate sul pianista: alla terza visita a una locanda in tutto (di città o del bosco, contate insieme), se si è in
 * una locanda nel bosco e a una visita tranquilla, il locandiere chiede
 * di riportare a casa un bardo che ha preso una sbronza solenne e non si regge in piedi, nella città più vicina. Il
 * bardo viaggia con il gruppo come ospite, ma **non vulnerabile**: è così ubriaco che i mostri non si curano di lui, quindi
 * non viene attaccato e non può morire. Arrivati, sua moglie paga e se lo porta in casa. Se la città viene distrutta, la
 * missione fallisce. Il bardo è il BardoLocanda, lo stesso in tutto il mondo (vedi
 * RegistroPersonaggi). Si fa una volta sola: finita, bene o male, non ne lascia una nuova (poi il bardo chiede un altro favore,
 * vedi IlViaggioDellaMoglie).
 * <ol>
 * <li>INCARICO, a inizio locazione, in una locanda nel bosco alla terza visita a una locanda qualsiasi: l'intermezzo con il
 * locandiere, che prende il posto della scena del primo ingresso se la locanda è nuova (vedi IntermezzoLocandaPrimaVisita);</li>
 * <li>ACCETTAZIONE, in locazione, nella stessa locanda: il bardo si unisce al gruppo e la missione si ricorda la
 * città più vicina;</li>
 * <li>VIAGGIO, a inizio locazione, fino alla città;</li>
 * <li>ARRIVO, in locazione, nella città: la moglie paga, e la missione si completa.</li>
 * </ol>
 */
public class NonSparateSulPianista extends MissioneAPassi {

	private static final String BARDO = "BARDO";
	// La terza visita: le visite precedenti alle locande sono già contate quando si entra
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
		super(MissionePredefinita.NON_SPARATE_SUL_PIANISTA);
	}

	@Override
	public String getNome() {
		return Testi.formatta("missioni", "NonSparateSulPianista.t1");
	}

	/**
	 * Il nome del bardo: quello del bardo della locanda, che è lo stesso in tutto il mondo (vedi RegistroPersonaggi).
	 */
	public String getBardo() {
		return parametro(BARDO, () -> RegistroPersonaggi.getResidente(PersonaggioNoto.BARDO_DELLE_LOCANDE).getNome());
	}

	@Override
	public String getDescrizione() {
		return Testi.formatta("missioni", "NonSparateSulPianista.t2", getBardo(), Lingue.corrente().conPreposizione(Preposizione.IN, getNomeCitta()));
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
		return getCitta() == null ? Testi.formatta("missioni", "NonSparateSulPianista.s1") : getCitta().getNomeProprio();
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
							// Il locandiere dice dove abita il bardo: da qui la città è nota, non prima (la condizione si controlla a ogni ingresso)
							Foresta.setLocazioneConosciuta(Foresta.getCoordinateLocazioneUnica(cittaPiuVicina()));
							CoordinateMD locanda = Foresta.getPosizioneDelGruppo();
							aggiungiProprieta(LOCANDA, locanda.getX() + "," + locanda.getY());
						})
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, () -> scenaDelLocandiere().getPagine())
						.poi(ACCETTAZIONE);
			case ACCETTAZIONE:
				return prendiInScorta(MomentoControllo.IN_LOCAZIONE, this::nellaLocanda,
								() -> RegistroPersonaggi.costruisciPersonaggio(RegistroPersonaggi.getResidente(PersonaggioNoto.BARDO_DELLE_LOCANDE),
										EquipaggiamentoIniziale.livelloCasuale(Statistiche.getLivello())), false)
						.esegui(() -> {
							aggiungiProprieta(CITTA, cittaPiuVicina().name());
							aggiungiProprieta(LIVELLO_DELL_INCARICO, String.valueOf(Statistiche.getLivello()));
							BusEventi.pubblica(new NotificaTestoParagrafo(Testi.formatta("missioni", "NonSparateSulPianista.s2", getBardo(), Lingue.corrente().conPreposizione(Preposizione.IN, getNomeCitta()))));
							attivaMissione();
						})
						.poi(VIAGGIO);
			case VIAGGIO:
				return scorta(MomentoControllo.PRE_LOCAZIONE, () -> Foresta.getCoordinateLocazioneUnica(getCitta()), false)
						.falliscoSe(this::isCittaDistrutta, () ->Testi.formatta("missioni", "NonSparateSulPianista.s3", getNomeCitta(), getBardo()))
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, () -> scenaDellaMoglie().getPagine())
						.poi(ARRIVO);
			case ARRIVO:
				return ricompensa(MomentoControllo.IN_LOCAZIONE, getRicompensa(),
								() ->Testi.formatta("missioni", "NonSparateSulPianista.s4", RegistroPersonaggi.getResidente(PersonaggioNoto.MOGLIE_DEL_BARDO).getNome(), getBardo(), getRicompensa()))
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
				.parlaIlLocandiere(Testi.formatta("missioni", "NonSparateSulPianista.1"))
				.parlaIlLocandiere(Testi.formatta("missioni", "NonSparateSulPianista.2", getBardo()))
				.parlaIlLocandiere(Testi.formatta("missioni", "NonSparateSulPianista.3", Lingue.corrente().conPreposizione(Preposizione.IN, cittaPiuVicina().getNomeProprio())))
				.parlaIlCapo(Testi.formatta("missioni", "NonSparateSulPianista.4"))
				.parlaIlLocandiere(Testi.formatta("missioni", "NonSparateSulPianista.5"));
	}

	private ScenaInCitta scenaDellaMoglie() {
		// Il bardo (il BardoLocanda, con il nome che ha in tutto il mondo) è ancora nel gruppo, si congeda a fine missione:
		// la scena lo mette dopo il capo, rivolto verso la moglie
		return ScenaInCitta.conMoglieDelBardo()
				.parlaIlMandante(Testi.formatta("missioni", "NonSparateSulPianista.6", getBardo()))
				.parlaIlCapo(Testi.formatta("missioni", "NonSparateSulPianista.7"))
				.parlaIlMandante(Testi.formatta("missioni", "NonSparateSulPianista.8"))
				.parlaIlMandante(Testi.formatta("missioni", "NonSparateSulPianista.9", getBardo()))
				.parlaLOspite(Testi.formatta("missioni", "NonSparateSulPianista.10"))
				.parlaIlCapo(Testi.formatta("missioni", "NonSparateSulPianista.11"));
	}

	private boolean isTerzaVisitaAUnaLocanda() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		if (Foresta.getLocazioneDelGruppo() != TipoLocazione.LOCANDA) {
			return false;
		}
		return visiteATutteLeLocande() >= VISITE_PRECEDENTI;
	}

	/**
	 * Quante volte il gruppo è entrato in una locanda, nel bosco o in città, in tutta la partita.
	 */
	private int visiteATutteLeLocande() {
		int visite = 0;
		for (int y = 0; y < Foresta.getDimensioneY(); y++) {
			for (int x = 0; x < Foresta.getDimensioneX(); x++) {
				CoordinateMD coordinate = new CoordinateMD(x, y);
				TipoLocazione tipo = Foresta.getLocazione(coordinate);
				if (tipo == TipoLocazione.LOCANDA || tipo.getCategoriaLocazione() == CategoriaLocazione.CITTA) {
					String visiteQui = Foresta.getLocazioneMD(coordinate).ottieniProprieta(Locande.LOCANDA_VISITE);
					visite += visiteQui == null ? 0 : Integer.parseInt(visiteQui);
				}
			}
		}
		return visite;
	}

	/**
	 * Se il locandiere sta per affidare il bardo al gruppo in questa locanda, o l'ha appena fatto con la sua scena e il bardo
	 * non si è ancora unito al gruppo (succede in locazione, dopo gli intermezzi di inizio locazione): la scena del primo
	 * ingresso gli cede il posto (vedi IntermezzoLocandaPrimaVisita), e non scatta nemmeno dopo la sua, con il bardo
	 * seduto nell'angolo.
	 */
	/**
	 * Nella locanda in cui il locandiere sta per chiedere di riportare a casa il bardo, la sua scena fa da benvenuto: il bardo non
	 * può essere anche seduto nell'angolo a raccontare la fiaba (vedi IntermezzoLocandaPrimaVisita).
	 */
	@Override
	public boolean bloccaIntermezzo(TipoIntermezzo tipo) {
		return tipo == TipoIntermezzo.INTERMEZZO_LOCANDA_PRIMA_VISITA && staPerAffidareIlBardo();
	}

	public boolean staPerAffidareIlBardo() {
		return INCARICO.equals(getPassoConIntermezzoInAttesa(MomentoIntermezzo.INIZIO_LOCAZIONE))
				|| (ACCETTAZIONE.equals(getPassoCorrente()) && nellaLocanda());
	}

	private boolean nellaLocanda() {
		CoordinateMD qui = Foresta.getPosizioneDelGruppo();
		return (qui.getX() + "," + qui.getY()).equals(ottieniProprieta(LOCANDA));
	}

	private boolean isCittaDistrutta() {
		return getCitta() != null && Foresta.isCittaDistrutta(getCitta());
	}

	/**
	 * La città ancora in piedi più vicina al gruppo, o null se non ce n'è più nessuna. Non fa conoscere la città al giocatore.
	 */
	private TipoLocazione cittaPiuVicina() {
		CoordinateMD qui = Foresta.getPosizioneDelGruppo();
		TipoLocazione piuVicina = null;
		int distanzaMinima = Integer.MAX_VALUE;
		for (TipoLocazione tipo : TipoLocazione.values()) {
			if (tipo.getCategoriaLocazione() != CategoriaLocazione.CITTA || Foresta.isCittaDistrutta(tipo)) {
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
		return piuVicina;
	}
}
