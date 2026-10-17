package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAcquistoConsumabile;
import com.threeamigos.foresta.eventi.interni.*;
import com.threeamigos.foresta.eventi.notifiche.*;
import com.threeamigos.foresta.interfacce.Arma;
import com.threeamigos.foresta.interfacce.VistaArtefatto;
import com.threeamigos.foresta.interfacce.VistaGruppoGiocatore;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.modellodati.*;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.CostruttoreArtefatto;
import com.threeamigos.foresta.oggetti.ModificatoreDelTesto;
import com.threeamigos.foresta.oggetti.Oggetto;
import com.threeamigos.foresta.oggetti.leggendari.CatalogoLeggendari;
import com.threeamigos.foresta.oggetti.leggendari.RegoleSetLeggendari;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.Dado;
import com.threeamigos.foresta.strumenti.Logger;
import com.threeamigos.foresta.strumenti.Misc;
import com.threeamigos.foresta.tipi.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Un insieme di personaggi guidati da un giocatore
 */

public class GruppoGiocatore extends Gruppo implements ScambiatoreArtefatti, VistaGruppoGiocatore {

	// Il leggendario che la modalità di prova mette nell'inventario del gruppo (vedi leggendari.txt)
	private static final String CHIAVE_FALCE_DI_PROVA = "FALCE_DEL_TRISTO_MIETITORE";

	/**
	 * Il gruppo del giocatore ricostruito da un modello dati, per esempio dopo aver caricato un salvataggio.
	 */
	public static GruppoGiocatore of(GruppoGiocatoreMD gruppoGiocatoreMD) {
		return new GruppoGiocatore(gruppoGiocatoreMD);
	}

	private static GruppoGiocatore istanza;
	
	public static GruppoGiocatore getIstanza() {
		if (istanza == null) {
			istanza = new GruppoGiocatore();
		}
		return istanza;
	}

	/**
	 * Dimentica il gruppo corrente: il prossimo getIstanza() ne crea uno nuovo sul ModelloDati installato.
	 * Serve ai test, che ricostruiscono una partita da zero a ogni prova.
	 */
	public static void azzeraIstanza() {
		istanza = null;
	}

	private GruppoGiocatore(GruppoGiocatoreMD gruppoGiocatoreMD) {
		setModelloDati(gruppoGiocatoreMD);
	}

	public GruppoGiocatore() {
		super();
		BusEventi.iscriviti(InternoStoccaggioArtefatto.class, this::suEventoRichiestaStoccaggioArtefatto);
		BusEventi.iscriviti(InternoPrelievoArtefatto.class, this::suEventoRichiestaPrelievoArtefatto);
		BusEventi.iscriviti(InternoAcquistoArtefatto.class, this::suEventoRichiestaAcquistoArtefatto);
		BusEventi.iscriviti(InternoVenditaArtefatto.class, this::suEventoRichiestaVenditaArtefatto);
		BusEventi.iscriviti(InternoIncantatura.class, this::suEventoIncantatura);
		BusEventi.iscriviti(ComandoAcquistoConsumabile.class, this::suEventoRichiestaAcquistoConsumabile);
	}

	/**
	 * Serve per riagganciare al GruppoGiocatore istanze di Personaggio.
	 */
	public void setModelloDati(GruppoGiocatoreMD gruppoGiocatoreMD) {
		this.md = gruppoGiocatoreMD;
		capo = null;
		personaggi.clear();
		List<PersonaggioMD> personaggiDaAggiungere = new ArrayList<>(gruppoGiocatoreMD.getPersonaggiMD());
		int indiceCapo = gruppoGiocatoreMD.getIndiceCapo();
		gruppoGiocatoreMD.svuotaPersonaggiMD();
		for (PersonaggioMD personaggioMD : personaggiDaAggiungere) {
			Personaggio personaggio = FabbricaPersonaggi.crea(personaggioMD.getClasse(), 1);
			personaggio.setModelloDati(personaggioMD);
			aggiungiPersonaggioSenzaNotificare(personaggio);
		}
		if (indiceCapo >= 0 && indiceCapo < personaggi.size()) {
			capo = personaggi.get(indiceCapo);
		}
		allineaIndiceCapo();
		ospiti.clear();
		for (PersonaggioMD ospiteMD : gruppoGiocatoreMD.getOspitiMD()) {
			Personaggio ospite = FabbricaPersonaggi.crea(ospiteMD.getClasse(), 1);
			ospite.setModelloDati(ospiteMD);
			ospiti.add(ospite);
		}
	}

	/**
	 * Chi viaggia con il gruppo senza farne parte, per esempio chi una missione deve scortare: non combatte, non
	 * conta nei limiti del gruppo e non si equipaggia. Gli ospiti possono essere quanti si vuole. Un ospite
	 * vulnerabile (un ostaggio, un ferito da soccorrere) può essere attaccato dagli avversari, e quindi morire (vedi
	 * {@link #getOspitiBersaglio()}); gli altri no.
	 */
	public final List<Personaggio> getOspiti() {
		return Collections.unmodifiableList(ospiti);
	}

	/**
	 * Aggiunge un ospite non vulnerabile: viaggia con il gruppo senza farne parte.
	 */
	public final void aggiungiOspite(Personaggio ospite) {
		aggiungiOspite(ospite, false);
	}

