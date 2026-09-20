package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoPortaInPrimoPiano;
import com.threeamigos.foresta.eventi.comandigiocatore.*;
import com.threeamigos.foresta.eventi.notifiche.*;
import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.motore.modellodati.*;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;
import com.threeamigos.foresta.ui.InterfacciaUtente;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Un insieme di personaggi guidati da un giocatore
 */

public class GruppoGiocatore extends Gruppo implements ScambiatoreArtefatti {

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

	private GruppoGiocatore(GruppoGiocatoreMD gruppoGiocatoreMD) {
		setModelloDati(gruppoGiocatoreMD);
	}

	public GruppoGiocatore() {
		super();
		BusEventi.iscriviti(ComandoStoccaggioArtefatto.class, this::suEventoRichiestaStoccaggioArtefatto);
		BusEventi.iscriviti(ComandoPrelievoArtefatto.class, this::suEventoRichiestaPrelievoArtefatto);
		BusEventi.iscriviti(ComandoAcquistoArtefatto.class, this::suEventoRichiestaAcquistoArtefatto);
		BusEventi.iscriviti(ComandoVenditaArtefatto.class, this::suEventoRichiestaVenditaArtefatto);
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
		gruppoGiocatoreMD.getPersonaggiMD().clear();
		for (PersonaggioMD personaggioMD : personaggiDaAggiungere) {
			Personaggio personaggio = personaggioMD.getClasse().getIstanza(1);
			personaggio.setModelloDati(personaggioMD);
			aggiungiPersonaggioSenzaNotificare(personaggio);
		}
	}

	private GruppoGiocatoreMD md = ModelloDati.getIstanza().getGruppoGiocatoreMD();
	private Locazione locazioneCorrente;

	// Serve per passare chi formula un incantesimo all'automa dalla locazione base.
	// Certo si potrebbe usare un evento sul bus ... che però per ora non è usato.
	private Personaggio formulante;

	@Override
	public final void reimposta() {
		super.reimposta();
		md.reimposta();
		md.setMonete(100);
		md.setPreziosi(5);
		md.setIncantesimi(ClasseIncantesimo.ARIA, 3);
		md.setIncantesimi(ClasseIncantesimo.ACQUA, 3);
		md.setIncantesimi(ClasseIncantesimo.TERRA, 3);
		md.setPozioniSalute(0);
		md.setPozioniSaluteGrande(0);
		md.setPozioniMagia(0);
		md.setPozioniMagiaGrande(0);
		md.setCoordinate(Foresta.getCoordinateLibere());
		Foresta.aggiornaMappaCircostante(this);

		// FIXME questo è lecito solo finché stiamo debuggando...
		md.setMonete(9999);
		for (ClasseIncantesimo classeIncantesimo : ClasseIncantesimo.values()) {
			md.setIncantesimi(classeIncantesimo, 99);
		}
		md.setPozioniSalute(99);
		md.setPozioniSaluteGrande(99);
		md.setPozioniMagia(99);
		md.setPozioniMagiaGrande(99);
		Foresta.ottieniMappa();
	}

	@Override
	public boolean isGruppoGiocatore() {
		return true;
	}

	@Override
	public final void aggiungiPersonaggio(Personaggio personaggio) {
		super.aggiungiPersonaggio(personaggio);
		String nome = personaggio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
		BusEventi.pubblica(new NotificaTestoFrase(nome + " è felice di poter far parte del gruppo."));
		md.addPersonaggioMD(personaggio.getModelloDati());
	}

	public final void aggiungiPersonaggioSenzaNotificare(Personaggio personaggio) {
		super.aggiungiPersonaggio(personaggio);
		md.addPersonaggioMD(personaggio.getModelloDati());
	}

	@Override
	public final void rimuoviPersonaggio(Personaggio p) {
		super.rimuoviPersonaggio(p);
		String nome = p.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
		BusEventi.pubblica(new NotificaTestoFrase(nome + " lascia il gruppo."));
	}

	public final int getMonete() {
		return md.getMonete();
	}

