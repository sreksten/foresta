package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.intermezzi.Verso;
import com.threeamigos.foresta.tipi.AbilitaInnataDiClasse;
import com.threeamigos.foresta.tipi.CaratteristicaDiClasse;
import com.threeamigos.foresta.tipi.MoltiplicatoreDiClasse;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import com.threeamigos.foresta.ui.immagini.ClassePersonaggioImmagine;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.DoomdarkFontMedium;
import com.threeamigos.foresta.ui.testo.DoomdarkFontSmall;
import com.threeamigos.foresta.ui.testo.TestoGrande;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Le due immagini del bestiario (vedi DisplayableCanvasBestiario): l'elenco delle voci, a sinistra, e la scheda della voce scelta, a destra.
 * <p>
 * Una voce chiusa (mai sbloccata) mostra solo "???" e la silhouette. Una aperta mostra il nome, le immagini (la femmina a sinistra e il maschio a destra,
 * che si guardano), la descrizione, le abilità innate, quel che si è scoperto delle forze e delle debolezze e il carattere della classe: i moltiplicatori
 * (quelli a zero, come la contrattazione di chi non sa contrattare, si omettono) con la nota che dice perché hanno quel valore. Se il maschile e il
 * femminile hanno valori o note diversi si mostrano tutti e due.
 */
final class ImmagineBestiario {

	static final int ALTEZZA_RIGA_ELENCO = 40;
	private static final int MARGINE_RIGA = 6;
	private static final int LATO_ICONA = 32;
	private static final int SPAZIO_FRA_SEZIONI = 14;
	private static final int ALTEZZA_MASSIMA_IMMAGINI = 200;
	private static final int SPAZIO_FRA_IMMAGINI = 24;
	private static final Color COLORE_SILHOUETTE = new Color(70, 70, 70);
	private static final Color COLORE_SELEZIONE = new Color(255, 255, 255, 45);
	private static final String CHIUSA = "???";

	private static final DoomdarkFont MEDIO = DoomdarkFontMedium.getInstance();
	private static final DoomdarkFont PICCOLO = DoomdarkFontSmall.getInstance();

	private ImmagineBestiario() {
	}

	/**
	 * L'elenco delle voci, una per riga alta {@link #ALTEZZA_RIGA_ELENCO}, con quella scelta in evidenza
	 */
	static BufferedImage elenco(VistaPartita vista, List<TipoPersonaggio> voci, int scelta, int larghezza) {
		BufferedImage immagine = BufferedImageBuilder.creaCompatibile(Math.max(1, larghezza), Math.max(1, voci.size() * ALTEZZA_RIGA_ELENCO),
				Transparency.TRANSLUCENT);
		Graphics2D g = immagine.createGraphics();
		try {
			for (int i = 0; i < voci.size(); i++) {
				TipoPersonaggio voce = voci.get(i);
				boolean aperta = vista.isVoceDelBestiarioSbloccata(voce);
				int y = i * ALTEZZA_RIGA_ELENCO;
				if (i == scelta) {
					g.setColor(COLORE_SELEZIONE);
					g.fillRect(0, y, larghezza, ALTEZZA_RIGA_ELENCO);
				}
				BufferedImage icona = iconaDellaVoce(voce);
				Image iconaDaDisegnare = aperta ? icona : sagomaIcona(voce, icona);
				int xTesto = MARGINE_RIGA;
				if (iconaDaDisegnare != null) {
					g.drawImage(iconaDaDisegnare, MARGINE_RIGA, y + (ALTEZZA_RIGA_ELENCO - iconaDaDisegnare.getHeight(null)) / 2, null);
					xTesto += iconaDaDisegnare.getWidth(null) + MARGINE_RIGA;
				}
				DoomdarkColorModel.Color colore = aperta
						? (i == scelta ? DoomdarkColorModel.Color.WHITE : DoomdarkColorModel.Color.LIGHT_GRAY)
						: (i == scelta ? DoomdarkColorModel.Color.MEDIUM_GRAY : DoomdarkColorModel.Color.DARK_GRAY);
				Image testo = ImageCache.get(aperta ? voce.getNomeDellaVoce() : CHIUSA, MEDIO, colore, Math.max(MEDIO.getHeight(), larghezza - xTesto - MARGINE_RIGA));
				g.drawImage(testo, xTesto, y + (ALTEZZA_RIGA_ELENCO - testo.getHeight(null)) / 2, null);
			}
		} finally {
			g.dispose();
		}
		return immagine;
	}

