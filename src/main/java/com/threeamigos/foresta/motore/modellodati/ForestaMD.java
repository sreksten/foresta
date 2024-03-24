package com.threeamigos.foresta.motore.modellodati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.Foresta;

public class ForestaMD implements Serializzabile {

	public static final int MAX_DIMENSIONE = 80;

	private int dimensioneX;
	private int dimensioneY;
	private ClassiLocazione[] arrayLocazioni;

	// Dati riguardanti la foresta in cui il gruppo si trova
	private int[] locazioniVisitate;
	private int[] locazioniConosciute;
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
	
	public final void impostaLocazione(CoordinateMD coordinate, ClassiLocazione classeLocazione) {
		arrayLocazioni[coordinate.getX() + coordinate.getY() * dimensioneX] = classeLocazione;
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
	
	public ClassiLocazione ottieniCasseLocazione(CoordinateMD coordinate) {
		return arrayLocazioni[coordinate.getX() + coordinate.getY() * dimensioneX];
	}

	public ClassiLocazione ottieniClasseLocazione(int x, int y) {
		return arrayLocazioni[x + y * dimensioneX];
	}

	public void reimposta(int dimensioneX, int dimensioneY) {
		this.dimensioneX = dimensioneX;
		this.dimensioneY = dimensioneY;
		arrayLocazioni = new ClassiLocazione[dimensioneX * dimensioneY];
		locazioniUniche = new EnumMap<>(ClassiLocazione.class);
		// Per memorizzare le informazioni sulle locazioni visitate e conosciute
		// abbiamo bisogno di un certo numero di bits equivalenti a
		// DIMENSIONE * DIMENSIONE e quindi in int (32 bit) fa
		int d = (dimensioneX * dimensioneY + 31) >> 5;
		locazioniVisitate = new int[d];
		locazioniConosciute = new int[d];
		minXConosciuta = -1;
		maxXConosciuta = -1;
		minYConosciuta = -1;
		maxYConosciuta = -1;
	}

	public void impostaClasseLocazione(CoordinateMD coordinate, ClassiLocazione classeLocazione) {
		arrayLocazioni[coordinate.getX() + coordinate.getY() * dimensioneX] = classeLocazione;
	}

	public final void impostaLocazioneVisitata(CoordinateMD coordinate) {
		int offset = coordinate.getX() + coordinate.getY() * dimensioneX;
		// offset / 32, offset % 31
		locazioniVisitate[offset >> 5] |= (1 << (offset & 0x1F));
	}

	public final void impostaLocazioneVisitata(CoordinateMD coordinate, boolean visitata) {
		impostaLocazioneVisitata(coordinate.getX(), coordinate.getY(), visitata);
	}

	public final boolean isLocazioneVisitata(CoordinateMD coordinate) {
		return isLocazioneVisitata(coordinate.getX(), coordinate.getY());
	}

	public final void impostaLocazioneConosciuta(CoordinateMD coordinate) {
		impostaLocazioneConosciuta(coordinate.getX(), coordinate.getY());
	}

	public final boolean isLocazioneConosciuta(CoordinateMD coordinate) {
		return isLocazioneConosciuta(coordinate.getX(), coordinate.getY());
	}

	public final void ottieniMappa() {
		for (int i = 0; i < locazioniConosciute.length; i++) {
			locazioniConosciute[i] = 0xFFFFFFFF;
		}
		minXConosciuta = 0;
		maxXConosciuta = Foresta.getDimensioneX() - 1;
		minYConosciuta = 0;
		maxYConosciuta = Foresta.getDimensioneY() - 1;
	}

	// implementazioni private che dipendono dal modello dati

	private final void impostaLocazioneVisitata(int x, int y, boolean visitata) {
		int offset = x + y * dimensioneX;
		if (visitata) {
			locazioniVisitate[offset >> 5] |= (1 << (offset & 0x1F));
		} else {
			locazioniVisitate[offset >> 5] &= ~(1 << (offset & 0x1F));
		}
	}

	private final boolean isLocazioneVisitata(int x, int y) {
		int offset = x + y * dimensioneX;
		return (locazioniVisitate[offset >> 5] & (1 << (offset & 0x1F))) != 0;
	}

	private final boolean isLocazioneConosciuta(int x, int y) {
		int offset = x + y * dimensioneX;
		return (locazioniConosciute[offset >> 5] & (1 << (offset & 0x1F))) != 0;
	}

	private final void impostaLocazioneConosciuta(int x, int y) {
		int offset = x + y * dimensioneX;
		locazioniConosciute[offset >> 5] |= (1 << (offset & 0x1F));
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
		stream.println(getLocazioni());
		stream.println(getLocazioniConosciute());
		stream.println(getLocazioniVisitate());
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException {
		String line = stream.readLine();
		StringTokenizer st = new StringTokenizer(line, PIPE);
		dimensioneX = Integer.parseInt(st.nextToken());
		dimensioneY = Integer.parseInt(st.nextToken());
		reimposta(dimensioneX, dimensioneY);
		line = stream.readLine();
		setLocazioni(line);
		line = stream.readLine();
		setLocazioniConosciute(line);
		line = stream.readLine();
		setLocazioniVisitate(line);
	}

	private String getLocazioni() {
		Map<ClassiLocazione, Character> mappa = new EnumMap<>(ClassiLocazione.class);
		for (ClassiLocazione classeCorrente : ClassiLocazione.values()) {
			mappa.put(classeCorrente, Character.valueOf((char)(classeCorrente.ordinal() + 'A')));
		}
		StringBuilder sb = new StringBuilder();
		for (int indice = 0; indice < arrayLocazioni.length; indice++) {
			sb.append(mappa.get(arrayLocazioni[indice]));
		}
		return sb.toString();
	}

	private void setLocazioni(String locazioni) {
		Map<Character, ClassiLocazione> mappa = new HashMap<>();
		for (ClassiLocazione classeCorrente : ClassiLocazione.values()) {
			mappa.put(Character.valueOf((char)(classeCorrente.ordinal() + 'A')), classeCorrente);
		}
		arrayLocazioni = new ClassiLocazione[dimensioneX * dimensioneY];
		for (int indice = 0; indice < arrayLocazioni.length; indice++) {
			ClassiLocazione classeLocazione = mappa.get(Character.valueOf(locazioni.charAt(indice))); 
			arrayLocazioni[indice] = classeLocazione;
			if (classeLocazione.isLocazioneUnica()) {
				locazioniUniche.put(classeLocazione, new CoordinateMD(indice % dimensioneX, indice / dimensioneX));
			}
		}
	}

	//TODO sempre che funzioni il metodo, impacchettare a nibble + 'A'
	private String getLocazioniVisitate() {
		StringBuilder sb = new StringBuilder();
		for (int x = 0; x < dimensioneX; x++) {
			for (int y = 0; y < dimensioneY; y++) {
				if (isLocazioneVisitata(x, y)) {
					sb.append("1");
				} else {
					sb.append("0");
				}
			}
		}
		return sb.toString();
	}

	//TODO sempre che funzioni il metodo, impacchettare a nibble + 'A'
	private void setLocazioniVisitate(String locazioniVisitate) {
		int stringOffset = 0;
		for (int x = 0; x < dimensioneX; x++) {
			for (int y = 0; y < dimensioneY; y++) {
				impostaLocazioneVisitata(x, y, locazioniVisitate.charAt(stringOffset) == '0');
			}
		}
	}

	//TODO sempre che funzioni il metodo, impacchettare a nibble + 'A'
	private String getLocazioniConosciute() {
		StringBuilder sb = new StringBuilder();
		for (int x = 0; x < dimensioneX; x++) {
			for (int y = 0; y < dimensioneY; y++) {
				if (isLocazioneConosciuta(x, y)) {
					sb.append("1");
				} else {
					sb.append("0");
				}
			}
		}
		return sb.toString();
	}

	//TODO sempre che funzioni il metodo, impacchettare a nibble + 'A'
	private void setLocazioniConosciute(String locazioniConosciute) {
		int stringOffset = 0;
		for (int x = 0; x < dimensioneX; x++) {
			for (int y = 0; y < dimensioneY; y++) {
				if (locazioniConosciute.charAt(stringOffset) == '1') {
					impostaLocazioneConosciuta(x, y);
				}
			}
		}
	}
}
