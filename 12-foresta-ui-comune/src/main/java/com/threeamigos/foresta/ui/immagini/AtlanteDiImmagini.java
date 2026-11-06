package com.threeamigos.foresta.ui.immagini;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Un atlante di immagini: tante immagini piccole raggruppate in un solo PNG (<code>img/&lt;cartella&gt;/atlante_&lt;cartella&gt;.png</code>) con il suo
 * descrittore (<code>atlante_&lt;cartella&gt;.txt</code>), che dice dove sta ciascuna. Le immagini originali non stanno nei jar (vedi
 * <code>sorgenti-immagini/</code> nel modulo): chi le chiede per nome, <code>icone/Aiuto.png</code>, le riceve ritagliate dall'atlante.
 * <p>
 * Il descrittore ha due formati, come lo scrive e lo legge <code>risorse_e_documenti_vari/atlanti/costruisci_atlante.py</code>: <b>a griglia</b>
 * (<code>CELLA</code> quadrata o <code>CELLA LxA</code>, <code>COLONNE</code>, una riga di nomi per riga di celle, <code>nome@LxA</code> per le immagini più piccole della cella e
 * <code>-</code> per una cella vuota: la prima riga e la prima colonna del PNG sono rosse, e a destra e in basso di ogni cella c'è la sua
 * linea rossa) e <b>libero</b> (<code>LIBERO</code>, <code>LARGHEZZA</code>, <code>ALTEZZA</code> e una riga <code>nome x y larghezza altezza</code>
 * per immagine). Le righe che cominciano con # sono commenti e <code>FUORI</code> elenca i file che restano a parte.
 */
public final class AtlanteDiImmagini {

	private static final String RADICE = "/com/threeamigos/foresta/img/";
	private static final Map<String, AtlanteDiImmagini> CARICATI = new ConcurrentHashMap<>();
	private static final AtlanteDiImmagini NESSUNO = new AtlanteDiImmagini("", Collections.emptyMap(), null);

	/**
	 * Dove sta un'immagine nell'atlante
	 */
	public static final class Riquadro {
		private final int x;
		private final int y;
		private final int larghezza;
		private final int altezza;

		Riquadro(int x, int y, int larghezza, int altezza) {
			this.x = x;
			this.y = y;
			this.larghezza = larghezza;
			this.altezza = altezza;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public int getLarghezza() {
			return larghezza;
		}

		public int getAltezza() {
			return altezza;
		}
	}

	private final String cartella;
	private final Map<String, Riquadro> riquadri;
	private volatile ImmagineDiPixel pagina;

	private AtlanteDiImmagini(String cartella, Map<String, Riquadro> riquadri, ImmagineDiPixel pagina) {
		this.cartella = cartella;
		this.riquadri = riquadri;
		this.pagina = pagina;
	}

	/**
	 * L'immagine di una risorsa, per esempio <code>icone/Aiuto.png</code>, se la sua cartella ha un atlante e lei ci sta; altrimenti null
	 * (e allora è un file a sé, o non esiste).
	 */
	public static ImmagineDiPixel cerca(String risorsa) {
		int barra = risorsa.indexOf('/');
		if (barra <= 0 || !risorsa.endsWith(".png")) {
			return null;
		}
		AtlanteDiImmagini atlante = atlanteDi(risorsa.substring(0, barra));
		return atlante.immagine(risorsa.substring(barra + 1, risorsa.length() - 4));
	}

	/**
	 * Le dimensioni di una risorsa che sta in un atlante (larghezza e altezza), senza ritagliarla; null se non ci sta
	 */
	public static int[] misura(String risorsa) {
		int barra = risorsa.indexOf('/');
		if (barra <= 0 || !risorsa.endsWith(".png")) {
			return null;
		}
		Riquadro riquadro = atlanteDi(risorsa.substring(0, barra)).riquadri.get(risorsa.substring(barra + 1, risorsa.length() - 4));
		return riquadro == null ? null : new int[]{riquadro.larghezza, riquadro.altezza};
	}

	/**
	 * L'atlante di una cartella di <code>img/</code>; senza descrittore è un atlante vuoto (nessuna immagine ci sta).
	 */
	public static AtlanteDiImmagini di(String cartella) {
		AtlanteDiImmagini atlante = atlanteDi(cartella);
		return atlante == NESSUNO ? new AtlanteDiImmagini(cartella, Collections.emptyMap(), null) : atlante;
	}

	private static AtlanteDiImmagini atlanteDi(String cartella) {
		AtlanteDiImmagini atlante = CARICATI.get(cartella);
		if (atlante == null) {
			atlante = leggi(cartella);
			CARICATI.put(cartella, atlante);
		}
		return atlante;
	}

