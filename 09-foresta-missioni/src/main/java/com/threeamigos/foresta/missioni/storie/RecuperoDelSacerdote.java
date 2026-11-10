package com.threeamigos.foresta.missioni.storie;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.leggendari.CatalogoLeggendari;
import com.threeamigos.foresta.artefatti.leggendari.OggettoLeggendario;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaArtefattoTrovato;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.intermezzi.ScenaInLocanda;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.InSorteggio;
import com.threeamigos.foresta.missioni.LocandaDelRacconto;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.incarichi.SceltaDegliIncarichi;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.AspettoDelMandante;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Le missioni di recupero del sacerdote: un sacerdote o una sacerdotessa racconta la storia buffa di un oggetto sacro finito nel posto
 * sbagliato, e il gruppo lo va a riprendere. L'oggetto è un leggendario di bottino della Santa Ignoranza (leggendari.txt), che resta al
 * gruppo: il sacerdote non lo rivuole indietro. Si offrono entrando in una città (nel sorteggio degli incarichi) o in una locanda del
 * racconto (vedi {@link LocandaDelRacconto}), a una visita tranquilla, una alla volta e nell'ordine di {@link #IN_ORDINE}; ognuna una volta
 * sola per partita.
 * <p>
 * I passi comuni sono il primo, INCARICO (a inizio locazione: si sceglie chi racconta e parte la scena), e il secondo, ACCETTAZIONE (in
 * locazione, nello stesso posto: si rivendica il posto della storia, lo si segna sulla mappa e la missione si attiva); il resto lo
 * decide ogni missione ({@link #passoDopoLAccettazione}, {@link #costruisciGliAltriPassi}).
 * <p>
 * I testi stanno in missioni.txt, con il prefisso della missione ({@link #prefisso}): _TITOLO, _DESCRIZIONE, _SCENA_1… (le battute
 * della scena, di chi le dice {@link #chiParlaNellaScena}), _POSTO_SEGNATO e _CONSEGNA.
 */
public abstract class RecuperoDelSacerdote extends MissioneAPassi implements InSorteggio {

	/**
	 * Le missioni del sacerdote, nell'ordine in cui si offrono: la seguente solo quando la precedente è finita
	 */
	public static final List<MissionePredefinita> IN_ORDINE = Arrays.asList(MissionePredefinita.LA_SPADA_DELL_ACQUASANTIERA,
			MissionePredefinita.IL_BASTONE_DEL_SANTONE, MissionePredefinita.GLI_SCHINIERI_DEL_PELLEGRINO);

	/**
	 * Nella scena parla chi racconta, il sacerdote
	 */
	protected static final char SACERDOTE = 'S';
	/**
	 * Nella scena parla il capo del gruppo
	 */
	protected static final char CAPO = 'C';

	protected static final String INCARICO = "INCARICO";
	protected static final String ACCETTAZIONE = "ACCETTAZIONE";
	private static final String MANDANTE = "MANDANTE";
	private static final String POSTO = "POSTO";
	private static final String SEPARATORE = ",";
	private static final List<TipoPersonaggio> SACERDOTI = Arrays.asList(TipoPersonaggio.SACERDOTE, TipoPersonaggio.SACERDOTESSA);

	protected RecuperoDelSacerdote(MissionePredefinita classe) {
		super(classe);
	}

	/**
	 * Il prefisso dei testi della missione in missioni.txt: "LA_SPADA_DELL_ACQUASANTIERA".
	 */
	protected abstract String prefisso();

	/**
	 * La chiave del leggendario di bottino che la missione dà al gruppo, in leggendari.txt.
	 */
	protected abstract String chiaveDelLeggendario();

	/**
	 * Chi dice le battute della scena, una lettera per battuta ({@link #SACERDOTE} o {@link #CAPO}): "SSCSC" vuol dire _SCENA_1 e
	 * _SCENA_2 del sacerdote, _SCENA_3 del capo e così via.
	 */
	protected abstract String chiParlaNellaScena();

	/**
	 * Il tipo di posto in cui si svolge la storia (una grotta, delle rovine, un bosco): la missione ne rivendica uno, vicino o costruito.
	 */
	protected abstract TipoLocazione tipoDelPosto();

	/**
	 * Il passo che segue l'accettazione.
	 */
	protected abstract String passoDopoLAccettazione();

	/**
	 * I passi propri della missione, dopo l'accettazione.
	 */
	protected abstract Passo costruisciGliAltriPassi(String id);

	@Override
	protected boolean aspettaUnaVisitaTranquilla() {
		return true;
	}

	@Override
	protected String creaIlNome() {
		return testo("_TITOLO");
	}

	@Override
	public String getDescrizione() {
		return testo("_DESCRIZIONE", "PARAM_SACERDOTE", maiuscola(getNomeDelSacerdote()));
	}

	/**
	 * Chi racconta: un sacerdote o una sacerdotessa, scelto la prima volta che serve.
	 */
	public TipoPersonaggio getSacerdote() {
		return TipoPersonaggio.valueOf(parametro(MANDANTE, () -> SACERDOTI.get(Dado.tira(0, SACERDOTI.size() - 1)).name()));
	}

	/**
	 * Chi racconta, con l'articolo: "il sacerdote", "la sacerdotessa".
	 */
	protected String getNomeDelSacerdote() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione(getSacerdote() == TipoPersonaggio.SACERDOTESSA
				? "RECUPERO_DEL_SACERDOTE_SACERDOTESSA" : "RECUPERO_DEL_SACERDOTE_SACERDOTE");
	}

	/**
	 * Il posto della storia, o null finché non è stato segnato sulla mappa.
	 */
	public CoordinateMD getPostoDellaStoria() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	/**
	 * Il leggendario che la missione dà al gruppo.
	 */
	public OggettoLeggendario getLeggendario() {
		return CatalogoLeggendari.getLeggendario(chiaveDelLeggendario())
				.orElseThrow(() -> new IllegalStateException("Manca in leggendari.txt: " + chiaveDelLeggendario()));
	}

	/**
	 * Nel posto della storia non c'è nessun oggetto a caso: l'oggetto sacro è già un bel premio.
	 */
	@Override
	public boolean isLocazioneSenzaOggetti(CoordinateMD coordinate) {
		return isAttiva() && coordinate.equals(getPostoDellaStoria());
	}

	@Override
	protected String passoIniziale() {
		return INCARICO;
	}

	@Override
	protected final Passo costruisciPasso(String id) {
		switch (id) {
			case INCARICO:
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, () -> isProntoPerOffrirsi() && SceltaDegliIncarichi.tocca(this))
						.esegui(() -> {
							SceltaDegliIncarichi.affidato(this);
							getSacerdote();
							CoordinateMD qui = Foresta.getPosizioneDelGruppo();
							aggiungiProprieta(POSTO, qui.getX() + SEPARATORE + qui.getY());
						})
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, this::getPagineDelRacconto)
						.poi(ACCETTAZIONE);
			case ACCETTAZIONE:
				return Passo.quando(MomentoControllo.IN_LOCAZIONE,
								() -> nelPosto() && RegistroMissioni.cercaOCostruisci(tipoDelPosto(), this).isPresent())
						.esegui(() -> {
							Foresta.setLocazioneConosciuta(getPostoDellaStoria());
							BusEventi.pubblica(new NotificaTestoParagrafo(testo("_POSTO_SEGNATO", "PARAM_SACERDOTE", maiuscola(getNomeDelSacerdote()))));
							attivaMissione();
						})
						.poi(passoDopoLAccettazione());
			default:
				return costruisciGliAltriPassi(id);
		}
	}

	/**
	 * Il leggendario passa al gruppo, con il commiato del sacerdote (_CONSEGNA).
	 */
	protected final void consegnaIlLeggendario() {
		OggettoLeggendario leggendario = getLeggendario();
		Artefatto artefatto = leggendario.costruisci(Statistiche.getLivello());
		GruppoGiocatore.getIstanza().addArtefatto(artefatto);
		List<Object> valori = new ArrayList<>(Arrays.asList("PARAM_SACERDOTE", maiuscola(getNomeDelSacerdote()),
				"PARAM_OGGETTO", leggendario.getNomeBreve()));
		valori.addAll(Arrays.asList(altriValoriDellaConsegna()));
		BusEventi.pubblica(new NotificaTestoParagrafo(testo("_CONSEGNA", valori.toArray())));
		BusEventi.pubblica(new NotificaArtefattoTrovato(artefatto, Statistiche.getLivello()));
	}

	/**
	 * Altri segnaposto del testo della consegna, a coppie ("PARAM_X", valore), oltre a PARAM_SACERDOTE e PARAM_OGGETTO: nessuno.
	 */
	protected Object[] altriValoriDellaConsegna() {
		return new Object[0];
	}

	/**
	 * La scena del racconto: in città il sacerdote si presenta come i mandanti degli incarichi, in locanda è seduto a un tavolo.
	 */
	private List<PaginaIntermezzo> getPagineDelRacconto() {
		String chiParla = chiParlaNellaScena();
		if (isInUnaCitta()) {
			ScenaInCitta scena = (getSacerdote() == TipoPersonaggio.SACERDOTESSA ? AspettoDelMandante.SACERDOTESSA : AspettoDelMandante.SACERDOTE)
					.nuovaScena();
			for (int i = 0; i < chiParla.length(); i++) {
				String battuta = testo("_SCENA_" + (i + 1));
				scena = chiParla.charAt(i) == CAPO ? scena.parlaIlCapo(battuta) : scena.parlaIlMandante(battuta);
			}
			return scena.getPagine();
		}
		ScenaInLocanda scena = ScenaInLocanda.conSacerdote(getSacerdote());
		for (int i = 0; i < chiParla.length(); i++) {
			String battuta = testo("_SCENA_" + (i + 1));
			scena = chiParla.charAt(i) == CAPO ? scena.parlaIlCapo(battuta) : scena.parlaIlLocandiere(battuta);
		}
		return scena.getPagine();
	}

	@Override
	public boolean isInAttesaDiOffrirsi() {
		return !isAttiva() && !isCompleta() && !isFallita() && INCARICO.equals(getPassoCorrente());
	}

	/**
	 * Se la storia si può raccontare adesso: in una città o in una locanda del racconto, a una visita tranquilla, e solo se tutte le
	 * missioni del sacerdote che vengono prima di questa sono finite. Se è pronto anche altro il sorteggio sceglie una sola missione.
	 */
	@Override
	public boolean isProntoPerOffrirsi() {
		return isDisponibile() && isLaProssima() && (isInUnaCitta() || LocandaDelRacconto.isQui()) && isVisitaTranquilla();
	}

	@Override
	public boolean puoAncoraOffrirsi() {
		return isLaProssima();
	}

	@Override
	public int getGiro() {
		String giro = ottieniProprieta(SceltaDegliIncarichi.GIRO);
		return giro == null ? -1 : Integer.parseInt(giro);
	}

	/**
	 * Se tutte le missioni del sacerdote che vengono prima di questa sono finite, bene o male.
	 */
	private boolean isLaProssima() {
		for (MissionePredefinita precedente : IN_ORDINE.subList(0, IN_ORDINE.indexOf(getClasse()))) {
			boolean finita = RegistroMissioni.getTutteLeMissioni().stream()
					.anyMatch(missione -> missione.getClasse() == precedente && (missione.isCompleta() || missione.isFallita()));
			if (!finita) {
				return false;
			}
		}
		return true;
	}

	private boolean isInUnaCitta() {
		TipoLocazione tipo = Foresta.getLocazioneDelGruppo();
		return tipo != null && tipo.getCategoriaLocazione() == CategoriaLocazione.CITTA && !Foresta.isCittaDistrutta(tipo);
	}

	private boolean nelPosto() {
		CoordinateMD qui = Foresta.getPosizioneDelGruppo();
		return qui != null && (qui.getX() + SEPARATORE + qui.getY()).equals(ottieniProprieta(POSTO));
	}

	/**
	 * Il testo della missione con quel suffisso: testo("_TITOLO").
	 */
	protected final String testo(String suffisso, Object... parametri) {
		return ProduttoreDiMissioniCasuale.testoDellaMissione(prefisso() + suffisso, parametri);
	}

	private static String maiuscola(String testo) {
		return com.threeamigos.foresta.testi.Lingue.corrente().inizialeMaiuscola(testo);
	}
}
