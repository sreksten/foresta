package com.threeamigos.foresta.missioni.storie;

import com.threeamigos.foresta.tipi.TipoIntermezzo;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.intermezzi.Locande;

import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.intermezzi.ScenaInLocanda;
import com.threeamigos.foresta.missioni.InSorteggio;
import com.threeamigos.foresta.missioni.LocandaDelRacconto;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.incarichi.SceltaDegliIncarichi;
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
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Preposizione;
import com.threeamigos.foresta.testi.Testi;

/**
 * Non sparate sul pianista: alla terza locanda (vedi LocandaDelRacconto.isTerzaLocanda: una locanda nel bosco, nuova o no, quando il gruppo è
 * già entrato due volte in una locanda), a una visita tranquilla, il locandiere chiede
 * di riportare a casa un bardo che ha preso una sbronza solenne e non si regge in piedi, nella città più vicina. Il
 * bardo viaggia con il gruppo come ospite, ma **non vulnerabile**: è così ubriaco che i mostri non si curano di lui, quindi
 * non viene attaccato e non può morire. Arrivati, sua moglie paga e se lo porta in casa. Se la città viene distrutta, la
 * missione fallisce. Il bardo è il BardoLocanda, lo stesso in tutto il mondo (vedi
 * RegistroPersonaggi). Si fa una volta sola: finita, bene o male, non ne lascia una nuova (poi il bardo chiede un altro favore,
 * vedi IlViaggioDellaMoglie).
 * <ol>
 * <li>INCARICO, a inizio locazione, alla terza locanda: l'intermezzo con il locandiere, e nessun'altra scena in quella visita (il
 * benvenuto di una locanda nuova slitta alla visita dopo, la recensione è di ripiego). Nel sorteggio delle missioni (SceltaDegliIncarichi)
 * ha la precedenza sulla leggenda del locandiere e sulle missioni del sacerdote;</li>
 * <li>ACCETTAZIONE, in locazione, nella stessa locanda: il bardo si unisce al gruppo e la missione si ricorda la
 * città più vicina;</li>
 * <li>VIAGGIO, a inizio locazione, fino alla città;</li>
 * <li>ARRIVO, in locazione, nella città: la moglie paga, e la missione si completa.</li>
 * </ol>
 */
public class NonSparateSulPianista extends MissioneAPassi implements InSorteggio {