	public final void addMonete(int quantita) {
		int valorePrecedente = md.getMonete();
		int valoreAttuale = valorePrecedente + quantita;
		md.setMonete(valoreAttuale);
		BusEventi.pubblica(new NotificaVariazioneDisponibilitaMonete(valorePrecedente, valoreAttuale));
	}

	public final void subMonete(int quantita) {
		addMonete(-quantita);
	}

	public final int getPreziosi() {
		return md.getPreziosi();
	}

	public final void addPreziosi(int quantita) {
		int valorePrecedente = md.getPreziosi();
		int valoreAttuale = valorePrecedente + quantita;
		md.setPreziosi(valoreAttuale);
		BusEventi.pubblica(new NotificaVariazioneDisponibilitaGemme(valorePrecedente, valoreAttuale));
	}

	public final void subPreziosi(int quantita) {
		addPreziosi(-quantita);
	}

	public final int getIncantesimi(ClasseIncantesimo classeIncantesimo) {
		return md.getIncantesimi(classeIncantesimo);
	}

	public final void addIncantesimi(ClasseIncantesimo classeIncantesimo, int quantita) {
		int valorePrecedente = md.getIncantesimi(classeIncantesimo);
		int valoreAttuale = valorePrecedente + quantita;
		md.setIncantesimi(classeIncantesimo, valoreAttuale);
		BusEventi.pubblica(new NotificaVariazioneDisponibilitaIncantesimi(classeIncantesimo, valorePrecedente, valoreAttuale));
	}

	public final void subIncantesimi(ClasseIncantesimo classeIncantesimo, int quantita) {
		addIncantesimi(classeIncantesimo, -quantita);
	}
	
	public final int getPozioniSalute() {
		return md.getPozioniSalute();
	}

	public final void addPozioniSalute(int quantita) {
		int valorePrecedente = md.getPozioniSalute();
		int valoreAttuale = valorePrecedente + quantita;
		md.setPozioniSalute(valoreAttuale);
		BusEventi.pubblica(new NotificaVariazioneDisponibilitaPozioniSalute(valorePrecedente, valoreAttuale));
	}

	public final void subPozioniSalute(int quantita) {
		addPozioniSalute(-quantita);
	}

	public final void consumaPozioneSalute(Comando azione) {
		consumaPozioneSalute(getPersonaggio(azione));
	}

	public final void consumaPozioneSalute(Personaggio personaggio) {
		subPozioniSalute(1);
		personaggio.addSalute(Costanti.RECUPERO_DA_POZIONE_SALUTE);
		BusEventi.pubblica(new NotificaTestoFrase(personaggio.getNome(
				Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) +
				" ha bevuto una pozione che fa riacquistare salute."));
	}

	public final int getPozioniSaluteGrande() {
		return md.getPozioniSaluteGrande();
	}
	
	public final void addPozioniSaluteGrande(int quantita) {
		int valorePrecedente = md.getPozioniSaluteGrande();
		int valoreAttuale = valorePrecedente + quantita;
		md.setPozioniSaluteGrande(valoreAttuale);
		BusEventi.pubblica(new NotificaVariazioneDisponibilitaPozioniSaluteGrandi(valorePrecedente, valoreAttuale));
	}

	public final void subPozioniSaluteGrande(int quantita) {
		addPozioniSaluteGrande(-quantita);
	}

	public final void consumaPozioneSaluteGrande(Comando azione) {
		consumaPozioneSaluteGrande(getPersonaggio(azione));
	}

	public final void consumaPozioneSaluteGrande(Personaggio personaggio) {
		subPozioniSaluteGrande(1);
		personaggio.addSaluteMassima(Costanti.AUMENTO_SALUTE_DA_POZIONE_SALUTE_GRANDE, "POZIONE_SALUTE_GRANDE");
		personaggio.addSalute(Costanti.RECUPERO_DA_POZIONE_SALUTE_GRANDE);
		BusEventi.pubblica(new NotificaTestoFrase(personaggio.getNome(
				Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) +
				" ha bevuto una pozione che recupera e fa aumentare la salute massima!"));
	}

	public final int getPozioniMagia() {
		return md.getPozioniMagia();
	}

