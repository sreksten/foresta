package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoErrore;
import com.threeamigos.foresta.intermezzi.BattutaIntermezzo;
import com.threeamigos.foresta.intermezzi.BattutaProgrammata;
import com.threeamigos.foresta.intermezzi.ElementoIntermezzo;
import com.threeamigos.foresta.intermezzi.ImmagineIntermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.StatoElemento;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * La schermata a tutto schermo degli intermezzi. Disegna una {@link PaginaIntermezzo} a
 * strati: lo sfondo (scalato a coprire tutta l'area, o l'ombra del drago se manca), gli
 * elementi nell'ordine della pagina con il loro stato al secondo corrente, il testo in
 * alto e i fumetti delle battute visibili, con la punta verso la bocca di chi parla.
 * <p>
 * Il tempo è quello reale trascorso da quando la pagina è comparsa: è lo stesso metro del
 * timer con cui il motore fa avanzare le pagine, quindi animazioni e dialoghi restano
 * allineati alla durata della pagina anche se qualche frame va perso.
 * <p>
 * Le immagini di tipo RISORSA e gli sprite sheet sono caricati al primo uso e tenuti
 * finché dura l'intermezzo (vedi {@link #svuota()}), senza passare da ImageCache; gli
 * sprite sheet sono ritagliati in fotogrammi una volta sola. Ogni immagine di tipo
 * ANIMAZIONE ha una sua istanza di {@link AnimazioneImmagine}, anch'essa tenuta per
 * tutto l'intermezzo.
 */
class DisplayableCanvasIntermezzo implements Finestra {

	// Distanza fra la bocca di chi parla e il bordo del fumetto collocato automaticamente
	private static final int DISTANZA_FUMETTO_VERTICALE = 24;
	private static final int DISTANZA_FUMETTO_ORIZZONTALE = 8;
	private static final int MARGINE_SCHERMO = 4;

	private final int width;
	private final int height;
	private final DisplayableCanvasIntroOutro testo;

	private PaginaIntermezzo pagina;
	private long inizioPaginaNanosecondi;

	private final Map<String, BufferedImage> immaginiRisorse = new HashMap<>();
	// I fotogrammi degli sprite sheet, per percorso e griglia; null se lo sheet manca
	private final Map<String, BufferedImage[]> fotogrammiSpriteSheet = new HashMap<>();
	// Un'animazione per ogni riferimento che la chiede (cioè per ogni elemento o sfondo)
	private final Map<ImmagineIntermezzo, AnimazioneImmagine> animazioni = new IdentityHashMap<>();
	// Le nuvole delle battute della pagina corrente, costruite alla prima comparsa; null
	// se il testo non si può disegnare
	private final Map<BattutaIntermezzo, BufferedImage> nuvole = new IdentityHashMap<>();
	// Dove sono stati disegnati gli elementi in questo frame, e in che verso, per puntare i fumetti
	private final Map<String, Rectangle> posizioni = new HashMap<>();
	private final Map<String, Boolean> versi = new HashMap<>();

	DisplayableCanvasIntermezzo(int width, int height, DisplayableCanvasIntroOutro testo) {
		this.width = width;
		this.height = height;
		this.testo = testo;
	}

	void mostra(PaginaIntermezzo pagina) {
		this.pagina = pagina;
		inizioPaginaNanosecondi = System.nanoTime();
		nuvole.clear();
	}

	/**
	 * Dimentica la pagina e le immagini caricate per l'intermezzo, alla sua fine.
	 */
	void svuota() {
		pagina = null;
		nuvole.clear();
		immaginiRisorse.clear();
		fotogrammiSpriteSheet.clear();
		animazioni.clear();
		posizioni.clear();
		versi.clear();
	}

	void disegna(Graphics2D graphics) {
		disegnaAl(graphics, (System.nanoTime() - inizioPaginaNanosecondi) / 1_000_000_000.0);
	}

	/**
	 * La pagina come appare a un certo numero di secondi da quando è comparsa.
	 */
	void disegnaAl(Graphics2D graphics, double secondi) {
		if (pagina == null) {
			return;
		}

		Object interpolazioneOriginale = graphics.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
		Composite compositeOriginale = graphics.getComposite();
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		disegnaSfondo(graphics, secondi);
		posizioni.clear();
		versi.clear();
		for (ElementoIntermezzo elemento : pagina.getElementi()) {
			disegnaElemento(graphics, elemento, elemento.getStatoAl(secondi), secondi);
		}
		graphics.setComposite(compositeOriginale);
		if (interpolazioneOriginale != null) {
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolazioneOriginale);
		}

