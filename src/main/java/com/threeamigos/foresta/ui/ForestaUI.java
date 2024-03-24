package com.threeamigos.foresta.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.Gioco;
import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.personaggi.Personaggio;

public class ForestaUI implements InterfacciaUtente {

	private Orientamento orientamento = Orientamento.VERTICALE;
	private boolean tuttoSchermo;

	private JFrame jframe;
	private Prompt prompt;
	private DisplayableCanvas displayableCanvas;
	private IconPanel iconPanel;

	public ForestaUI(Orientamento orientamento, boolean tuttoSchermo) {
		this.orientamento = orientamento;
		this.tuttoSchermo = tuttoSchermo;
		SwingUtilities.invokeLater(this::createAndShowGUI);
	}
	
	private void createAndShowGUI() {
		ImageCache.init();

		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		jframe = new JFrame("La Foresta");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		int width;
		int height;
		if (tuttoSchermo) {
			width = screenDimension.width;
			height = screenDimension.height;
		} else {
			if (orientamento == Orientamento.VERTICALE) {
				width = ImageCache.SPACING +
						ImageCache.corniceMappa.getWidth() +
						ImageCache.SPACING + 
						ImageCache.locazioni.get(ClassiLocazione.BOSCO).getWidth() +
						ImageCache.SPACING +
						ImageCache.corniceGrande.getWidth() +
						ImageCache.SPACING;
				if (screenDimension.width < width) {
					width = screenDimension.width;
				}
				height = ImageCache.SPACING +
						ImageCache.corniceGrande.getHeight() +
						ImageCache.SPACING +
						ImageCache.corniceIncantesimi.getHeight() +
						ImageCache.SPACING +
						ImageCache.corniceGrande.getHeight() +
						ImageCache.SPACING +
						ImageCache.icone[0].getHeight() +
						ImageCache.SPACING;
				if (screenDimension.height< width) {
					height = screenDimension.height;
				}
			} else {
				width = screenDimension.width < 640 ? screenDimension.width : 640;
				height = screenDimension.height < 200 ? screenDimension.height : 200;
			}
		}

		jframe.setLayout(null);
		Container c = jframe.getContentPane();
		c.setBackground(Color.BLACK);
		c.setPreferredSize(new Dimension(width, height));

		prompt = new Prompt();
		jframe.add(prompt);
		prompt.setLocation((width - prompt.getSize().width) / 2, (height - prompt.getSize().height) / 2);

		Logger.log("Orientamento: " + orientamento);
		int altezzaIconPanel = 72;			
		if (orientamento == Orientamento.VERTICALE) {
			displayableCanvas = new DisplayableCanvas(width, height - altezzaIconPanel);
			jframe.add(displayableCanvas);
			displayableCanvas.setLocation(0, 0);
			iconPanel = new IconPanel(IconPanel.ORIENTAMENTO_ORIZZONTALE);
			jframe.add(iconPanel);
			iconPanel.setSize(width, altezzaIconPanel);
			iconPanel.setLocation(0, height - altezzaIconPanel - 1);
		} else {
			displayableCanvas = new DisplayableCanvas(320, height);
			jframe.add(displayableCanvas);
			displayableCanvas.setLocation(0, 0);
			iconPanel = new IconPanel(IconPanel.ORIENTAMENTO_VERTICALE);
			jframe.add(iconPanel);
			iconPanel.setSize(altezzaIconPanel, height);
			iconPanel.setLocation(width - altezzaIconPanel - 1, 0);
		}

		jframe.pack();
		jframe.setResizable(false);		
		jframe.setLocation((screenDimension.width - jframe.getSize().width) / 2, (screenDimension.height - jframe.getSize().height) / 2);
		jframe.setVisible(true);
		UI.setInterfacciaUtentePronta();
	}

	public void reinizializza() {
		displayableCanvas.reinizializza();
	}

	public void intro() {
		displayableCanvas.intro();
	}

	public void nuovoGiocoOCaricaPrecedente() {
		displayableCanvas.nuovoGiocoOCaricaPrecedente();
	}

	public void selezioneSlotSalvataggioDaCaricare() {
		displayableCanvas.selezioneSlotSalvataggioDaCaricare();
	}

	public void mappa() {
		displayableCanvas.mappa();
	}

	public void centraMappa() {
		displayableCanvas.centraMappa();
	}

	public void selezioneSlotSalvataggioDaSalvare() {
		displayableCanvas.selezioneSlotSalvataggioDaSalvare();
	}
	
