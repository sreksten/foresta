package com.threeamigos.foresta.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.List;

import javax.swing.JPanel;

import com.threeamigos.foresta.motore.Azioni;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Gioco;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Un pannello che contiene bottoni con immagine personalizzata.
 * Se la dimensione del pannello non e' sufficiente a contenerli tutti,
 * vengono aggiunti dei bottoni "Precedente" e "Successivo" in testa
 * e in coda alla fila dei bottoni, che servono a scorrere tra le varie
 * scelte. L'evento dei bottoni Precedente e Successivo non viene
 * trasmesso ad un eventuale listener.
 *
 * La classe implementa ComponentListener per il resize.
 */
public class IconPanel extends JPanel implements java.awt.event.ActionListener {

	private static final long serialVersionUID = 1L;
	
	public static final int ORIENTAMENTO_ORIZZONTALE = 0;
	public static final int ORIENTAMENTO_VERTICALE = 1;

	private ImageButton[] bottoni;
	private ImageButton bottonePrec;
	private ImageButton bottoneSucc;
	private int quanteScelte = 0;
	private int saltaPrimi = 0;
	private int orientamento = ORIENTAMENTO_ORIZZONTALE;

	private Image[] copyrightImages = new Image[3];
	private int[] copyrightImagesXOffset= new int[3];
	private int[] copyrightImagesYOffset= new int[3];
	
	public IconPanel(int orientamento) {
		this.orientamento = orientamento;
		inizializza();
	}

	private final void inizializza() {
		setLayout(null);
		if (orientamento == ORIENTAMENTO_ORIZZONTALE) {
			setSize(1, 72);
			bottonePrec = new ImageButton(ImageCache.icone[ClassiIcona.SINISTRA], this, Comando.SINISTRA.ordinal());
			bottoneSucc = new ImageButton(ImageCache.icone[ClassiIcona.DESTRA], this, Comando.DESTRA.ordinal());
		} else {
			bottonePrec = new ImageButton(ImageCache.icone[ClassiIcona.SU], this, Comando.SU.ordinal());
			bottoneSucc = new ImageButton(ImageCache.icone[ClassiIcona.GIU], this, Comando.GIU.ordinal());
			setSize(72, 1);
		}
		setBackground(Color.black);
	}

