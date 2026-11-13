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
import com.threeamigos.foresta.strumenti.dado.ContestoLancio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import com.threeamigos.foresta.testi.Testi;

/**
 * Il gruppo del giocatore: il suo personaggio (il capo), i compagni reclutati, chi si unisce a tempo (un aiuto, uno Scheletro della
 * Mietitura) e gli ospiti che viaggiano con loro senza farne parte. Oltre alle scorte di {@link Gruppo} ha l'inventario comune degli
 * artefatti (quelli che nessuno ha equipaggiati: è uno {@link ScambiatoreArtefatti}, come il singolo personaggio, e lo usa la schermata
 * dell'inventario), le sfide vinte e la fuga.
 * <p>
 * Durante la partita ce n'è uno solo ({@link #getIstanza()}), e i suoi dati sono quelli del {@link GruppoGiocatoreMD} della partita in
 * corso, che il gruppo prende dal fornitore (vedi {@link #setFornitoreDiDati}): ogni cambiamento va scritto anche lì, altrimenti non si
 * salverebbe. Altre istanze, senza bus, si fanno solo per mostrare i salvataggi (vedi {@link #of}).
 */

public class GruppoGiocatore extends Gruppo implements ScambiatoreArtefatti, VistaGruppoGiocatore {

	/**
	 * Un gruppo ricostruito da un modello dati, separato da quello della partita: lo usano i salvataggi per mostrare chi c'è in uno slot
	 * (la testata del salvataggio). Non ascolta il bus e non annuncia chi vi entra (InternoPersonaggioEntratoNelGruppo è solo del gruppo
	 * della partita: altrimenti il bestiario sbloccherebbe le classi di uno slot solo a guardarlo). Anche questo chiede il fornitore di dati,
	 * che deve essere installato.
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
	
	/**
	 * Il gruppo della partita in corso; la prima chiamata lo crea sui dati del fornitore e lo iscrive al bus.
	 */
	public static GruppoGiocatore getIstanza() {
		if (istanza == null) {
			istanza = new GruppoGiocatore();
		}
		return istanza;
	}

	/**
	 * Dimentica il gruppo corrente: il prossimo getIstanza() ne crea uno nuovo sul ModelloDati installato. Lo chiama
	 * StatoDelMotore.azzera, quando si riparte da zero (e quindi anche i test, che ricostruiscono una partita a ogni prova).
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
		dellaPartita = false;
		setModelloDati(gruppoGiocatoreMD);
	}

	public GruppoGiocatore() {
		super();
		dellaPartita = true;
		iscrizioni.add(BusEventi.iscriviti(InternoStoccaggioArtefatto.class, this::suEventoRichiestaStoccaggioArtefatto));
		iscrizioni.add(BusEventi.iscriviti(InternoPrelievoArtefatto.class, this::suEventoRichiestaPrelievoArtefatto));
	}

	/**
	 * Fa diventare il gruppo quello descritto dal modello dati (dopo la rilettura di un salvataggio, o per la testata di uno slot): per ogni
	 * {@link PersonaggioMD} crea il personaggio della sua classe e gli dà quei dati, rimette il capo nella posizione salvata e ricrea gli
	 * ospiti. I dati dei personaggi vengono tolti e rimessi nel modello uno per uno, nello stesso ordine, passando da
	 * {@link #aggiungiPersonaggioSenzaNotificare}.
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
		BusEventi.pubblica(new NotificaTestoFrase(Testi.formatta("messaggi_personaggi", "OSPITE_VIAGGIA", ospite.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA))));
	}

	/**
	 * Se quell'ospite può essere attaccato (un ostaggio, un ferito da soccorrere): il modello dati lo ricorda per identificativo.
	 */
	public final boolean isOspiteVulnerabile(Personaggio ospite) {
		return md.getOspitiVulnerabili().contains(ospite.getModelloDati().getUuid());
	}

	@Override
	public final boolean isOspiteVulnerabile(VistaPersonaggio ospite) {
		// La UI dà una vista: si cerca l'ospite che è proprio quell'oggetto, e si chiede a lui
		return ospiti.stream().filter(o -> o == ospite).anyMatch(this::isOspiteVulnerabile);
	}

