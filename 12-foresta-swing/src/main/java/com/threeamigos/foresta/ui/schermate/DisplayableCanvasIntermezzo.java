package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoErrore;
import com.threeamigos.foresta.intermezzi.ImmagineIntermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.StatoElemento;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import com.threeamigos.foresta.tipi.MappaPersonaggioImmagine;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.schermate.ScenaDellIntermezzo.Disegno;
import com.threeamigos.foresta.ui.schermate.ScenaDellIntermezzo.Fumetto;
import com.threeamigos.foresta.ui.schermate.ScenaDellIntermezzo.Quadro;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.sprite.AnimazioneImmagine;
import com.threeamigos.foresta.ui.sprite.DescrizioneFumetto;
import com.threeamigos.foresta.ui.sprite.SpriteFumetto;
import com.threeamigos.foresta.ui.sprite.SpriteRivelazioneArtefatto;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * La schermata a tutto schermo degli intermezzi in Swing. Dove sta ogni cosa a ogni istante (lo sfondo, gli elementi con il loro stato, il testo in alto, i
 * fumetti con la punta verso la bocca di chi parla) lo dice {@link ScenaDellIntermezzo}, nel modulo comune, a strati; qui c'è il disegno, e la rivelazione
 * dell'artefatto.
 * <p>
 * Le immagini di tipo RISORSA e gli sprite sheet sono caricati al primo uso e tenuti finché dura l'intermezzo (vedi {@link #svuota()}), senza passare da
 * ImageCache; gli sprite sheet sono ritagliati in fotogrammi una volta sola. Ogni immagine di tipo ANIMAZIONE ha una sua istanza di
 * {@link AnimazioneImmagine}, anch'essa tenuta per tutto l'intermezzo.
 */
public class DisplayableCanvasIntermezzo implements Finestra {

	private final int width;
	private final int height;
	private final DisplayableCanvasIntroOutro testo;
	private final ScenaDellIntermezzo scena;

	private final Map<String, BufferedImage> immaginiRisorse = new HashMap<>();
	// I fotogrammi degli sprite sheet, per percorso e griglia; null se lo sheet manca
	private final Map<String, BufferedImage[]> fotogrammiSpriteSheet = new HashMap<>();
	// Un'animazione per ogni riferimento che la chiede (cioè per ogni elemento o sfondo)
	private final Map<ImmagineIntermezzo, AnimazioneImmagine> animazioni = new IdentityHashMap<>();
	// Le nuvole delle battute della pagina corrente come immagini, costruite alla prima comparsa; null se il testo non si può disegnare
	private final Map<DescrizioneFumetto, BufferedImage> nuvole = new IdentityHashMap<>();
	// La rivelazione dell'artefatto della pagina (vedi PaginaIntermezzo.conRivelazioneDiArtefatto), una volta sola per pagina
	private SpriteRivelazioneArtefatto rivelazione;
	private boolean rivelazioneConclusa;

	public DisplayableCanvasIntermezzo(int width, int height, DisplayableCanvasIntroOutro testo) {
		this.width = width;
		this.height = height;
		this.testo = testo;
		this.scena = new ScenaDellIntermezzo(width, height, this::misuraDi);
	}

	/**
	 * Mostra la pagina, facendo ripartire da zero il suo tempo.
	 */
	public void mostra(PaginaIntermezzo pagina) {
		scena.mostra(pagina);
		rivelazione = null;
		rivelazioneConclusa = false;
		nuvole.clear();
	}

	/**
	 * La pagina continua come se fosse comparsa quei secondi fa: per saltare al fumetto successivo.
	 */
	public void saltaA(double secondi) {
		// Chi salta avanti non aspetta l'artefatto: la rivelazione si chiude
		if (rivelazione != null) {
			rivelazione.concludi();
		}
		scena.saltaA(secondi);
	}

	/**
	 * Dimentica la pagina e le immagini caricate per l'intermezzo, alla sua fine.
	 */
	public void svuota() {
		scena.svuota();
		rivelazione = null;
		nuvole.clear();
		immaginiRisorse.clear();
		fotogrammiSpriteSheet.clear();
		animazioni.clear();
	}

	/**
	 * Disegna la pagina com'è al tempo trascorso dalla sua comparsa.
	 */
	public void disegna(Graphics2D graphics) {
		disegnaAl(graphics, scena.secondiTrascorsi());
	}

