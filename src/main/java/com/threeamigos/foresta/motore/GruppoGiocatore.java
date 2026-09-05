package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.GruppoGiocatoreMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.TipoRiposo;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;
import com.threeamigos.foresta.ui.InterfacciaUtente;
import com.threeamigos.foresta.ui.UI;

import java.util.function.Function;

/**
 * Un insieme di personaggi guidati da un giocatore
 */

public class GruppoGiocatore extends Gruppo {

	private GruppoGiocatore() {
		super();
	}
	
	private static GruppoGiocatore istanza;
	
	public static GruppoGiocatore getIstanza() {
		if (istanza == null) {
			istanza = new GruppoGiocatore();
		}
		return istanza;
	}

	private final GruppoGiocatoreMD md = ModelloDati.getIstanza().getGruppoGiocatoreMD();
	private Locazione locazioneCorrente;

	@Override
	public final void reimposta() {
		super.reimposta();
		md.reimposta();
		md.setMonete(100);
		md.setPreziosi(5);
		md.setIncantesimi(ClassiIncantesimo.ARIA, 3);
		md.setIncantesimi(ClassiIncantesimo.ACQUA, 3);
		md.setIncantesimi(ClassiIncantesimo.TERRA, 3);
		md.setPozioniSalute(0);
		md.setPozioniSaluteGrande(0);
		md.setPozioniMagia(0);
		md.setCoordinate(Foresta.getCoordinateLibere());
		Foresta.aggiorna(this);

		// FIXME questo è lecito solo finché stiamo debuggando...
		md.setMonete(9999);
		for (ClassiIncantesimo classeIncantesimo : ClassiIncantesimo.values()) {
			md.setIncantesimi(classeIncantesimo, 99);
		}
		md.setPozioniSalute(99);
		md.setPozioniSaluteGrande(99);
		md.setPozioniMagia(99);
		Foresta.ottieniMappa();
	}

	@Override
	public final void aggiungiPersonaggio(Personaggio personaggio) {
		super.aggiungiPersonaggio(personaggio);
		String nome = personaggio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
		UI.notifica(nome + " è felice di poter far parte del gruppo.");
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
		UI.notifica(nome + " lascia il gruppo.");
	}

	public final int getMonete() {
		return md.getMonete();
	}

	public final void addMonete(int quantita) {
		md.setMonete(md.getMonete() + quantita);
		UI.variaMonete(quantita);
	}

	public final void subMonete(int quantita) {
		md.setMonete(md.getMonete() - quantita);
		UI.variaMonete(-quantita);
	}

	public final int getPreziosi() {
		return md.getPreziosi();
	}

	public final void addPreziosi(int quantita) {
		md.setPreziosi(md.getPreziosi() + quantita);
		UI.variaGemme(quantita);
	}

	public final void subPreziosi(int quantita) {
		md.setPreziosi(md.getPreziosi() - quantita);
		UI.variaGemme(-quantita);
	}

	public final int getIncantesimi(ClassiIncantesimo classeIncantesimo) {
		return md.getIncantesimi(classeIncantesimo);
	}

	public final void addIncantesimi(ClassiIncantesimo classeIncantesimo, int quantita) {
		md.setIncantesimi(classeIncantesimo, md.getIncantesimi(classeIncantesimo) + quantita);
		UI.variaIncantesimi(classeIncantesimo, quantita);
	}

	public final void subIncantesimi(ClassiIncantesimo classeIncantesimo, int quantita) {
		md.setIncantesimi(classeIncantesimo, md.getIncantesimi(classeIncantesimo) - quantita);
		UI.variaIncantesimi(classeIncantesimo, -quantita);
	}
	
	public final int getPozioniSalute() {
		return md.getPozioniSalute();
	}

	public final void addPozioniSalute(int quantita) {
		md.setPozioniSalute(md.getPozioniSalute() + quantita);
		UI.variaPozioniSalute(quantita);
	}

	public final void subPozioniSalute(int quantita) {
		md.setPozioniSalute(md.getPozioniSalute() - quantita);
		UI.variaPozioniSalute(-quantita);
	}

	public final int getPozioniSaluteGrande() {
		return md.getPozioniSaluteGrande();
	}
	
	public final void addPozioniSaluteGrande(int quantita) {
		md.setPozioniSalute(md.getPozioniSaluteGrande() + quantita);
		UI.variaPozioniSaluteGrande(quantita);
	}

	public final void subPozioniSaluteGrande(int quantita) {
		md.setPozioniSaluteGrande(md.getPozioniSaluteGrande() - quantita);
		UI.variaPozioniSaluteGrande(-quantita);
	}
	
	public final int getPozioniMagia() {
		return md.getPozioniMagia();
	}

	public final void addPozioniMagia(int quantita) {
		md.setPozioniMagia(md.getPozioniMagia() + quantita);
		UI.variaPozioniMagia(quantita);
	}

	public final void subPozioniMagia(int quantita) {
		md.setPozioniMagia(md.getPozioniMagia() - quantita);
		UI.variaPozioniMagia(-quantita);
	}

	public final void setCoordinate(CoordinateMD coordinate) {
		md.setCoordinate(coordinate);
		Foresta.aggiorna(this);
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
		UI.primoPiano(InterfacciaUtente.Finestra.STATO);
		UI.rinfresca();
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
			UI.notifica(sb.toString());
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
			UI.notifica(sb.toString());
		}
		getPersonaggiVivi().forEach(p -> p.riposa(ore, tipoRiposo));
		UI.notifica("Il sole sorge e l'avventura ricomincia.");
		UI.primoPiano(InterfacciaUtente.Finestra.STATO);
		UI.rinfresca();
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
			UI.notifica(notifica);
			addMonete(quantita);
			subPreziosi(md.getPreziosi());
		}
	}

	public final void fugge() {
		UI.notifica(chiMaiuscolo() + ", in preda al panico, cerca la salvezza nella fuga! Sfortunatamente riceve gravi ferite e perde molte delle cose in suo possesso!");

		Function<Integer, Integer> calcolaPerdita = m -> Dado.tiraAncheSenzaRange(0, m / 2);

		subMonete(calcolaPerdita.apply(md.getMonete()));
		subPreziosi(calcolaPerdita.apply(md.getPreziosi()));
		for (ClassiIncantesimo classeIncantesimo : ClassiIncantesimo.values()) {
			int totaleIncantesimi = md.getIncantesimi(classeIncantesimo);
			md.setIncantesimi(classeIncantesimo, calcolaPerdita.apply(totaleIncantesimi));
		}
		subPozioniSalute(calcolaPerdita.apply(md.getPozioniSalute()));
		subPozioniSaluteGrande(calcolaPerdita.apply(md.getPozioniSaluteGrande()));
		subPozioniMagia(calcolaPerdita.apply(md.getPozioniMagia()));

		getPersonaggiVivi().forEach(Personaggio::fugge);

		UI.primoPiano(InterfacciaUtente.Finestra.STATO);
		UI.rinfresca();
	}

	public boolean isInLocazioneUnica(ClassiLocazione classeLocazioneUnica) {
		return getClasseLocazioneCorrente() == classeLocazioneUnica;
	}
}
