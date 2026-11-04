package com.threeamigos.foresta.missioni.storie;

import com.threeamigos.foresta.intermezzi.GruppoInScena;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Verso;
import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.*;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.Ricompensa;
import com.threeamigos.foresta.missioni.richieste.ViandanteRichiesto;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.statistiche.LineaTemporale;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.*;
import java.util.function.Supplier;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Genere;
import com.threeamigos.foresta.testi.Testi;

/**
 * Dopo due o tre giorni di cammino, entrando in un bosco, il gruppo incontra per caso un viandante che chiede un favore: andare
 * in una certa città, sconfiggere dei mostri che le danno noia (vedi {@link ViandanteRichiesto}, da missioni.txt) e tornare da lui,
 * nel bosco dove si sono incontrati. Il giorno dell'incontro, la richiesta, quanti mostri e la città si fissano subito, all'inizio
 * della partita, e restano quelli anche dopo un salvataggio.
 * <ol>
 * <li>PARAMETRI, prima della prima locazione: si fissano giorno, richiesta, quantità e città (vedi
 * {@link #generaParametri});</li>
 * <li>INCONTRO, prima di una locazione di bosco, dal giorno fissato in poi e a una visita tranquilla: l'intermezzo del viandante;</li>
 * <li>ACCETTAZIONE, in locazione, nel bosco: la missione si attiva e si ricorda il punto di partenza;</li>
 * <li>ANDATA, prima di una locazione: il gruppo arriva nella città (se nel frattempo viene distrutta la missione fallisce);</li>
 * <li>CACCIA, dopo ogni locazione: sconfitti i mostri, dovunque, da quando il gruppo è in città;</li>
 * <li>RITORNO, prima di una locazione: il gruppo torna al punto di partenza, e l'intermezzo del ringraziamento;</li>
 * <li>RICOMPENSA, in locazione: le monete, tante per ogni mostro sconfitto e per ogni livello del mondo di quando si è
 * incontrato il viandante.</li>
 * </ol>
 */
public class IlViandante extends MissioneAPassi {

	/**
	 * Il giorno dell'incontro si sceglie fra questi due (il primo giorno della partita è l'1): dopo due o tre giorni di cammino.
	 */
	public static final int GIORNO_MINIMO = 3;
	public static final int GIORNO_MASSIMO = 4;

	private static final String PARAMETRI = "PARAMETRI";
	private static final String INCONTRO = "INCONTRO";
	private static final String ACCETTAZIONE = "ACCETTAZIONE";
	private static final String ANDATA = "ANDATA";
	private static final String CACCIA = "CACCIA";
	private static final String RITORNO = "RITORNO";
	private static final String RICOMPENSA = "RICOMPENSA";

	private static final String GIORNO = "GIORNO";
	private static final String RIGA = "RIGA";
	private static final String QUANTITA = "QUANTITA";
	private static final String CITTA = "CITTA";
	private static final String LIVELLO_DELL_INCONTRO = "LIVELLO_DELL_INCONTRO";

	private static final String ID_EROE = "eroe";
	private static final String ID_VIANDANTE = "viandante";
	// Il capo sta qui e gli altri alla sua sinistra, come nelle scene in città
	private static final double X_CAPO = 0.38;

	public IlViandante() {
		super(MissionePredefinita.IL_VIANDANTE);
	}

	/**
	 * Il giorno, da quello fissato in poi, in cui il viandante si fa incontrare; 0 finché non è fissato.
	 */
	public int getGiornoDellIncontro() {
		String giorno = getParametro(GIORNO);
		return giorno == null ? 0 : Integer.parseInt(giorno);
	}

	/**
	 * Che cosa chiede il viandante, o null finché non è fissato.
	 */
	public ViandanteRichiesto getRichiesta() {
		String riga = getParametro(RIGA);
		return riga == null ? null : ViandanteRichiesto.da(riga);
	}

	/**
	 * Quanti mostri chiede di sconfiggere; 0 finché non è fissato.
	 */
	public int getQuantita() {
		String quantita = getParametro(QUANTITA);
		return quantita == null ? 0 : Integer.parseInt(quantita);
	}

	/**
	 * La città in cui il gruppo deve andare, o null finché non è fissata.
	 */
	public TipoLocazione getCitta() {
		String citta = getParametro(CITTA);
		return citta == null ? null : TipoLocazione.valueOf(citta);
	}

	/**
	 * Quante monete paga il viandante alla fine: quelle di ogni mostro, per ogni mostro, per il livello del mondo di quando si è
	 * incontrato il viandante (finché non si è incontrato, quello di adesso).
	 */
	public int getMonete() {
		ViandanteRichiesto richiesta = getRichiesta();
		return richiesta == null ? 0 : richiesta.getMonete() * getQuantita() * getLivelloDellIncontro();
	}

