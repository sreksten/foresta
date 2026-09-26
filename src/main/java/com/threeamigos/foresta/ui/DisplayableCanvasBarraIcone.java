package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoDiGioco;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.ComandiPossibili;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * La barra delle icone di comando: sostituisce le vecchie PannelloIcone/ImageButton,
 * veri componenti Swing che convivevano male con l'animazione continua di
 * DisplayableCanvas (vedi DisplayableCanvas.run). Se la fascia non è sufficiente a
 * contenere tutte le icone, vengono aggiunte icone "Precedente" e "Successivo" in
 * testa e in coda, che servono a scorrere tra le varie scelte.
 */
class DisplayableCanvasBarraIcone implements Finestra {

	static final int ICONA_WIDTH = 62;
	static final int ICONA_HEIGHT = 64;

	static class IconaVisibile {
		final Rectangle rettangolo;
		final Comando comando;
		final BufferedImage icona;

		IconaVisibile(Rectangle rettangolo, Comando comando, BufferedImage icona) {
			this.rettangolo = rettangolo;
			this.comando = comando;
			this.icona = icona;
		}
	}

	private final int orientamento;
	final int offsetX;
	final int offsetY;
	final int larghezza;
	final int altezza;

	private final Comando comandoPrecedente;
	private final BufferedImage iconaPrecedente;
	private final Comando comandoSuccessivo;
	private final BufferedImage iconaSuccessivo;

	private final Image[] copyrightImages = new Image[3];
	private final int[] copyrightImagesXOffset = new int[3];
	private final int[] copyrightImagesYOffset = new int[3];

	private Comando[] comandi = new Comando[0];
	private BufferedImage[] icone = new BufferedImage[0];
	private int saltaPrimi = 0;

	final List<IconaVisibile> iconeVisibili = new ArrayList<>();

	int mouseX = -1;
	int mouseY = -1;
	boolean mousePremuto = false;

	DisplayableCanvasBarraIcone(int orientamento, int offsetX, int offsetY, int larghezza, int altezza) {
		this.orientamento = orientamento;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.larghezza = larghezza;
		this.altezza = altezza;

		if (orientamento == DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE) {
			comandoPrecedente = Comando.SINISTRA;
			iconaPrecedente = ClasseIcona.SINISTRA.getIcona();
			comandoSuccessivo = Comando.DESTRA;
			iconaSuccessivo = ClasseIcona.DESTRA.getIcona();
		} else {
			comandoPrecedente = Comando.SU;
			iconaPrecedente = ClasseIcona.SU.getIcona();
			comandoSuccessivo = Comando.GIU;
			iconaSuccessivo = ClasseIcona.GIU.getIcona();
		}

		DoomdarkFont fontSmall = DoomdarkFontSmall.getInstance();
		DoomdarkColorModel.Color color = DoomdarkColorModel.Color.VERY_DARK_GRAY;
		copyrightImages[0] = DoomdarkTextProducer.getImage("La Foresta", fontSmall, color);
		copyrightImages[1] = DoomdarkTextProducer.getImage("copyright 1984-2026", fontSmall, color);
		copyrightImages[2] = DoomdarkTextProducer.getImage("Stefano Reksten", fontSmall, color);
		for (int i = 0; i < 3; i++) {
			copyrightImagesXOffset[i] = larghezza - copyrightImages[i].getWidth(null) - 1;
			copyrightImagesYOffset[i] = altezza - fontSmall.getHeight() * (3 - i) - 1;
		}
	}

	void impostaAzioni() {
		List<Comando> possibiliAzioni = ComandiPossibili.getComandi();
		int quanteScelte = possibiliAzioni.size();
		comandi = new Comando[quanteScelte];
		icone = new BufferedImage[quanteScelte];
		for (int i = 0; i < quanteScelte; i++) {
			Comando comando = possibiliAzioni.get(i);
			if (comando == null) {
				throw new IllegalArgumentException("Comando nullo");
			}
			comandi[i] = comando;
			icone[i] = getIcona(comando);
		}
		saltaPrimi = 0;
		ridistribuisciScelte();
	}

