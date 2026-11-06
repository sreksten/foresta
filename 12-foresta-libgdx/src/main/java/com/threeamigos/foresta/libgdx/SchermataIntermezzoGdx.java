package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.threeamigos.foresta.intermezzi.ImmagineIntermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.immagini.FotogrammiDelFuocoDaCampo;
import com.threeamigos.foresta.ui.schermate.MisureDelleImmagini;
import com.threeamigos.foresta.ui.schermate.ScenaDellIntermezzo;
import com.threeamigos.foresta.ui.schermate.ScenaDellIntermezzo.Disegno;
import com.threeamigos.foresta.ui.schermate.ScenaDellIntermezzo.Fumetto;
import com.threeamigos.foresta.ui.schermate.ScenaDellIntermezzo.Quadro;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;

/**
 * La schermata a tutto schermo degli intermezzi in libGDX. Dove sta ogni cosa a ogni istante (lo sfondo, gli elementi con il loro stato, il testo in alto, i
 * fumetti con la punta verso la bocca di chi parla) lo dice {@link ScenaDellIntermezzo}, nel modulo comune, lo stesso di Swing; qui c'è il disegno: le immagini
 * per nome (anche i fotogrammi di un foglio di sprite), il fuoco da campo calcolato pixel per pixel, il testo nell'alfabeto grande e i fumetti. Le immagini si
 * tengono finché dura l'intermezzo (le texture sono di {@link ImmaginiGdx}). La rivelazione dell'artefatto (vedi
 * {@link PaginaIntermezzo#conRivelazioneDiArtefatto}) è {@link RivelazioneDellArtefattoGdx}: parte quando l'elemento di destinazione (il capo) è stato
 * disegnato e quindi se ne conosce il posto, e al termine vola verso di lui.
 */
public final class SchermataIntermezzoGdx {

	private final int width;
	private final int height;
	private final ImmaginiGdx immagini;
	private final TestoGdx medio;
	private final SchermataIntroOutroGdx testo;
	private final MisureDelleImmagini misureDelleImmagini;
	private final ScenaDellIntermezzo scena;
	// I fotogrammi del fuoco da campo come regioni di texture, fatti alla prima volta
	private TextureRegion[] fuoco;
	// La rivelazione dell'artefatto della pagina, una volta sola per pagina
	private RivelazioneDellArtefattoGdx rivelazione;
	private boolean rivelazioneConclusa;

	public SchermataIntermezzoGdx(int width, int height, ImmaginiGdx immagini, TestoGdx medio, SchermataIntroOutroGdx testo) {
		this.width = width;
		this.height = height;
		this.immagini = immagini;
		this.medio = medio;
		this.testo = testo;
		this.misureDelleImmagini = SchermataDiScambioArtefattiGdx.misure(immagini);
		this.scena = new ScenaDellIntermezzo(width, height, this::misuraDi);
	}

	/** Mostra la pagina, facendo ripartire da zero il suo tempo. */
	public void mostra(PaginaIntermezzo pagina) {
		scena.mostra(pagina);
		rivelazione = null;
		rivelazioneConclusa = false;
	}

	/** La pagina continua come se fosse comparsa quei secondi fa: per saltare al fumetto successivo. */
	public void saltaA(double secondi) {
		// Chi salta avanti non aspetta l'artefatto: la rivelazione si chiude
		if (rivelazione != null) {
			rivelazione.concludi();
		}
		scena.saltaA(secondi);
	}

	/** Dimentica la pagina, alla fine dell'intermezzo. */
	public void svuota() {
		scena.svuota();
		rivelazione = null;
	}

	public ScenaDellIntermezzo getScena() {
		return scena;
	}

