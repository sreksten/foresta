package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.AbilitaInnataDiClasse;
import com.threeamigos.foresta.tipi.CaratteristicaDiClasse;
import com.threeamigos.foresta.tipi.MoltiplicatoreDiClasse;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.Verso;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna.Elemento;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.immagini.VersiDeiPersonaggi;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.FontTool;
import com.threeamigos.foresta.ui.testo.LayoutDelTestoGrande;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Le due colonne del bestiario, come descrizioni senza immagini (vedi {@link SchermataBestiario}): l'elenco delle voci, a sinistra, e la scheda della voce
 * scelta, a destra.
 * <p>
 * Una voce chiusa (mai sbloccata) mostra solo "???" e la silhouette. Una aperta mostra il nome, le immagini (la femmina a sinistra e il maschio a destra,
 * che si guardano), la descrizione, le abilità innate, quel che si è scoperto delle forze e delle debolezze e il carattere della classe: i moltiplicatori
 * (quelli a zero, come la contrattazione di chi non sa contrattare, si omettono) con la nota che dice perché hanno quel valore. Se il maschile e il
 * femminile hanno valori o note diversi si mostrano tutti e due.
 */
public final class ContenutiDelBestiario {

	public static final int ALTEZZA_RIGA_ELENCO = 40;
	private static final int MARGINE_RIGA = 6;
	private static final int LATO_ICONA = 32;
	private static final int SPAZIO_FRA_SEZIONI = 14;
	private static final int ALTEZZA_MASSIMA_IMMAGINI = 200;
	private static final int SPAZIO_FRA_IMMAGINI = 24;
	/**
	 * Il colore delle silhouette (RGB) e quello dell'evidenziazione della voce scelta (ARGB)
	 */
	public static final int COLORE_SILHOUETTE = 0x464646;
	private static final int COLORE_SELEZIONE = (45 << 24) | 0xFFFFFF;
	private static final String CHIUSA = "???";

	private static final DoomdarkFont MEDIO = DoomdarkFont.medio();

	// Lo svolazzo che separa i mostri dai personaggi giocanti nell'elenco. È fra i file non a blocchi 2x2 (fondinon2x2): si legge senza zoom
	private static final String RISORSA_SEPARATORE = "fondinon2x2/Separatore.png";

	private ContenutiDelBestiario() {
	}

	/**
	 * L'indice della prima voce dopo i mostri (l'ultimo è la viverna), davanti alla quale sta il separatore dell'elenco; -1 se non c'è niente da
	 * separare.
	 */
	public static int indiceDelSeparatore(List<TipoPersonaggio> voci) {
		int dopoIMostri = 0;
		for (int i = 0; i < voci.size(); i++) {
			if (voci.get(i).ordinal() <= TipoPersonaggio.VIVERNA.ordinal()) {
				dopoIMostri = i + 1;
			}
		}
		return dopoIMostri > 0 && dopoIMostri < voci.size() ? dopoIMostri : -1;
	}

	/**
	 * La riga dell'elenco in cui sta la voce: dopo il separatore, che ha una riga sua, le voci scendono di una
	 */
	public static int rigaDellaVoce(List<TipoPersonaggio> voci, int voce) {
		int separatore = indiceDelSeparatore(voci);
		return separatore >= 0 && voce >= separatore ? voce + 1 : voce;
	}

	/**
	 * La voce che sta in quella riga dell'elenco, o -1 per la riga del separatore o fuori dall'elenco
	 */
	public static int voceDellaRiga(List<TipoPersonaggio> voci, int riga) {
		int separatore = indiceDelSeparatore(voci);
		if (riga < 0 || riga == separatore) {
			return -1;
		}
		int voce = separatore >= 0 && riga > separatore ? riga - 1 : riga;
		return voce < voci.size() ? voce : -1;
	}

	private static int numeroDiRighe(List<TipoPersonaggio> voci) {
		return voci.size() + (indiceDelSeparatore(voci) >= 0 ? 1 : 0);
	}

	/**
	 * La misura di un'immagine ridotta (mai ingrandita) per stare in quella larghezza e altezza, tenendo le proporzioni
	 */
	private static Misura adatta(Misura immagine, int larghezzaMassima, int altezzaMassima) {
		if (immagine.larghezza <= larghezzaMassima && immagine.altezza <= altezzaMassima) {
			return immagine;
		}
		double fattore = Math.min((double) larghezzaMassima / immagine.larghezza, (double) altezzaMassima / immagine.altezza);
		return new Misura(Math.max(1, (int) Math.round(immagine.larghezza * fattore)), Math.max(1, (int) Math.round(immagine.altezza * fattore)));
	}

