package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.threeamigos.foresta.artefatti.VistaArtefatto;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.sprite.CodaDelleRivelazioni;
import com.threeamigos.foresta.ui.sprite.RivelazioneDellArtefatto;
import com.threeamigos.foresta.ui.sprite.RivelazioneDellArtefatto.Cerchio;
import com.threeamigos.foresta.ui.sprite.RivelazioneDellArtefatto.Fotogramma;
import com.threeamigos.foresta.ui.sprite.RivelazioneDellArtefatto.Pannello;
import com.threeamigos.foresta.ui.sprite.RivelazioneDellArtefatto.Raggio;
import com.threeamigos.foresta.ui.sprite.RivelazioneDellArtefatto.Riga;
import com.threeamigos.foresta.ui.sprite.RivelazioneDellArtefatto.Stella;
import com.threeamigos.foresta.ui.schermate.ScenaDellIntermezzo;
import com.threeamigos.foresta.ui.testo.FontTool;

import java.util.List;

/**
 * La rivelazione di un artefatto in libGDX: un cerchio luminoso con l'oggetto sopra, raggi di luce che girano, scintille, e sotto un pannello scuro con
 * nome, descrizione ed effetti. I tempi, la geometria a ogni istante e il testo del pannello sono di {@link RivelazioneDellArtefatto}, nel modulo comune,
 * lo stesso di Swing. Qui le sfumature radiali di Swing sono triangoli con il colore ai vertici: i raggi dall'alfa piena alla base a zero alla punta, il
 * cerchio e l'alone anelli concentrici i cui vertici hanno il colore della sfumatura in quel punto.
 */
public final class RivelazioneDellArtefattoGdx implements CodaDelleRivelazioni.InCoda {

	private static final int SEGMENTI = 48;
	// Dove stanno gli anelli, in frazioni del raggio: più fitti dove il colore cambia di più
	private static final float[] ANELLI = {0f, 0.2f, 0.4f, 0.55f, 0.7f, 0.85f, 1f};

	private final RivelazioneDellArtefatto modello;
	private final ImmaginiGdx immagini;
	private final TestoGdx medio;
	private final String risorsaDellOggetto;

	/**
	 * @param area         dove sta la rivelazione, centrata: tutto lo schermo
	 * @param destinazione dove vola all'uscita (il riquadro del gruppo)
	 */
	public RivelazioneDellArtefattoGdx(VistaArtefatto artefatto, int livelloMondo, Area area, Punto destinazione, ImmaginiGdx immagini, TestoGdx medio) {
		this.immagini = immagini;
		this.medio = medio;
		String risorsa = RivelazioneDellArtefatto.risorsaDellOggetto(artefatto);
		Misura misura = null;
		if (risorsa != null) {
			try {
				double zoom = ScenaDellIntermezzo.mezzaRisoluzione(risorsa) ? LivelloDiZoom.valore() : 1.0;
				misura = new Misura((int) Math.round(immagini.larghezza(risorsa, 1f) * zoom), (int) Math.round(immagini.altezza(risorsa, 1f) * zoom));
			} catch (IllegalArgumentException e) {
				// L'immagine non c'è: resta il cerchio
				risorsa = null;
			}
		}
		this.risorsaDellOggetto = risorsa;
		this.modello = new RivelazioneDellArtefatto(artefatto, livelloMondo, area, destinazione, misura, System::nanoTime);
	}

	public RivelazioneDellArtefatto getModello() {
		return modello;
	}

	@Override
	public boolean isAttivo() {
		return modello.isAttivo();
	}

	/** La chiude subito, senza l'uscita: quando chi guarda salta avanti. */
	public void concludi() {
		modello.concludi();
	}

	/** Con altre rivelazioni in coda la sosta si accorcia. */
	@Override
	public void accelera() {
		modello.accelera();
	}

	/** Disegna il fotogramma di adesso e fa passare il tempo vero. */
	public void anima(Batch batch, Camera camera, Viewport viewport) {
		disegna(batch, camera, viewport, modello.getSecondi());
		modello.avanza();
	}