	private List<Personaggio> getOspitiVulnerabiliVivi() {
		return ospiti.stream().filter(this::isOspiteVulnerabile).filter(Personaggio::isVivo).collect(Collectors.toList());
	}

	/**
	 * Gli ospiti vulnerabili vivi: le tattiche degli avversari li mettono fra i bersagli possibili insieme ai personaggi vivi
	 * (vedi ContestoTattico), e il combattimento fa passare loro i turni e gli effetti di stato come ai personaggi.
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
		BusEventi.pubblica(new NotificaTestoFrase(Testi.formatta("messaggi_personaggi", "OSPITE_SI_SEPARA", ospite.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA))));
	}

	private GruppoGiocatoreMD md = getGruppoGiocatoreMDInCorso();

	@Override
	public GruppoMD getGruppoMD() {
		return md;
	}
	private final List<Personaggio> ospiti = new ArrayList<>();
	// Come togliere dal bus gli eventi che il gruppo ascolta (vedi azzeraIstanza)
	private final List<Runnable> iscrizioni = new ArrayList<>();
	// Vero per il gruppo che gioca, falso per le copie fatte con of() per le testate dei salvataggi: solo il primo annuncia chi vi entra
	private final boolean dellaPartita;

	// Il personaggio a cui il giocatore ha chiesto di lanciare un incantesimo (o di cantare, o di usare un'abilità): LocazioneBase lo mette
	// qui e l'Automa lo rilegge per offrire i suoi incantesimi, canti e abilità, poi LocazioneBase per eseguire la scelta. Non si salva;
	// null quando nessuno sta formulando.
	private Personaggio formulante;

	@Override
	public final void reimposta() {
		super.reimposta();
		md.reimposta();
		ospiti.clear();
		// Si comincia senza incantesimi né pozioni (li ha azzerati md.reimposta()): li dà l'eremita alla fine della prima locazione (vedi
		// DotazioneDellEremita)
	}

	@Override
	public boolean isGruppoGiocatore() {
		return true;
	}

	/**
	 * Un personaggio entra nel gruppo: lo si annuncia (“entra volentieri” o no, secondo il motivo per cui entra) e lo si aggiunge anche al
	 * modello dati. Non controlla i limiti del gruppo: lo fa prima chi lo fa entrare (per chi entra a tempo, vedi
	 * {@link #faiPostoAUnoATempo()}).
	 */
	@Override
	public final void aggiungiPersonaggio(Personaggio personaggio) {
		super.aggiungiPersonaggio(personaggio);
		diventaMembro(personaggio);
		String nome = personaggio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
		boolean volentieri = personaggio.getMotivoNelGruppo().isEntraVolentieri();
		BusEventi.pubblica(new NotificaTestoFrase(Testi.formatta("messaggi_personaggi", volentieri ? "ENTRA_VOLENTIERI" : "ENTRA_NEL_GRUPPO", nome)));
		md.addPersonaggioMD(personaggio.getModelloDati());
		allineaIndiceCapo();
	}

	/**
	 * Aggiunge un personaggio al gruppo senza pubblicare le notifiche di testo: serve quando si ricostruisce il gruppo da un modello dati.
	 * L'evento interno InternoPersonaggioEntratoNelGruppo invece parte lo stesso, se è il gruppo della partita (vedi diventaMembro).
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
		// Anche chi ci sta a tempo e chi si ritrova in un gruppo ricaricato: il bestiario sblocca la voce della sua classe. Non per le copie
		// delle testate dei salvataggi (vedi of)
		if (dellaPartita) {
			BusEventi.pubblica(new InternoPersonaggioEntratoNelGruppo(personaggio));
		}
	}

	/**
	 * Riscrive nel modello dati la posizione del capo, che si salva con la partita: va chiamato dopo ogni cambiamento dell'ordine o dei
	 * membri del gruppo. Con il gruppo vuoto (capo null, indexOf dà -1) scrive 0.
	 */
	private void allineaIndiceCapo() {
		md.setIndiceCapo(Math.max(0, personaggi.indexOf(capo)));
	}