	private static final String BARDO = "BARDO";
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
	protected String creaIlNome() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("NON_SPARATE_SUL_PIANISTA_TITOLO");
	}

	/**
	 * Il nome del bardo: quello del bardo della locanda, che è lo stesso in tutto il mondo (vedi RegistroPersonaggi).
	 */
	public String getBardo() {
		return parametro(BARDO, () -> RegistroPersonaggi.getResidente(PersonaggioNoto.BARDO_DELLE_LOCANDE).getNome());
	}

	@Override
	public String getDescrizione() {
		return descrizioneDalModello();
	}

	@Override
	protected String produzioneDellaDescrizione(String passo) {
		return "NON_SPARATE_SUL_PIANISTA_DESCRIZIONE";
	}

	@Override
	protected Object[] valoriStabiliDellaDescrizione() {
		return valori();
	}

	/**
	 * Tutti i segnaposto dei testi della storia: il bardo, la città dove abita (la sua città, finché non è accettata) e le monete.
	 * Senza "private" per i test.
	 */
	Object[] valori() {
		return new Object[] {"PARAM_BARDO", getBardo(), "PARAM_CITTA", getNomeCitta(),
				"PARAM_IN_CITTA", Lingue.corrente().conPreposizione(Preposizione.IN, getNomeCitta()), "PARAM_MONETE", getRicompensa()};
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
		return getCitta() == null ? ProduttoreDiMissioniCasuale.testoDellaMissione("NON_SPARATE_SUL_PIANISTA_SUA_CITTA") : getCitta().getNomeProprio();
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
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, () -> isProntoPerOffrirsi() && SceltaDegliIncarichi.tocca(this))
						.esegui(() -> {
							SceltaDegliIncarichi.affidato(this);
							getBardo();
							// Il locandiere dice dove abita il bardo: da qui la città è nota, non prima (la condizione si controlla a ogni ingresso)
							Foresta.setLocazioneConosciuta(Foresta.getCoordinateLocazioneUnica(cittaPiuVicina()));
							CoordinateMD locanda = Foresta.getPosizioneDelGruppo();
							aggiungiProprieta(LOCANDA, locanda.getX() + "," + locanda.getY());
							// In una locanda nuova la scena del locandiere prende il posto del benvenuto, che scatterà alla visita dopo
							LocazioneMD qui = Foresta.getLocazioneMD(locanda);
							if (qui.ottieniProprieta(Locande.LOCANDA_VISITATA) == null) {
								qui.aggiungiProprieta(Locande.LOCANDA_BENVENUTO_RIMANDATO, LocazioneMD.AFFERMATIVO);
							}
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
							BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("NON_SPARATE_SUL_PIANISTA_ACCETTATO", valori())));
							attivaMissione();
						})
						.poi(VIAGGIO);
			case VIAGGIO:
				return scorta(MomentoControllo.PRE_LOCAZIONE, () -> Foresta.getCoordinateLocazioneUnica(getCitta()), false)
						.falliscoSe(this::isCittaDistrutta, () ->ProduttoreDiMissioniCasuale.testoDellaMissione("NON_SPARATE_SUL_PIANISTA_CITTA_DISTRUTTA", valori()))
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, () -> scenaDellaMoglie().getPagine())
						.poi(ARRIVO);
			case ARRIVO:
				return ricompensa(MomentoControllo.IN_LOCAZIONE, getRicompensa(),
								() ->ProduttoreDiMissioniCasuale.testoDellaMissione("NON_SPARATE_SUL_PIANISTA_RICOMPENSA", "PARAM_MOGLIE", RegistroPersonaggi.getResidente(PersonaggioNoto.MOGLIE_DEL_BARDO).getNome(), "PARAM_BARDO", getBardo(), "PARAM_MONETE", getRicompensa()))
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
				.parlaIlLocandiere(ProduttoreDiMissioniCasuale.testoDellaMissione("NON_SPARATE_SUL_PIANISTA_INCONTRO_1"))
				.parlaIlLocandiere(ProduttoreDiMissioniCasuale.testoDellaMissione("NON_SPARATE_SUL_PIANISTA_INCONTRO_2", "PARAM_BARDO", getBardo()))
				.parlaIlLocandiere(ProduttoreDiMissioniCasuale.testoDellaMissione("NON_SPARATE_SUL_PIANISTA_INCONTRO_3", "PARAM_IN_CITTA", Lingue.corrente().conPreposizione(Preposizione.IN, cittaPiuVicina().getNomeProprio())))
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("NON_SPARATE_SUL_PIANISTA_INCONTRO_4"))
				.parlaIlLocandiere(ProduttoreDiMissioniCasuale.testoDellaMissione("NON_SPARATE_SUL_PIANISTA_INCONTRO_5"));
	}

	private ScenaInCitta scenaDellaMoglie() {
		// Il bardo (il BardoLocanda, con il nome che ha in tutto il mondo) è ancora nel gruppo, si congeda a fine missione:
		// la scena lo mette dopo il capo, rivolto verso la moglie
		return ScenaInCitta.conMoglieDelBardo()
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("NON_SPARATE_SUL_PIANISTA_MOGLIE_1", "PARAM_BARDO", getBardo()))
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("NON_SPARATE_SUL_PIANISTA_MOGLIE_2"))
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("NON_SPARATE_SUL_PIANISTA_MOGLIE_3"))
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("NON_SPARATE_SUL_PIANISTA_MOGLIE_4", "PARAM_BARDO", getBardo()))
				.parlaLOspite(ProduttoreDiMissioniCasuale.testoDellaMissione("NON_SPARATE_SUL_PIANISTA_MOGLIE_5"))
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("NON_SPARATE_SUL_PIANISTA_MOGLIE_6"));
	}

	@Override
	public boolean isInAttesaDiOffrirsi() {
		return !isAttiva() && !isCompleta() && !isFallita() && INCARICO.equals(getPassoCorrente());
	}

	/**
	 * Se il locandiere può chiedere adesso di riportare a casa il bardo: in una locanda del racconto, a una visita tranquilla, e se c'è
	 * ancora una città in piedi dove riportarlo.
	 */
	@Override
	public boolean isProntoPerOffrirsi() {
		return LocandaDelRacconto.isTerzaLocanda() && cittaPiuVicina() != null && isVisitaTranquilla();
	}

	/**
	 * Alla terza locanda parte il pianista, anche se sono pronte la leggenda del locandiere o una missione del sacerdote.
	 */
	@Override
	public boolean haLaPrecedenza() {
		return true;
	}

	@Override
	public boolean puoAncoraOffrirsi() {
		return true;
	}

	@Override
	public int getGiro() {
		String giro = ottieniProprieta(SceltaDegliIncarichi.GIRO);
		return giro == null ? -1 : Integer.parseInt(giro);
	}

	/**
	 * Nella locanda in cui il locandiere sta per affidare il bardo al gruppo, o l'ha appena fatto e il bardo non si è ancora unito al gruppo,
	 * c'è solo la sua scena: nessun'altra (il benvenuto di una locanda nuova, con il bardo seduto nell'angolo a raccontare la fiaba, slitta
	 * alla visita dopo; vedi IntermezzoLocandaPrimaVisita).
	 */
	@Override
	public boolean bloccaIntermezzo(TipoIntermezzo tipo) {
		return staPerAffidareIlBardo();
	}

	/**
	 * Se il locandiere sta per affidare il bardo al gruppo in questa locanda, o l'ha appena fatto con la sua scena e il bardo non si è ancora
	 * unito al gruppo (succede in locazione, dopo gli intermezzi di inizio locazione).
	 */
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
