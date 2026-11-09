package com.threeamigos.foresta.libgdx;

import com.threeamigos.foresta.ui.schermate.SchermateIntroOutro;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.threeamigos.foresta.interfacce.GestorePunteggi;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.strumenti.TestataSalvataggio;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ColonnaScorrevole;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna.Elemento;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.schermate.DisposizioneDiSchermata;
import com.threeamigos.foresta.ui.schermate.IntroDelGioco;
import com.threeamigos.foresta.ui.schermate.MisureDelleImmagini;
import com.threeamigos.foresta.ui.schermate.SchermateSenzaPartita;

import java.util.Collection;

/**
 * Le schermate senza partita in corso in libGDX: l'introduzione (storia che scorre, loghi, classifica, trofei), i messaggi su sfondo con l'ombra del drago, le
 * statistiche di fine partita, la scelta dello slot di salvataggio, la sconfitta e la vittoria. Che cosa mostrano lo dicono {@link SchermateSenzaPartita} (le
 * schermate ferme) e {@link IntroDelGioco} (l'intro, che dipende dal tempo), nel modulo comune; qui si disegna.
 */
public final class SchermataIntroOutroGdx implements SchermateIntroOutro {

	private static final String OMBRA_DEL_DRAGO = "fondi/OmbraDelDrago.png";
	private static final String TRIONFO = "fondi/Trionfo.png";
	private static final String SFONDO_STORIA = "fondinon2x2/SfondoStoria.png";

	private final int width;
	private final int height;
	private final ImmaginiGdx immagini;
	private final DisegnoDiContenutoGdx disegno;
	private final SchermateSenzaPartita schermate;
	private final IntroDelGioco intro;
	private final GestorePunteggi gestorePunteggi;
	private final float zoom = (float) LivelloDiZoom.valore();

	public SchermataIntroOutroGdx(int width, int height, GestorePunteggi gestorePunteggi, VistaPartita vistaPartita, ImmaginiGdx immagini, TestoGdx piccolo,
								  TestoGdx medio) {
		this.width = width;
		this.height = height;
		this.immagini = immagini;
		this.gestorePunteggi = gestorePunteggi;
		this.disegno = new DisegnoDiContenutoGdx(immagini, piccolo, medio);
		MisureDelleImmagini misure = new MisureDelleImmagini() {
			@Override
			public Misura personaggio(TipoPersonaggio classe) {
				TextureRegion regione = immagini.regione(NomiDelleImmagini.personaggio(classe));
				return new Misura(Math.round(regione.getRegionWidth() * zoom), Math.round(regione.getRegionHeight() * zoom));
			}

			@Override
			public Misura immagine(String risorsa) {
				return new Misura(immagini.larghezza(risorsa, 1f), immagini.altezza(risorsa, 1f));
			}
		};
		this.schermate = new SchermateSenzaPartita(width, height, vistaPartita, misure);
		this.intro = new IntroDelGioco(width, height, gestorePunteggi, vistaPartita, misure);
		// I loghi con l'alone si preparano ora, prima che l'intro parta: se no costano uno scatto quando compaiono
		for (Elemento logo : IntroDelGioco.loghiDaPrecaricare()) {
			disegno.precarica(logo);
		}
	}

	public void resettaSequenza() {
		schermate.resettaSequenza();
		intro.resettaSequenza();
	}

	public void posizionaSuPunteggi() {
		intro.posizionaSuPunteggi();
	}

	public void incrementaSequenza(int lunghezzaMassima) {
		schermate.incrementaSequenza(lunghezzaMassima);
	}

	public void impostaMessaggio(String messaggio) {
		schermate.impostaMessaggio(messaggio);
	}

	public void setSalvataggiDisponibili(Collection<TestataSalvataggio> salvataggiDisponibili) {
		schermate.setSalvataggiDisponibili(salvataggiDisponibili);
	}

	private void disegnaOmbraDelDrago(Batch batch, float opacita) {
		Color precedente = new Color(batch.getColor());
		batch.setColor(1f, 1f, 1f, opacita);
		immagini.disegna(batch, OMBRA_DEL_DRAGO, (width - immagini.larghezza(OMBRA_DEL_DRAGO, zoom)) >> 1, (height - immagini.altezza(OMBRA_DEL_DRAGO, zoom)) >> 1,
				zoom);
		batch.setColor(precedente);
	}

	/**
	 * Disegna una schermata ferma: lo sfondo e gli elementi
	 */
	public void disegna(Batch batch, DisposizioneDiSchermata schermata) {
		switch (schermata.getSfondo()) {
			case OMBRA_DEL_DRAGO:
				disegnaOmbraDelDrago(batch, 1f);
				break;
			case TRIONFO:
				immagini.disegna(batch, TRIONFO, (width - immagini.larghezza(TRIONFO, zoom)) / 2, (height - immagini.altezza(TRIONFO, zoom)) / 2, zoom);
				break;
			default:
				break;
		}
		disegno.disegna(batch, schermata.getContenuto(), 0, 0, 1f);
	}

	/** L'ombra del drago, a tutto schermo: lo sfondo delle pagine degli intermezzi senza sfondo */
	public void ombraDelDrago(Batch batch) {
		disegnaOmbraDelDrago(batch, 1f);
	}