	/** Quanto è grande l'immagine com'è disegnata; null se la risorsa non c'è (l'elemento allora non si disegna). */
	private Misura misuraDi(ImmagineIntermezzo origine) {
		try {
			return ScenaDellIntermezzo.misura(origine, misureDelleImmagini);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/** Disegna la pagina com'è al tempo trascorso dalla sua comparsa. */
	public void disegna(Batch batch, Camera camera, Viewport viewport) {
		disegnaAl(batch, camera, viewport, scena.secondiTrascorsi());
	}

	/** La pagina come appare a un certo numero di secondi da quando è comparsa. */
	public void disegnaAl(Batch batch, Camera camera, Viewport viewport, double secondi) {
		Quadro quadro = scena.quadroAl(secondi);
		if (quadro == null) {
			return;
		}
		Disegno sfondo = quadro.getSfondo();
		TextureRegion regioneDelloSfondo = sfondo == null ? null : regione(sfondo);
		if (regioneDelloSfondo == null) {
			testo.ombraDelDrago(batch);
		} else {
			disegnaRegione(batch, regioneDelloSfondo, sfondo);
		}
		Area ritaglio = quadro.getRitaglio();
		boolean ritagliato = ritaglio != null
				&& RitaglioGdx.entra(batch, camera, viewport, ritaglio.getX(), ritaglio.getY(), ritaglio.getLarghezza(), ritaglio.getAltezza());
		for (Disegno elemento : quadro.getElementi()) {
			TextureRegion regione = regione(elemento);
			if (regione != null) {
				disegnaRegione(batch, regione, elemento);
			}
		}
		if (ritagliato) {
			RitaglioGdx.esci(batch);
		}

		disegnaLaRivelazione(batch, camera, viewport);

		testo.testoCentrato(batch, quadro.getTesto());

		for (Fumetto fumetto : quadro.getFumetti()) {
			NuvolaDiFumettoGdx.disegna(batch, immagini, medio, fumetto.getDescrizione(), DoomdarkColorModel.Color.BLACK, fumetto.getX(), fumetto.getY(),
					fumetto.getPuntaVersoX(), fumetto.getPuntaVersoY(), 1f);
		}
	}

	/**
	 * La rivelazione dell'artefatto della pagina, se ce n'è una e non è finita: parte quando l'elemento di destinazione è stato disegnato e quindi se ne
	 * conosce il posto.
	 */
	private void disegnaLaRivelazione(Batch batch, Camera camera, Viewport viewport) {
		PaginaIntermezzo pagina = scena.getPagina();
		if (pagina.getRivelazioneArtefatto() == null || rivelazioneConclusa) {
			return;
		}
		if (rivelazione == null) {
			Area destinazione = scena.getPosizione(pagina.getIdDestinazioneRivelazione());
			if (destinazione == null) {
				return;
			}
			rivelazione = new RivelazioneDellArtefattoGdx(pagina.getRivelazioneArtefatto(), pagina.getLivelloMondoRivelazione(), new Area(0, 0, width, height),
					new Punto(destinazione.getX() + destinazione.getLarghezza() / 2, destinazione.getY() + destinazione.getAltezza() / 2), immagini, medio);
		}
		rivelazione.anima(batch, camera, viewport);
		if (!rivelazione.isAttivo()) {
			rivelazioneConclusa = true;
		}
	}

	private void disegnaRegione(Batch batch, TextureRegion regione, Disegno disegno) {
		Color precedente = new Color(batch.getColor());
		batch.setColor(1f, 1f, 1f, disegno.getOpacita());
		if (disegno.isSpecchiata()) {
			batch.draw(regione, disegno.getX() + disegno.getLarghezza(), disegno.getY(), -disegno.getLarghezza(), disegno.getAltezza());
		} else {
			batch.draw(regione, disegno.getX(), disegno.getY(), disegno.getLarghezza(), disegno.getAltezza());
		}
		batch.setColor(precedente);
	}

	/** La regione di texture dell'immagine di un disegno; null se la risorsa non c'è. */
	private TextureRegion regione(Disegno disegno) {
		ScenaDellIntermezzo.Immagine immagine = disegno.getImmagine();
		try {
			if (immagine.isAnimazione()) {
				return fotogrammaDelFuoco(immagine.getSecondi());
			}
			if (immagine.isFotogrammaDiUnFoglio()) {
				int larghezzaFoglio = immagini.larghezza(immagine.getRisorsa(), 1f);
				int altezzaFoglio = immagini.altezza(immagine.getRisorsa(), 1f);
				int larghezza = larghezzaFoglio / immagine.getColonne();
				int altezza = altezzaFoglio / immagine.getRighe();
				int colonna = immagine.getFotogramma() % immagine.getColonne();
				int riga = immagine.getFotogramma() / immagine.getColonne();
				return immagini.porzione(immagine.getRisorsa(), colonna * larghezza, riga * altezza, larghezza, altezza);
			}
			return immagini.regione(immagine.getRisorsa());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private TextureRegion fotogrammaDelFuoco(double secondi) {
		if (fuoco == null) {
			fuoco = new TextureRegion[FotogrammiDelFuocoDaCampo.FOTOGRAMMI];
			for (int i = 0; i < fuoco.length; i++) {
				fuoco[i] = immagini.regioneDaPixel(FotogrammiDelFuocoDaCampo.fotogrammi()[i], false);
			}
		}
		return fuoco[FotogrammiDelFuocoDaCampo.indiceAl(secondi)];
	}
}