	public void confermaUscita() {
		displayableCanvas.confermaUscita();
	}
	
	public void muoviMappa(Comando direzione) {
		displayableCanvas.muoviMappa(direzione);
	}

	public void perso() {
		displayableCanvas.perso();
	}

	public void vinto() {
		displayableCanvas.vinto();
	}

	public void statistiche() {
		displayableCanvas.statistiche();
	}

	public void hiscore() {
		displayableCanvas.hiscore();
	}

	public void scriviGrande(String messaggio) {
		displayableCanvas.scriviGrande(messaggio);
	}

	public void notifica(String messaggio) {
		displayableCanvas.notifica(messaggio);
	}

	public void primoPiano(InterfacciaUtente.Finestra finestra) {
		displayableCanvas.primoPiano(finestra);
	}

	public void chiediTesto() {
		prompt.setVisible(true);
	}

	public void riceviTesto(String testo) {
		prompt.setVisible(false);
		Gioco.riceviTesto(testo);
	}

	public void impostaAzioni() {
		iconPanel.impostaAzioni();
	}
	
	public void preparaLocazione() {
		displayableCanvas.preparaLocazione();
	}

	public void infoCombattimento(boolean mostra, Personaggio combattente, Personaggio avversario) {
		Logger.log("ForestaApplet::infoCombattimento(" + mostra + ")");
		if (!mostra) {
			displayableCanvas.getRiquadroCombattimento().setVisible(false);
		} else {
			DisplayableCanvasRiquadroCombattimento info = displayableCanvas.getRiquadroCombattimento();
			info.setCombattente(combattente);
			info.setAvversario(avversario);
			info.setVisible(true);
		}
		rinfresca();
	}

	@Override
	public void notificaMorte(Personaggio personaggio) {
		displayableCanvas.notificaMorte(personaggio);
	}
	
	@Override
	public void variaForza(Personaggio personaggio, int variazione) {
		displayableCanvas.variaForza(personaggio, variazione);
	}

	@Override
	public void variaForzaMassima(Personaggio personaggio, int variazione) {
		displayableCanvas.variaForzaMassima(personaggio, variazione);
	}

	@Override
	public void variaMagia(Personaggio personaggio, int variazione) {
		displayableCanvas.variaMagia(personaggio, variazione);
	}
	
	@Override
	public void variaMagiaMassima(Personaggio personaggio, int variazione) {
		displayableCanvas.variaMagiaMassima(personaggio, variazione);
	}
	
	@Override
	public void variaCoraggio(Personaggio personaggio, int variazione) {
		displayableCanvas.variaCoraggio(personaggio, variazione);
	}
	
	@Override
	public void variaValore(Personaggio personaggio, int variazione) {
		displayableCanvas.variaValore(personaggio, variazione);
	}
	
	@Override
	public void variaCarisma(Personaggio personaggio, int variazione) {
		displayableCanvas.variaCarisma(personaggio, variazione);
	}
	
	@Override
	public void variaStanchezza(Personaggio personaggio, int variazione) {
		displayableCanvas.variaStanchezza(personaggio, variazione);
	}

	@Override
	public void variaTempo(Personaggio personaggio, int variazione) {
		displayableCanvas.variaTempo(personaggio, variazione);
	}

	@Override
	public void variaGemme(int variazione) {
		displayableCanvas.variaGemme(variazione);
	}

	@Override
	public void variaMonete(int variazione) {
		displayableCanvas.variaMonete(variazione);
	}

	@Override
	public void variaPunti(int variazione) {
		displayableCanvas.variaPunti(variazione);
	}

	@Override
	public void variaIncantesimi(ClassiIncantesimo classeIncantesimo, int variazione) {
		displayableCanvas.variaIncantesimi(classeIncantesimo, variazione);
	}

	@Override
	public void variaPozioniForza(int variazione) {
		displayableCanvas.variaPozioniForza(variazione);
	}

	@Override
	public void variaPozioniMagia(int variazione) {
		displayableCanvas.variaPozioniMagia(variazione);
	}

	@Override
	public void variaPozioniGrandeForza(int variazione) {
		displayableCanvas.variaPozioniGrandeForza(variazione);
	}

	@Override
	public void variaMappa() {
		displayableCanvas.variaMappa();
	}
	
	@Override
	public void raccogliOggetto() {
		displayableCanvas.raccogliOggetto();
	}

	public void rinfresca() {
		Logger.log("ForestaApplet::rinfresca()");
		jframe.invalidate();
		jframe.repaint();
	}
}