	/** Disegna la rivelazione all'istante {@code t} dall'inizio. */
	public void disegna(Batch batch, Camera camera, Viewport viewport, float t) {
		Fotogramma f = modello.fotogramma(t);
		Area ritaglio = f.ritaglio;
		boolean ritagliato = ritaglio != null
				&& RitaglioGdx.entra(batch, camera, viewport, ritaglio.getX(), ritaglio.getY(), ritaglio.getLarghezza(), ritaglio.getAltezza());
		Area area = f.area;
		riempi(batch, area, f.alfaDelVelo, 0x000000);
		riempi(batch, area, Math.max(0, Math.min(255, f.alfaDelLampo)), 0xFFFFFF);
		if (f.isCerchioVisibile()) {
			disegnaIlCerchio(batch, f);
		}
		if (ritagliato) {
			RitaglioGdx.esci(batch);
		}
	}

	private void disegnaIlCerchio(Batch batch, Fotogramma f) {
		for (Raggio raggio : f.raggi) {
			int vicino = argb(raggio.rgb, raggio.alfa);
			int lontano = argb(raggio.rgb, 0f);
			float[] v = raggio.vertici;
			immagini.riempiTriangolo(batch, v[0], v[1], vicino, v[2], v[3], lontano, v[4], v[5], vicino);
		}
		disegnaUnCerchioSfumato(batch, f.alone);
		disegnaUnCerchioSfumato(batch, f.sfera);
		disegnaIlBordo(batch, f);

		for (Stella stella : f.stelle) {
			disegnaUnaStella(batch, stella);
		}

		if (f.oggetto != null && risorsaDellOggetto != null) {
			TextureRegion regione = immagini.regione(risorsaDellOggetto);
			Color precedente = new Color(batch.getColor());
			batch.setColor(1f, 1f, 1f, f.oggetto.alfa);
			batch.draw(regione, f.oggetto.x, f.oggetto.y, f.oggetto.larghezza, f.oggetto.altezza);
			batch.setColor(precedente);
		}

		if (f.opacitaDelPannello > 0) {
			disegnaIlPannello(batch, f.pannello, f.opacitaDelPannello);
		}
	}

	/** Anelli concentrici, ognuno diviso in segmenti, con a ogni vertice il colore che la sfumatura ha in quel punto. */
	private void disegnaUnCerchioSfumato(Batch batch, Cerchio c) {
		for (int anello = 0; anello + 1 < ANELLI.length; anello++) {
			float r1 = c.raggio * ANELLI[anello];
			float r2 = c.raggio * ANELLI[anello + 1];
			for (int s = 0; s < SEGMENTI; s++) {
				double a1 = 2 * Math.PI * s / SEGMENTI;
				double a2 = 2 * Math.PI * (s + 1) / SEGMENTI;
				float x1 = (float) (c.centroX + Math.cos(a1) * r1);
				float y1 = (float) (c.centroY + Math.sin(a1) * r1);
				float x2 = (float) (c.centroX + Math.cos(a2) * r1);
				float y2 = (float) (c.centroY + Math.sin(a2) * r1);
				float x3 = (float) (c.centroX + Math.cos(a2) * r2);
				float y3 = (float) (c.centroY + Math.sin(a2) * r2);
				float x4 = (float) (c.centroX + Math.cos(a1) * r2);
				float y4 = (float) (c.centroY + Math.sin(a1) * r2);
				immagini.riempiQuadrilatero(batch, x1, y1, c.argbIn(x1, y1), x2, y2, c.argbIn(x2, y2), x3, y3, c.argbIn(x3, y3), x4, y4, c.argbIn(x4, y4));
			}
		}
	}