	/**
	 * Fa posto a un personaggio a tempo che sta per entrare: in tutto non si superano {@link CostantiPersonaggi#MAX_PERSONAGGI_GRUPPO_TOTALE}
	 * personaggi, quindi a gruppo pieno se ne va quello a tempo (un aiuto, uno Scheletro risvegliato, chi è nel gruppo per una missione)
	 * il cui tempo è più vicino alla scadenza, anche se è caduto.
	 *
	 * @return se ora c'è posto: falso solo se il gruppo è pieno e nessuno è a tempo
	 */
	public final boolean faiPostoAUnoATempo() {
		if (getNumeroPersonaggi() < CostantiPersonaggi.MAX_PERSONAGGI_GRUPPO_TOTALE) {
			return true;
		}
		// Fra chi è a tempo, quello con meno TEMPO rimasto (chi non ha l'attributo conta 0, e se ne va per primo)
		Optional<Personaggio> inScadenza = personaggi.stream()
				.filter(Personaggio::isATempo)
				.min(Comparator.comparingDouble(p -> p.getModelloDati().getOptional(TipoAttributo.TEMPO).orElse(0.0d)));
		inScadenza.ifPresent(this::rimuoviPersonaggio);
		return inScadenza.isPresent();
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

	/**
	 * Fa passare un turno ai personaggi vivi e agli ospiti vulnerabili (vedi {@link Personaggio#trascorriUnTurno()}) e congeda chi non è
	 * sopravvissuto: lo si chiama a ogni round di combattimento, a ogni passo fra una locazione e l'altra e, dormendo, per ogni ora di
	 * sonno pesata dal tipo di riposo (vedi Pernottamento).
	 */
	public final void trascorriUnTurno() {
		for (Personaggio personaggio : getPersonaggiVivi()) {
			personaggio.trascorriUnTurno();
		}
		// Anche gli ospiti vulnerabili, che possono essere colpiti e avere degli stati, non sono fra i personaggi del gruppo
		for (Personaggio ospite : getOspitiBersaglio()) {
			ospite.trascorriUnTurno();
		}
		congedaChiNonRisorge();
	}

	/**
	 * Toglie il personaggio dal gruppo e dal modello dati, e racconta come se ne va (lo Scheletro della Mietitura torna polvere, gli altri
	 * lasciano il gruppo).
	 */
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
					? Testi.formatta("messaggi_personaggi", "POLVERE_FINE_EFFETTO", nome)
					: Testi.formatta("messaggi_personaggi", "POLVERE", nome)));
		} else {
			BusEventi.pubblica(new NotificaTestoFrase(Testi.formatta("messaggi_personaggi", "LASCIA_IL_GRUPPO", nome)));
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

	/**
	 * Nelle botteghe il gruppo manda avanti chi sa trattare meglio: la CONTRATTAZIONE più alta tra i personaggi vivi (0 se non ce n'è
	 * nessuno). La usa CommercioDelGruppo per i prezzi.
	 */
	public final int getContrattazione() {
		return getPersonaggiVivi().stream().mapToInt(p -> p.get(TipoAttributo.CONTRATTAZIONE)).max().orElse(0);
	}

	/**
	 * I pezzi del set leggendario a cui appartiene l'artefatto, cercati su tutto il gruppo: addosso a ogni personaggio (anche ai morti) e
	 * nell'inventario comune. Serve alla UI (ElencoDegliArtefatti) per dire quali pezzi si hanno già e dove stanno.
	 */
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
	 * scorte, da {@value CostantiPersonaggi#FUGA_PERDITA_MINIMA_PERCENTUALE} a {@value CostantiPersonaggi#FUGA_PERDITA_MASSIMA_PERCENTUALE}% di ognuna
	 * (la stessa percentuale per tutte, tirata una volta; arrotondato per difetto: con poche pozioni può non perderne): pergamene e pozioni. Gli artefatti restano; le monete e i preziosi li perde la cassa (vedi economia.Cassa).
	 */
	public final void fugge() {
		BusEventi.pubblica(new NotificaTestoFrase(Testi.formatta("messaggi_personaggi", "FUGA", chiMaiuscolo())));

		int percentuale = Dado.tira(ContestoLancio.FUGA_PERDITA_SCORTE, CostantiPersonaggi.FUGA_PERDITA_MINIMA_PERCENTUALE, CostantiPersonaggi.FUGA_PERDITA_MASSIMA_PERCENTUALE);
		// Quanti se ne perdono di una scorta di m: la percentuale di m, con la divisione intera che arrotonda per difetto
		Function<Integer, Integer> calcolaPerdita = m -> m * percentuale / 100;

		for (TipoIncantesimo tipoIncantesimo : TipoIncantesimo.values()) {
			subIncantesimi(tipoIncantesimo, calcolaPerdita.apply(md.getIncantesimi(tipoIncantesimo)));
		}
		subPozioniSalute(calcolaPerdita.apply(md.getPozioniSalute()));
		subPozioniSaluteGrande(calcolaPerdita.apply(md.getPozioniSaluteGrande()));
		subPozioniMagia(calcolaPerdita.apply(md.getPozioniMagia()));
		subPozioniMagiaGrande(calcolaPerdita.apply(md.getPozioniMagiaGrande()));
		subPozioniPanacea(calcolaPerdita.apply(md.getPozioniPanacea()));
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

	/**
	 * Chi sta formulando l'incantesimo (o il canto, o l'abilità) che il giocatore ha scelto; null se nessuno.
	 */
	public Personaggio getFormulante() {
		return formulante;
	}

	public void setFormulante(Personaggio formulante) {
		this.formulante = formulante;
	}

	// Scambiatore Artefatti

	/**
	 * Artefatti disponibili al gruppo ma non in uso da un personaggio specifico. È una lista nuova a ogni chiamata: per cambiare l'inventario
	 * si usano {@link #addArtefatto} e {@link #removeArtefatto}.
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
	 * Lo usa la richiesta di prelievo (InternoPrelievoArtefatto) che AutomaInventario pubblica per la schermata dell'inventario.
	 *
	 * @return il motivo del rifiuto, oppure vuoto se si può
	 */
	public Optional<RegoleEquipaggiamento.EsitoControlloRichiestaEquipaggiamento> controllaPrelievo(Personaggio personaggio, Artefatto artefatto) {
		return personaggio.puoEquipaggiare(artefatto);
	}

	/**
	 * Le posizioni nel gruppo dei personaggi vivi che possono equipaggiare l'artefatto: slot libero, livello sufficiente, mani
	 * libere, carico. Le pergamene non hanno mai candidati. Chi è a tempo non conta: se ne andrebbe subito portandosi via
	 * l'artefatto.
	 */
	public List<Integer> chiPuoEquipaggiare(Artefatto artefatto) {
		List<Integer> posizioni = new ArrayList<>();
		for (int i = 0; i < personaggi.size(); i++) {
			Personaggio personaggio = personaggi.get(i);
			if (personaggio.isVivo() && !personaggio.isATempo() && !personaggio.puoEquipaggiare(artefatto).isPresent()) {
				posizioni.add(i);
			}
		}
		return posizioni;
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

	// AutomaInventario, per la schermata dell'inventario, chiede di riporre un artefatto di un personaggio nell'inventario comune: si può sempre, e si approva
	private void suEventoRichiestaStoccaggioArtefatto(InternoStoccaggioArtefatto eventoRichiestaStoccaggio) {
		Artefatto artefatto = (Artefatto) eventoRichiestaStoccaggio.getOggettoDaSpostare();
		stocca(eventoRichiestaStoccaggio.getParteAttiva(), artefatto);
		BusEventi.pubblica(eventoRichiestaStoccaggio.approvazione());
	}

	// AutomaInventario, per la schermata dell'inventario, chiede di dare a un personaggio un artefatto dell'inventario comune: si approva se le regole
	// dell'equipaggiamento lo permettono, altrimenti si rifiuta con la frase che il personaggio dice nel fumetto
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
}