	public final void addPozioniMagia(int quantita) {
		int valorePrecedente = md.getPozioniMagia();
		int valoreAttuale = valorePrecedente + quantita;
		md.setPozioniMagia(valoreAttuale);
		BusEventi.pubblica(new NotificaVariazioneDisponibilitaPozioniMagia(valorePrecedente, valoreAttuale));
	}

	public final void subPozioniMagia(int quantita) {
		addPozioniMagia(-quantita);
	}

	public final void consumaPozioneMagia(Comando azione) {
		consumaPozioneMagia(getPersonaggio(azione));
	}

	public final void consumaPozioneMagia(Personaggio personaggio) {
		subPozioniMagia(1);
		personaggio.addMagia(Costanti.RECUPERO_DA_POZIONE_MAGIA);
		BusEventi.pubblica(new NotificaTestoFrase(personaggio.getNome(
				Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) +
				" ha bevuto una pozione che fa riacquistare magia."));
	}

	public final int getPozioniMagiaGrande() {
		return md.getPozioniMagiaGrande();
	}

	public final void addPozioniMagiaGrande(int quantita) {
		int valorePrecedente = md.getPozioniMagiaGrande();
		int valoreAttuale = valorePrecedente + quantita;
		md.setPozioniMagiaGrande(valoreAttuale);
		BusEventi.pubblica(new NotificaVariazioneDisponibilitaPozioniMagia(valorePrecedente, valoreAttuale));
	}

	public final void subPozioniMagiaGrande(int quantita) {
		addPozioniMagiaGrande(-quantita);
	}

	public final void consumaPozioneMagiaGrande(Comando azione) {
		consumaPozioneMagiaGrande(getPersonaggio(azione));
	}