	/**
	 * I nomi delle immagini che stanno nell'atlante, senza estensione
	 */
	public Set<String> getNomi() {
		return Collections.unmodifiableSet(riquadri.keySet());
	}

	public Riquadro getRiquadro(String nome) {
		return riquadri.get(nome);
	}

	/**
	 * L'immagine con quel nome (senza estensione), o null se non c'è
	 */
	public ImmagineDiPixel immagine(String nome) {
		Riquadro r = riquadri.get(nome);
		if (r == null) {
			return null;
		}
		ImmagineDiPixel tutta = pagina;
		if (tutta == null) {
			tutta = LetturaPng.leggiRisorsa(RADICE + cartella + "/atlante_" + cartella + ".png");
			pagina = tutta;
		}
		return tutta.ritaglia(r.x, r.y, r.larghezza, r.altezza);
	}

	private static AtlanteDiImmagini leggi(String cartella) {
		try (InputStream flusso = AtlanteDiImmagini.class.getResourceAsStream(RADICE + cartella + "/atlante_" + cartella + ".txt")) {
			if (flusso == null) {
				return NESSUNO;
			}
			return new AtlanteDiImmagini(cartella, leggiIlDescrittore(flusso, cartella), null);
		} catch (IOException e) {
			throw new IllegalStateException("Descrittore dell'atlante " + cartella + " non leggibile", e);
		}
	}

	private static Map<String, Riquadro> leggiIlDescrittore(InputStream flusso, String cartella) throws IOException {
		Map<String, Riquadro> riquadri = new LinkedHashMap<>();
		int cellaL = 32;
		int cellaA = 32;
		boolean libero = false;
		int riga = 0;
		Map<String, String> alias = new LinkedHashMap<>();
		try (BufferedReader lettore = new BufferedReader(new InputStreamReader(flusso, StandardCharsets.UTF_8))) {
			String linea;
			while ((linea = lettore.readLine()) != null) {
				linea = linea.trim();
				if (linea.isEmpty() || linea.startsWith("#")) {
					continue;
				}
				String[] parole = linea.split("\\s+");
				switch (parole[0]) {
					case "CELLA":
						String[] misuraCella = parole[1].split("x");
						cellaL = Integer.parseInt(misuraCella[0]);
						cellaA = misuraCella.length > 1 ? Integer.parseInt(misuraCella[1]) : cellaL;
						break;
					case "ALIAS":
						alias.put(parole[1], parole[2]);
						break;
					case "LIBERO":
						libero = true;
						break;
					case "COLONNE":
					case "FUORI":
					case "LARGHEZZA":
					case "ALTEZZA":
						break;
					default:
						if (libero) {
							riquadri.put(parole[0], new Riquadro(Integer.parseInt(parole[1]), Integer.parseInt(parole[2]),
									Integer.parseInt(parole[3]), Integer.parseInt(parole[4])));
						} else {
							leggiUnaRigaDiCelle(riquadri, parole, riga++, cellaL, cellaA, cartella);
						}
						break;
				}
			}
		}
		for (Map.Entry<String, String> a : alias.entrySet()) {
			Riquadro altro = riquadri.get(a.getValue());
			if (altro == null) {
				throw new IllegalStateException("Alias " + a.getKey() + " di " + a.getValue() + " che non c'è, nell'atlante " + cartella);
			}
			riquadri.put(a.getKey(), altro);
		}
		return riquadri;
	}

	private static void leggiUnaRigaDiCelle(Map<String, Riquadro> riquadri, String[] voci, int riga, int cellaL, int cellaA, String cartella) {
		int passoX = cellaL + 1;
		int passoY = cellaA + 1;
		for (int colonna = 0; colonna < voci.length; colonna++) {
			if (voci[colonna].equals("-")) {
				continue;
			}
			String nome = voci[colonna];
			int larghezza = cellaL;
			int altezza = cellaA;
			int chiocciola = nome.indexOf('@');
			if (chiocciola >= 0) {
				String[] misura = nome.substring(chiocciola + 1).split("x");
				larghezza = Integer.parseInt(misura[0]);
				altezza = Integer.parseInt(misura[1]);
				nome = nome.substring(0, chiocciola);
			}
			if (riquadri.put(nome, new Riquadro(1 + colonna * passoX, 1 + riga * passoY, larghezza, altezza)) != null) {
				throw new IllegalStateException("Immagine ripetuta nell'atlante " + cartella + ": " + nome);
			}
		}
	}
}