	/**
	 * La pagina come appare a un certo numero di secondi da quando è comparsa.
	 */
	public void disegnaAl(Graphics2D graphics, double secondi) {
		Quadro quadro = scena.quadroAl(secondi);
		if (quadro == null) {
			return;
		}

		// Copia del contesto: su un Graphics2D nuovo l'hint di interpolazione vale null
		// e non si può ripristinare, lascerebbe il bilineare a chi disegna dopo.
		Graphics2D graphicsImmagini = (Graphics2D) graphics.create();
		try {
			graphicsImmagini.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			disegnaSfondo(graphicsImmagini, quadro);
			Area ritaglio = quadro.getRitaglio();
			if (ritaglio != null) {
				graphicsImmagini.setClip(ritaglio.getX(), ritaglio.getY(), ritaglio.getLarghezza(), ritaglio.getAltezza());
			}
			for (Disegno elemento : quadro.getElementi()) {
				disegnaElemento(graphicsImmagini, elemento);
			}
		} finally {
			graphicsImmagini.dispose();
		}

		disegnaRivelazione(graphics);

		testo.scriviTestoCentrato(graphics, quadro.getTesto());

		for (Fumetto fumetto : quadro.getFumetti()) {
			disegnaFumetto(graphics, fumetto);
		}
	}

	/**
	 * La rivelazione dell'artefatto della pagina, se ce n'è una e non è finita: parte quando l'elemento di destinazione (il capo) è
	 * stato disegnato e quindi se ne conosce il posto, e al termine vola verso di lui.
	 */
	private void disegnaRivelazione(Graphics2D graphics) {
		PaginaIntermezzo pagina = scena.getPagina();
		if (pagina.getRivelazioneArtefatto() == null || rivelazioneConclusa) {
			return;
		}
		if (rivelazione == null) {
			Area destinazione = scena.getPosizione(pagina.getIdDestinazioneRivelazione());
			if (destinazione == null) {
				return;
			}
			rivelazione = new SpriteRivelazioneArtefatto(pagina.getRivelazioneArtefatto(), pagina.getLivelloMondoRivelazione(),
					new Rectangle(0, 0, width, height),
					new Point(destinazione.getX() + destinazione.getLarghezza() / 2, destinazione.getY() + destinazione.getAltezza() / 2));
		}
		rivelazione.anima(graphics);
		if (!rivelazione.isAttivo()) {
			rivelazioneConclusa = true;
		}
	}

	private void disegnaSfondo(Graphics2D graphics, Quadro quadro) {
		Disegno sfondo = quadro.getSfondo();
		BufferedImage immagine = sfondo == null ? null : immagine(sfondo.getImmagine());
		if (immagine == null) {
			testo.disegnaOmbraDelDrago(graphics);
			return;
		}
		graphics.drawImage(immagine, sfondo.getX(), sfondo.getY(), sfondo.getLarghezza(), sfondo.getAltezza(), null);
	}

	private void disegnaElemento(Graphics2D graphics, Disegno elemento) {
		BufferedImage immagine = immagine(elemento.getImmagine());
		if (immagine == null) {
			return;
		}
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, elemento.getOpacita()));
		if (elemento.isSpecchiata()) {
			graphics.drawImage(immagine, elemento.getX() + elemento.getLarghezza(), elemento.getY(), -elemento.getLarghezza(), elemento.getAltezza(), null);
		} else {
			graphics.drawImage(immagine, elemento.getX(), elemento.getY(), elemento.getLarghezza(), elemento.getAltezza(), null);
		}
	}

	private void disegnaFumetto(Graphics2D graphics, Fumetto fumetto) {
		BufferedImage nuvola = nuvole.computeIfAbsent(fumetto.getDescrizione(), d -> SpriteFumetto.costruisciNuvola(d, DoomdarkColorModel.Color.BLACK));
		graphics.drawImage(nuvola, fumetto.getX(), fumetto.getY() - nuvola.getHeight(), null);
		SpriteFumetto.disegnaPunta(graphics, fumetto.getX(), fumetto.getY(), nuvola.getWidth(), nuvola.getHeight(), fumetto.getPuntaVersoX(), fumetto.getPuntaVersoY());
	}

	/**
	 * Quanto è grande un'immagine com'è caricata (già con lo zoom); null se non c'è.
	 */
	private Misura misuraDi(ImmagineIntermezzo origine) {
		BufferedImage immagine = immagine(origine, 0, null);
		return immagine == null ? null : new Misura(immagine.getWidth(), immagine.getHeight());
	}

	private BufferedImage immagine(ScenaDellIntermezzo.Immagine immagine) {
		return immagine(immagine.getOrigine(), immagine.getSecondi(), immagine.getStato());
	}

	/**
	 * L'immagine da disegnare in questo istante; null se non è disponibile.
	 */
	private BufferedImage immagine(ImmagineIntermezzo immagine, double secondi, StatoElemento stato) {
		switch (immagine.getTipo()) {
			case PERSONAGGIO:
				return MappaPersonaggioImmagine.getImmagine(immagine.getClassePersonaggio());
			case LOCAZIONE:
				return ImageCache.locazioni.get(immagine.getTipoLocazione());
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
			// Le locazioni, i personaggi e gli oggetti stanno nei file a metà risoluzione, come in ImageCache
			BufferedImage caricata = BufferedImageBuilder.provaACaricare(percorso, ScenaDellIntermezzo.mezzaRisoluzione(percorso) ? LivelloDiZoom.valore() : 1.0);
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