	/**
	 * Aggiunge un ospite: viaggia con il gruppo senza farne parte. Se è vulnerabile gli avversari possono attaccarlo.
	 */
	public final void aggiungiOspite(Personaggio ospite, boolean vulnerabile) {
		ospiti.add(ospite);
		md.aggiungiOspite(ospite.getModelloDati());
		if (vulnerabile) {
			md.aggiungiOspiteVulnerabile(ospite.getModelloDati().getUuid());
		}
		BusEventi.pubblica(new NotificaTestoFrase(ospite.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " viaggia con il gruppo."));
	}

	public final boolean isOspiteVulnerabile(Personaggio ospite) {
		return md.getOspitiVulnerabili().contains(ospite.getModelloDati().getUuid());
	}

	@Override
	public final boolean isOspiteVulnerabile(VistaPersonaggio ospite) {
		return ospiti.stream().filter(o -> o == ospite).anyMatch(this::isOspiteVulnerabile);
	}

	private List<Personaggio> getOspitiVulnerabiliVivi() {
		return ospiti.stream().filter(this::isOspiteVulnerabile).filter(Personaggio::isVivo).collect(Collectors.toList());
	}

	/**
	 * Gli ospiti vulnerabili vivi: un avversario che non ha un bersaglio preferito li sceglie fra i personaggi vivi
	 * con la stessa probabilità, quindi con tre personaggi vivi e un ospite vulnerabile un attacco su quattro va
	 * all'ospite.
	 */
	@Override
	public List<Personaggio> getOspitiBersaglio() {
		return getOspitiVulnerabiliVivi();
	}

	/**
	 * Toglie un ospite dal gruppo.
	 */
	public final void rimuoviOspite(Personaggio ospite) {
		ospiti.remove(ospite);
		md.togliOspite(ospite.getModelloDati());
		md.togliOspiteVulnerabile(ospite.getModelloDati().getUuid());
		BusEventi.pubblica(new NotificaTestoFrase(ospite.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " si separa dal gruppo."));
	}

	private GruppoGiocatoreMD md = ModelloDati.getIstanza().getGruppoGiocatoreMD();

	@Override
	protected GruppoMD getGruppoMD() {
		return md;
	}
	private final List<Personaggio> ospiti = new ArrayList<>();
	private Locazione locazioneCorrente;

	// Serve per passare chi formula un incantesimo all'automa dalla locazione base.
	// Certo si potrebbe usare un evento sul bus ... che però per ora non è usato.
	private Personaggio formulante;

	@Override
	public final void reimposta() {
		super.reimposta();
		md.reimposta();
		ospiti.clear();
		md.setMonete(Costanti.MONETE_INIZIALI);
		md.setPreziosi(Costanti.PREZIOSI_INIZIALI);
		md.setIncantesimi(TipoIncantesimo.ARIA, 3);
		md.setIncantesimi(TipoIncantesimo.ACQUA, 3);
		md.setIncantesimi(TipoIncantesimo.TERRA, 3);
		md.setPozioniSalute(Costanti.POZIONI_SALUTE_INIZIALI);
		md.setPozioniSaluteGrande(0);
		md.setPozioniMagia(0);
		md.setPozioniMagiaGrande(0);
		md.setPozioniAumentoLivello(0);
		md.setCoordinate(Foresta.getCoordinateLibere());
		Foresta.aggiornaMappaCircostante(this);

		if (ModalitaDiProva.isAttiva()) {
			md.setMonete(9999);
			for (TipoIncantesimo tipoIncantesimo : TipoIncantesimo.values()) {
				md.setIncantesimi(tipoIncantesimo, 99);
			}
			md.setPozioniSalute(99);
			md.setPozioniSaluteGrande(99);
			md.setPozioniMagia(99);
			md.setPozioniMagiaGrande(99);
			md.setPozioniAumentoLivello(99);
			// La Falce del Tristo Mietitore, per provare la Mietitura
			CatalogoLeggendari.getLeggendario(CHIAVE_FALCE_DI_PROVA).ifPresent(falce -> addArtefatto(falce.costruisci()));
			// Due spade di livello 1 per provare i modificatori temporanei sul bersaglio (vedi modificatori_temporanei.md)
			addArtefatto(spadaDiProva("la Spada dell'Inciampo", "che fa inciampare chi colpisce",
					ModificatoreDelTesto.da("FORTUNA =0 @BERSAGLIO 2TURNI")));
			addArtefatto(spadaDiProva("la Spada della Debolezza", "che toglie forza a chi colpisce",
					ModificatoreDelTesto.da("FORZA -2 @BERSAGLIO 3TURNI")));
			Foresta.ottieniMappa();
		}
	}