	/** Il contorno bianco del cerchio, di due pixel. */
	private void disegnaIlBordo(Batch batch, Fotogramma f) {
		int bianco = argb(0xFFFFFF, f.alfaDelBordo);
		float esterno = f.raggio + 1f;
		float interno = f.raggio - 1f;
		for (int s = 0; s < SEGMENTI; s++) {
			double a1 = 2 * Math.PI * s / SEGMENTI;
			double a2 = 2 * Math.PI * (s + 1) / SEGMENTI;
			immagini.riempiQuadrilatero(batch,
					(float) (f.centroX + Math.cos(a1) * interno), (float) (f.centroY + Math.sin(a1) * interno), bianco,
					(float) (f.centroX + Math.cos(a2) * interno), (float) (f.centroY + Math.sin(a2) * interno), bianco,
					(float) (f.centroX + Math.cos(a2) * esterno), (float) (f.centroY + Math.sin(a2) * esterno), bianco,
					(float) (f.centroX + Math.cos(a1) * esterno), (float) (f.centroY + Math.sin(a1) * esterno), bianco);
		}
	}

	/** Due rombi stretti incrociati, uno in verticale e uno in orizzontale. */
	private void disegnaUnaStella(Batch batch, Stella s) {
		int bianco = argb(0xFFFFFF, s.alfa);
		float stretto = s.d * 0.25f;
		immagini.riempiQuadrilatero(batch, s.x, s.y - s.d, bianco, s.x + stretto, s.y, bianco, s.x, s.y + s.d, bianco, s.x - stretto, s.y, bianco);
		immagini.riempiQuadrilatero(batch, s.x - s.d, s.y, bianco, s.x, s.y + stretto, bianco, s.x + s.d, s.y, bianco, s.x, s.y - stretto, bianco);
	}

	/** Lo sfondo scuro con gli angoli smussati, il bordo e le righe di testo, tutti con l'opacità del pannello. */
	private void disegnaIlPannello(Batch batch, Pannello pannello, float opacita) {
		Color sfondo = new Color(0f, 0f, 0f, 200f / 255f * opacita);
		Color bordo = new Color(((pannello.rgbDelBordo >> 16) & 0xFF) / 255f, ((pannello.rgbDelBordo >> 8) & 0xFF) / 255f, (pannello.rgbDelBordo & 0xFF) / 255f,
				pannello.alfaDelBordo * opacita);
		int x = pannello.x;
		int y = pannello.y;
		int l = pannello.larghezza;
		int a = pannello.altezza;
		// Sfondo e bordo con gli angoli tagliati di qualche pixel, come un rettangolo smussato
		int[] smusso = {4, 2, 1, 1};
		for (int i = 0; i < smusso.length; i++) {
			immagini.riempi(batch, x + smusso[i], y + i, l - 2 * smusso[i], 1, sfondo);
			immagini.riempi(batch, x + smusso[i], y + a - 1 - i, l - 2 * smusso[i], 1, sfondo);
		}
		immagini.riempi(batch, x, y + smusso.length, l, a - 2 * smusso.length, sfondo);
		immagini.riempi(batch, x + smusso[0], y, l - 2 * smusso[0], 1, bordo);
		immagini.riempi(batch, x + smusso[0], y + a - 1, l - 2 * smusso[0], 1, bordo);
		immagini.riempi(batch, x, y + smusso[0], 1, a - 2 * smusso[0], bordo);
		immagini.riempi(batch, x + l - 1, y + smusso[0], 1, a - 2 * smusso[0], bordo);
		List<Riga> righe = pannello.getRighe();
		for (Riga riga : righe) {
			int yRiga = y + riga.getY();
			for (String parte : FontTool.split(medio.getFont(), riga.getTesto(), riga.getLarghezzaMassima())) {
				medio.disegna(batch, parte, x + riga.getX(), yRiga, TestoGdx.coloreConAlfa(riga.getColore(), opacita), 1f);
				yRiga += medio.getFont().getHeight();
			}
		}
	}

	private void riempi(Batch batch, Area area, int alfa, int rgb) {
		if (alfa > 0) {
			immagini.riempi(batch, area.getX(), area.getY(), area.getLarghezza(), area.getAltezza(),
					new Color(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f, alfa / 255f));
		}
	}

	private static int argb(int rgb, float alfa) {
		return (Math.round(255 * Math.max(0f, Math.min(1f, alfa))) << 24) | (rgb & 0xFFFFFF);
	}
}