	private int getLivelloDellIncontro() {
		String livello = ottieniProprieta(LIVELLO_DELL_INCONTRO);
		return livello != null ? Integer.parseInt(livello) : Statistiche.getLivello();
	}

	private String nomeDellaCitta() {
		TipoLocazione citta = getCitta();
		return citta == null ? "" : citta.getNomeDellaCitta();
	}

	@Override
	protected String creaIlNome() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIANDANTE_TITOLO");
	}

	@Override
	public String getDescrizione() {
		return descrizioneDalModello();
	}

	@Override
	protected String produzioneDellaDescrizione(String passo) {
		if (getRichiesta() == null || !isAttiva()) {
			return "IL_VIANDANTE_DESCRIZIONE_ATTESA";
		}
		switch (passo) {
			case CACCIA:
				return "IL_VIANDANTE_DESCRIZIONE_CACCIA";
			case RITORNO:
				return "IL_VIANDANTE_DESCRIZIONE_RITORNO";
			default:
				return "IL_VIANDANTE_DESCRIZIONE_ANDATA";
		}
	}

	@Override
	protected Object[] valoriStabiliDellaDescrizione() {
		return valori();
	}

	/**
	 * Quanti mostri sono stati sconfitti finora: l'unico valore che cambia.
	 */
	@Override
	protected Object[] valoriDellaDescrizione() {
		ViandanteRichiesto richiesta = getRichiesta();
		return richiesta == null ? new Object[0] : new Object[] {"PARAM_SCONFITTI", getConteggioNelPassoCorrente(eventoSconfitto(richiesta.getNemico()))};
	}

	/**
	 * Tutti i segnaposto dei testi della storia che non cambiano mentre dura: la città, i mostri e le monete (vuoti finché non c'è la
	 * richiesta). Senza "private" per i test.
	 */
	Object[] valori() {
		ViandanteRichiesto richiesta = getRichiesta();
		if (richiesta == null) {
			return new Object[0];
		}
		String quanti = Lingue.corrente().cardinale(getQuantita(), Genere.MASCHILE);
		return new Object[] {"PARAM_CITTA", nomeDellaCitta(), "PARAM_QUANTI", quanti, "PARAM_PLURALE", richiesta.getPlurale(),
				"PARAM_MOSTRI", quanti + " " + richiesta.getPlurale(), "PARAM_MONETE", getMonete()};
	}

	@Override
	protected boolean aspettaUnaVisitaTranquilla() {
		return true;
	}

	/**
	 * Si fissa una volta sola, all'inizio: il giorno dell'incontro, la richiesta, quanti mostri e una città in piedi.
	 */
	private Map<String, Supplier<String>> parametri() {
		Map<String, Supplier<String>> parametri = new LinkedHashMap<>();
		parametri.put(GIORNO, () -> String.valueOf(Dado.tira(GIORNO_MINIMO, GIORNO_MASSIMO)));
		parametri.put(RIGA, () -> ProduttoreDiMissioniCasuale.rigaDiMissioni("VIANDANTE"));
		// La riga è già stata fissata: generaParametri le scrive nell'ordine in cui le trova
		parametri.put(QUANTITA, () -> {
			ViandanteRichiesto richiesta = getRichiesta();
			return String.valueOf(Dado.tira(richiesta.getQuantitaMinima(), richiesta.getQuantitaMassima()));
		});
		parametri.put(CITTA, () -> sceglieUnaCitta().name());
		return parametri;
	}

	/**
	 * Una città a caso fra quelle ancora in piedi; se il Drago le ha distrutte tutte, una qualunque (la missione si chiude da sola, vedi
	 * isCittaDistrutta).
	 */
	private static TipoLocazione sceglieUnaCitta() {
		List<TipoLocazione> citta = cittaDellaForesta(true);
		if (citta.isEmpty()) {
			citta = cittaDellaForesta(false);
		}
		return citta.get(Dado.tiraAncheAUnaFaccia(citta.size()) - 1);
	}

	private static List<TipoLocazione> cittaDellaForesta(boolean soloInPiedi) {
		List<TipoLocazione> citta = new ArrayList<>();
		for (TipoLocazione tipo : TipoLocazione.values()) {
			if (tipo.getCategoriaLocazione() == CategoriaLocazione.CITTA && Foresta.getCoordinateLocazioneUnica(tipo) != null
					&& !(soloInPiedi && Foresta.isCittaDistrutta(tipo))) {
				citta.add(tipo);
			}
		}
		return citta;
	}

	/**
	 * Se è l'ora dell'incontro: dal giorno fissato in poi, entrando in un bosco, quando nessun'altra missione mostra un intermezzo
	 * (o subito, in un bosco, se la partenza è stata forzata dalla finestra di prova).
	 */
	private boolean isIncontroPossibile() {
		// Dalla finestra di prova si può forzare: allora basta essere in un bosco
		if (isInLuogoDellaPartenzaForzata()) {
			return true;
		}
		return LineaTemporale.getGiorno() >= getGiornoDellIncontro()
				&& Foresta.getLocazioneDelGruppo() == TipoLocazione.BOSCO
				&& isVisitaTranquilla();
	}

	private boolean isCittaDistrutta() {
		TipoLocazione citta = getCitta();
		return citta != null && Foresta.isCittaDistrutta(citta);
	}

	private void accetta() {
		aggiungiProprieta(LIVELLO_DELL_INCONTRO, String.valueOf(Statistiche.getLivello()));
		ViandanteRichiesto richiesta = getRichiesta();
		BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIANDANTE_ACCETTATO", valori())));
		attivaMissione();
	}

	/**
	 * La scena nel bosco: l'eroe a sinistra, il viandante che arriva da destra e si ferma davanti a lui. Il testo in alto usa
	 * l'alfabeto grande, senza accenti.
	 */
	/**
	 * La scena nel bosco: il viandante arriva da destra e tutto il gruppo lo guarda, nello stesso ordine delle scene in città
	 * (vedi ScenaNegozio): da destra verso sinistra il capo, gli ospiti e il resto del gruppo.
	 */
	private PaginaIntermezzo scenaNelBosco(String testo) {
		PaginaIntermezzo pagina = new PaginaIntermezzo(testo)
				.conSfondo(ImmagineIntermezzo.locazione(TipoLocazione.BOSCO))
				.conElemento(ElementoIntermezzo.personaggio(ID_VIANDANTE, TipoPersonaggio.VIANDANTE, 1.1, 0.61)
						.conBocca(0.5, -0.15)
						.guarda(Verso.SINISTRA)
						.poi(Tappa.inSecondi(2).verso(0.62, 0.61)));
		return GruppoInScena.aggiungi(pagina, ID_EROE, GruppoGiocatore.getIstanza().getCapo().getClasse(), X_CAPO, 0.61);
	}

	private List<PaginaIntermezzo> getPagineDellIncontro() {
		ViandanteRichiesto richiesta = getRichiesta();
		return Collections.singletonList(scenaNelBosco(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIANDANTE_INCONTRO"))
				.conBattuta(BattutaIntermezzo.di(ID_VIANDANTE, richiesta.getRichiesta(nomeDellaCitta())).daSecondo(2))
				.conBattuta(BattutaIntermezzo.di(ID_EROE, richiesta.getBattutaDelCapo()))
				.conBattuta(BattutaIntermezzo.di(ID_VIANDANTE, richiesta.getRisposta())));
	}

	private List<PaginaIntermezzo> getPagineDelRitorno() {
		return Collections.singletonList(scenaNelBosco(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIANDANTE_RITORNO"))
				.conBattuta(BattutaIntermezzo.di(ID_VIANDANTE, getRichiesta().getRitorno()).daSecondo(2))
				.conBattuta(BattutaIntermezzo.di(ID_EROE, ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIANDANTE_SALUTO"))));
	}

	@Override
	protected String passoIniziale() {
		return PARAMETRI;
	}

	@Override
	protected Passo costruisciPasso(String id) {
		switch (id) {
			case PARAMETRI:
				return generaParametri(MomentoControllo.PRE_LOCAZIONE, parametri()).poi(INCONTRO);
			case INCONTRO:
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, this::isIncontroPossibile)
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, this::getPagineDellIncontro)
						.poi(ACCETTAZIONE);
			case ACCETTAZIONE:
				return Passo.quando(MomentoControllo.IN_LOCAZIONE, () -> true).esegui(this::accetta).poi(ANDATA);
			case ANDATA:
				return vai(MomentoControllo.PRE_LOCAZIONE, getCitta())
						.falliscoSe(this::isCittaDistrutta, () ->ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIANDANTE_CITTA_DISTRUTTA", "PARAM_CITTA", nomeDellaCitta()))
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(getRichiesta().getArrivo(nomeDellaCitta()))))
						.poi(CACCIA);
			case CACCIA:
				return sconfiggi(MomentoControllo.POST_LOCAZIONE, getRichiesta().getNemico(), getQuantita())
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIANDANTE_CACCIA_FINITA"))))
						.poi(RITORNO);
			case RITORNO:
				return tornaAlPuntoDiPartenza(MomentoControllo.PRE_LOCAZIONE)
						.segnala(this::getPuntoDiPartenza)
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, this::getPagineDelRitorno)
						.poi(RICOMPENSA);
			case RICOMPENSA:
				return ricompensa(MomentoControllo.IN_LOCAZIONE, Ricompensa.inMonete(getMonete()),
						() -> ProduttoreDiMissioniCasuale.testoDellaMissione("IL_VIANDANTE_RICOMPENSA", "PARAM_MONETE", getMonete()))
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}
}