	private BufferedImage getIcona(Comando comando) {
		if (comando == Comando.PERSONAGGIO_1 || comando == Comando.PERSONAGGIO_2 ||
				comando == Comando.PERSONAGGIO_3 || comando == Comando.PERSONAGGIO_4 ||
				comando == Comando.PERSONAGGIO_5) {
			return getIconaPersonaggio(comando.ordinal() - Comando.PERSONAGGIO_1.ordinal()).getIcona();
		}
		return ClasseIcona.ofComando(comando).getIcona();
	}

	private ClasseIcona getIconaPersonaggio(int indice) {
		Personaggio personaggio = GruppoGiocatore.getIstanza().getPersonaggio(indice);
		if (personaggio == null) {
			throw new IllegalStateException("Personaggio non trovato con indice " + indice);
		}
		return ClasseIcona.ofClasse(personaggio.getClasse());
	}

	private void ridistribuisciScelte() {
		iconeVisibili.clear();

		int quanteScelte = comandi.length;
		int iconePossibili;
		if (orientamento == DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE) {
			iconePossibili = larghezza / 66;
		} else {
			iconePossibili = altezza / 66;
		}
		int iconeDaVisualizzare;
		boolean precedente = false;
		boolean successivo = false;
		if (iconePossibili >= quanteScelte) {
			iconeDaVisualizzare = quanteScelte;
			saltaPrimi = 0;
		} else {
			iconeDaVisualizzare = iconePossibili - 1;
			if (saltaPrimi > 0) {
				precedente = true;
				if (iconeDaVisualizzare + saltaPrimi < quanteScelte) {
					successivo = true;
					iconeDaVisualizzare = iconePossibili - 2;
				}
			} else {
				successivo = true;
			}
		}
		int totaleIcone = iconeDaVisualizzare + (precedente ? 1 : 0) + (successivo ? 1 : 0);
		int iconeDaSaltare = saltaPrimi;

		if (orientamento == DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE) {
			int offset = (larghezza - totaleIcone * 62) >> 1;
			int offsetVerticale = (altezza - ClasseIcona.getAltezzaMassima()) >> 1;
			if (precedente) {
				iconeVisibili.add(new IconaVisibile(new Rectangle(offset, offsetVerticale, ICONA_WIDTH, ICONA_HEIGHT), comandoPrecedente, iconaPrecedente));
				offset += 62;
			}
			for (int i = 0; i < quanteScelte; i++) {
				if (iconeDaSaltare > 0) {
					iconeDaSaltare--;
					continue;
				}
				iconeVisibili.add(new IconaVisibile(new Rectangle(offset, offsetVerticale, ICONA_WIDTH, ICONA_HEIGHT), comandi[i], icone[i]));
				offset += 62; // 31 + 2 pixel vuoti di spazio
				iconeDaVisualizzare--;
				if (iconeDaVisualizzare == 0) {
					break;
				}
			}
			if (successivo) {
				iconeVisibili.add(new IconaVisibile(new Rectangle(offset, offsetVerticale, ICONA_WIDTH, ICONA_HEIGHT), comandoSuccessivo, iconaSuccessivo));
			}
		} else {
			int offset = (altezza - totaleIcone * 64 - 2 * (totaleIcone - 1)) >> 1;
			if (precedente) {
				iconeVisibili.add(new IconaVisibile(new Rectangle(2, offset, ICONA_WIDTH, ICONA_HEIGHT), comandoPrecedente, iconaPrecedente));
				offset += 32;
			}
			for (int i = 0; i < quanteScelte; i++) {
				if (iconeDaSaltare > 0) {
					iconeDaSaltare--;
					continue;
				}
				iconeVisibili.add(new IconaVisibile(new Rectangle(2, offset, ICONA_WIDTH, ICONA_HEIGHT), comandi[i], icone[i]));
				offset += 32; // 32 + 2 pixel vuoti di spazio
				iconeDaVisualizzare--;
				if (iconeDaVisualizzare == 0) {
					break;
				}
			}
			if (successivo) {
				iconeVisibili.add(new IconaVisibile(new Rectangle(2, offset, ICONA_WIDTH, ICONA_HEIGHT), comandoSuccessivo, iconaSuccessivo));
			}
		}
	}

