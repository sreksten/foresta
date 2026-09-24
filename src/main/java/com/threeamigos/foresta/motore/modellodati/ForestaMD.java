package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.locazioni.ClassiLocazione;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumMap;
import java.util.Map;

public class ForestaMD implements Serializzabile {

	private int dimensioneX;
	private int dimensioneY;

	/**
	 * Una casella per ogni posizione della mappa, in ordine row-major:
	 * l'indice è x + y * dimensioneX. Ognuna ha il suo modello dati, quindi
	 * due locande sono due locande e non la stessa.
	 */
	private LocazioneMD[] arrayLocazioni;

	// Stato derivato dalle caselle conosciute: serve al viewport della mappa
	// a tutto schermo, e tenerlo aggiornato nel setter costa meno che ricavarlo.
	private int minXConosciuta;
	private int maxXConosciuta;
	private int minYConosciuta;
	private int maxYConosciuta;

	// Serve tenerne traccia per le informazioni che i PNG danno al gruppo.
	// Si potrebbe fare anche un ciclo su tutta la foresta ma così si fa prima.
	private Map<ClassiLocazione, CoordinateMD> locazioniUniche;

	public int getDimensioneX() {
		return dimensioneX;
	}

	public int getDimensioneY() {
		return dimensioneY;
	}

	public int getMinXConosciuta() {
		return minXConosciuta;
	}

	public int getMaxXConosciuta() {
		return maxXConosciuta;
	}

	public int getMinYConosciuta() {
		return minYConosciuta;
	}

	public int getMaxYConosciuta() {
		return maxYConosciuta;
	}

	////////////

	/**
	 * Posa sulla casella una locazione nuova: il modello dati precedente se ne va
	 * insieme alla locazione che rappresentava, e con lui il nome e il fatto di
	 * essere stata portata a termine. Restano invece "visitata" e "conosciuta",
	 * che sono della casella e non di quel che ci si trova sopra.
	 */
	public final void impostaLocazione(CoordinateMD coordinate, ClassiLocazione classeLocazione) {
		int offset = offset(coordinate.getX(), coordinate.getY());
		LocazioneMD precedente = arrayLocazioni[offset];
		LocazioneMD locazioneMD = new LocazioneMD(classeLocazione);
		if (precedente != null) {
			trasferisciProprieta(precedente, locazioneMD, LocazioneMD.VISITATA);
			trasferisciProprieta(precedente, locazioneMD, LocazioneMD.CONOSCIUTA);
		}
		arrayLocazioni[offset] = locazioneMD;
	}

	private void trasferisciProprieta(LocazioneMD da, LocazioneMD a, String nome) {
		String valore = da.ottieniProprieta(nome);
		if (valore != null) {
			a.aggiungiProprieta(nome, valore);
		}
	}

	public final CoordinateMD ottieniCoordinateLocazioneUnica(ClassiLocazione classeLocazione) {
		return locazioniUniche.get(classeLocazione);
	}

	public final void aggiungiLocazioneUnica(ClassiLocazione classeLocazione, CoordinateMD coordinate) {
		locazioniUniche.put(classeLocazione, coordinate);
	}
	
	public final void rimuoviLocazioneUnica(ClassiLocazione classeLocazione) {
		locazioniUniche.remove(classeLocazione);
	}

	public LocazioneMD ottieniLocazioneMD(CoordinateMD coordinate) {
		return arrayLocazioni[offset(coordinate.getX(), coordinate.getY())];
	}

	public ClassiLocazione ottieniClasseLocazione(CoordinateMD coordinate) {
		return ottieniClasseLocazione(coordinate.getX(), coordinate.getY());
	}

	public ClassiLocazione ottieniClasseLocazione(int x, int y) {
		LocazioneMD locazioneMD = arrayLocazioni[offset(x, y)];
		// Durante la costruzione della Foresta le caselle non ancora assegnate sono vuote
		return locazioneMD == null ? null : locazioneMD.getClasse();
	}

