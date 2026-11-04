package com.threeamigos.foresta.missioni.leggende;

import com.threeamigos.foresta.missioni.incarichi.SceltaDegliIncarichi;
import com.threeamigos.foresta.missioni.InSorteggio;
import com.threeamigos.foresta.tipi.AspettoDelMandante;
import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.leggendari.CatalogoLeggendari;
import com.threeamigos.foresta.artefatti.leggendari.OggettoLeggendario;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaArtefattoTrovato;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.*;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.Arrays;
import java.util.List;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Testi;

/**
 * La falce del Tristo Mietitore. In una città qualsiasi (a una visita tranquilla) un personaggio a caso, spaventatissimo, giura di aver
 * visto la Morte in persona nella foresta, da lontano, con la sua falce e circondata da scheletri; chiede il gruppo, incuriosito, e lui
 * indica il posto, che finisce sulla mappa. In realtà era un necromante: nelle rovine il gruppo trova lui, con quattro scheletri (due
 * da una parte e due dall'altra) e, finché il necromante è vivo, ne arrivano altri (vedi LocazioneBase). Sconfitto il necromante, la sua
 * falce, la Falce del Tristo Mietitore (un leggendario di bottino di leggendari.txt, di cui il racconto riprende la leggenda), passa al gruppo.
 * Si offre una volta sola per partita.
 * <ol>
 * <li>INCARICO, a inizio locazione, in una città: si pescano il narratore e il nome del necromante e parte l'intermezzo;</li>
 * <li>ACCETTAZIONE, in locazione, nella stessa città: si rivendicano le rovine, si segnano sulla mappa e la missione si attiva;</li>
 * <li>SCONTRO, a fine locazione, nelle rovine, quando il necromante è sconfitto: la falce passa al gruppo.</li>
 * </ol>
 */
public class LaFalceDelTristoMietitore extends MissioneAPassi implements InSorteggio {

	/**
	 * La chiave della Falce in leggendari.txt
	 */
	public static final String CHIAVE_DELLA_FALCE = "FALCE_DEL_TRISTO_MIETITORE";
	/**
	 * Gli scheletri con cui il necromante comincia: due a sinistra e due a destra.
	 */
	public static final int SCHELETRI_INIZIALI = 4;

	private static final String INCARICO = "INCARICO";
	private static final String ACCETTAZIONE = "ACCETTAZIONE";
	private static final String SCONTRO = "SCONTRO";
	private static final String ASPETTO = "ASPETTO";
	private static final String NECROMANTE = "NECROMANTE";
	private static final String POSTO = "POSTO";
	private static final String SEPARATORE = ",";

	// Chi può raccontare: chi si incontra in una città
	private static final List<AspettoDelMandante> NARRATORI = Arrays.asList(AspettoDelMandante.QUALUNQUE, AspettoDelMandante.DONNA,
			AspettoDelMandante.ARMAIOLO, AspettoDelMandante.ALCHIMISTA, AspettoDelMandante.LOCANDIERE,
			AspettoDelMandante.VENDITORE_DI_PERGAMENE, AspettoDelMandante.INCANTATORE);

	public LaFalceDelTristoMietitore() {
		super(MissionePredefinita.LA_FALCE_DEL_TRISTO_MIETITORE);
	}

	@Override
	protected boolean aspettaUnaVisitaTranquilla() {
		return true;
	}

