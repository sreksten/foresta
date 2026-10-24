package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoInvioTesto;
import com.threeamigos.foresta.serializzazione.Serializzabile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.DoomdarkFontSmall;

/**
 * Il campo in cui il giocatore scrive un testo (il nome di un personaggio, il suo nome in classifica...): l'invio lo manda al motore come comando, o a chi lo ha chiesto per sé.
 */
public class Prompt extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private class MyFocusListener extends FocusAdapter {
		@Override
		public void focusLost(FocusEvent e) {
			// Il fuoco si riprende solo se è passato a un altro componente della stessa finestra (un click sul canvas):
			// non se è passato a un'altra applicazione o a un'altra finestra, come quella di prova
			Component altro = e.getOppositeComponent();
			if (isShowing() && altro != null && SwingUtilities.getWindowAncestor(altro) == SwingUtilities.getWindowAncestor(e.getComponent())) {
				e.getComponent().requestFocus();
			}
		}
	}

	// Un JTextField e non un java.awt.TextField: un componente AWT (nativo) ha una visibilità sua e, aggiunto a
	// finestra già aperta (a fine logo, vedi ForestaUI), comparirebbe anche con il Prompt nascosto e senza cornice
	private final JTextField tf;

	// I due font del gioco hanno gli stessi caratteri
	private static final DoomdarkFont FONT_DEI_TESTI = DoomdarkFontSmall.getInstance();

	// Chi ha chiesto il testo per sé (la UI, senza passare dal motore): se c'è, l'invio lo riceve lui invece del bus
	private Consumer<String> alInvio;

	public String getText() {
		return soloCaratteriDelFont(Serializzabile.pulisciCampo(tf.getText()));
	}

	/**
	 * Toglie i caratteri che il font del gioco non sa disegnare (una "ü", un "_"...): un nome così non si leggerebbe, e il riquadro del gruppo non saprebbe scriverlo.
	 */
	static String soloCaratteriDelFont(String testo) {
		if (testo == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder(testo.length());
		for (char c : testo.toCharArray()) {
			if (FONT_DEI_TESTI.supportaIlCarattere(c)) {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public Prompt() {
		super();
		BufferedImage cornice = ImageCache.corniceCombattimento;
		setSize(new Dimension(cornice.getWidth(), cornice.getHeight()));
		setLayout(null);
		tf = new JTextField();
		tf.addFocusListener(new MyFocusListener());
		// Il "|" separa i campi dei salvataggi, e il font non ha tutti i caratteri: quelli che non si possono disegnare non si lasciano scrivere
		tf.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (!Character.isISOControl(c) && !FONT_DEI_TESTI.supportaIlCarattere(c)) {
					e.consume();
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// Esc rinuncia, ma solo per un testo chiesto dalla UI: gli altri il motore li aspetta
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE && alInvio != null) {
					setVisible(false);
				}
			}
		});
		tf.setSize(cornice.getWidth() - 20, cornice.getHeight() - 10);
		tf.setLocation(10, (cornice.getHeight() - tf.getSize().height) / 2);
		tf.setBackground(Color.white);
		Font f = new Font("DialogInput", Font.PLAIN, tf.getSize().height - 6);
		tf.setFont(f);
		add(tf);
		tf.addActionListener(this);
		setVisible(false);
	}

	@Override
	public void setVisible(boolean visible) {
		alInvio = null;
		if (visible) {
			tf.setText("");
		}
		super.setVisible(visible);
		if (visible) {
			tf.requestFocus();
		}
		repaint();
	}

	/**
	 * Chiude il Prompt se sta ancora aspettando proprio quella risposta (chi l'aveva chiesta non la vuole più): se nel frattempo ne
	 * aspetta un'altra, o è già chiuso, non fa niente.
	 */
	public void annulla(Consumer<String> alInvioDaAnnullare) {
		if (isVisible() && alInvio != null && alInvio == alInvioDaAnnullare) {
			setVisible(false);
		}
	}

	/**
	 * Mostra il Prompt già compilato con un testo, con il cursore in fondo.
	 */
	public void mostra(String testoPredefinito) {
		setVisible(true);
		tf.setText(testoPredefinito);
		tf.setCaretPosition(testoPredefinito.length());
	}

	/**
	 * Come {@link #mostra(String)}, ma l'invio non va al motore: il testo lo riceve <code>alInvio</code> (con Esc non lo riceve nessuno).
	 */
	public void mostra(String testoPredefinito, Consumer<String> alInvio) {
		mostra(testoPredefinito);
		this.alInvio = alInvio;
	}

	/**
	 * ActionListener interface
	 */
	public void actionPerformed(ActionEvent e) {
		Consumer<String> richiedente = alInvio;
		setVisible(false);
		if (richiedente != null) {
			richiedente.accept(getText());
			return;
		}
		// Il testo incollato può ancora contenere un "|"
		BusEventi.pubblica(new ComandoInvioTesto(getText()));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(ImageCache.corniceCombattimento, 0, 0, null);
	}
}