	public void reimposta(int dimensioneX, int dimensioneY) {
		this.dimensioneX = dimensioneX;
		this.dimensioneY = dimensioneY;
		arrayLocazioni = new LocazioneMD[dimensioneX * dimensioneY];
		locazioniUniche = new EnumMap<>(ClassiLocazione.class);
		minXConosciuta = -1;
		maxXConosciuta = -1;
		minYConosciuta = -1;
		maxYConosciuta = -1;
	}

	public final void impostaLocazioneVisitata(CoordinateMD coordinate) {
		impostaLocazioneVisitata(coordinate, true);
	}

	public final void impostaLocazioneVisitata(CoordinateMD coordinate, boolean visitata) {
		LocazioneMD locazioneMD = ottieniLocazioneMD(coordinate);
		if (visitata) {
			locazioneMD.aggiungiProprieta(LocazioneMD.VISITATA, LocazioneMD.AFFERMATIVO);
		} else {
			locazioneMD.rimuoviProprieta(LocazioneMD.VISITATA);
		}
	}

	public final boolean isLocazioneVisitata(CoordinateMD coordinate) {
		return ottieniLocazioneMD(coordinate).ottieniProprieta(LocazioneMD.VISITATA) != null;
	}

	public final void impostaLocazioneConosciuta(CoordinateMD coordinate) {
		impostaLocazioneConosciuta(coordinate.getX(), coordinate.getY());
	}

	public final boolean isLocazioneConosciuta(CoordinateMD coordinate) {
		return ottieniLocazioneMD(coordinate).ottieniProprieta(LocazioneMD.CONOSCIUTA) != null;
	}

	public final void ottieniMappa() {
		for (int y = 0; y < dimensioneY; y++) {
			for (int x = 0; x < dimensioneX; x++) {
				impostaLocazioneConosciuta(x, y);
			}
		}
	}

	// implementazioni private che dipendono dal modello dati

	private int offset(int x, int y) {
		return x + y * dimensioneX;
	}

	private void impostaLocazioneConosciuta(int x, int y) {
		arrayLocazioni[offset(x, y)].aggiungiProprieta(LocazioneMD.CONOSCIUTA, LocazioneMD.AFFERMATIVO);
		aggiornaEstremiConosciuti(x, y);
	}

	private void aggiornaEstremiConosciuti(int x, int y) {
		if (minXConosciuta == -1 || minXConosciuta > x) {
			minXConosciuta = x;
		}
		if (maxXConosciuta == -1 || maxXConosciuta < x) {
			maxXConosciuta = x;
		}
		if (minYConosciuta == -1 || minYConosciuta > y) {
			minYConosciuta = y;
		}
		if (maxYConosciuta == -1 || maxYConosciuta < y) {
			maxYConosciuta = y;
		}
	}

	// Routine per il salvataggio

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.print(dimensioneX);
		stream.print(PIPE);
		stream.println(dimensioneY);
		// Le dimensioni fanno già da conteggio: seguono dimensioneX * dimensioneY
		// righe in ordine row-major, una per casella
		for (LocazioneMD locazioneMD : arrayLocazioni) {
			locazioneMD.salva(stream);
		}
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		String line = stream.readLine();
		LettoreCampi st = new LettoreCampi(line);
		dimensioneX = Integer.parseInt(st.testo());
		dimensioneY = Integer.parseInt(st.testo());
		reimposta(dimensioneX, dimensioneY);
		for (int indice = 0; indice < arrayLocazioni.length; indice++) {
			LocazioneMD locazioneMD = new LocazioneMD();
			locazioneMD.leggi(stream);
			arrayLocazioni[indice] = locazioneMD;
			int x = indice % dimensioneX;
			int y = indice / dimensioneX;
			if (locazioneMD.getClasse().isLocazioneUnica()) {
				locazioniUniche.put(locazioneMD.getClasse(), new CoordinateMD(x, y));
			}
			if (locazioneMD.ottieniProprieta(LocazioneMD.CONOSCIUTA) != null) {
				aggiornaEstremiConosciuti(x, y);
			}
		}
	}
}