		testo.scriviTestoCentrato(graphics, pagina.getTesto());

		for (BattutaProgrammata battutaProgrammata : pagina.getBattuteProgrammate()) {
			if (battutaProgrammata.isVisibileAl(secondi)) {
				disegnaFumetto(graphics, battutaProgrammata.getBattuta());
			}
		}
	}

	private void disegnaSfondo(Graphics2D graphics, double secondi) {
		BufferedImage sfondo = pagina.getSfondo() == null ? null : immagine(pagina.getSfondo(), secondi, null);
		if (sfondo == null) {
			testo.disegnaOmbraDelDrago(graphics);
			return;
		}
		// Scalato per coprire tutta l'area mantenendo le proporzioni, centrato
		double scala = Math.max((double) width / sfondo.getWidth(), (double) height / sfondo.getHeight());
		int larghezzaSfondo = (int) Math.ceil(sfondo.getWidth() * scala);
		int altezzaSfondo = (int) Math.ceil(sfondo.getHeight() * scala);
		graphics.drawImage(sfondo, (width - larghezzaSfondo) / 2, (height - altezzaSfondo) / 2,
				larghezzaSfondo, altezzaSfondo, null);
	}

	private void disegnaElemento(Graphics2D graphics, ElementoIntermezzo elemento, StatoElemento stato, double secondi) {
		BufferedImage immagine = immagine(elemento.getImmagine(), secondi, stato);
		if (immagine == null) {
			return;
		}
		int larghezza = (int) Math.round(immagine.getWidth() * stato.getScala());
		int altezza = (int) Math.round(immagine.getHeight() * stato.getScala());
		int x = (int) Math.round(stato.getX() * width) - larghezza / 2;
		int y = (int) Math.round(stato.getY() * height) - altezza / 2;
		posizioni.put(elemento.getId(), new Rectangle(x, y, larghezza, altezza));
		versi.put(elemento.getId(), stato.isSpecchiato());

		float opacita = (float) Math.max(0, Math.min(1, stato.getOpacita()));
		if (opacita == 0 || larghezza <= 0 || altezza <= 0) {
			return;
		}
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacita));
		if (stato.isSpecchiato()) {
			graphics.drawImage(immagine, x + larghezza, y, -larghezza, altezza, null);
		} else {
			graphics.drawImage(immagine, x, y, larghezza, altezza, null);
		}
	}

	private void disegnaFumetto(Graphics2D graphics, BattutaIntermezzo battuta) {
		Point punta = puntaDellaBattuta(battuta);
		if (punta == null) {
			return;
		}
		BufferedImage nuvola = nuvola(battuta);
		if (nuvola == null) {
			return;
		}
		int larghezza = nuvola.getWidth();
		int altezza = nuvola.getHeight();
		// (x, y) è l'angolo in basso a sinistra, come per SpriteFumetto
		int x;
		int y;
		if (battuta.hasPosizioneNuvola()) {
			x = (int) Math.round(battuta.getNuvolaX() * width) - larghezza / 2;
			y = (int) Math.round(battuta.getNuvolaY() * height) + altezza / 2;
		} else {
			// Sopra chi parla, spostato verso il centro dello schermo
			y = punta.y - DISTANZA_FUMETTO_VERTICALE;
			x = punta.x < width / 2 ? punta.x + DISTANZA_FUMETTO_ORIZZONTALE : punta.x - larghezza - DISTANZA_FUMETTO_ORIZZONTALE;
		}
		x = Math.max(MARGINE_SCHERMO, Math.min(x, width - larghezza - MARGINE_SCHERMO));
		y = Math.max(altezza + MARGINE_SCHERMO, Math.min(y, height - MARGINE_SCHERMO));

		graphics.drawImage(nuvola, x, y - altezza, null);
		SpriteFumetto.disegnaPunta(graphics, x, y, larghezza, altezza, punta.x, punta.y);
	}

	/**
	 * La bocca di chi parla nella posizione che ha in questo frame, o il punto fisso.
	 */
	private Point puntaDellaBattuta(BattutaIntermezzo battuta) {
		if (battuta.getIdElemento() == null) {
			return new Point((int) Math.round(battuta.getPuntoX() * width), (int) Math.round(battuta.getPuntoY() * height));
		}
		Rectangle rettangolo = posizioni.get(battuta.getIdElemento());
		if (rettangolo == null) {
			return null;
		}
		ElementoIntermezzo elemento = pagina.getElemento(battuta.getIdElemento());
		double boccaX = Boolean.TRUE.equals(versi.get(battuta.getIdElemento())) ? 1 - elemento.getBoccaX() : elemento.getBoccaX();
		return new Point(rettangolo.x + (int) Math.round(boccaX * rettangolo.width),
				rettangolo.y + (int) Math.round(elemento.getBoccaY() * rettangolo.height));
	}

	private BufferedImage nuvola(BattutaIntermezzo battuta) {
		if (!nuvole.containsKey(battuta)) {
			BufferedImage nuvola = null;
			try {
				nuvola = SpriteFumetto.costruisciNuvola(battuta.getTesto(), width / 3,
						DoomdarkFontMedium.getInstance(), DoomdarkColorModel.Color.BLACK);
			} catch (DoomdarkFont.UnsupportedCharacterException e) {
				BusEventi.pubblica(new InternoErrore("Battuta non disegnabile: \"" + battuta.getTesto() + "\" - " + e.getMessage()));
			}
			nuvole.put(battuta, nuvola);
		}
		return nuvole.get(battuta);
	}

	/**
	 * L'immagine da disegnare in questo istante; null se non è disponibile.
	 */
	private BufferedImage immagine(ImmagineIntermezzo immagine, double secondi, StatoElemento stato) {
		switch (immagine.getTipo()) {
			case PERSONAGGIO:
				return ClassePersonaggioImmagine.getImmagine(immagine.getClassePersonaggio());
			case LOCAZIONE:
				return ImageCache.locazioni.get(immagine.getClasseLocazione());
			case SPRITE_SHEET:
				BufferedImage[] fotogrammi = fotogrammi(immagine);
				return fotogrammi == null ? null : fotogrammi[immagine.getFotogrammaAl(secondi)];
			case ANIMAZIONE:
				return animazioni.computeIfAbsent(immagine, i -> AnimazioniIntermezzo.crea(i.getAnimazione()))
						.getFotogramma(secondi, stato);
			default:
				return risorsa(immagine.getRisorsa());
		}
	}

	/**
	 * Caricata una volta sola: anche se manca, così l'errore non si ripete a ogni frame.
	 */
	private BufferedImage risorsa(String percorso) {
		if (!immaginiRisorse.containsKey(percorso)) {
			BufferedImage caricata = BufferedImageBuilder.provaACaricare(percorso);
			if (caricata == null) {
				BusEventi.pubblica(new InternoErrore("Immagine di intermezzo non trovata: " + percorso));
			}
			immaginiRisorse.put(percorso, caricata);
		}
		return immaginiRisorse.get(percorso);
	}

	/**
	 * I fotogrammi di uno sprite sheet, numerati riga per riga. Sono sotto-immagini dello
	 * sheet (getSubimage), quindi non ne duplicano i pixel.
	 */
	private BufferedImage[] fotogrammi(ImmagineIntermezzo spriteSheet) {
		String chiave = spriteSheet.getRisorsa() + '#' + spriteSheet.getColonne() + 'x' + spriteSheet.getRighe();
		if (!fotogrammiSpriteSheet.containsKey(chiave)) {
			BufferedImage sheet = risorsa(spriteSheet.getRisorsa());
			BufferedImage[] fotogrammi = null;
			if (sheet != null) {
				int larghezzaFotogramma = sheet.getWidth() / spriteSheet.getColonne();
				int altezzaFotogramma = sheet.getHeight() / spriteSheet.getRighe();
				fotogrammi = new BufferedImage[spriteSheet.getColonne() * spriteSheet.getRighe()];
				for (int riga = 0; riga < spriteSheet.getRighe(); riga++) {
					for (int colonna = 0; colonna < spriteSheet.getColonne(); colonna++) {
						fotogrammi[riga * spriteSheet.getColonne() + colonna] = sheet.getSubimage(
								colonna * larghezzaFotogramma, riga * altezzaFotogramma, larghezzaFotogramma, altezzaFotogramma);
					}
				}
			}
			fotogrammiSpriteSheet.put(chiave, fotogrammi);
		}
		return fotogrammiSpriteSheet.get(chiave);
	}
}