	private static List<String> righe(DoomdarkFont font, String testo, int larghezzaMassima) {
		return FontTool.split(font, testo, larghezzaMassima);
	}

	/**
	 * L'elenco delle voci, una per riga alta {@link #ALTEZZA_RIGA_ELENCO}, con quella scelta in evidenza
	 */
	public static ContenutoDiColonna elenco(VistaPartita vista, List<TipoPersonaggio> voci, int scelta, int larghezza, MisureDelleImmagini misure) {
		List<Elemento> elementi = new ArrayList<>();
		for (int i = 0; i < voci.size(); i++) {
			TipoPersonaggio voce = voci.get(i);
			boolean aperta = vista.isVoceDelBestiarioSbloccata(voce);
			int y = rigaDellaVoce(voci, i) * ALTEZZA_RIGA_ELENCO;
			if (i == scelta) {
				elementi.add(Elemento.rettangolo(larghezza, ALTEZZA_RIGA_ELENCO, COLORE_SELEZIONE, 0, y));
			}
			// L'icona: l'immagine della classe ridotta, per tutte allo stesso modo (le icone disegnate a mano che alcune classi hanno non si usano qui)
			Misura icona = adatta(misure.personaggio(voce), LATO_ICONA, LATO_ICONA);
			elementi.add(Elemento.immagine(NomiDelleImmagini.personaggio(voce), icona.larghezza, icona.altezza, false, aperta ? null : COLORE_SILHOUETTE,
					MARGINE_RIGA, y + (ALTEZZA_RIGA_ELENCO - icona.altezza) / 2));
			int xTesto = MARGINE_RIGA + icona.larghezza + MARGINE_RIGA;
			DoomdarkColorModel.Color colore = aperta
					? (i == scelta ? DoomdarkColorModel.Color.WHITE : DoomdarkColorModel.Color.LIGHT_GRAY)
					: (i == scelta ? DoomdarkColorModel.Color.MEDIUM_GRAY : DoomdarkColorModel.Color.DARK_GRAY);
			List<String> testo = righe(MEDIO, aperta ? voce.getNomeDellaVoce() : CHIUSA, Math.max(MEDIO.getHeight(), larghezza - xTesto - MARGINE_RIGA));
			elementi.add(Elemento.testo(testo, MEDIO, colore, xTesto, y + (ALTEZZA_RIGA_ELENCO - testo.size() * MEDIO.getHeight()) / 2));
		}
		int separatore = indiceDelSeparatore(voci);
		if (separatore >= 0) {
			// Lo svolazzo fra i mostri e i personaggi giocanti, ridotto se non ci sta nella larghezza
			Misura svolazzo = adatta(misure.immagine(RISORSA_SEPARATORE), Math.max(1, larghezza - 2 * MARGINE_RIGA), ALTEZZA_RIGA_ELENCO);
			elementi.add(Elemento.immagine(RISORSA_SEPARATORE, svolazzo.larghezza, svolazzo.altezza, false, null, (larghezza - svolazzo.larghezza) / 2,
					separatore * ALTEZZA_RIGA_ELENCO + (ALTEZZA_RIGA_ELENCO - svolazzo.altezza) / 2));
		}
		return new ContenutoDiColonna(larghezza, numeroDiRighe(voci) * ALTEZZA_RIGA_ELENCO, elementi);
	}

	/**
	 * La scheda della voce, larga quanto chiesto
	 */
	public static ContenutoDiColonna scheda(VistaPartita vista, TipoPersonaggio voce, int larghezza, MisureDelleImmagini misure) {
		boolean aperta = vista.isVoceDelBestiarioSbloccata(voce);
		Colonna colonna = new Colonna(larghezza);
		com.threeamigos.foresta.ui.immagini.ImmagineDiPixel nome = LayoutDelTestoGrande.immagine(aperta ? voce.getNomeDellaVoce() : CHIUSA, larghezza, true);
		colonna.grande(aperta ? nome : LayoutDelTestoGrande.scura(nome), "bestiario|" + voce + "|" + larghezza + "|" + aperta,
				(larghezza - nome.getLarghezza()) / 2, SPAZIO_FRA_SEZIONI);
		aggiungiLeImmagini(voce, aperta, larghezza, colonna, misure);
		if (aperta) {
			colonna.testo(righe(MEDIO, voce.getDescrizione(), larghezza), MEDIO, DoomdarkColorModel.Color.LIGHT_GRAY, 0, SPAZIO_FRA_SEZIONI);
			aggiungiLeAbilitaInnate(vista, voce, larghezza, colonna);
			aggiungiLeCaratteristiche(vista, voce, larghezza, colonna);
			aggiungiIlCarattere(vista, voce, larghezza, colonna);
		}
		return colonna.contenuto();
	}

