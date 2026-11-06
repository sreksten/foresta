package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.threeamigos.foresta.ui.sfx.AnimazioneDelLogo;
import com.threeamigos.foresta.ui.sfx.LogoIniziale;
import com.threeamigos.foresta.ui.sfx.ScieDelLogo;
import com.threeamigos.foresta.ui.sfx.TestaDellaCometa;
import com.threeamigos.foresta.ui.sfx.TorciaDelLogo;

import java.util.Arrays;
import java.util.List;

/**
 * Il logo iniziale in libGDX, gemello di <code>PannelloLogoIniziale</code> di Swing: la logica (i contorni, i corridori, le fasi, le opacita') e' di
 * {@link AnimazioneDelLogo}, con i parametri di {@link LogoIniziale}, e qui c'e' il disegno, centrato sullo schermo e senza zoom, sul fondo nero che
 * lascia il fotogramma. Dove Swing ha un gradiente radiale e una polilinea con spessore, qui ci sono i pixel gia' pronti del modulo comune:
 * <ul>
 * <li>la torcia ({@link TorciaDelLogo}) ricalcola a ogni fotogramma il logo visibile solo attorno ai corridori, in una texture che si riscrive;</li>
 * <li>le scie ({@link ScieDelLogo}) si accumulano, aggiungendo ogni fotogramma solo i tratti nuovi, in un'altra texture;</li>
 * <li>la testa luminosa di ogni corridore ({@link TestaDellaCometa}) e' un'immagine sola, messa dove sta il corridore.</li>
 * </ul>
 * Il logo intero, la testa e le scie si disegnano con l'opacita' dell'animazione come alpha del colore del batch.
 */
public final class LogoInizialeGdx implements Disposable {

	private final AnimazioneDelLogo animazione;
	private final int x;
	private final int y;
	private final TextureRegion logo;
	private final TextureRegion testa;
	private final TorciaDelLogo torcia;
	private final TexturaAggiornabileGdx texturaDellaTorcia;
	private final ScieDelLogo scie;
	private final TexturaAggiornabileGdx texturaDelleScie;
	// Fino a che punto del percorso di ogni corridore le scie sono gia' stampate
	private final int[] stampati;

	/**
	 * @param larghezzaDelloSchermo la larghezza dello schermo virtuale, su cui il logo si centra
	 * @param altezzaDelloSchermo   la sua altezza
	 */
	public LogoInizialeGdx(ImmaginiGdx immagini, int larghezzaDelloSchermo, int altezzaDelloSchermo) {
		this.animazione = LogoIniziale.animazione();
		int larghezza = animazione.getLarghezza();
		int altezza = animazione.getAltezza();
		this.x = (larghezzaDelloSchermo - larghezza) / 2;
		this.y = (altezzaDelloSchermo - altezza) / 2;
		this.logo = immagini.regioneDaPixel(animazione.getLogo(), true);
		this.testa = immagini.regioneDaPixel(TestaDellaCometa.immagine(), true);
		this.torcia = new TorciaDelLogo(animazione.getLogo());
		this.texturaDellaTorcia = new TexturaAggiornabileGdx(larghezza, altezza, true);
		this.scie = new ScieDelLogo(larghezza, altezza);
		this.texturaDelleScie = new TexturaAggiornabileGdx(larghezza, altezza, true);
		this.stampati = new int[animazione.getCorridori().size()];
	}

	/** Un fotogramma dell'animazione */
	public void avanza() {
		animazione.avanza();
		stampaLeScie();
	}

	public boolean isFinito() {
		return animazione.isFinito();
	}

	/**
	 * Aggiunge alle scie i tratti percorsi dai corridori dall'ultimo fotogramma; se un corridore e' tornato indietro (il ciclo e' ricominciato) le scie
	 * si rifanno da capo.
	 */
	private void stampaLeScie() {
		List<AnimazioneDelLogo.Corridore> corridori = animazione.getCorridori();
		for (int i = 0; i < stampati.length; i++) {
			if (corridori.get(i).getIndice() < stampati[i]) {
				scie.azzera();
				Arrays.fill(stampati, 0);
				break;
			}
		}
		for (int i = 0; i < stampati.length; i++) {
			AnimazioneDelLogo.Corridore corridore = corridori.get(i);
			if (!corridore.isPartito()) {
				continue;
			}
			List<AnimazioneDelLogo.Posizione> percorso = corridore.getPercorso();
			int fino = corridore.getIndice();
			for (int k = stampati[i]; k < fino; k++) {
				scie.aggiungi(percorso.get(k), percorso.get(k + 1));
			}
			stampati[i] = fino;
		}
	}

	/**
	 * Disegna il fotogramma corrente; lo sfondo nero e' quello che il chiamante ha gia' messo
	 */
	public void disegna(Batch batch) {
		Color precedente = new Color(batch.getColor());
		if (animazione.isTorciaAttiva()) {
			// ancora nel tracciamento: il logo si vede solo vicino ai corridori
			texturaDellaTorcia.aggiorna(torcia.rivela(animazione.getCorridori(), animazione.getRaggioTorcia()));
			batch.setColor(1f, 1f, 1f, 1f);
			batch.draw(texturaDellaTorcia.getRegione(), x, y);
		} else {
			float opacita = (float) animazione.getOpacitaLogo();
			if (opacita > 0f) {
				batch.setColor(1f, 1f, 1f, opacita);
				batch.draw(logo, x, y);
			}
		}

		// scie e teste vanno sopra al logo, con l'opacita' delle scie (che le fa svanire nella dissolvenza o nella scomparsa)
		float opacitaScie = (float) animazione.getOpacitaScie();
		if (opacitaScie > 0f) {
			if (scie.isCambiato()) {
				texturaDelleScie.aggiorna(scie.getPixel());
				scie.segnaLetto();
			}
			batch.setColor(1f, 1f, 1f, opacitaScie);
			batch.draw(texturaDelleScie.getRegione(), x, y);
			if (animazione.isConTeste()) {
				for (AnimazioneDelLogo.Corridore corridore : animazione.getCorridori()) {
					if (corridore.isPartito()) {
						AnimazioneDelLogo.Posizione p = corridore.getPosizione();
						batch.draw(testa, (float) (x + p.x - TestaDellaCometa.RAGGIO), (float) (y + p.y - TestaDellaCometa.RAGGIO),
								2 * TestaDellaCometa.RAGGIO, 2 * TestaDellaCometa.RAGGIO);
					}
				}
			}
		}
		batch.setColor(precedente);
	}

	/**
	 * Libera le texture che si riscrivono; quelle di sola lettura sono di {@link ImmaginiGdx}
	 */
	@Override
	public void dispose() {
		texturaDellaTorcia.dispose();
		texturaDelleScie.dispose();
	}
}