	/** Testo centrato in alto, con a capo automatico, nell'alfabeto grande: per chi compone una schermata attorno al testo (gli intermezzi) */
	public void testoCentrato(Batch batch, String testo) {
		disegna(batch, schermate.testo(testo));
	}

	public void scrivi(Batch batch, boolean conOmbraDelDrago) {
		disegna(batch, schermate.scrivi(conOmbraDelDrago));
	}

	public void statistiche(Batch batch) {
		disegna(batch, schermate.statistiche());
	}

	public void selezioneSlotDaCaricare(Batch batch) {
		disegna(batch, schermate.selezioneSlotDaCaricare());
	}

	public void selezioneSlotDaSalvare(Batch batch) {
		disegna(batch, schermate.selezioneSlotDaSalvare());
	}

	public void confermaUscita(Batch batch) {
		disegna(batch, schermate.confermaUscita());
	}

	public void perso(Batch batch) {
		disegna(batch, schermate.perso());
	}

	public void vinto(Batch batch) {
		disegna(batch, schermate.vinto());
	}

	public void hiscore(Batch batch) {
		disegna(batch, schermate.hiscore(gestorePunteggi));
	}

	/**
	 * L'intro, un fotogramma: la storia sale sullo sfondo della storia, e il logo della Foresta la segue e si ferma al suo posto mentre la storia continua a
	 * salire; allora compare anche il 3AM, poi loghi e sfondo spariscono insieme; la classifica sale e si ferma, poi sparisce; i trofei scorrono tutti. Poi si
	 * ricomincia.
	 */
	public void intro(Batch batch) {
		IntroDelGioco.Quadro quadro = intro.avanza();
		if (quadro.isOmbraDelDrago()) {
			disegnaOmbraDelDrago(batch, 1f);
		}
		if (quadro.getOpacitaDelloSfondoDellaStoria() > 0) {
			Color precedente = new Color(batch.getColor());
			batch.setColor(1f, 1f, 1f, quadro.getOpacitaDelloSfondoDellaStoria());
			immagini.disegna(batch, SFONDO_STORIA, (width - immagini.larghezza(SFONDO_STORIA, 1f)) >> 1, (height - immagini.altezza(SFONDO_STORIA, 1f)) >> 1, 1f);
			batch.setColor(precedente);
		}
		disegnaLaColonna(batch, quadro);
		for (IntroDelGioco.Logo logo : quadro.getLoghi()) {
			disegno.disegnaElemento(batch, logo.getElemento(), logo.getOpacita());
		}
	}

	/**
	 * La colonna che scorre, sfumata ai bordi dell'area: un elemento piccolo ha l'opacità del suo centro, un testo grande molto alto (la storia) si disegna a
	 * fette, ognuna con la sua
	 */
	private void disegnaLaColonna(Batch batch, IntroDelGioco.Quadro quadro) {
		ColonnaScorrevole colonna = quadro.getColonna();
		ContenutoDiColonna contenuto = quadro.getContenuto();
		Area area = colonna.getArea();
		int x0 = area.getX() + (area.getLarghezza() - contenuto.getLarghezza()) / 2;
		int y0 = area.getY() + (int) Math.round(colonna.getY());
		for (Elemento e : contenuto.getElementi()) {
			int y = y0 + e.getY();
			if (e.getTipo() == Elemento.Tipo.TESTO_GRANDE && e.getAltezza() > 2 * IntroDelGioco.FASCIA) {
				disegnaAFette(batch, colonna, e, x0 + e.getX(), y, quadro.getOpacitaDellaColonna());
				continue;
			}
			if (y + e.getAltezza() < area.getY() || y > area.getY() + area.getAltezza()) {
				continue;
			}
			int centro = y + e.getAltezza() / 2 - area.getY();
			float opacita = colonna.opacitaAllaQuota(Math.max(0, Math.min(area.getAltezza() - 1, centro))) * quadro.getOpacitaDellaColonna();
			disegno.disegnaElemento(batch, e, x0 + e.getX(), y, opacita);
		}
	}

	private void disegnaAFette(Batch batch, ColonnaScorrevole colonna, Elemento e, int x, int y, float opacitaDellaColonna) {
		TextureRegion regione = disegno.testoGrande(e);
		Texture texture = regione.getTexture();
		Area area = colonna.getArea();
		int altezza = e.getAltezza();
		Color precedente = new Color(batch.getColor());
		for (int riga = 0; riga < altezza; riga += 2) {
			int fine = Math.min(altezza, riga + 2);
			int yFetta = y + riga;
			if (yFetta + 2 < area.getY() || yFetta > area.getY() + area.getAltezza()) {
				continue;
			}
			float opacita = colonna.opacitaAllaQuota(Math.max(0, Math.min(area.getAltezza() - 1, yFetta + 1 - area.getY()))) * opacitaDellaColonna;
			if (opacita <= 0f) {
				continue;
			}
			// La regione è capovolta: per la cima si usa v2 e per il fondo v (vedi SpriteBatch.draw con una TextureRegion)
			float vCima = regione.getV2() + (regione.getV() - regione.getV2()) * riga / altezza;
			float vFondo = regione.getV2() + (regione.getV() - regione.getV2()) * fine / altezza;
			batch.setColor(1f, 1f, 1f, opacita);
			batch.draw(texture, x, yFetta, e.getLarghezza(), fine - riga, regione.getU(), vCima, regione.getU2(), vFondo);
		}
		batch.setColor(precedente);
	}
}
