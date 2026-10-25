package com.threeamigos.foresta.gruppi;

import com.threeamigos.foresta.personaggi.RegoleEquipaggiamento;
import com.threeamigos.foresta.gruppi.Gruppo;



import com.threeamigos.foresta.artefatti.*;
import com.threeamigos.foresta.artefatti.leggendari.RegoleSetLeggendari;
import com.threeamigos.foresta.tipi.TipoSlotArtefatto;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.*;
import com.threeamigos.foresta.eventi.notifiche.*;
import com.threeamigos.foresta.interfacce.VistaGruppoGiocatore;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.modellodati.*;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Un insieme di personaggi guidati da un giocatore
 */

public class GruppoGiocatore extends Gruppo implements ScambiatoreArtefatti, VistaGruppoGiocatore {

	/**
	 * Il gruppo del giocatore ricostruito da un modello dati, per esempio dopo aver caricato un salvataggio.
	 */
	public static GruppoGiocatore of(GruppoGiocatoreMD gruppoGiocatoreMD) {
		return new GruppoGiocatore(gruppoGiocatoreMD);
	}

	// Da dove prendere i dati del gruppo della partita in corso: il gruppo non sa chi li custodisce (lo dice chi li custodisce)
	private static volatile Supplier<GruppoGiocatoreMD> fornitoreDiDati;

	/**
	 * Dice al gruppo da dove prendere i suoi dati quando ne nasce uno nuovo. Lo fa chi li custodisce e li sostituisce quando si carica un
	 * salvataggio, quindi il fornitore va chiamato a ogni nuovo gruppo e non una volta sola.
	 */
	public static void setFornitoreDiDati(Supplier<GruppoGiocatoreMD> fornitore) {
		fornitoreDiDati = fornitore;
	}

	private static GruppoGiocatoreMD getGruppoGiocatoreMDInCorso() {
		Supplier<GruppoGiocatoreMD> fornitore = fornitoreDiDati;
		if (fornitore == null) {
			throw new IllegalStateException("Nessuno ha detto al gruppo dove sono i suoi dati (vedi setFornitoreDiDati)");
		}
		return fornitore.get();
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
		if (istanza != null) {
			// Il vecchio gruppo non deve restare iscritto al bus: gestirebbe anch'esso gli eventi, insieme al nuovo
			istanza.disiscriviti();
		}
		istanza = null;
	}

	/**
	 * Toglie dal bus gli eventi che questo gruppo ascolta.
	 */
	private void disiscriviti() {
		iscrizioni.forEach(Runnable::run);
		iscrizioni.clear();
	}

	private GruppoGiocatore(GruppoGiocatoreMD gruppoGiocatoreMD) {
		setModelloDati(gruppoGiocatoreMD);
	}

