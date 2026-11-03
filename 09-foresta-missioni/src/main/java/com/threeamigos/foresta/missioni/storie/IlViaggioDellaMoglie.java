package com.threeamigos.foresta.missioni.storie;

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
import com.threeamigos.foresta.missioni.ProprietaDiMissione;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.mondo.RegistroPersonaggi;
import com.threeamigos.foresta.personaggi.EquipaggiamentoIniziale;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.PersonaggioNoto;
import com.threeamigos.foresta.tipi.TipoLocazione;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Preposizione;
import com.threeamigos.foresta.testi.Testi;

/**
 * Il viaggio della moglie: dopo che il gruppo ha riportato a casa il bardo (vedi NonSparateSulPianista), rientrando in una
 * locanda nel bosco, a una visita tranquilla, il bardo ringrazia il gruppo e, viste le sue capacità, gli chiede di
 * accompagnare sua moglie dalla città dove l'aveva riportato la prima volta a un'altra città. È una missione di scorta: la
 * moglie viaggia con il gruppo come ospite non vulnerabile (non si può ferire, e non può morire).
 * <ol>
 * <li>INCARICO, a inizio locazione, in una locanda nel bosco già visitata, dopo che NonSparateSulPianista è stata completata:
 * l'intermezzo del bardo;</li>
 * <li>ACCETTAZIONE, in locazione, nella stessa locanda: la missione si attiva, e segna sulla mappa la città di partenza;</li>
 * <li>PRELEVAMENTO, a inizio locazione, nella città di partenza: l'intermezzo con la moglie;</li>
 * <li>PARTENZA, in locazione, nella città di partenza: la moglie si unisce al gruppo;</li>
 * <li>VIAGGIO, a inizio locazione, fino alla città di destinazione, scelta a caso fra le altre;</li>
 * <li>ARRIVO, in locazione, nella città di destinazione: la moglie paga e si separa dal gruppo, e la missione si completa.</li>
 * </ol>
 * Se la città di partenza o quella di arrivo viene distrutta prima della fine, la missione fallisce. Il suo nome è "Il viaggio
 * di" e il nome della moglie del bardo.
 * <p>
 * Come NonSparateSulPianista si fa una volta sola: finita, bene o male, non ne lascia una nuova (non ridefinisce
 * MissioneAPassi.isRipetibile, che è falso).
 */
public class IlViaggioDellaMoglie extends MissioneAPassi {

	private static final String CITTA_DI_PARTENZA = "CITTA_DI_PARTENZA";
	private static final String CITTA_DI_ARRIVO = ProprietaDiMissione.CITTA_DI_ARRIVO;
	private static final String LOCANDA = "LOCANDA";
	private static final String LIVELLO_DELL_INCARICO = "LIVELLO_DELL_INCARICO";
	// La paga base, per il primo livello del mondo: si moltiplica per il livello di quando si accetta il viaggio
	private static final int RICOMPENSA = 25;
	// La locanda deve essere già stata visitata: alla prima visita scatta l'intermezzo di benvenuto
	private static final int VISITE_PRECEDENTI = 1;

	private static final String INCARICO = "INCARICO";
	private static final String ACCETTAZIONE = "ACCETTAZIONE";
	private static final String PRELEVAMENTO = "PRELEVAMENTO";
	private static final String PARTENZA = "PARTENZA";
	private static final String VIAGGIO = "VIAGGIO";
	private static final String ARRIVO = "ARRIVO";

	public IlViaggioDellaMoglie() {
		super(MissionePredefinita.IL_VIAGGIO_DELLA_MOGLIE);
	}

	/**
	 * Il nome della moglie del bardo, lo stesso in tutto il mondo (vedi RegistroPersonaggi).
	 */
	private static String getMoglie() {
		return RegistroPersonaggi.getResidente(PersonaggioNoto.MOGLIE_DEL_BARDO).getNome();
	}

	private static String getBardo() {
		return RegistroPersonaggi.getResidente(PersonaggioNoto.BARDO_DELLE_LOCANDE).getNome();
	}