	public void impostaAzioni() {
		List<Comando> possibiliAzioni = Azioni.getComandi();
		quanteScelte = possibiliAzioni.size();
		bottoni = new ImageButton[quanteScelte];
		int icona;
		for (int i = 0; i < quanteScelte; i++) {
			icona = -1;
			switch (possibiliAzioni.get(i)) {
			case ACCAMPAMENTO:
				icona = ClassiIcona.ACCAMPAMENTO;
				break;
			case ACQUA:
				icona = ClassiIcona.ACQUA;
				break;
			case AIUTO:
				icona = ClassiIcona.AIUTO;
				break;
			case ALCHIMISTA:
				icona = ClassiIcona.ALCHIMISTA;
				break;
			case AMICIZIA:
				icona = ClassiIcona.AMICIZIA;
				break;
			case ANNULLA:
				icona = ClassiIcona.ANNULLA;
				break;
			case ARIA:
				icona = ClassiIcona.ARIA;
				break;
			case BARDO:
				icona = ClassiIcona.BARDO;
				break;
			case CANTASTORIE:
				icona = ClassiIcona.CANTASTORIE;
				break;
			case COMBATTIMENTO:
				icona = ClassiIcona.COMBATTIMENTO;
				break;
			case CORRUZIONE:
				icona = ClassiIcona.CORRUZIONE;
				break;
			case DESTRA:
				icona = ClassiIcona.DESTRA;
				break;
			case ELFA:
				icona = ClassiIcona.ELFA;
				break;
			case ELFO:
				icona = ClassiIcona.ELFO;
				break;
			case ESCI_DA_CITTA:
				icona = ClassiIcona.ESCI_DA_CITTA;
				break;
			case EST:
				icona = ClassiIcona.EST;
				break;
			case FEMMINA:
				icona = ClassiIcona.FEMMINA;
				break;
			case FLOPPY:
				icona = ClassiIcona.FLOPPY;
				break;
			case FORZA:
				icona = ClassiIcona.FORZA;
				break;
			case FUGA:
				icona = ClassiIcona.FUGA;
				break;
			case FULMINE:
				icona = ClassiIcona.FULMINE;
				break;
			case FUOCO:
				icona = ClassiIcona.FUOCO;
				break;
			case GIU:
				icona = ClassiIcona.GIU;
				break;
			case GRANDE_FORZA:
				icona = ClassiIcona.GRANDE_FORZA;
				break;
			case GRUPPO:
				icona = ClassiIcona.GRUPPO;
				break;
			case GUERRIERA:
				icona = ClassiIcona.GUERRIERA;
				break;
			case GUERRIERO:
				icona = ClassiIcona.GUERRIERO;
				break;
			case INCANTESIMO:
				icona = ClassiIcona.INCANTESIMO;
				break;
			case LADRA:
				icona = ClassiIcona.LADRA;
				break;
			case LADRO:
				icona = ClassiIcona.LADRO;
				break;
			case LOCANDA:
				icona = ClassiIcona.LOCANDA;
				break;
			case MAGA:
				icona = ClassiIcona.MAGA;
				break;
			case MAGIA:
				icona = ClassiIcona.MAGIA;
				break;
			case MAGO:
				icona = ClassiIcona.MAGO;
				break;
			case MAPPA:
				icona = ClassiIcona.MAPPA;
				break;
			case MASCHIO:
				icona = ClassiIcona.MASCHIO;
				break;
			case MORTE:
				icona = ClassiIcona.MORTE;
				break;
			case NO:
				icona = ClassiIcona.NO;
				break;
			case NO_INCANTESIMO:
				icona = ClassiIcona.NO_INCANTESIMO;
				break;
			case NORD:
				icona = ClassiIcona.NORD;
				break;
			case NUMERO_1:
				icona = ClassiIcona.NUMERO_1;
				break;
			case NUMERO_2:
				icona = ClassiIcona.NUMERO_2;
				break;
			case NUMERO_3:
				icona = ClassiIcona.NUMERO_3;
				break;
			case NUMERO_4:
				icona = ClassiIcona.NUMERO_4;
				break;
			case NUMERO_5:
				icona = ClassiIcona.NUMERO_5;
				break;
			case OVEST:
				icona = ClassiIcona.OVEST;
				break;
			case PERGAMENA:
				icona = ClassiIcona.PERGAMENA;
				break;
			case PERSONAGGIO_1:
				icona = getIconaPersonaggio(0);
				break;
			case PERSONAGGIO_2:
				icona = getIconaPersonaggio(1);
				break;
			case PERSONAGGIO_3:
				icona = getIconaPersonaggio(2);
				break;
			case PERSONAGGIO_4:
				icona = getIconaPersonaggio(3);
				break;
			case PERSONAGGIO_5:
				icona = getIconaPersonaggio(4);
				break;
			case RESURREZIONE:
				icona = ClassiIcona.RESURREZIONE;
				break;
			case RUTTOLOMEO:
				icona = ClassiIcona.RUTTOLOMEO;
				break;
			case SI:
				icona = ClassiIcona.SI;
				break;
			case SINGOLO:
				icona = ClassiIcona.SINGOLO;
				break;
			case SINISTRA:
				icona = ClassiIcona.SINISTRA;
				break;
			case STORPSGORBLIN:
				icona = ClassiIcona.STORPSGORBLIN;
				break;
			case SU:
				icona = ClassiIcona.SU;
				break;
			case SUD:
				icona = ClassiIcona.SUD;
				break;
			case TERRA:
				icona = ClassiIcona.TERRA;
				break;
			default:
				break;
			}
			bottoni[i] = new ImageButton(ImageCache.icone[icona], this, possibiliAzioni.get(i).ordinal());
		}
		saltaPrimi = 0;
		ridistribuisciScelte();
	}

	private int getIconaPersonaggio(int ordinale) {
		Personaggio personaggio = GruppoGiocatore.getIstanza().getPersonaggio(ordinale);
		if (personaggio == null)
			return -1;
		switch (personaggio.getClasse()) {
		case CENTAURO:
			return ClassiIcona.CENTAURO;
		case EREMITA:
			return ClassiIcona.EREMITA;
		case GIGANTE:
			return ClassiIcona.GIGANTE;
		case GOBLIN:
			return ClassiIcona.GOBLIN;
		case HOBGOBLIN:
			return ClassiIcona.HOBGOBLIN;
		case MINOTAURO:
			return ClassiIcona.MINOTAURO;
		case TITANO:
			return ClassiIcona.TITANO;
		case GUERRIERA:
			return ClassiIcona.GUERRIERA;
		case GUERRIERO:
			return ClassiIcona.GUERRIERO;
		case LADRA:
			return ClassiIcona.LADRA;
		case LADRO:
			return ClassiIcona.LADRO;
		case CANTASTORIE:
			return ClassiIcona.CANTASTORIE;
		case BARDO:
			return ClassiIcona.BARDO;
		case ELFA:
			return ClassiIcona.ELFA;
		case ELFO:
			return ClassiIcona.ELFO;
		case MAGA:
			return ClassiIcona.MAGA;
		case MAGO:
			return ClassiIcona.MAGO;
		case OMBRAFIAMMA:
			return ClassiIcona.OMBRAFIAMMA;
		default:
			throw new IllegalArgumentException();
		}
	}