	/**
	 * Il contenuto che si compone dall'alto in basso: ogni pezzo lascia un po' di spazio sopra di sé
	 */
	private static final class Colonna {
		private final int larghezza;
		private final List<Elemento> elementi = new ArrayList<>();
		private int y;

		Colonna(int larghezza) {
			this.larghezza = larghezza;
		}

		void testo(List<String> righe, DoomdarkFont font, DoomdarkColorModel.Color colore, int x, int spazioSopra) {
			y += spazioSopra;
			elementi.add(Elemento.testo(righe, font, colore, x, y));
			y += righe.size() * font.getHeight();
		}

		void grande(com.threeamigos.foresta.ui.immagini.ImmagineDiPixel pixel, String chiave, int x, int spazioSopra) {
			y += spazioSopra;
			elementi.add(Elemento.testoGrande(pixel, chiave, x, y));
			y += pixel.getAltezza();
		}

		/**
		 * Una riga di elementi: sopra di lei lo spazio, e poi la sua altezza
		 */
		int iniziaUnaRiga(int spazioSopra) {
			y += spazioSopra;
			return y;
		}

		void aggiungi(Elemento elemento) {
			elementi.add(elemento);
		}

		void finisciLaRiga(int altezza) {
			y += altezza;
		}

		ContenutoDiColonna contenuto() {
			return new ContenutoDiColonna(larghezza, y, elementi);
		}
	}

	/**
	 * Le immagini della voce, una accanto all'altra: la femmina a sinistra rivolta a destra e il maschio a destra rivolto a sinistra, così che si guardino;
	 * per chi ha una sola forma, l'immagine com'è. Chiusa è la silhouette.
	 */
	private static void aggiungiLeImmagini(TipoPersonaggio voce, boolean aperta, int larghezza, Colonna colonna, MisureDelleImmagini misure) {
		TipoPersonaggio femminile = voce.getFormaFemminile();
		List<TipoPersonaggio> classi = new ArrayList<>();
		List<Verso> versi = new ArrayList<>();
		if (femminile != null) {
			classi.add(femminile);
			versi.add(Verso.DESTRA);
			classi.add(voce);
			versi.add(Verso.SINISTRA);
		} else {
			classi.add(voce);
			versi.add(null);
		}
		int larghezzaMassimaPerImmagine = (larghezza - SPAZIO_FRA_IMMAGINI * (classi.size() - 1)) / classi.size();
		List<Misura> adattate = new ArrayList<>();
		int larghezzaTotale = SPAZIO_FRA_IMMAGINI * (classi.size() - 1);
		int altezza = 1;
		for (TipoPersonaggio classe : classi) {
			Misura adattata = adatta(misure.personaggio(classe), larghezzaMassimaPerImmagine, ALTEZZA_MASSIMA_IMMAGINI);
			adattate.add(adattata);
			larghezzaTotale += adattata.larghezza;
			altezza = Math.max(altezza, adattata.altezza);
		}
		int y = colonna.iniziaUnaRiga(SPAZIO_FRA_SEZIONI);
		int x = (larghezza - Math.max(1, larghezzaTotale)) / 2;
		for (int i = 0; i < classi.size(); i++) {
			Misura adattata = adattate.get(i);
			boolean specchiata = versi.get(i) != null && VersiDeiPersonaggi.serveSpecchiare(classi.get(i), versi.get(i));
			// Allineate in basso, come se stessero tutte sullo stesso pavimento
			colonna.aggiungi(Elemento.immagine(NomiDelleImmagini.personaggio(classi.get(i)), adattata.larghezza, adattata.altezza, specchiata,
					aperta ? null : COLORE_SILHOUETTE, x, y + altezza - adattata.altezza));
			x += adattata.larghezza + SPAZIO_FRA_IMMAGINI;
		}
		colonna.finisciLaRiga(altezza);
	}

	// --- Le sezioni della scheda

	private static void titolo(Colonna colonna, String testo) {
		colonna.testo(Collections.singletonList(testo), MEDIO, DoomdarkColorModel.Color.YELLOW, 0, SPAZIO_FRA_SEZIONI);
	}

