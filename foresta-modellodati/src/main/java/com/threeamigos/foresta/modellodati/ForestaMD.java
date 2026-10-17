package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.TipoLocazione;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.threeamigos.foresta.modellodati.Serializzabile.stampaRiga;

/**
 * I dati della mappa della Foresta: una casella per ogni posizione, ciascuna con il suo {@link LocazioneMD}.
 * <p>
 * Oltre alle caselle tiene a portata di mano tre informazioni derivate, così da non doverle ricavare ogni volta
 * con un ciclo su tutta la mappa: dove stanno le locazioni uniche (quelle che nella foresta esistono una volta
 * sola), quali sono i limiti dell'area conosciuta dal giocatore (serve alla mappa a tutto schermo) e un numero di
 * versione che cambia a ogni modifica visibile della mappa (serve a chi la disegna per sapere se la sua copia
 * in memoria è da rifare).
 * <p>
 * Le caselle "visitata" e "conosciuta" sono proprietà del modello dati della casella, non campi di questa classe.
 * Il salvataggio scrive le dimensioni e poi una riga per casella (vedi {@link #salva(PrintWriter)}).
 *
 * @author Stefano Reksten
 */
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
	private Map<TipoLocazione, CoordinateMD> locazioniUniche;

	// Cambia ogni volta che cambia visivamente la mappa (conosciuta/visitata),
	// così chi disegna la mappa generale (la UI) sa quando la sua cache è da ricostruire.
	// Il contatore è unico per tutta l'esecuzione: se ogni partita ripartisse da 0,
	// una partita nuova o caricata potrebbe avere la stessa versione di quella
	// disegnata per ultima (morendo nella prima locazione, per esempio) e la mappa
	// a video resterebbe quella vecchia.
	// Non è un dato della partita (non si salva): sta qui, e non in chi disegna, perché
	// solo questa classe sa quando la mappa cambia (lo incrementa nei suoi tre metodi che la modificano).
	private static final AtomicInteger ULTIMA_VERSIONE_MAPPA = new AtomicInteger();
	private int versioneMappa;

	public int getVersioneMappa() {
		return versioneMappa;
	}

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
	 * Mette nella casella una locazione nuova. Il modello dati precedente se ne va
	 * insieme alla locazione che rappresentava, e con lui il nome e il fatto di
	 * essere stata portata a termine. Restano invece "visitata" e "conosciuta",
	 * che sono della casella e non di quel che ci si trova sopra.
	 */
	public final void impostaLocazione(CoordinateMD coordinate, TipoLocazione tipoLocazione) {
		int offset = offset(coordinate.getX(), coordinate.getY());
		LocazioneMD precedente = arrayLocazioni[offset];
		LocazioneMD locazioneMD = new LocazioneMD(tipoLocazione);
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

	/**
	 * Le coordinate della locazione unica di quel tipo.
	 *
	 * @return le coordinate, o null se nella foresta non c'è (ancora) una locazione di quel tipo
	 */
	public final CoordinateMD ottieniCoordinateLocazioneUnica(TipoLocazione tipoLocazione) {
		return locazioniUniche.get(tipoLocazione);
	}

	/**
	 * Ricorda dove sta la locazione unica di quel tipo.
	 */
	public final void aggiungiLocazioneUnica(TipoLocazione tipoLocazione, CoordinateMD coordinate) {
		locazioniUniche.put(tipoLocazione, coordinate);
	}
	
	/**
	 * Dimentica la locazione unica di quel tipo, per esempio perché è stata distrutta.
	 */
	public final void rimuoviLocazioneUnica(TipoLocazione tipoLocazione) {
		locazioniUniche.remove(tipoLocazione);
	}

	/**
	 * Il modello dati della casella in quella posizione.
	 *
	 * @return il modello dati, o null se la casella non è ancora stata assegnata
	 *         (succede solo mentre la foresta si costruisce)
	 */
	public LocazioneMD ottieniLocazioneMD(CoordinateMD coordinate) {
		return arrayLocazioni[offset(coordinate.getX(), coordinate.getY())];
	}

	/**
	 * Il tipo di locazione della casella, o null se non è ancora stata assegnata.
	 */
	public TipoLocazione ottieniTipoLocazione(CoordinateMD coordinate) {
		return ottieniTipoLocazione(coordinate.getX(), coordinate.getY());
	}

	/**
	 * Come {@link #ottieniTipoLocazione(CoordinateMD)}, con colonna e riga separate.
	 */
	public TipoLocazione ottieniTipoLocazione(int x, int y) {
		LocazioneMD locazioneMD = arrayLocazioni[offset(x, y)];
		// Durante la costruzione della Foresta le caselle non ancora assegnate sono vuote
		return locazioneMD == null ? null : locazioneMD.getTipo();
	}

	/**
	 * Prepara una mappa vuota di quella misura: nessuna casella assegnata, nessuna locazione unica, nessuna casella
	 * conosciuta. Anche la versione della mappa cambia, perché chi la disegna deve ricostruirla.
	 *
	 * @param dimensioneX il numero di colonne
	 * @param dimensioneY il numero di righe
	 */
	public void reimposta(int dimensioneX, int dimensioneY) {
		this.dimensioneX = dimensioneX;
		this.dimensioneY = dimensioneY;
		arrayLocazioni = new LocazioneMD[dimensioneX * dimensioneY];
		locazioniUniche = new EnumMap<>(TipoLocazione.class);
		minXConosciuta = -1;
		maxXConosciuta = -1;
		minYConosciuta = -1;
		maxYConosciuta = -1;
		versioneMappa = ULTIMA_VERSIONE_MAPPA.incrementAndGet();
	}

	/**
	 * Segna la casella come visitata dal gruppo.
	 */
	public final void impostaLocazioneVisitata(CoordinateMD coordinate) {
		impostaLocazioneVisitata(coordinate, true);
	}

	/**
	 * Segna la casella come visitata o, con <code>false</code>, toglie il segno. Cambia la versione della mappa.
	 */
	public final void impostaLocazioneVisitata(CoordinateMD coordinate, boolean visitata) {
		LocazioneMD locazioneMD = ottieniLocazioneMD(coordinate);
		if (visitata) {
			locazioneMD.aggiungiProprieta(LocazioneMD.VISITATA, LocazioneMD.AFFERMATIVO);
		} else {
			locazioneMD.rimuoviProprieta(LocazioneMD.VISITATA);
		}
		versioneMappa = ULTIMA_VERSIONE_MAPPA.incrementAndGet();
	}

	public final boolean isLocazioneVisitata(CoordinateMD coordinate) {
		return ottieniLocazioneMD(coordinate).ottieniProprieta(LocazioneMD.VISITATA) != null;
	}

	/**
	 * Segna la casella come conosciuta: il giocatore sa che esiste e la mappa la mostra. Aggiorna anche i limiti
	 * dell'area conosciuta e la versione della mappa.
	 */
	public final void impostaLocazioneConosciuta(CoordinateMD coordinate) {
		impostaLocazioneConosciuta(coordinate.getX(), coordinate.getY());
	}

	public final boolean isLocazioneConosciuta(CoordinateMD coordinate) {
		return ottieniLocazioneMD(coordinate).ottieniProprieta(LocazioneMD.CONOSCIUTA) != null;
	}

	/**
	 * Rende conosciute tutte le caselle: il gruppo ha ottenuto la mappa completa della Foresta.
	 */
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
		versioneMappa = ULTIMA_VERSIONE_MAPPA.incrementAndGet();
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

	/**
	 * Scrive su una riga le dimensioni della mappa e poi una riga per ogni casella, in ordine per riga. Le dimensioni
	 * fanno da conteggio, quindi non serve altro.
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		stampaRiga(stream,
				dimensioneX,
				dimensioneY);
		for (LocazioneMD locazioneMD : arrayLocazioni) {
			locazioneMD.salva(stream);
		}
	}

	/**
	 * Rilegge la mappa scritta da {@link #salva(PrintWriter)}. Mentre legge ricostruisce anche
	 * le informazioni derivate: dove stanno le locazioni uniche e i limiti dell'area conosciuta.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException {
		String line = stream.readLine();
		LettoreCampi st = new LettoreCampi(line);
		dimensioneX = st.intero();
		dimensioneY = st.intero();
		reimposta(dimensioneX, dimensioneY);
		for (int indice = 0; indice < arrayLocazioni.length; indice++) {
			LocazioneMD locazioneMD = new LocazioneMD();
			locazioneMD.leggi(stream);
			arrayLocazioni[indice] = locazioneMD;
			int x = indice % dimensioneX;
			int y = indice / dimensioneX;
			if (locazioneMD.getTipo().isLocazioneUnica()) {
				locazioniUniche.put(locazioneMD.getTipo(), new CoordinateMD(x, y));
			}
			if (locazioneMD.ottieniProprieta(LocazioneMD.CONOSCIUTA) != null) {
				aggiornaEstremiConosciuti(x, y);
			}
		}
	}
}