	public final void consumaPozioneMagiaGrande(Personaggio personaggio) {
		subPozioniMagiaGrande(1);
		personaggio.addMagiaMassima(Costanti.AUMENTO_MAGIA_DA_POZIONE_MAGIA_GRANDE, "POZIONE_MAGIA_GRANDE");
		personaggio.addMagia(Costanti.RECUPERO_DA_POZIONE_MAGIA_GRANDE);
		BusEventi.pubblica(new NotificaTestoFrase(personaggio.getNome(
				Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) +
				" ha bevuto una pozione che recupera e fa aumentare la magia massima!"));
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

	private ClassiLocazione getClasseLocazioneCorrente(int x, int y) {
		return Foresta.getLocazione(x, y);
	}

	/**
	 * Usata da oggetti Cofano per sapere se può essere vuoto o meno
	 */
	public ClassiLocazione getClasseLocazioneCorrente() {
		return Foresta.getLocazione(md.getCoordinate());
	}

	public Locazione getLocazioneCorrente() {
		return locazioneCorrente;
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
		ClassiLocazione locazioneCorrente;
		for (int i = 1; i <= Comando.MAX_MOVIMENTO; i++) {
			if (getY() - i == 0) {
				return i;
			}
			locazioneCorrente = getClasseLocazioneCorrente(getX(), getY() - i);
			if (locazioneCorrente != ClassiLocazione.BOSCO && locazioneCorrente != ClassiLocazione.RADURA) {
				return i;
			}
		}
		return Comando.MAX_MOVIMENTO;
	}

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
		ClassiLocazione locazioneCorrente;
		for (int i = 1; i <= Comando.MAX_MOVIMENTO; i++) {
			if (getX() + i == Foresta.getDimensioneX() - 1) {
				return i;
			}
			locazioneCorrente = getClasseLocazioneCorrente(getX() + i, getY());
			if (locazioneCorrente != ClassiLocazione.BOSCO && locazioneCorrente != ClassiLocazione.RADURA) {
				return i;
			}
		}
		return Comando.MAX_MOVIMENTO;
	}

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
		ClassiLocazione locazioneCorrente;
		for (int i = 1; i <= Comando.MAX_MOVIMENTO; i++) {
			if (getY() + i == Foresta.getDimensioneY() - 1) {
				return i;
			}
			locazioneCorrente = getClasseLocazioneCorrente(getX(), getY() + i);
			if (locazioneCorrente != ClassiLocazione.BOSCO && locazioneCorrente != ClassiLocazione.RADURA) {
				return i;
			}
		}
		return Comando.MAX_MOVIMENTO;
	}

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
		ClassiLocazione locazioneCorrente;
		for (int i = 1; i <= Comando.MAX_MOVIMENTO; i++) {
			if (getX() - i == 0) {
				return i;
			}
			locazioneCorrente = getClasseLocazioneCorrente(getX() - i, getY());
			if (locazioneCorrente != ClassiLocazione.BOSCO && locazioneCorrente != ClassiLocazione.RADURA) {
				return i;
			}
		}
		return Comando.MAX_MOVIMENTO;
	}

	public final void muoveOvest(int passi) {
		int maxPassiOvest = getMaxPassiOvest();
		if (passi > maxPassiOvest) {
			passi = maxPassiOvest;
		}
		setCoordinate(new CoordinateMD(getX() - passi, getY()));
	}

	public final void riposa(TipoRiposo tipoRiposo) {
		getPersonaggiVivi().forEach(p -> p.riposa(1, tipoRiposo));
		BusEventi.pubblica(new InternoPortaInPrimoPiano(InterfacciaUtente.Finestra.STATO));
	}

	public final void pernotta(TipoRiposo tipoRiposo) {
		Logger.log("Inizio pernottamento");
		ClassiLocazione classeLocazione = getClasseLocazioneCorrente();
		int ore = LineaTemporale.oreFinoAlMattino();
		boolean alCoperto = classeLocazione == ClassiLocazione.LOCANDA
				|| classeLocazione.getTipoLocazione() == ClassiLocazione.TipoLocazione.CITTA;
		Logger.log("Pernottamento al coperto? " + alCoperto);
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
		BusEventi.pubblica(new NotificaTestoFrase("Il sole sorge e l'avventura ricomincia."));
	}

	/**
	 * La vendita dei preziosi ha miglior successo se nel gruppo c'è un ladro
	 */
	public final void vendePreziosi() {
		if (md.getPreziosi() > 0) {
			int quantita = md.getPreziosi();
			if (getPersonaggiVivi().stream().anyMatch(p -> p.getClasse() == ClassePersonaggio.LADRA || p.getClasse() == ClassePersonaggio.LADRO)) {
				quantita += Dado.tiraAncheAUnaFaccia(md.getPreziosi());
			}
            String notifica = chiMaiuscolo() +
                    " ha venduto i preziosi raccolti, ricavandone " +
                    (quantita == 1 ? " una moneta." : (quantita + " monete."));
			BusEventi.pubblica(new NotificaTestoFrase(notifica));
			addMonete(quantita);
			subPreziosi(md.getPreziosi());
			BusEventi.pubblica(new InternoPortaInPrimoPiano(InterfacciaUtente.Finestra.STATISTICHE));
		}
	}

	public final void fugge() {
		BusEventi.pubblica(new NotificaTestoFrase(chiMaiuscolo() + ", in preda al panico, cerca la salvezza nella fuga!" +
				" Sfortunatamente riceve gravi ferite e perde molte delle cose in suo possesso!"));

		Function<Integer, Integer> calcolaPerdita = m -> Dado.tiraAncheSenzaRange(0, m / 2);

		subMonete(calcolaPerdita.apply(md.getMonete()));
		subPreziosi(calcolaPerdita.apply(md.getPreziosi()));
		for (ClasseIncantesimo classeIncantesimo : ClasseIncantesimo.values()) {
			int totaleIncantesimi = md.getIncantesimi(classeIncantesimo);
			md.setIncantesimi(classeIncantesimo, calcolaPerdita.apply(totaleIncantesimi));
		}
		subPozioniSalute(calcolaPerdita.apply(md.getPozioniSalute()));
		subPozioniSaluteGrande(calcolaPerdita.apply(md.getPozioniSaluteGrande()));
		subPozioniMagia(calcolaPerdita.apply(md.getPozioniMagia()));

		getPersonaggiVivi().forEach(Personaggio::fugge);

		BusEventi.pubblica(new InternoPortaInPrimoPiano(InterfacciaUtente.Finestra.STATO));
	}

	public boolean isInLocazioneUnica(ClassiLocazione classeLocazioneUnica) {
		return getClasseLocazioneCorrente() == classeLocazioneUnica;
	}

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

	public void addArtefatto(Artefatto artefatto) {
		md.getArtefatti().add(artefatto.getModelloDati());
	}

	public void removeArtefatto(Artefatto artefatto) {
		md.getArtefatti().remove(artefatto.getModelloDati());
	}

	private void suEventoRichiestaStoccaggioArtefatto(ComandoStoccaggioArtefatto eventoRichiestaStoccaggio) {
		Artefatto artefatto = (Artefatto) eventoRichiestaStoccaggio.getOggettoDaSpostare();
		eventoRichiestaStoccaggio.getParteAttiva().removeArtefatto(artefatto);
		addArtefatto(artefatto);
		BusEventi.pubblica(new NotificaApprovazioneStoccaggioArtefatto(eventoRichiestaStoccaggio));
	}

	private void suEventoRichiestaPrelievoArtefatto(ComandoPrelievoArtefatto eventoRichiestaPrelievoArtefatto) {
		Artefatto artefatto = (Artefatto) eventoRichiestaPrelievoArtefatto.getOggettoDaSpostare();
		Personaggio personaggio = (Personaggio) eventoRichiestaPrelievoArtefatto.getParteAttiva();
		if (personaggio.getCarico() + eventoRichiestaPrelievoArtefatto.getOggettoDaSpostare().getPeso() <= personaggio.getCaricoMassimo()) {
			removeArtefatto(artefatto);
			personaggio.addArtefatto(artefatto);
			BusEventi.pubblica(new NotificaApprovazionePrelievoArtefatto(eventoRichiestaPrelievoArtefatto));
		} else {
			BusEventi.pubblica(new NotificaRifiutoPrelievoArtefatto(eventoRichiestaPrelievoArtefatto));
		}
	}

	private void suEventoRichiestaAcquistoArtefatto(ComandoAcquistoArtefatto comandoAcquistoArtefatto) {
		Artefatto artefatto = (Artefatto) comandoAcquistoArtefatto.getOggettoDaSpostare();
		int costoOggetto = comandoAcquistoArtefatto.getOggettoDaSpostare().getCostoAcquisto();
		if (getMonete() >= costoOggetto) {
			addArtefatto(artefatto);
			subMonete(artefatto.getCostoAcquisto());
			comandoAcquistoArtefatto.getParteRemota().removeArtefatto(artefatto);
			BusEventi.pubblica(new NotificaApprovazioneAcquistoArtefatto(comandoAcquistoArtefatto));
		} else {
			BusEventi.pubblica(new NotificaRifiutoAcquistoArtefatto(comandoAcquistoArtefatto));
		}
	}

	private void suEventoRichiestaVenditaArtefatto(ComandoVenditaArtefatto eventoRichiestaVendita) {
		Artefatto artefatto = (Artefatto) eventoRichiestaVendita.getOggettoDaSpostare();
		removeArtefatto(artefatto);
		addMonete(artefatto.getCostoAcquisto());
		eventoRichiestaVendita.getParteRemota().addArtefatto(artefatto);
		BusEventi.pubblica(new NotificaApprovazioneVenditaArtefatto(eventoRichiestaVendita));
	}

	private void suEventoRichiestaAcquistoConsumabile(ComandoAcquistoConsumabile comandoAcquistoConsumabile) {
		int costoOggetto = comandoAcquistoConsumabile.getPrezzo();
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
					comandoAcquistoConsumabile.getPersonaggio()
							.addMagiaMassima(Costanti.AUMENTO_MAGIA_DA_POZIONE_MAGIA_GRANDE, "ALCHIMISTA");
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
			subMonete(comandoAcquistoConsumabile.getPrezzo());
			BusEventi.pubblica(new NotificaApprovazioneAcquistoConsumabile(comandoAcquistoConsumabile));
		} else {
			BusEventi.pubblica(new NotificaRifiutoAcquistoConsumabile(comandoAcquistoConsumabile));
		}
	}

}