	@Override
	protected String creaIlNome() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FALCE_DEL_TRISTO_MIETITORE_TITOLO");
	}

	@Override
	public String getDescrizione() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FALCE_DEL_TRISTO_MIETITORE_DESCRIZIONE", "PARAM_NARRATORE", Lingue.corrente().inizialeMaiuscola(getNarratore()));
	}

	@Override
	public String getRicordoDellaLocazione() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FALCE_DEL_TRISTO_MIETITORE_RICORDO", "PARAM_NECROMANTE", getNomeDelNecromante());
	}

	/**
	 * Il necromante, con il nome che gli ha dato la missione.
	 */
	public String getNomeDelNecromante() {
		return parametro(NECROMANTE, () -> ProduttoreDiMissioniCasuale.rigaDiMissioni("NOME_NECROMANTE"));
	}

	/**
	 * Le rovine dove il necromante aspetta il gruppo, o null finché non sono state trovate.
	 */
	public CoordinateMD getCovo() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	private AspettoDelMandante getAspetto() {
		return AspettoDelMandante.valueOf(parametro(ASPETTO, () -> NARRATORI.get(Dado.tira(0, NARRATORI.size() - 1)).name()));
	}

	/**
	 * Chi ha raccontato, con l'articolo: "l'armaiolo", "una donna".
	 */
	private String getNarratore() {
		switch (getAspetto()) {
			case DONNA:
				return ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FALCE_DEL_TRISTO_MIETITORE_NARRATORE_DONNA");
			case ARMAIOLO:
				return ProduttoreDiMissioniCasuale.testoDellaMissione("RUOLO_ARMAIOLO");
			case ALCHIMISTA:
				return ProduttoreDiMissioniCasuale.testoDellaMissione("RUOLO_ALCHIMISTA");
			case LOCANDIERE:
				return ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FALCE_DEL_TRISTO_MIETITORE_NARRATORE_LOCANDIERE");
			case VENDITORE_DI_PERGAMENE:
				return ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FALCE_DEL_TRISTO_MIETITORE_NARRATORE_VENDITORE");
			case INCANTATORE:
				return ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FALCE_DEL_TRISTO_MIETITORE_NARRATORE_INCANTATORE");
			default:
				return ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FALCE_DEL_TRISTO_MIETITORE_NARRATORE_UOMO");
		}
	}

	/**
	 * Il necromante e i suoi scheletri: il necromante al centro, due scheletri per parte.
	 */
	public IncontroDiMissione getIncontro() {
		return IncontroDiMissione.di(TipoPersonaggio.SCHELETRO, SCHELETRI_INIZIALI).conCapoAlCentro(getNomeDelNecromante(), TipoPersonaggio.NECROMANTE);
	}

	/**
	 * Nelle rovine del necromante non c'è nessun oggetto: la Falce è già un bel premio.
	 */
	@Override
	public boolean isLocazioneSenzaOggetti(CoordinateMD coordinate) {
		return SCONTRO.equals(getPassoCorrente()) && coordinate.equals(getCovo());
	}

	@Override
	protected String passoIniziale() {
		return INCARICO;
	}

	@Override
	protected Passo costruisciPasso(String id) {
		switch (id) {
			case INCARICO:
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, () -> isProntoPerOffrirsi() && SceltaDegliIncarichi.tocca(this))
						.esegui(() -> {
							SceltaDegliIncarichi.affidato(this);
							getAspetto();
							getNomeDelNecromante();
							CoordinateMD qui = Foresta.getPosizioneDelGruppo();
							aggiungiProprieta(POSTO, qui.getX() + SEPARATORE + qui.getY());
						})
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, this::getPagineDelRacconto)
						.poi(ACCETTAZIONE);
			case ACCETTAZIONE:
				return Passo.quando(MomentoControllo.IN_LOCAZIONE,
								() -> nelPosto() && RegistroMissioni.cercaOCostruisci(TipoLocazione.ROVINE, this).isPresent())
						.esegui(() -> {
							Foresta.setLocazioneConosciuta(getCovo());
							BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FALCE_DEL_TRISTO_MIETITORE_ROVINE_SEGNATE", "PARAM_NARRATORE", Lingue.corrente().inizialeMaiuscola(getNarratore()))));
							attivaMissione();
						})
						.poi(SCONTRO);
			case SCONTRO:
				return combattiIlCapo(this::getCovo, getIncontro())
						.esegui(this::consegnaLaFalce)
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	private void consegnaLaFalce() {
		OggettoLeggendario falce = CatalogoLeggendari.getLeggendario(CHIAVE_DELLA_FALCE).orElseThrow(IllegalStateException::new);
		Artefatto artefatto = falce.costruisci(Statistiche.getLivello());
		GruppoGiocatore.getIstanza().addArtefatto(artefatto);
		BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FALCE_DEL_TRISTO_MIETITORE_SCONFITTO", "PARAM_NECROMANTE", getNomeDelNecromante(), "PARAM_FALCE", falce.getNomeBreve())));
		BusEventi.pubblica(new NotificaArtefattoTrovato(artefatto, Statistiche.getLivello()));
	}

	/**
	 * Il racconto: lo spavento, la leggenda della falce (le battute di leggendari.txt), il posto.
	 */
	private List<PaginaIntermezzo> getPagineDelRacconto() {
		OggettoLeggendario falce = CatalogoLeggendari.getLeggendario(CHIAVE_DELLA_FALCE).orElseThrow(IllegalStateException::new);
		ScenaInCitta scena = getAspetto().nuovaScena()
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FALCE_DEL_TRISTO_MIETITORE_SCENA_1"))
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FALCE_DEL_TRISTO_MIETITORE_SCENA_2"))
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FALCE_DEL_TRISTO_MIETITORE_SCENA_3"));
		for (String battuta : falce.getLeggenda()) {
			scena.parlaIlMandante(battuta);
		}
		return scena
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FALCE_DEL_TRISTO_MIETITORE_SCENA_4"))
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FALCE_DEL_TRISTO_MIETITORE_SCENA_5"))
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FALCE_DEL_TRISTO_MIETITORE_SCENA_6"))
				.getPagine();
	}

	@Override
	public boolean isInAttesaDiOffrirsi() {
		return !isAttiva() && !isCompleta() && !isFallita() && INCARICO.equals(getPassoCorrente());
	}

	/**
	 * Se la storia si può raccontare adesso: in una città, a una visita tranquilla. Se è pronto anche altro il sorteggio sceglie una sola missione.
	 */
	@Override
	public boolean isProntoPerOffrirsi() {
		return isDisponibile() && isInUnaCitta() && isVisitaTranquilla();
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

	private boolean isInUnaCitta() {
		TipoLocazione tipo = Foresta.getLocazioneDelGruppo();
		return tipo != null && tipo.getCategoriaLocazione() == CategoriaLocazione.CITTA && !Foresta.isCittaDistrutta(tipo);
	}

	private boolean nelPosto() {
		CoordinateMD qui = Foresta.getPosizioneDelGruppo();
		return qui != null && (qui.getX() + SEPARATORE + qui.getY()).equals(ottieniProprieta(POSTO));
	}
}