	void disegna(Graphics2D graphics) {
		graphics.setColor(Color.black);
		graphics.fillRect(offsetX, offsetY, larghezza, altezza);
		for (IconaVisibile iconaVisibile : iconeVisibili) {
			Rectangle r = iconaVisibile.rettangolo;
			int x = offsetX + r.x;
			int y = offsetY + r.y;
			graphics.drawImage(iconaVisibile.icona, x, y, null);
			disegnaBordo(graphics, r, x, y);
		}
		copyright(graphics);
	}

	private void disegnaBordo(Graphics2D graphics, Rectangle localRect, int x, int y) {
		if (!localRect.contains(mouseX, mouseY)) {
			return;
		}
		int sx = x + ICONA_WIDTH - 1;
		int sy = y + ICONA_HEIGHT - 1;
		graphics.setColor(mousePremuto ? Color.white : Color.lightGray);
		graphics.drawLine(x, y, sx, y);
		graphics.drawLine(x, y, x, sy);
		graphics.drawLine(x, sy, sx, sy);
		graphics.drawLine(sx, sy, sx, y);
	}

	void copyright(Graphics2D graphics) {
		for (int i = 0; i < 3; i++) {
			graphics.drawImage(copyrightImages[i], offsetX + copyrightImagesXOffset[i], offsetY + copyrightImagesYOffset[i], null);
		}
	}

	/**
	 * Se il punto (relativo all'angolo della barra) spetta alla barra: qui il suo rettangolo, ma una barra le cui
	 * icone escono dal rettangolo (vedi DisplayableCanvasBarraIconeDock) può reclamare anche i punti fuori.
	 */
	boolean contiene(int x, int y) {
		return x >= 0 && y >= 0 && x < larghezza && y < altezza;
	}

	/**
	 * Se la barra sta animando qualcosa da sola, anche a mouse fermo: allora DisplayableCanvas la ridisegna a ogni
	 * fotogramma. La barra classica non anima mai.
	 */
	boolean inTransizione() {
		return false;
	}

	@Override
	public void processaMovimento(int x, int y) {
		mouseX = x;
		mouseY = y;
	}

	@Override
	public void processaUscita(int x, int y) {
		mouseX = -1;
		mouseY = -1;
		mousePremuto = false;
	}

	@Override
	public void processaPressione(int x, int y, Tasto tasto) {
		if (tasto == Tasto.SINISTRO) {
			mouseX = x;
			mouseY = y;
			mousePremuto = true;
		}
	}

	@Override
	public void processaRilascio(int x, int y, Tasto tasto) {
		if (tasto == Tasto.SINISTRO) {
			mousePremuto = false;
		}
	}

	@Override
	public void processaClick(int x, int y, Tasto tasto) {
		if (tasto != Tasto.SINISTRO) {
			return;
		}
		for (IconaVisibile iconaVisibile : iconeVisibili) {
			if (iconaVisibile.rettangolo.contains(x, y)) {
				esegui(iconaVisibile.comando);
				return;
			}
		}
	}

	/**
	 * Il click su un'icona: le frecce scorrono le scelte, le altre mandano il comando al gioco.
	 */
	void esegui(Comando azione) {
		if (azione == Comando.SU || azione == Comando.SINISTRA) {
			saltaPrimi--;
			if (saltaPrimi == 1) {
				saltaPrimi = 0;
			}
			ridistribuisciScelte();
		} else if (azione == Comando.GIU || azione == Comando.DESTRA) {
			saltaPrimi++;
			if (saltaPrimi == 1) {
				saltaPrimi = 2;
			}
			ridistribuisciScelte();
		} else {
			BusEventi.pubblica(new ComandoDiGioco(azione));
		}
	}
}