	/**
	 * L'icona della voce, a 32 pixel di lato al più: quella della classe, o se non ne ha una la sua immagine ridotta
	 */
	private static BufferedImage iconaDellaVoce(TipoPersonaggio voce) {
		BufferedImage icona = ClassePersonaggioImmagine.getIcona(voce);
		if (icona != null) {
			return icona;
		}
		return (BufferedImage) ImageCache.getComposta(Arrays.<Object>asList("iconaDaImmagine", voce),
				() -> adatta(ClassePersonaggioImmagine.getImmagine(voce), LATO_ICONA, LATO_ICONA));
	}

	private static Image sagomaIcona(TipoPersonaggio voce, BufferedImage icona) {
		if (icona == null) {
			return null;
		}
		return ImageCache.getComposta(Arrays.<Object>asList("sagomaIcona", voce, COLORE_SILHOUETTE), () -> ImageCache.sagomaScura(icona, COLORE_SILHOUETTE));
	}

	/**
	 * La scheda della voce, larga quanto chiesto
	 */
	static BufferedImage scheda(VistaPartita vista, TipoPersonaggio voce, int larghezza) {
		boolean aperta = vista.isVoceDelBestiarioSbloccata(voce);
		List<Pezzo> pezzi = new ArrayList<>();
		BufferedImage nome = TestoGrande.immagine(aperta ? voce.getNomeDellaVoce() : CHIUSA, larghezza, true);
		pezzi.add(new Pezzo(aperta ? nome : TestoGrande.scura(nome), (larghezza - nome.getWidth()) / 2, SPAZIO_FRA_SEZIONI));
		BufferedImage immagini = immagini(voce, aperta, larghezza);
		pezzi.add(new Pezzo(immagini, (larghezza - immagini.getWidth()) / 2, SPAZIO_FRA_SEZIONI));
		if (aperta) {
			pezzi.add(new Pezzo(ImageCache.get(voce.getDescrizione(), MEDIO, DoomdarkColorModel.Color.LIGHT_GRAY, larghezza), 0, SPAZIO_FRA_SEZIONI));
			aggiungiLeAbilitaInnate(vista, voce, larghezza, pezzi);
			aggiungiLeCaratteristiche(vista, voce, larghezza, pezzi);
			aggiungiIlCarattere(vista, voce, larghezza, pezzi);
		}
		return componi(pezzi, larghezza);
	}

	/**
	 * Le immagini della voce, una accanto all'altra: la femmina a sinistra rivolta a destra e il maschio a destra rivolto a sinistra, così che si guardino;
	 * per chi ha una sola forma, l'immagine com'è. Chiusa è la silhouette.
	 */
	private static BufferedImage immagini(TipoPersonaggio voce, boolean aperta, int larghezza) {
		TipoPersonaggio femminile = voce.getFormaFemminile();
		List<BufferedImage> immagini = new ArrayList<>();
		if (femminile != null) {
			immagini.add(daMostrare(femminile, Verso.DESTRA, aperta));
			immagini.add(daMostrare(voce, Verso.SINISTRA, aperta));
		} else {
			immagini.add(daMostrare(voce, null, aperta));
		}
		int larghezzaMassimaPerImmagine = (larghezza - SPAZIO_FRA_IMMAGINI * (immagini.size() - 1)) / immagini.size();
		List<BufferedImage> adattate = new ArrayList<>();
		int larghezzaTotale = SPAZIO_FRA_IMMAGINI * (immagini.size() - 1);
		int altezza = 1;
		for (BufferedImage immagine : immagini) {
			BufferedImage adattata = adatta(immagine, larghezzaMassimaPerImmagine, ALTEZZA_MASSIMA_IMMAGINI);
			adattate.add(adattata);
			larghezzaTotale += adattata.getWidth();
			altezza = Math.max(altezza, adattata.getHeight());
		}
		BufferedImage riga = BufferedImageBuilder.creaCompatibile(Math.max(1, larghezzaTotale), altezza, Transparency.TRANSLUCENT);
		Graphics2D g = riga.createGraphics();
		try {
			int x = 0;
			for (BufferedImage adattata : adattate) {
				// Allineate in basso, come se stessero tutte sullo stesso pavimento
				g.drawImage(adattata, x, altezza - adattata.getHeight(), null);
				x += adattata.getWidth() + SPAZIO_FRA_IMMAGINI;
			}
		} finally {
			g.dispose();
		}
		return riga;
	}

