package com.threeamigos.foresta.libgdx.sviluppo;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.threeamigos.foresta.libgdx.ImmaginiGdx;
import com.threeamigos.foresta.libgdx.SpriteATempoGdx;
import com.threeamigos.foresta.libgdx.SpriteAnnuncioGlobaleGdx;
import com.threeamigos.foresta.libgdx.SpriteEffettoGdx;
import com.threeamigos.foresta.libgdx.SpriteFumettoGdx;
import com.threeamigos.foresta.libgdx.SpriteGdx;
import com.threeamigos.foresta.libgdx.SpriteInDissolvenzaGdx;
import com.threeamigos.foresta.libgdx.TestoGdx;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.sprite.DescrizioneAnnuncioGlobale;
import com.threeamigos.foresta.ui.sprite.DescrizioneFumetto;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteATempo;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteEffetto;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;

import java.util.ArrayList;
import java.util.List;

/**
 * La pagina di prova degli sprite per chi sviluppa: a ogni giro (quando i precedenti sono finiti) partono uno sprite a tempo con l'icona (verde e rosso, uno
 * ritagliato a un'area), un effetto, un annuncio, un fumetto e un'immagine che sfuma. Non è parte del gioco: i testi sono campioni, non si traducono.
 */
public final class PaginaDegliSprite {

	private final ImmaginiGdx immagini;
	private final TestoGdx piccolo;
	private final TestoGdx medio;
	private final int larghezza;
	private final int altezza;
	private final List<SpriteGdx> sprites = new ArrayList<>();

	public PaginaDegliSprite(ImmaginiGdx immagini, TestoGdx piccolo, TestoGdx medio, int larghezza, int altezza) {
		this.immagini = immagini;
		this.piccolo = piccolo;
		this.medio = medio;
		this.larghezza = larghezza;
		this.altezza = altezza;
	}

	private Misura misura(String risorsa) {
		return new Misura(immagini.larghezza(risorsa, 1f), immagini.altezza(risorsa, 1f));
	}

	private void avvia() {
		String moneta = "icone/Moneta.png";
		String salute = "icone/PozioneSalute.png";
		// L'ancora a destra, come i riquadri del gruppo: x è il bordo destro del testo
		sprites.add(new SpriteATempoGdx(DescrizioneSpriteATempo.conVariazione(moneta, misura(moneta), 25, medio.getFont(), 200, 300, false, "Monete variate"),
				immagini, medio));
		sprites.add(new SpriteATempoGdx(DescrizioneSpriteATempo.conVariazione(salute, misura(salute), -7, medio.getFont(), 340, 300, true, "Salute variata"),
				immagini, medio));
		// Uno ritagliato: si vede solo la parte dentro l'area, anche se parte da fuori
		SpriteATempoGdx ritagliato = new SpriteATempoGdx(DescrizioneSpriteATempo.conVariazione(moneta, misura(moneta), 3, medio.getFont(), 500, 330, true,
				"Ritagliato"), immagini, medio);
		ritagliato.setRitaglio(new Area(480, 290, 80, 20));
		sprites.add(ritagliato);

		sprites.add(new SpriteEffettoGdx(DescrizioneSpriteEffetto.colorato("Avvelenato", medio.getFont(), DoomdarkColorModel.Color.GREEN, 200, 200)
				.centraEntro(200, 0, larghezza), medio));
		sprites.add(new SpriteEffettoGdx(DescrizioneSpriteEffetto.conColoreRgb("Furtività +5", medio.getFont(), 0xFFFFA040, 420, 200), medio));
		sprites.add(new SpriteEffettoGdx(DescrizioneSpriteEffetto.colorato("Mancato", piccolo.getFont(), DoomdarkColorModel.Color.MEDIUM_GRAY, 600, 200),
				piccolo));

		sprites.add(new SpriteAnnuncioGlobaleGdx(new DescrizioneAnnuncioGlobale("Missione completata", "Il fienile dei goblin", larghezza, altezza, false),
				immagini));

		sprites.add(new SpriteFumettoGdx(new DescrizioneFumetto("Attenti, dietro di voi! Ce n'è un altro, più grosso degli altri.", 160, medio.getFont()),
				DoomdarkColorModel.Color.BLACK, 60, 140, 40, 220, immagini, medio));
		sprites.add(new SpriteFumettoGdx(new DescrizioneFumetto("Grazie, è stato un piacere.", 160, medio.getFont()),
				DoomdarkColorModel.Color.BLACK, 700, 140, 760, 230, immagini, medio));

		sprites.add(new SpriteInDissolvenzaGdx(immagini, "personaggi/Drago.png", 1f, 600, 380));
		sprites.add(new SpriteInDissolvenzaGdx(immagini, "personaggi/Goblin.png", 1f, 700, 380, 0.8f, 1f));
	}

	public void disegna(Batch batch, Camera camera, Viewport viewport) {
		sprites.removeIf(sprite -> !sprite.isAttivo());
		if (sprites.isEmpty()) {
			avvia();
		}
		piccolo.disegna(batch, "Sprite: a tempo, effetto, annuncio, fumetto, dissolvenza (ripartono quando sono finiti)", 20, 20,
				DoomdarkColorModel.Color.LIGHT_GRAY);
		immagini.riempi(batch, 480, 290, 80, 20, new com.badlogic.gdx.graphics.Color(0.3f, 0.3f, 0.3f, 1f));
		for (SpriteGdx sprite : new ArrayList<>(sprites)) {
			sprite.anima(batch, camera, viewport);
		}
	}
}