	private final void ridistribuisciScelte() {
		if (bottoni == null) {
			return;
		}
		removeAll();

		Dimension d = getSize();
		int width = d.width;
		int height = d.height;
		int iconePossibili; // il numero di icone che entrano nella finestra
		int iconeDaVisualizzare; // quante icone stampare escluso prec/succ
		if (orientamento == ORIENTAMENTO_ORIZZONTALE)
			iconePossibili = width / 66;
		else
			iconePossibili = height / 66;
		boolean precedente = false;
		boolean successivo = false; // deve stampare sin/su o des/giu?
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
		if (orientamento == ORIENTAMENTO_ORIZZONTALE) {
			int offset = (width - totaleIcone * 62) >> 1;
				int offsetVerticale = height - ImageCache.icone[0].getHeight() >> 1;
				ImageButton b;
				if (precedente) {
					b = bottonePrec;
					b.setLocation(offset, offsetVerticale);
					add(b);
					b.repaint();
					offset += 62;
				}
				for (int i = 0; i < bottoni.length; i++) {
					if (iconeDaSaltare > 0) {
						iconeDaSaltare--;
					} else {
						b = bottoni[i];
						b.setLocation(offset, offsetVerticale);
						add(b);
						b.repaint();
						offset += 62; // 31 + 2 pixel vuoti di spazio
						iconeDaVisualizzare--;
						if (iconeDaVisualizzare == 0)
							break;
					}
				}
				if (successivo) {
					b = bottoneSucc;
					b.setLocation(offset, offsetVerticale);
					add(b);
					b.repaint();
				}
		} else { // ORIENTAMENTO_VERTICALE
			int offset = (height - totaleIcone * 64 - 2 * (totaleIcone - 1)) >> 1;
			ImageButton b = null;
			if (precedente) {
				b = bottonePrec;
				b.setLocation(2, offset);
				add(b);
				b.repaint();
				offset += 32;
			}
			for (int i = 0; i < bottoni.length; i++) {
				if (iconeDaSaltare > 0) {
					iconeDaSaltare--;
				} else {
					b = bottoni[i];
					b.setLocation(2, offset);
					add(b);
					b.repaint();
					offset += 32; // 32 + 2 pixel vuoti di spazio
					iconeDaVisualizzare--;
					if (iconeDaVisualizzare == 0)
						break;
				}
			}
			if (successivo) {
				b = bottoneSucc;
				b.setLocation(2, offset);
				add(b);
				b.repaint();
			}
		}
		repaint();
	}

	// ActionListener interface
	public void actionPerformed(java.awt.event.ActionEvent e) {
		Comando azione = Comando.of(e.getID());
		if (azione == Comando.SU || azione == Comando.SINISTRA) {
			saltaPrimi--;
			if (saltaPrimi == 1)
				saltaPrimi = 0;
			ridistribuisciScelte();
		} else if (azione == Comando.GIU || azione == Comando.DESTRA) {
			saltaPrimi++;
			if (saltaPrimi == 1)
				saltaPrimi = 2;
			ridistribuisciScelte();
		} else {
			Gioco.processaAzione(azione);
		}
	}

	@Override
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		copyright((Graphics2D)graphics);
	}

	
	
	private final void copyright(Graphics2D graphics) {
		if (copyrightImages[0] == null) {
			DoomdarkFont fontSmall = DoomdarkFontSmall.getInstance();
			copyrightImages[0] = DoomdarkTextProducer.getImage(
					"La Foresta",
					fontSmall,
					DoomdarkColorModel.Color.VERY_DARK_GRAY);
			copyrightImages[1] = DoomdarkTextProducer.getImage(
					"copyright 1984-2019",
					fontSmall,
					DoomdarkColorModel.Color.VERY_DARK_GRAY);
			copyrightImages[2] = DoomdarkTextProducer.getImage(
					"Stefano Reksten",
					fontSmall,
					DoomdarkColorModel.Color.VERY_DARK_GRAY);
			for (int i = 0; i < 3; i++) {
				copyrightImagesXOffset[i] = getWidth() - copyrightImages[i].getWidth(null) - 1;
				copyrightImagesYOffset[i] = getHeight() - fontSmall.getHeight() * (3 - i) - 1;
			}
		}
		for (int i = 0; i < 3; i++) {
			graphics.drawImage(copyrightImages[i], copyrightImagesXOffset[i], copyrightImagesYOffset[i], null);
		}
	}
}