	private static BufferedImage daMostrare(TipoPersonaggio classe, Verso verso, boolean aperta) {
		BufferedImage immagine = verso == null ? ClassePersonaggioImmagine.getImmagine(classe) : ClassePersonaggioImmagine.getImmagine(classe, verso);
		if (aperta) {
			return immagine;
		}
		return (BufferedImage) ImageCache.getSagomaDelPersonaggio(classe, verso, COLORE_SILHOUETTE, () -> immagine);
	}

	private static BufferedImage adatta(BufferedImage immagine, int larghezzaMassima, int altezzaMassima) {
		if (immagine.getWidth() <= larghezzaMassima && immagine.getHeight() <= altezzaMassima) {
			return immagine;
		}
		double fattore = Math.min((double) larghezzaMassima / immagine.getWidth(), (double) altezzaMassima / immagine.getHeight());
		int larghezza = Math.max(1, (int) Math.round(immagine.getWidth() * fattore));
		int altezza = Math.max(1, (int) Math.round(immagine.getHeight() * fattore));
		BufferedImage ridotta = BufferedImageBuilder.creaCompatibile(larghezza, altezza, Transparency.TRANSLUCENT);
		Graphics2D g = ridotta.createGraphics();
		try {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(immagine, 0, 0, larghezza, altezza, null);
		} finally {
			g.dispose();
		}
		return ridotta;
	}

	// --- Le sezioni della scheda

	private static void aggiungiLeAbilitaInnate(VistaPartita vista, TipoPersonaggio voce, int larghezza, List<Pezzo> pezzi) {
		Map<String, AbilitaInnataDiClasse> abilita = new LinkedHashMap<>();
		for (TipoPersonaggio forma : forme(voce)) {
			for (AbilitaInnataDiClasse a : vista.getAbilitaInnate(forma)) {
				abilita.putIfAbsent(a.getNome(), a);
			}
		}
		if (abilita.isEmpty()) {
			return;
		}
		pezzi.add(titolo("Abilità innate"));
		for (AbilitaInnataDiClasse a : abilita.values()) {
			pezzi.add(new Pezzo(ImageCache.get(a.getNome(), MEDIO, DoomdarkColorModel.Color.WHITE, larghezza), 0, 6));
			pezzi.add(new Pezzo(ImageCache.get(a.getDescrizione(), PICCOLO, DoomdarkColorModel.Color.MEDIUM_GRAY, larghezza), 0, 0));
		}
	}

	private static void aggiungiLeCaratteristiche(VistaPartita vista, TipoPersonaggio voce, int larghezza, List<Pezzo> pezzi) {
		List<CaratteristicaDiClasse> caratteristiche = voce.getCaratteristiche();
		pezzi.add(titolo("Forze e debolezze"));
		if (caratteristiche.isEmpty()) {
			pezzi.add(new Pezzo(ImageCache.get("Niente di particolare.", MEDIO, DoomdarkColorModel.Color.MEDIUM_GRAY, larghezza), 0, 6));
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
			pezzi.add(new Pezzo(ImageCache.get(caratteristica.getDescrizione(), MEDIO, colore, larghezza), 0, 6));
		}
		if (nonScoperte > 0) {
			String testo = nonScoperte == 1 ? "1 ancora da scoprire" : nonScoperte + " ancora da scoprire";
			pezzi.add(new Pezzo(ImageCache.get(testo, MEDIO, DoomdarkColorModel.Color.DARK_GRAY, larghezza), 0, 6));
		}
	}