	public GruppoGiocatore() {
		super();
		iscrizioni.add(BusEventi.iscriviti(InternoStoccaggioArtefatto.class, this::suEventoRichiestaStoccaggioArtefatto));
		iscrizioni.add(BusEventi.iscriviti(InternoPrelievoArtefatto.class, this::suEventoRichiestaPrelievoArtefatto));
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

	private GruppoGiocatoreMD md = getGruppoGiocatoreMDInCorso();

	@Override
	public GruppoMD getGruppoMD() {
		return md;
	}
	private final List<Personaggio> ospiti = new ArrayList<>();
	// Come togliere dal bus gli eventi che il gruppo ascolta (vedi azzeraIstanza)
	private final List<Runnable> iscrizioni = new ArrayList<>();

	// Serve per passare chi formula un incantesimo all'automa dalla locazione base.
	// Certo si potrebbe usare un evento sul bus ... che però per ora non è usato.
	private Personaggio formulante;

	@Override
	public final void reimposta() {
		super.reimposta();
		md.reimposta();
		ospiti.clear();
		md.setMonete(CostantiPersonaggi.MONETE_INIZIALI);
		md.setPreziosi(CostantiPersonaggi.PREZIOSI_INIZIALI);
		md.setIncantesimi(TipoIncantesimo.ARIA, 3);
		md.setIncantesimi(TipoIncantesimo.ACQUA, 3);
		md.setIncantesimi(TipoIncantesimo.TERRA, 3);
		md.setPozioniSalute(CostantiPersonaggi.POZIONI_SALUTE_INIZIALI);
		md.setPozioniSaluteGrande(0);
		md.setPozioniMagia(0);
		md.setPozioniMagiaGrande(0);
		md.setPozioniAumentoLivello(0);
	}

	@Override
	public boolean isGruppoGiocatore() {
		return true;
	}

	@Override
	public final void aggiungiPersonaggio(Personaggio personaggio) {
		super.aggiungiPersonaggio(personaggio);
		diventaMembro(personaggio);
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
		diventaMembro(personaggio);
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
	 * Chi entra nel gruppo per restare (il giocatore, un compagno reclutato) smette di essere un PNG, se ha un nome proprio (un personaggio giocante ne ha sempre uno); chi ci sta a tempo (un aiuto, lo
	 * Scheletro della Mietitura) lo resta, come gli ospiti (vedi {@link #aggiungiOspite(Personaggio)}), che non passano da qui.
	 */
	private void diventaMembro(Personaggio personaggio) {
		if (!personaggio.getMotivoNelGruppo().isATempo() && personaggio.getNomeProprio().isPresent()) {
			personaggio.getModelloDati().setPng(false);
		}
		// Anche chi ci sta a tempo e chi si ritrova in un gruppo ricaricato: il bestiario sblocca la voce della sua classe
		BusEventi.pubblica(new InternoPersonaggioEntratoNelGruppo(personaggio.getClasse()));
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

	/**
	 * Se un sacerdote offerto da una missione è già entrato nel gruppo (vedi OffertaDelSacerdote).
	 */
	public final boolean isSacerdoteAccolto() {
		return md.isSacerdoteAccolto();
	}

	public final void setSacerdoteAccolto(boolean accolto) {
		md.setSacerdoteAccolto(accolto);
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
	 * Il gruppo vende tutti i suoi preziosi per quelle monete, già calcolate da chi chiama (a che prezzo si vendano lo sa il mondo).
	 */
	public final void vendiPreziosiPer(int monete) {
		addMonete(monete);
		subPreziosi(md.getPreziosi());
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

	@Override
	public List<RegoleSetLeggendari.Pezzo> getPezziDelSet(VistaArtefatto artefatto) {
		List<Collection<ArtefattoMD>> equipaggiamenti = personaggi.stream()
				.map(p -> p.getModelloDati().getArtefatti())
				.collect(Collectors.toList());
		List<ArtefattoMD> inventario = getInventario().stream().map(Artefatto::getModelloDati).collect(Collectors.toList());
		return RegoleSetLeggendari.pezzi(Artefatto.da(artefatto).getModelloDati(), equipaggiamenti, inventario);
	}

	/**
	 * Fa riposare per quelle ore tutti i personaggi vivi e gli ospiti che si possono ferire.
	 */
	public final void riposa(int ore, TipoRiposo tipoRiposo) {
		getPersonaggiVivi().forEach(p -> p.riposa(ore, tipoRiposo));
		// Anche gli ospiti che si possono ferire recuperano
		getOspitiVulnerabiliVivi().forEach(p -> p.riposa(ore, tipoRiposo));
	}

	/**
	 * Il gruppo fugge: ogni personaggio vivo subisce delle ferite (vedi {@link Personaggio#fugge}) e il gruppo perde una parte delle sue
	 * scorte, il {@value CostantiPersonaggi#FUGA_PERDITA_CONSUMABILI_PERCENTUALE}% di ognuna (arrotondato per difetto: con poche pozioni non se ne
	 * perdono): monete, preziosi, pergamene e pozioni. Gli artefatti restano.
	 */
	public final void fugge() {
		BusEventi.pubblica(new NotificaTestoFrase(chiMaiuscolo() + ", in preda al panico, cerca la salvezza nella fuga!" +
				" Sfortunatamente riceve delle ferite e perde una parte delle cose in suo possesso!"));

		Function<Integer, Integer> calcolaPerdita = m -> m * CostantiPersonaggi.FUGA_PERDITA_CONSUMABILI_PERCENTUALE / 100;

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
	}

	/**
	 * Dà quei punti esperienza a tutti i personaggi vivi, e lo dice a chi tiene il conto del livello del mondo (le statistiche).
	 */
	public void addPuntiEsperienza(int puntiEsperienza) {
		BusEventi.pubblica(new InternoPuntiEsperienzaAssegnati(puntiEsperienza));
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

	/**
	 * Compra l'artefatto a quel prezzo, già calcolato da chi chiama: lo mette nell'inventario e toglie le monete, se il gruppo le ha.
	 *
	 * @return true se il gruppo aveva abbastanza monete
	 */
	public boolean compra(Artefatto artefatto, int costo) {
		if (getMonete() < costo) {
			return false;
		}
		addArtefatto(artefatto);
		subMonete(costo);
		return true;
	}
}