	private static void aggiungiLeAbilitaInnate(VistaPartita vista, TipoPersonaggio voce, int larghezza, Colonna colonna) {
		Map<String, AbilitaInnataDiClasse> abilita = new LinkedHashMap<>();
		for (TipoPersonaggio forma : forme(voce)) {
			for (AbilitaInnataDiClasse a : vista.getAbilitaInnate(forma)) {
				abilita.putIfAbsent(a.getNome(), a);
			}
		}
		if (abilita.isEmpty()) {
			return;
		}
		titolo(colonna, TestiDellaUI.get("BESTIARIO_ABILITA_INNATE"));
		for (AbilitaInnataDiClasse a : abilita.values()) {
			colonna.testo(righe(MEDIO, a.getNome(), larghezza), MEDIO, DoomdarkColorModel.Color.WHITE, 0, 6);
			colonna.testo(righe(MEDIO, a.getDescrizione(), larghezza), MEDIO, DoomdarkColorModel.Color.MEDIUM_GRAY, 0, 0);
		}
	}

	private static void aggiungiLeCaratteristiche(VistaPartita vista, TipoPersonaggio voce, int larghezza, Colonna colonna) {
		List<CaratteristicaDiClasse> caratteristiche = voce.getCaratteristiche();
		titolo(colonna, TestiDellaUI.get("BESTIARIO_FORZE_E_DEBOLEZZE"));
		if (caratteristiche.isEmpty()) {
			colonna.testo(righe(MEDIO, TestiDellaUI.get("BESTIARIO_NIENTE_DI_PARTICOLARE"), larghezza), MEDIO, DoomdarkColorModel.Color.MEDIUM_GRAY, 0, 6);
			return;
		}
		Set<String> scoperte = vista.getCaratteristicheScoperte(voce);
		int nonScoperte = 0;
		for (CaratteristicaDiClasse caratteristica : caratteristiche) {
			if (!scoperte.contains(caratteristica.getChiave())) {
				nonScoperte++;
				continue;
			}
			// Una debolezza è rossa, una resistenza o un'immunità verde
			DoomdarkColorModel.Color colore = caratteristica.getMoltiplicatore() > 1.0d && caratteristica.getGenere() == CaratteristicaDiClasse.Genere.DANNO
					? DoomdarkColorModel.Color.RED : DoomdarkColorModel.Color.GREEN;
			colonna.testo(righe(MEDIO, caratteristica.getDescrizione(), larghezza), MEDIO, colore, 0, 6);
		}
		if (nonScoperte > 0) {
			String testo = TestiDellaUI.get(nonScoperte == 1 ? "BESTIARIO_UNA_DA_SCOPRIRE" : "BESTIARIO_ANCORA_DA_SCOPRIRE", nonScoperte);
			colonna.testo(righe(MEDIO, testo, larghezza), MEDIO, DoomdarkColorModel.Color.DARK_GRAY, 0, 6);
		}
	}

	/**
	 * I moltiplicatori della classe con le loro note. Per chi ha due forme si mostrano i valori di tutte e due se sono diversi, e le note di tutte e due se
	 * sono diverse; un moltiplicatore a zero (la classe non ha quell'attributo) non si mostra.
	 */
	private static void aggiungiIlCarattere(VistaPartita vista, TipoPersonaggio voce, int larghezza, Colonna colonna) {
		List<TipoPersonaggio> forme = forme(voce);
		List<Map<String, MoltiplicatoreDiClasse>> perForma = new ArrayList<>();
		for (TipoPersonaggio forma : forme) {
			Map<String, MoltiplicatoreDiClasse> mappa = new LinkedHashMap<>();
			for (MoltiplicatoreDiClasse m : vista.getMoltiplicatoriDiClasse(forma)) {
				mappa.put(m.getChiave(), m);
			}
			perForma.add(mappa);
		}
		boolean titoloAggiunto = false;
		for (String chiave : perForma.get(perForma.size() - 1).keySet()) {
			List<TipoPersonaggio> formeConValore = new ArrayList<>();
			List<MoltiplicatoreDiClasse> valori = new ArrayList<>();
			for (int i = 0; i < forme.size(); i++) {
				MoltiplicatoreDiClasse m = perForma.get(i).get(chiave);
				if (m != null && m.getMoltiplicatore() > 0.0d) {
					formeConValore.add(forme.get(i));
					valori.add(m);
				}
			}
			if (valori.isEmpty()) {
				continue;
			}
			if (!titoloAggiunto) {
				titolo(colonna, TestiDellaUI.get("BESTIARIO_CARATTERE"));
				titoloAggiunto = true;
			}
			aggiungiUnMoltiplicatore(formeConValore, valori, larghezza, colonna);
		}
	}

