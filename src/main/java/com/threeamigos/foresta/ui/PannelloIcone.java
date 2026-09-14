package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoComandoDiGioco;
import com.threeamigos.foresta.motore.ComandiPossibili;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Un pannello che contiene bottoni con immagine personalizzata.
 * Se la dimensione del pannello non è sufficiente a contenerli tutti,
 * vengono aggiunti dei bottoni "Precedente" e "Successivo" in testa
 * e in coda alla fila dei bottoni, che servono a scorrere tra le varie
 * scelte. L'evento dei bottoni Precedente e Successivo non viene
 * trasmesso a un eventuale listener.
 * <p>
 * La classe implementa ComponentListener per il resize.
 */
public class PannelloIcone extends JPanel implements java.awt.event.ActionListener {

	private static final long serialVersionUID = 1L;
	
	public static final int ORIENTAMENTO_ORIZZONTALE = 0;
	public static final int ORIENTAMENTO_VERTICALE = 1;

	private final int orientamento;
	private final Image[] copyrightImages = new Image[3];
	private final int[] copyrightImagesXOffset= new int[3];
	private final int[] copyrightImagesYOffset= new int[3];

	private ImageButton[] bottoni;
	private ImageButton bottonePrecedente;
	private ImageButton bottoneSuccessivo;
	private int quanteScelte = 0;
	private int saltaPrimi = 0;

	public PannelloIcone(int orientamento) {
		this.orientamento = orientamento;
		inizializza();
	}

	private void inizializza() {
		setLayout(null);
		if (orientamento == ORIENTAMENTO_ORIZZONTALE) {
			setSize(1, 72);
			bottonePrecedente = new ImageButton(ClasseIcona.SINISTRA.getIcona(), this, Comando.SINISTRA.ordinal());
			bottoneSuccessivo = new ImageButton(ClasseIcona.DESTRA.getIcona(), this, Comando.DESTRA.ordinal());
		} else {
			bottonePrecedente = new ImageButton(ClasseIcona.SU.getIcona(), this, Comando.SU.ordinal());
			bottoneSuccessivo = new ImageButton(ClasseIcona.GIU.getIcona(), this, Comando.GIU.ordinal());
			setSize(72, 1);
		}
		setBackground(Color.black);
	}

	public void impostaAzioni() {
		List<Comando> possibiliAzioni = ComandiPossibili.getComandi();
		quanteScelte = possibiliAzioni.size();
		bottoni = new ImageButton[quanteScelte];
		ClasseIcona classeIcona;
		for (int i = 0; i < quanteScelte; i++) {

			Comando comando = possibiliAzioni.get(i);

			if (comando == null) {
				throw new IllegalArgumentException("Comando nullo");
			}

			if (comando == Comando.PERSONAGGIO_1 || comando == Comando.PERSONAGGIO_2 ||
					comando == Comando.PERSONAGGIO_3 || comando == Comando.PERSONAGGIO_4 ||
					comando == Comando.PERSONAGGIO_5) {
				classeIcona = getIconaPersonaggio(comando.ordinal() - Comando.PERSONAGGIO_1.ordinal());
			} else {
				classeIcona = ClasseIcona.ofComando(comando);
			}
			bottoni[i] = new ImageButton(classeIcona.getIcona(), this, possibiliAzioni.get(i).ordinal());
		}
		saltaPrimi = 0;
		ridistribuisciScelte();
	}

	private ClasseIcona getIconaPersonaggio(int indice) {
		Personaggio personaggio = GruppoGiocatore.getIstanza().getPersonaggio(indice);
		if (personaggio == null) {
			throw new IllegalStateException("Personaggio non trovato con indice " + indice);
		}
		return ClasseIcona.ofClasse(personaggio.getClasse());
	}

	private void ridistribuisciScelte() {
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
				int offsetVerticale = height - ClasseIcona.getAltezzaMassima() >> 1;
				ImageButton b;
				if (precedente) {
					b = bottonePrecedente;
					b.setLocation(offset, offsetVerticale);
					add(b);
					b.repaint();
					offset += 62;
				}
            for (ImageButton imageButton : bottoni) {
                if (iconeDaSaltare > 0) {
                    iconeDaSaltare--;
                } else {
                    b = imageButton;
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
				b = bottoneSuccessivo;
				b.setLocation(offset, offsetVerticale);
				add(b);
				b.repaint();
			}
		} else { // ORIENTAMENTO_VERTICALE
			int offset = (height - totaleIcone * 64 - 2 * (totaleIcone - 1)) >> 1;
			ImageButton b = null;
			if (precedente) {
				b = bottonePrecedente;
				b.setLocation(2, offset);
				add(b);
				b.repaint();
				offset += 32;
			}
            for (ImageButton imageButton : bottoni) {
                if (iconeDaSaltare > 0) {
                    iconeDaSaltare--;
                } else {
                    b = imageButton;
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
				b = bottoneSuccessivo;
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
			BusEventi.pubblica(new EventoComandoDiGioco(azione));
		}
	}

	@Override
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		copyright((Graphics2D)graphics);
	}

	private void copyright(Graphics2D graphics) {
		if (copyrightImages[0] == null) {
			DoomdarkFont fontSmall = DoomdarkFontSmall.getInstance();
			copyrightImages[0] = DoomdarkTextProducer.getImage(
					"La Foresta",
					fontSmall,
					DoomdarkColorModel.Color.VERY_DARK_GRAY);
			copyrightImages[1] = DoomdarkTextProducer.getImage(
					"copyright 1984-2026",
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