	@Override
	protected String creaIlNome() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DELLA_MOGLIE_TITOLO", "PARAM_MOGLIE", getMoglie());
	}

	@Override
	public String getDescrizione() {
		return descrizioneDalModello();
	}

	@Override
	protected String produzioneDellaDescrizione(String passo) {
		return getCittaDiPartenza() == null || getCittaDiArrivo() == null ? "IL_VIAGGIO_DELLA_MOGLIE_DESCRIZIONE_ATTESA" : "IL_VIAGGIO_DELLA_MOGLIE_DESCRIZIONE";
	}

	@Override
	protected Object[] valoriStabiliDellaDescrizione() {
		return valori();
	}

	/**
	 * Tutti i segnaposto dei testi della storia: il bardo e la moglie e, quando le città sono scelte, la partenza e l'arrivo e le monete.
	 * Senza "private" per i test.
	 */
	Object[] valori() {
		if (getCittaDiPartenza() == null || getCittaDiArrivo() == null) {
			return new Object[] {"PARAM_BARDO", getBardo(), "PARAM_MOGLIE", getMoglie()};
		}
		return new Object[] {"PARAM_BARDO", getBardo(), "PARAM_MOGLIE", getMoglie(), "PARAM_PARTENZA", getCittaDiPartenza().getNomeDellaCitta(),
				"PARAM_A_ARRIVO", Lingue.corrente().conPreposizione(Preposizione.A, getCittaDiArrivo().getNomeProprio()), "PARAM_MONETE", getRicompensa()};
	}

	@Override
	protected boolean aspettaUnaVisitaTranquilla() {
		return true;
	}

	/**
	 * La città da dove parte la moglie: quella in cui il gruppo ha riportato il bardo la prima volta (vedi
	 * NonSparateSulPianista); null finché la missione non è accettata.
	 */
	public TipoLocazione getCittaDiPartenza() {
		String citta = ottieniProprieta(CITTA_DI_PARTENZA);
		return citta == null ? null : TipoLocazione.valueOf(citta);
	}

	/**
	 * La città dove va la moglie, scelta a caso fra le altre; null finché la missione non è accettata.
	 */
	public TipoLocazione getCittaDiArrivo() {
		String citta = ottieniProprieta(CITTA_DI_ARRIVO);
		return citta == null ? null : TipoLocazione.valueOf(citta);
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
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, () -> isRitornoInUnaLocanda() && getPrimaCittaDelBardo().isPresent()
								&& !altreCitta().isEmpty() && isVisitaTranquilla())
						.esegui(() -> {
							TipoLocazione partenza = getPrimaCittaDelBardo().orElseThrow(IllegalStateException::new);
							aggiungiProprieta(CITTA_DI_PARTENZA, partenza.name());
							List<TipoLocazione> altre = altreCitta();
							aggiungiProprieta(CITTA_DI_ARRIVO, altre.get(Dado.tiraAncheAUnaFaccia(altre.size()) - 1).name());
							CoordinateMD locanda = Foresta.getPosizioneDelGruppo();
							aggiungiProprieta(LOCANDA, locanda.getX() + "," + locanda.getY());
						})
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, () -> scenaDelBardo().getPagine())
						.poi(ACCETTAZIONE);
			case ACCETTAZIONE:
				return Passo.quando(MomentoControllo.IN_LOCAZIONE, this::nellaLocanda)
						.esegui(() -> {
							aggiungiProprieta(LIVELLO_DELL_INCARICO, String.valueOf(Statistiche.getLivello()));
							Foresta.setLocazioneConosciuta(Foresta.getCoordinateLocazioneUnica(getCittaDiPartenza()));
							Foresta.setLocazioneConosciuta(Foresta.getCoordinateLocazioneUnica(getCittaDiArrivo()));
							BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DELLA_MOGLIE_AFFIDATA", valori())));
							attivaMissione();
						})
						.poi(PRELEVAMENTO);
			case PRELEVAMENTO:
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, () -> Foresta.getLocazioneDelGruppo() == getCittaDiPartenza())
						.segnala(() -> Foresta.getCoordinateLocazioneUnica(getCittaDiPartenza()))
						.falliscoSe(() -> isCittaDistrutta(getCittaDiPartenza()), () ->ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DELLA_MOGLIE_PARTENZA_DISTRUTTA", "PARAM_CITTA", getNomeCitta(getCittaDiPartenza()), "PARAM_MOGLIE", getMoglie()))
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, () -> scenaDellaMoglie().getPagine())
						.poi(PARTENZA);
			case PARTENZA:
				return prendiInScorta(MomentoControllo.IN_LOCAZIONE, () -> Foresta.getLocazioneDelGruppo() == getCittaDiPartenza(),
								() -> RegistroPersonaggi.costruisciPersonaggio(RegistroPersonaggi.getResidente(PersonaggioNoto.MOGLIE_DEL_BARDO),
										EquipaggiamentoIniziale.livelloCasuale(Statistiche.getLivello())), false)
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DELLA_MOGLIE_SI_UNISCE", valori()))))
						.poi(VIAGGIO);
			case VIAGGIO:
				return scorta(MomentoControllo.PRE_LOCAZIONE, () -> Foresta.getCoordinateLocazioneUnica(getCittaDiArrivo()), false)
						.falliscoSe(() -> isCittaDistrutta(getCittaDiArrivo()), () ->ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DELLA_MOGLIE_ARRIVO_DISTRUTTO", "PARAM_CITTA", getNomeCitta(getCittaDiArrivo()), "PARAM_MOGLIE", getMoglie()))
						.poi(ARRIVO);
			case ARRIVO:
				return ricompensa(MomentoControllo.IN_LOCAZIONE, getRicompensa(),
								() ->ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DELLA_MOGLIE_RICOMPENSA", "PARAM_MOGLIE", getMoglie(), "PARAM_MONETE", getRicompensa()))
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	/**
	 * Quanto paga la moglie: la paga base per il livello del mondo di quando il viaggio è stato accettato.
	 */
	private int getRicompensa() {
		String livello = ottieniProprieta(LIVELLO_DELL_INCARICO);
		return RICOMPENSA * (livello != null ? Integer.parseInt(livello) : Statistiche.getLivello());
	}

	private ScenaInLocanda scenaDelBardo() {
		TipoLocazione partenza = getCittaDiPartenza();
		TipoLocazione arrivo = getCittaDiArrivo();
		return ScenaInLocanda.conBardo()
				.parlaIlBardo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DELLA_MOGLIE_BARDO_1"))
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DELLA_MOGLIE_BARDO_2"))
				.parlaIlBardo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DELLA_MOGLIE_BARDO_3", "PARAM_MOGLIE", getMoglie()))
				.parlaIlBardo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DELLA_MOGLIE_BARDO_4"))
				.parlaIlBardo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DELLA_MOGLIE_BARDO_5", "PARAM_MOGLIE", getMoglie(), "PARAM_IN_PARTENZA", Lingue.corrente().conPreposizione(Preposizione.IN, partenza.getNomeProprio()), "PARAM_ARRIVO", arrivo.getNomeProprio()))
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DELLA_MOGLIE_BARDO_6"))
				.parlaIlBardo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DELLA_MOGLIE_BARDO_7"));
	}

	private ScenaInCitta scenaDellaMoglie() {
		return ScenaInCitta.conMoglieDelBardo()
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DELLA_MOGLIE_MOGLIE_1", "PARAM_BARDO", getBardo(), "PARAM_MOGLIE", getMoglie()))
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DELLA_MOGLIE_MOGLIE_2", valori()))
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DELLA_MOGLIE_MOGLIE_3"))
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DELLA_MOGLIE_MOGLIE_4"));
	}

	/**
	 * Se il gruppo è in una locanda nel bosco già visitata: alla prima visita scatta l'intermezzo di benvenuto.
	 */
	private boolean isRitornoInUnaLocanda() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		if (Foresta.getLocazioneDelGruppo() != TipoLocazione.LOCANDA) {
			return false;
		}
		String visite = Foresta.getLocazioneMD(Foresta.getPosizioneDelGruppo()).ottieniProprieta(Locande.LOCANDA_VISITE);
		return visite != null && Integer.parseInt(visite) >= VISITE_PRECEDENTI;
	}

	private boolean nellaLocanda() {
		CoordinateMD qui = Foresta.getPosizioneDelGruppo();
		return (qui.getX() + "," + qui.getY()).equals(ottieniProprieta(LOCANDA));
	}

	/**
	 * La città in cui il gruppo ha riportato il bardo (la missione NonSparateSulPianista completata, che si fa una volta sola).
	 */
	private static Optional<TipoLocazione> getPrimaCittaDelBardo() {
		return RegistroMissioni.getMissioniCompletate().stream()
				.filter(NonSparateSulPianista.class::isInstance)
				.map(NonSparateSulPianista.class::cast)
				.map(NonSparateSulPianista::getCitta)
				.filter(citta -> citta != null)
				.findFirst();
	}

	/**
	 * Le città ancora in piedi diverse da quella di partenza, fra cui si sceglie a caso la destinazione; vuota se la città di
	 * partenza è distrutta o non ce ne sono altre.
	 */
	private List<TipoLocazione> altreCitta() {
		List<TipoLocazione> altre = new ArrayList<>();
		Optional<TipoLocazione> partenza = getPrimaCittaDelBardo();
		if (!partenza.isPresent() || isCittaDistrutta(partenza.get())) {
			return altre;
		}
		for (TipoLocazione tipo : TipoLocazione.values()) {
			if (tipo.getCategoriaLocazione() == CategoriaLocazione.CITTA && tipo != partenza.get() && !isCittaDistrutta(tipo)
					&& Foresta.getCoordinateLocazioneUnica(tipo) != null) {
				altre.add(tipo);
			}
		}
		return altre;
	}

	private static boolean isCittaDistrutta(TipoLocazione citta) {
		return citta != null && Foresta.isCittaDistrutta(citta);
	}

	private static String getNomeCitta(TipoLocazione citta) {
		return citta == null ? ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIAGGIO_DELLA_MOGLIE_CITTA_GENERICA") : citta.getNomeDellaCitta();
	}
}