	private static void aggiungiUnMoltiplicatore(List<TipoPersonaggio> forme, List<MoltiplicatoreDiClasse> valori, int larghezza, Colonna colonna) {
		boolean valoriUguali = valori.stream().map(MoltiplicatoreDiClasse::getMoltiplicatoreInTesto).distinct().count() == 1;
		// Il nome a sinistra, i valori a destra, ognuno del suo colore
		List<String> testiDeiValori = new ArrayList<>();
		List<DoomdarkColorModel.Color> coloriDeiValori = new ArrayList<>();
		List<Integer> larghezzeDeiValori = new ArrayList<>();
		int larghezzaValori = 0;
		for (int i = 0; i < valori.size(); i++) {
			MoltiplicatoreDiClasse m = valori.get(i);
			boolean conNome = !valoriUguali && forme.size() > 1;
			if (valoriUguali && i > 0) {
				break;
			}
			String testo = (conNome ? forme.get(i).getNome() + " " : "") + "x" + m.getMoltiplicatoreInTesto();
			int larghezzaDelValore = Math.max(1, FontTool.getWidth(MEDIO, testo));
			testiDeiValori.add(testo);
			coloriDeiValori.add(colore(m));
			larghezzeDeiValori.add(larghezzaDelValore);
			larghezzaValori += larghezzaDelValore + (testiDeiValori.size() > 1 ? 12 : 0);
		}
		List<String> nome = righe(MEDIO, valori.get(0).getNome(), Math.max(MEDIO.getHeight(), larghezza - larghezzaValori - 12));
		int altezza = Math.max(nome.size() * MEDIO.getHeight(), MEDIO.getHeight());
		int y = colonna.iniziaUnaRiga(8);
		colonna.aggiungi(Elemento.testo(nome, MEDIO, DoomdarkColorModel.Color.LIGHT_GRAY, 0, y));
		int x = larghezza - larghezzaValori;
		for (int i = 0; i < testiDeiValori.size(); i++) {
			colonna.aggiungi(Elemento.testo(Collections.singletonList(testiDeiValori.get(i)), MEDIO, coloriDeiValori.get(i), x, y));
			x += larghezzeDeiValori.get(i) + 12;
		}
		colonna.finisciLaRiga(altezza);
		// La nota: una sola se è uguale per tutte le forme, altrimenti una per forma
		Set<String> note = new LinkedHashSet<>();
		for (MoltiplicatoreDiClasse m : valori) {
			note.add(m.getNota());
		}
		if (note.size() == 1 || forme.size() == 1) {
			String nota = valori.get(0).getNota();
			if (!nota.isEmpty()) {
				colonna.testo(righe(MEDIO, nota, larghezza), MEDIO, DoomdarkColorModel.Color.MEDIUM_GRAY, 0, 0);
			}
		} else {
			for (int i = 0; i < valori.size(); i++) {
				if (!valori.get(i).getNota().isEmpty()) {
					colonna.testo(righe(MEDIO, forme.get(i).getNome() + ": " + valori.get(i).getNota(), larghezza), MEDIO,
							DoomdarkColorModel.Color.MEDIUM_GRAY, 0, 0);
				}
			}
		}
	}

	/**
	 * Verde sopra la media e rosso sotto, tranne per la stanchezza: lì un valore alto stanca di più, ed è un difetto
	 */
	private static DoomdarkColorModel.Color colore(MoltiplicatoreDiClasse moltiplicatore) {
		int segno = "STANCHEZZA".equals(moltiplicatore.getChiave()) ? -moltiplicatore.getSegno() : moltiplicatore.getSegno();
		switch (segno) {
			case 1:
				return DoomdarkColorModel.Color.GREEN;
			case -1:
				return DoomdarkColorModel.Color.RED;
			default:
				return DoomdarkColorModel.Color.MEDIUM_GRAY;
		}
	}

	/**
	 * Le classi della voce, la femminile prima: chi ha una sola forma ne ha una sola
	 */
	private static List<TipoPersonaggio> forme(TipoPersonaggio voce) {
		TipoPersonaggio femminile = voce.getFormaFemminile();
		return femminile == null ? Collections.singletonList(voce) : Arrays.asList(femminile, voce);
	}
}