	private static Artefatto spadaDiProva(String nome, String descrizione, ModificatoreAttributoMD modificatore) {
		return CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.SPADA)
				.setNome(nome)
				.setDescrizione(descrizione)
				.setLivello(1)
				.setDanniBase(10)
				.setCostoAcquisto(30)
				.setPeso(2)
				.setModificatore(modificatore)
				.costruisci();
	}

	@Override
	public boolean isGruppoGiocatore() {
		return true;
	}

	@Override
	public final void aggiungiPersonaggio(Personaggio personaggio) {
		super.aggiungiPersonaggio(personaggio);
		String nome = personaggio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
		boolean volentieri = personaggio.getMotivoNelGruppo().isEntraVolentieri();
		BusEventi.pubblica(new NotificaTestoFrase(nome + (volentieri ? " è felice di poter far parte del gruppo."
				: " entra a far parte del gruppo.")));
		md.addPersonaggioMD(personaggio.getModelloDati());
		allineaIndiceCapo();
	}

	/**
	 * Aggiunge un personaggio al gruppo senza pubblicare le notifiche: serve quando si ricostruisce il gruppo.
	 */
	public final void aggiungiPersonaggioSenzaNotificare(Personaggio personaggio) {
		super.aggiungiPersonaggio(personaggio);
		md.addPersonaggioMD(personaggio.getModelloDati());
		allineaIndiceCapo();
	}

	/**
	 * Come {@link Gruppo#spostaPersonaggio}, e anche l'ordine del modello dati, che è quello che si salva.
	 */
	@Override
	public boolean spostaPersonaggio(Personaggio p, int direzione) {
		if (!super.spostaPersonaggio(p, direzione)) {
			return false;
		}
		List<PersonaggioMD> inOrdine = new ArrayList<>();
		for (Personaggio personaggio : personaggi) {
			inOrdine.add(personaggio.getModelloDati());
		}
		md.riordinaPersonaggiMD(inOrdine);
		allineaIndiceCapo();
		return true;
	}

	/**
	 * Il modello dati ricorda dove sta il capo, che si salva con la partita.
	 */
	private void allineaIndiceCapo() {
		md.setIndiceCapo(Math.max(0, personaggi.indexOf(capo)));
	}

	/**
	 * Congeda chi è morto e non può essere risuscitato (vedi TipoMotivoNelGruppo.isLasciaSeMuore): lo Scheletro della
	 * Mietitura che cade torna polvere, e nessun incantesimo di Resurrezione lo riporta indietro. Non lo si toglie
	 * nell'istante in cui muore perché la morte può arrivare nel mezzo di un ciclo sui personaggi del gruppo (un
	 * incantesimo di gruppo, per esempio): va chiamato fra un'azione e l'altra.
	 */
	public final void congedaChiNonRisorge() {
		for (Personaggio p : new ArrayList<>(personaggi)) {
			if (!p.isVivo() && p.getMotivoNelGruppo().isLasciaSeMuore()) {
				rimuoviPersonaggio(p);
			}
		}
	}

	@Override
	public final void rimuoviPersonaggio(Personaggio p) {
		super.rimuoviPersonaggio(p);
		// Anche dal modello dati, altrimenti ricomparirebbe dopo un salvataggio
		md.rimuoviPersonaggioMD(p.getModelloDati());
		allineaIndiceCapo();
		String nome = p.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
		if (p.getMotivoNelGruppo() == TipoMotivoNelGruppo.MIETITURA) {
			// Non se ne va: o è caduto, e non c'è un'altra morte da raccontare, o è finito l'effetto della Mietitura che lo teneva in piedi
			BusEventi.pubblica(new NotificaTestoFrase(p.isVivo()
					? nome + " torna alla polvere: l'effetto della Mietitura è finito."
					: nome + " torna ad essere polvere."));
		} else {
			BusEventi.pubblica(new NotificaTestoFrase(nome + " lascia il gruppo."));
		}
	}

	/**
	 * Le sfide a carta, forbici e sasso vinte in tutta la partita.
	 */
	public final int getSfideVinte() {
		return md.getSfideVinte();
	}

	/**
	 * Conta una sfida a carta, forbici e sasso vinta in più.
	 */
	public final void addSfidaVinta() {
		md.setSfideVinte(md.getSfideVinte() + 1);
	}

	public final int getMonete() {
		return md.getMonete();
	}

	/**
	 * Aggiunge monete al gruppo e lo annuncia alla UI.
	 */
	public final void addMonete(int quantita) {
		int valorePrecedente = md.getMonete();
		int valoreAttuale = valorePrecedente + quantita;
		md.setMonete(valoreAttuale);
		BusEventi.pubblica(new NotificaVariazioneDisponibilitaMonete(valorePrecedente, valoreAttuale));
	}

	/**
	 * Toglie monete al gruppo.
	 */
	public final void subMonete(int quantita) {
		addMonete(-quantita);
	}

	/**
	 * Nelle botteghe il gruppo manda avanti chi sa trattare meglio: la CONTRATTAZIONE più alta tra i personaggi vivi.
	 */
	public final int getContrattazione() {
		return getPersonaggiVivi().stream().mapToInt(Personaggio::getContrattazione).max().orElse(0);
	}

	/**
	 * Quanto paga il gruppo un oggetto di quel costo (vedi RegoleContrattazione).
	 */
	public final int prezzoAcquisto(int costo) {
		return RegoleContrattazione.prezzoAcquisto(costo, getContrattazione());
	}

	/**
	 * Quanto ricava il gruppo vendendo un oggetto di quel costo (vedi RegoleContrattazione).
	 */
	public final int prezzoVendita(int costo) {
		return RegoleContrattazione.prezzoVendita(costo, getContrattazione());
	}

	/**
	 * Il costo della fusione di quel che c'è sul banco, scontato secondo la contrattazione del gruppo.
	 */
	public final int costoFusione(Collection<Artefatto> banco) {
		return RegoleIncantatura.costo(banco, getContrattazione());
	}

	/**
	 * Il costo di una fusione che trasferisce quel numero di effetti, scontato secondo la contrattazione del gruppo.
	 */
	public final int costoFusione(int effettiDaTrasferire) {
		return RegoleIncantatura.costo(effettiDaTrasferire, getContrattazione());
	}

	public final int getPreziosi() {
		return md.getPreziosi();
	}

	/**
	 * Aggiunge pietre preziose al gruppo e lo annuncia alla UI.
	 */
	public final void addPreziosi(int quantita) {
		int valorePrecedente = md.getPreziosi();
		int valoreAttuale = valorePrecedente + quantita;
		md.setPreziosi(valoreAttuale);
		BusEventi.pubblica(new NotificaVariazioneDisponibilitaPreziosi(valorePrecedente, valoreAttuale));
	}

	/**
	 * Toglie pietre preziose al gruppo.
	 */
	public final void subPreziosi(int quantita) {
		addPreziosi(-quantita);
	}

	public final void setCoordinate(CoordinateMD coordinate) {
		md.setCoordinate(coordinate);
		Foresta.aggiornaMappaCircostante(this);
	}

	public final int getX() {
		return md.getCoordinate().getX();
	}

	public final int getY() {
		return md.getCoordinate().getY();
	}

	public final CoordinateMD getCoordinate() {
		return md.getCoordinate();
	}

	private TipoLocazione getTipoLocazioneCorrente(int x, int y) {
		return Foresta.getLocazione(x, y);
	}

	/**
	 * Usata da oggetti Cofano per sapere se può essere vuoto o meno
	 */
	public TipoLocazione getTipoLocazioneCorrente() {
		return Foresta.getLocazione(md.getCoordinate());
	}

	public Locazione getLocazioneCorrente() {
		return locazioneCorrente;
	}

	@Override
	public List<RegoleSetLeggendari.Pezzo> getPezziDelSet(VistaArtefatto artefatto) {
		List<Collection<ArtefattoMD>> equipaggiamenti = personaggi.stream()
				.map(p -> p.getModelloDati().getArtefatti())
				.collect(Collectors.toList());
		List<ArtefattoMD> inventario = getInventario().stream().map(Artefatto::getModelloDati).collect(Collectors.toList());
		return RegoleSetLeggendari.pezzi(Artefatto.da(artefatto).getModelloDati(), equipaggiamenti, inventario);
	}

	@Override
	public TipoOggetto getTipoOggettoInLocazione() {
		Oggetto oggetto = locazioneCorrente == null ? null : locazioneCorrente.getOggetto();
		return oggetto == null ? null : oggetto.getClasse();
	}

	public void setLocazioneCorrente(Locazione locazioneCorrente) {
		this.locazioneCorrente = locazioneCorrente;
	}

	public final void setLocazioneCorrenteVisitata() {
		Foresta.setLocazioneVisitata(md.getCoordinate());
	}

	public final int getMaxPassiNord() {
		if (getY() == 0) {
			return 0;
		}
		TipoLocazione locazioneCorrente;
		for (int i = 1; i <= Comando.MAX_MOVIMENTO; i++) {
			if (getY() - i == 0) {
				return i;
			}
			locazioneCorrente = getTipoLocazioneCorrente(getX(), getY() - i);
			if (locazioneCorrente != TipoLocazione.BOSCO && locazioneCorrente != TipoLocazione.RADURA) {
				return i;
			}
		}
		return Comando.MAX_MOVIMENTO;
	}

	/**
	 * Sposta il gruppo verso nord di quei passi, fermandosi al bordo della mappa.
	 */
	public final void muoveNord(int passi) {
		int maxPassiNord = getMaxPassiNord();
		if (passi > maxPassiNord) {
			passi = maxPassiNord;
		}
		setCoordinate(new CoordinateMD(getX(), getY() - passi));
	}

	public final int getMaxPassiEst() {
		if (getX() == Foresta.getDimensioneX() - 1)
			return 0;
		TipoLocazione locazioneCorrente;
		for (int i = 1; i <= Comando.MAX_MOVIMENTO; i++) {
			if (getX() + i == Foresta.getDimensioneX() - 1) {
				return i;
			}
			locazioneCorrente = getTipoLocazioneCorrente(getX() + i, getY());
			if (locazioneCorrente != TipoLocazione.BOSCO && locazioneCorrente != TipoLocazione.RADURA) {
				return i;
			}
		}
		return Comando.MAX_MOVIMENTO;
	}

	/**
	 * Sposta il gruppo verso est di quei passi, fermandosi al bordo della mappa.
	 */
	public final void muoveEst(int passi) {
		int maxPassiEst = getMaxPassiEst();
		if (passi > maxPassiEst) {
			passi = maxPassiEst;
		}
		setCoordinate(new CoordinateMD(getX() + passi, getY()));
	}

	public final int getMaxPassiSud() {
		if (getY() == Foresta.getDimensioneY() - 1) {
			return 0;
		}
		TipoLocazione locazioneCorrente;
		for (int i = 1; i <= Comando.MAX_MOVIMENTO; i++) {
			if (getY() + i == Foresta.getDimensioneY() - 1) {
				return i;
			}
			locazioneCorrente = getTipoLocazioneCorrente(getX(), getY() + i);
			if (locazioneCorrente != TipoLocazione.BOSCO && locazioneCorrente != TipoLocazione.RADURA) {
				return i;
			}
		}
		return Comando.MAX_MOVIMENTO;
	}

	/**
	 * Sposta il gruppo verso sud di quei passi, fermandosi al bordo della mappa.
	 */
	public final void muoveSud(int passi) {
		int maxPassiSud = getMaxPassiSud();
		if (passi > maxPassiSud) {
			passi = maxPassiSud;
		}
		setCoordinate(new CoordinateMD(getX(), getY() + passi));
	}

	public final int getMaxPassiOvest() {
		if (getX() == 0) {
			return 0;
		}
		TipoLocazione locazioneCorrente;
		for (int i = 1; i <= Comando.MAX_MOVIMENTO; i++) {
			if (getX() - i == 0) {
				return i;
			}
			locazioneCorrente = getTipoLocazioneCorrente(getX() - i, getY());
			if (locazioneCorrente != TipoLocazione.BOSCO && locazioneCorrente != TipoLocazione.RADURA) {
				return i;
			}
		}
		return Comando.MAX_MOVIMENTO;
	}

	/**
	 * Sposta il gruppo verso ovest di quei passi, fermandosi al bordo della mappa.
	 */
	public final void muoveOvest(int passi) {
		int maxPassiOvest = getMaxPassiOvest();
		if (passi > maxPassiOvest) {
			passi = maxPassiOvest;
		}
		setCoordinate(new CoordinateMD(getX() - passi, getY()));
	}

	/**
	 * Fa riposare un'ora tutti i personaggi vivi e gli ospiti che si possono ferire.
	 */
	public final void riposa(TipoRiposo tipoRiposo) {
		getPersonaggiVivi().forEach(p -> p.riposa(1, tipoRiposo));
		// Anche gli ospiti che si possono ferire recuperano
		getOspitiVulnerabiliVivi().forEach(p -> p.riposa(1, tipoRiposo));
		BusEventi.pubblica(new InternoMostraFinestraStato());
	}

	/**
	 * Fa passare la notte al gruppo, fino al mattino, con il tipo di riposo indicato.
	 */
	public final void pernotta(TipoRiposo tipoRiposo) {
		Logger.log("Inizio pernottamento");
		TipoLocazione tipoLocazione = getTipoLocazioneCorrente();
		int ore = LineaTemporale.oreFinoAlMattino();
		boolean alCoperto = tipoLocazione == TipoLocazione.LOCANDA
				|| tipoLocazione.getCategoria() == CategoriaLocazione.CITTA;
		Logger.log(() -> "Pernottamento al coperto? " + alCoperto);
		if (alCoperto) {
			StringBuilder sb = new StringBuilder("La stanchezza accumulata ed il tepore delle coperte fanno addormentare subito ");
			if (getNumeroPersonaggiVivi() > 1) {
				sb.append("tutto il gruppo");
			} else {
				sb.append(capo.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE));
			}
			sb.append(". La notte alla locanda trascorre placida e tranquilla.");
			BusEventi.pubblica(new NotificaTestoFrase(sb.toString()));
		} else {
			StringBuilder sb = new StringBuilder("Il gruppo decide di accamparsi qui per riposare un po'. Dopo aver stabilito i turni di guardia, i ")
					.append(Misc.getCardinaleM(personaggi.size())).append(" intrepidi avventurieri si godono un meritato riposo. ");
			if (ore == 1) {
				sb.append("Ma un'ora sola è veramente insufficiente");
			} else if (ore == 2) {
				sb.append("Ma due ore sono insufficienti");
			} else if (ore == 3) {
				sb.append("Ma tre ore sono troppo poche");
			} else if (ore == 4) {
				sb.append("Ma quattro ore sono un po' poche");
			} else if (ore == 5) {
				sb.append("Cinque ore sono appena sufficienti");
			} else {
				if (getNumeroPersonaggiVivi() > 1) {
					sb.append("Il gruppo");
				} else {
					sb.append(capo.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA));
				}
				sb.append(" ha riposato abbastanza");
			}
			sb.append(" per rimettersi in forze.");
			BusEventi.pubblica(new NotificaTestoFrase(sb.toString()));
		}
		getPersonaggiVivi().forEach(p -> p.riposa(ore, tipoRiposo));
		getOspitiVulnerabiliVivi().forEach(p -> p.riposa(ore, tipoRiposo));
		BusEventi.pubblica(new NotificaTestoFrase("Il sole sorge e l'avventura ricomincia."));
	}

	/**
	 * Un prezioso vale quanto il livello del mondo, così le pietre e le corone seguono i prezzi che salgono col livello;
	 * la vendita ha miglior successo se nel gruppo c'è un ladro
	 */
	public final void vendePreziosi() {
		if (md.getPreziosi() > 0) {
			int valore = Statistiche.getLivello();
			int quantita = md.getPreziosi() * valore;
			if (getPersonaggiVivi().stream().anyMatch(p -> p.getClasse() == TipoPersonaggio.LADRA || p.getClasse() == TipoPersonaggio.LADRO)) {
				quantita += Dado.tiraAncheAUnaFaccia(md.getPreziosi()) * valore;
			}
            String notifica = chiMaiuscolo() +
                    " ha venduto i preziosi raccolti, ricavandone " +
                    (quantita == 1 ? " una moneta." : (quantita + " monete."));
			BusEventi.pubblica(new NotificaTestoFrase(notifica));
			addMonete(quantita);
			subPreziosi(md.getPreziosi());
			BusEventi.pubblica(new InternoMostraFinestraStatistiche());
		}
	}

	// TODO riguardare la fuga, oggi troppo penalizzante: oltre a perdere fino a metà di
	// monete, preziosi, incantesimi e pozioni, ogni personaggio vivo subisce da 50 a 100
	// danni (PersonaggioBase.fugge), e se muore il capo la partita è persa.
	/**
	 * Il gruppo fugge: subisce gravi ferite e perde molte delle cose che ha con sé.
	 */
	public final void fugge() {
		BusEventi.pubblica(new NotificaTestoFrase(chiMaiuscolo() + ", in preda al panico, cerca la salvezza nella fuga!" +
				" Sfortunatamente riceve gravi ferite e perde molte delle cose in suo possesso!"));

		Function<Integer, Integer> calcolaPerdita = m -> Dado.tiraAncheSenzaRange(0, m / 2);

		subMonete(calcolaPerdita.apply(md.getMonete()));
		subPreziosi(calcolaPerdita.apply(md.getPreziosi()));
		for (TipoIncantesimo tipoIncantesimo : TipoIncantesimo.values()) {
			subIncantesimi(tipoIncantesimo, calcolaPerdita.apply(md.getIncantesimi(tipoIncantesimo)));
		}
		subPozioniSalute(calcolaPerdita.apply(md.getPozioniSalute()));
		subPozioniSaluteGrande(calcolaPerdita.apply(md.getPozioniSaluteGrande()));
		subPozioniMagia(calcolaPerdita.apply(md.getPozioniMagia()));
		subPozioniMagiaGrande(calcolaPerdita.apply(md.getPozioniMagiaGrande()));
		subPozioniAumentoLivello(calcolaPerdita.apply(md.getPozioniAumentoLivello()));

		getPersonaggiVivi().forEach(Personaggio::fugge);

		BusEventi.pubblica(new InternoMostraFinestraStato());
	}

	public boolean isInLocazioneUnica(TipoLocazione tipoLocazioneUnica) {
		return getTipoLocazioneCorrente() == tipoLocazioneUnica;
	}

	/**
	 * Dà quei punti esperienza a tutti i personaggi vivi e alle statistiche del gioco.
	 */
	public void addPuntiEsperienza(int puntiEsperienza) {
		Statistiche.addPuntiEsperienza(puntiEsperienza);
		for (Personaggio personaggio : getPersonaggiVivi()) {
			personaggio.addPuntiEsperienza(puntiEsperienza);
		}
	}

	public Personaggio getFormulante() {
		return formulante;
	}

	public void setFormulante(Personaggio formulante) {
		this.formulante = formulante;
	}

	// Scambiatore Artefatti

	/**
	 * Artefatti disponibili al gruppo ma non in uso da un personaggio specifico.
	 */
	public Collection<Artefatto> getInventario() {
		return md.getArtefatti().stream().map(Artefatto::di).collect(Collectors.toList());
	}

	/**
	 * Mette l'artefatto nell'inventario del gruppo, dove nessun artefatto è equipaggiato.
	 */
	public void addArtefatto(Artefatto artefatto) {
		// Nell'inventario del gruppo nessun artefatto è equipaggiato
		artefatto.getModelloDati().setSlotEquipaggiamento(null);
		md.aggiungiArtefatto(artefatto.getModelloDati());
	}

	/**
	 * Toglie l'artefatto dall'inventario del gruppo.
	 */
	public void removeArtefatto(Artefatto artefatto) {
		md.togliArtefatto(artefatto.getModelloDati());
	}

	/**
	 * Il controllo di un prelievo dall'inventario del gruppo: il personaggio può equipaggiare quell'artefatto, così com'è adesso?
	 * Lo usano la schermata dell'inventario (tramite l'evento di prelievo) e le tattiche.
	 *
	 * @return il motivo del rifiuto, oppure vuoto se si può
	 */
	public Optional<RegoleEquipaggiamento.EsitoControlloRichiestaEquipaggiamento> controllaPrelievo(Personaggio personaggio, Artefatto artefatto) {
		return personaggio.puoEquipaggiare(artefatto);
	}

	/**
	 * Il passaggio di un artefatto dall'inventario del gruppo al personaggio, senza controlli (vanno fatti prima con
	 * {@link #controllaPrelievo}) e senza notifiche.
	 */
	public void equipaggia(Personaggio personaggio, Artefatto artefatto) {
		removeArtefatto(artefatto);
		personaggio.addArtefatto(artefatto);
	}

	/**
	 * Il passaggio di un artefatto dal personaggio all'inventario del gruppo (non ha controlli: si può sempre togliere).
	 */
	public void stocca(ScambiatoreArtefatti personaggio, Artefatto artefatto) {
		personaggio.removeArtefatto(artefatto);
		addArtefatto(artefatto);
	}

	/**
	 * Il controllo preventivo del cambio d'arma, sulla situazione "senza l'arma che esce" (vedi RegoleEquipaggiamento.valutaCambioArma);
	 * non cambia niente e non pubblica niente. Include il massimo dei cambi per combattimento.
	 *
	 * @param nuova un'arma dell'inventario del gruppo, da mano principale o a due mani
	 * @return vuoto se il cambio si può fare, altrimenti il motivo
	 */
	public Optional<RegoleEquipaggiamento.EsitoControlloRichiestaEquipaggiamento> controllaCambioArma(Personaggio personaggio, Artefatto nuova) {
		RegoleEquipaggiamento.EsitoControlloRichiestaEquipaggiamento esito =
				RegoleEquipaggiamento.valutaCambioArma(personaggio, nuova.getModelloDati());
		return esito.getMotivo() == null ? Optional.empty() : Optional.of(esito);
	}

	/**
	 * Se il personaggio può ancora cambiare arma in questo combattimento (Costanti.MAX_CAMBI_ARMA_PER_COMBATTIMENTO) e con quest'arma;
	 * ricorda l'esito della valutazione nello stato del combattimento, così non si rifà (vedi {@link StatoDelCombattimento}).
	 */
	public boolean puoCambiareArma(Personaggio personaggio, Artefatto nuova) {
		StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
		if (stato.getCambiDArma(personaggio) >= Costanti.MAX_CAMBI_ARMA_PER_COMBATTIMENTO) {
			return false;
		}
		Boolean valutata = stato.getValutazioneArma(personaggio, nuova);
		if (valutata != null) {
			return valutata;
		}
		boolean utilizzabile = !controllaCambioArma(personaggio, nuova).isPresent();
		stato.registraArmaValutata(personaggio, nuova, utilizzabile);
		return utilizzabile;
	}

	/**
	 * Il cambio d'arma come azione sola: se si può (puoCambiareArma) toglie dalle mani le armi che fanno posto, le mette nell'inventario
	 * del gruppo e impugna la nuova; conta il cambio nello stato del combattimento. È atomico: il controllo è già fatto, quindi non teme
	 * rifiuti a metà. Non consuma il turno: lo fa chi lo chiama. Niente notifiche di inventario, la UI vede le variazioni dei personaggi.
	 *
	 * @return true se il cambio è avvenuto
	 */
	public boolean cambiaArma(Personaggio personaggio, Artefatto nuova) {
		if (!puoCambiareArma(personaggio, nuova)) {
			return false;
		}
		for (ArtefattoMD uscente : RegoleEquipaggiamento.uscentiPerCambioArma(personaggio, nuova.getModelloDati())) {
			stocca(personaggio, Artefatto.di(uscente));
		}
		equipaggia(personaggio, nuova);
		StatoDelCombattimento.getIstanza().registraCambioArma(personaggio);
		return true;
	}

	/**
	 * Le armi dell'inventario del gruppo che il personaggio può prendere al posto di quelle che ha in mano e che danno quel tipo di
	 * danno (Arma.daTipoDanno); servono a una tattica come "un'arma sacra contro i non morti".
	 */
	public List<Artefatto> armiPerCambio(Personaggio personaggio, TipoDanno tipoDanno) {
		List<Artefatto> trovate = new ArrayList<>();
		for (Artefatto artefatto : getInventario()) {
			TipoSlotArtefatto slot = artefatto.getTipo().getSlotArtefatto();
			if (!(artefatto instanceof Arma) || (slot != TipoSlotArtefatto.MANO_PRINCIPALE && slot != TipoSlotArtefatto.ENTRAMBE_LE_MANI)) {
				continue;
			}
			if (((Arma) artefatto).daTipoDanno(tipoDanno) && puoCambiareArma(personaggio, artefatto)) {
				trovate.add(artefatto);
			}
		}
		return trovate;
	}

	private void suEventoRichiestaStoccaggioArtefatto(InternoStoccaggioArtefatto eventoRichiestaStoccaggio) {
		Artefatto artefatto = (Artefatto) eventoRichiestaStoccaggio.getOggettoDaSpostare();
		stocca(eventoRichiestaStoccaggio.getParteAttiva(), artefatto);
		BusEventi.pubblica(eventoRichiestaStoccaggio.approvazione());
	}

	private void suEventoRichiestaPrelievoArtefatto(InternoPrelievoArtefatto eventoRichiestaPrelievoArtefatto) {
		Artefatto artefatto = (Artefatto) eventoRichiestaPrelievoArtefatto.getOggettoDaSpostare();
		Personaggio personaggio = (Personaggio) eventoRichiestaPrelievoArtefatto.getParteAttiva();
		Optional<RegoleEquipaggiamento.EsitoControlloRichiestaEquipaggiamento> motivoRifiuto = controllaPrelievo(personaggio, artefatto);
		if (motivoRifiuto.isPresent()) {
			RegoleEquipaggiamento.EsitoControlloRichiestaEquipaggiamento esito = motivoRifiuto.get();
			BusEventi.pubblica(eventoRichiestaPrelievoArtefatto.rifiuto(esito.getFumetto()));
		} else {
			equipaggia(personaggio, artefatto);
			BusEventi.pubblica(eventoRichiestaPrelievoArtefatto.approvazione());
		}
	}

	private void suEventoRichiestaAcquistoArtefatto(InternoAcquistoArtefatto comandoAcquistoArtefatto) {
		Artefatto artefatto = (Artefatto) comandoAcquistoArtefatto.getOggettoDaSpostare();
		int costoOggetto = prezzoAcquisto(artefatto.getCostoAcquisto());
		if (getMonete() >= costoOggetto) {
			addArtefatto(artefatto);
			subMonete(costoOggetto);
			comandoAcquistoArtefatto.getParteRemota().removeArtefatto(artefatto);
			BusEventi.pubblica(comandoAcquistoArtefatto.approvazione());
		} else {
			BusEventi.pubblica(comandoAcquistoArtefatto.rifiuto());
		}
	}

	private void suEventoRichiestaVenditaArtefatto(InternoVenditaArtefatto eventoRichiestaVendita) {
		vende(eventoRichiestaVendita);
	}

	/**
	 * Vende un artefatto del gruppo al commerciante, che lo compra solo se tratta quel genere di oggetti:
	 * l'armaiolo niente pergamene, il venditore di pergamene solo quelle.
	 * Pubblica NotificaApprovazioneVenditaArtefatto o NotificaRifiutoVenditaArtefatto.
	 *
	 * @return true se la vendita è riuscita
	 */
	boolean vende(InternoVenditaArtefatto eventoRichiestaVendita) {
		Artefatto artefatto = (Artefatto) eventoRichiestaVendita.getOggettoDaSpostare();
		if (!eventoRichiestaVendita.getParteRemota().tratta(artefatto)) {
			BusEventi.pubblica(eventoRichiestaVendita.rifiuto());
			return false;
		}
		int ricavo = prezzoVendita(artefatto.getCostoAcquisto());
		removeArtefatto(artefatto);
		addMonete(ricavo);
		eventoRichiestaVendita.getParteRemota().addArtefatto(artefatto);
		BusEventi.pubblica(eventoRichiestaVendita.approvazione());
		return true;
	}

	private void suEventoIncantatura(InternoIncantatura comandoIncantatura) {
		incanta(comandoIncantatura.getBanco(), comandoIncantatura.getNomeProprio());
	}

	/**
	 * La fusione dall'incantatore: ricontrolla le regole (RegoleIncantatura), fa pagare, copia sull'artefatto
	 * gli effetti delle pergamene, che spariscono, e rimette l'artefatto nell'inventario del gruppo.
	 * Pubblica NotificaApprovazioneIncantatura o NotificaRifiutoIncantatura.
	 *
	 * @return il motivo del rifiuto, oppure vuoto se la fusione è riuscita
	 */
	public Optional<TipoMotivoRifiutoIncantatura> incanta(BancoDiLavoro banco, String nomeProprio) {
		Optional<TipoMotivoRifiutoIncantatura> motivo = RegoleIncantatura.verifica(banco.getInventario(), getMonete(), getContrattazione());
		if (motivo.isPresent()) {
			BusEventi.pubblica(new NotificaRifiutoIncantatura(motivo.get()));
			return motivo;
		}
		int costo = costoFusione(banco.getInventario());
		Artefatto incantato = RegoleIncantatura.fondi(banco, nomeProprio);
		subMonete(costo);
		addArtefatto(incantato);
		BusEventi.pubblica(new NotificaApprovazioneIncantatura(incantato, costo));
		return Optional.empty();
	}

	private void suEventoRichiestaAcquistoConsumabile(ComandoAcquistoConsumabile comandoAcquistoConsumabile) {
		// Il prezzo lo decide l'offerta dell'alchimista; se il comando non ne corrisponde a nessuna, si ignora
		Optional<OffertaConsumabile> offerta = OfferteAlchimista.trova(this, comandoAcquistoConsumabile.getTipoConsumabile(),
				comandoAcquistoConsumabile.getClasseIncantesimo(), comandoAcquistoConsumabile.getUuidPersonaggio());
		if (!offerta.isPresent()) {
			return;
		}
		int costoOggetto = prezzoAcquisto(offerta.get().getCosto());
		if (getMonete() >= costoOggetto) {
			switch (comandoAcquistoConsumabile.getTipoConsumabile()) {
				case POZIONE_SALUTE:
					addPozioniSalute(1);
					break;
				case POZIONE_SALUTE_GRANDE:
					addPozioniSaluteGrande(1);
					break;
				case POZIONE_MAGIA:
					addPozioniMagia(1);
					break;
				case POZIONE_MAGIA_GRANDE:
					addPozioniMagiaGrande(1);
					break;
				case AUMENTO_MAGIA_SINGOLO:
					personaggi.stream().filter(p -> p.getUuid().equals(comandoAcquistoConsumabile.getUuidPersonaggio())).findFirst()
							.ifPresent(p -> p.addMagiaMassima(Costanti.AUMENTO_MAGIA_DA_POZIONE_MAGIA_GRANDE, "ALCHIMISTA"));
					break;
				case AUMENTO_MAGIA_GRUPPO:
					for (Personaggio personaggio : getPersonaggiVivi()) {
						if (!personaggio.isPNG()) {
							personaggio.addMagiaMassima(Costanti.AUMENTO_MAGIA_DA_POZIONE_MAGIA_GRANDE, "ALCHIMISTA");
						}
					}
					break;
				case INCANTESIMO:
					addIncantesimi(comandoAcquistoConsumabile.getClasseIncantesimo(), 1);
					break;
				case MAPPA_PARZIALE_FORESTA:
					int x = getCoordinate().getX();
					int y = getCoordinate().getY();
					Foresta.ottieniMappaZona(x - 7, y - 7, x + 7, y + 7);
					break;
				case MAPPA_COMPLETA_FORESTA:
					Foresta.ottieniMappa();
					break;
				default:
					throw new IllegalArgumentException("Tipo consumabile non valido");
			}
			subMonete(costoOggetto);
			BusEventi.pubblica(new NotificaApprovazioneAcquistoConsumabile(comandoAcquistoConsumabile, costoOggetto));
		} else {
			BusEventi.pubblica(new NotificaRifiutoAcquistoConsumabile(comandoAcquistoConsumabile));
		}
	}

}