	/**
	 * I moltiplicatori della classe con le loro note. Per chi ha due forme si mostrano i valori di tutte e due se sono diversi, e le note di tutte e due se
	 * sono diverse; un moltiplicatore a zero (la classe non ha quell'attributo) non si mostra.
	 */
	private static void aggiungiIlCarattere(VistaPartita vista, TipoPersonaggio voce, int larghezza, List<Pezzo> pezzi) {
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
				pezzi.add(titolo("Carattere"));
				titoloAggiunto = true;
			}
			aggiungiUnMoltiplicatore(formeConValore, valori, larghezza, pezzi);
		}
	}

	private static void aggiungiUnMoltiplicatore(List<TipoPersonaggio> forme, List<MoltiplicatoreDiClasse> valori, int larghezza, List<Pezzo> pezzi) {
		boolean valoriUguali = valori.stream().map(MoltiplicatoreDiClasse::getMoltiplicatoreInTesto).distinct().count() == 1;
		// Il nome a sinistra, i valori a destra, ognuno del suo colore
		List<Image> parti = new ArrayList<>();
		int larghezzaValori = 0;
		for (int i = 0; i < valori.size(); i++) {
			MoltiplicatoreDiClasse m = valori.get(i);
			boolean conNome = !valoriUguali && forme.size() > 1;
			if (valoriUguali && i > 0) {
				break;
			}
			String testo = (conNome ? forme.get(i).getNome() + " " : "") + "x" + m.getMoltiplicatoreInTesto();
			Image valore = ImageCache.get(testo, MEDIO, colore(m));
			parti.add(valore);
			larghezzaValori += valore.getWidth(null) + (parti.size() > 1 ? 12 : 0);
		}
		Image nome = ImageCache.get(valori.get(0).getNome(), MEDIO, DoomdarkColorModel.Color.LIGHT_GRAY, Math.max(MEDIO.getHeight(), larghezza - larghezzaValori - 12));
		int altezza = nome.getHeight(null);
		for (Image parte : parti) {
			altezza = Math.max(altezza, parte.getHeight(null));
		}
		BufferedImage riga = BufferedImageBuilder.creaCompatibile(larghezza, altezza, Transparency.TRANSLUCENT);
		Graphics2D g = riga.createGraphics();
		try {
			g.drawImage(nome, 0, 0, null);
			int x = larghezza - larghezzaValori;
			for (Image parte : parti) {
				g.drawImage(parte, x, 0, null);
				x += parte.getWidth(null) + 12;
			}
		} finally {
			g.dispose();
		}
		pezzi.add(new Pezzo(riga, 0, 8));
		// La nota: una sola se è uguale per tutte le forme, altrimenti una per forma
		Set<String> note = new LinkedHashSet<>();
		for (MoltiplicatoreDiClasse m : valori) {
			note.add(m.getNota());
		}
		if (note.size() == 1 || forme.size() == 1) {
			String nota = valori.get(0).getNota();
			if (!nota.isEmpty()) {
				pezzi.add(new Pezzo(ImageCache.get(nota, PICCOLO, DoomdarkColorModel.Color.MEDIUM_GRAY, larghezza), 0, 0));
			}
		} else {
			for (int i = 0; i < valori.size(); i++) {
				if (!valori.get(i).getNota().isEmpty()) {
					pezzi.add(new Pezzo(ImageCache.get(forme.get(i).getNome() + ": " + valori.get(i).getNota(), PICCOLO,
							DoomdarkColorModel.Color.MEDIUM_GRAY, larghezza), 0, 0));
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
		return femminile == null ? Arrays.asList(voce) : Arrays.asList(femminile, voce);
	}

	private static Pezzo titolo(String testo) {
		return new Pezzo(ImageCache.get(testo, MEDIO, DoomdarkColorModel.Color.YELLOW), 0, SPAZIO_FRA_SEZIONI);
	}

	// --- La composizione

	/**
	 * Un pezzo della scheda: l'immagine, a che distanza dal bordo sinistro e quanto spazio lascia sopra di sé
	 */
	private static final class Pezzo {
		private final Image immagine;
		private final int x;
		private final int spazioSopra;

		Pezzo(Image immagine, int x, int spazioSopra) {
			this.immagine = immagine;
			this.x = x;
			this.spazioSopra = spazioSopra;
		}
	}

	private static BufferedImage componi(List<Pezzo> pezzi, int larghezza) {
		int altezza = 0;
		for (Pezzo pezzo : pezzi) {
			altezza += pezzo.spazioSopra + pezzo.immagine.getHeight(null);
		}
		BufferedImage scheda = BufferedImageBuilder.creaCompatibile(Math.max(1, larghezza), Math.max(1, altezza), Transparency.TRANSLUCENT);
		Graphics2D g = scheda.createGraphics();
		try {
			g.setComposite(AlphaComposite.SrcOver);
			int y = 0;
			for (Pezzo pezzo : pezzi) {
				y += pezzo.spazioSopra;
				g.drawImage(pezzo.immagine, pezzo.x, y, null);
				y += pezzo.immagine.getHeight(null);
			}
		} finally {
			g.dispose();
		}
		return scheda;
	}
}
