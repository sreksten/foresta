package com.threeamigos.foresta.ui.sviluppo;

import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.tipi.Orientamento;
import com.threeamigos.foresta.tipi.SchermoDiProva;
import com.threeamigos.foresta.ui.MisuraDelloSchermo;
import com.threeamigos.foresta.ui.ScelteDiAvvio;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * La finestra che si apre prima del gioco quando lo si avvia dal desktop in modalità di prova: le opzioni di avvio si scelgono qui invece di
 * ricordarsi come si scrivono. Parte da quelle scritte sulla riga di comando ({@link ScelteDiAvvio#da}) e restituisce gli argomenti da dare a
 * {@code OpzioniDiAvvio}; sotto c'è la riga di comando equivalente, da ricopiare. Da qui si può anche spegnere la modalità di prova.
 * <p>
 * Le opzioni che valgono solo per libGDX (il mouse come un dito) non si mostrano, ma se sono scritte sulla riga di comando restano.
 */
public final class FinestraDelleOpzioniDiAvvio {

	private static final int MARGINE = 8;
	private static final String DESKTOP = "Schermo del desktop (nessuna simulazione)";
	private static final String A_MISURA = "A misura…";
	private static final String ORIENTAMENTO_AUTOMATICO = "Automatico (verticale se si simula uno schermo)";
	private static final String LINGUA_DA_SCEGLIERE = "La chiede il gioco";

	private final ScelteDiAvvio scelte;
	private final JDialog finestra = new JDialog((Frame) null, "La Foresta - opzioni di avvio", true);
	private final JCheckBox modalitaDiProva = new JCheckBox("Modalità di prova (trucchi e finestra degli strumenti di prova)");
	private final JComboBox<Object> schermo = new JComboBox<>();
	private final JSpinner larghezza = new JSpinner(new SpinnerNumberModel(800, 320, 8000, 1));
	private final JSpinner altezza = new JSpinner(new SpinnerNumberModel(480, 240, 8000, 1));
	private final JSpinner densita = new JSpinner(new SpinnerNumberModel(1.0, 0.5, 5.0, 0.125));
	private final JComboBox<Object> orientamento = new JComboBox<>(new Object[]{ORIENTAMENTO_AUTOMATICO, Orientamento.ORIZZONTALE, Orientamento.VERTICALE});
	private final JComboBox<String> lingua = new JComboBox<>();
	private final JSpinner zoom = new JSpinner(new SpinnerNumberModel(2.0, 0.25, 8.0, 0.25));
	private final JCheckBox tuttoSchermo = new JCheckBox("Tutto schermo");
	private final JCheckBox saltaLogo = new JCheckBox("Salta il logo iniziale");
	private final JCheckBox barraClassica = new JCheckBox("Barra delle icone classica (senza effetto Dock)");
	private final JCheckBox joystickVirtuale = new JCheckBox("Joystick virtuale sulla mappa");
	private final JTextField cartellaDiLog = new JTextField(24);
	private final JTextField altri = new JTextField(24);
	private final JTextArea rigaDiComando = new JTextArea(3, 40);
	private boolean avvia;
	private boolean aggiornando;

	private FinestraDelleOpzioniDiAvvio(ScelteDiAvvio scelte) {
		this.scelte = scelte;
		costruisci();
		mostraLeScelte();
		aggiornaLaRigaDiComando();
	}

	/**
	 * Apre la finestra (modale, sul thread di Swing) con le scelte degli argomenti e aspetta: gli argomenti scelti, o niente se si è scelto di uscire.
	 * Va chiamata prima del primo messaggio di log: la cartella del log si imposta qui, come proprietà di sistema.
	 */
	public static Optional<String[]> chiedi(String[] argomenti) {
		ScelteDiAvvio scelte = ScelteDiAvvio.da(argomenti);
		boolean[] avvia = new boolean[1];
		try {
			SwingUtilities.invokeAndWait(() -> {
				FinestraDelleOpzioniDiAvvio finestra = new FinestraDelleOpzioniDiAvvio(scelte);
				finestra.finestra.setVisible(true);
				avvia[0] = finestra.avvia;
			});
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return Optional.empty();
		} catch (InvocationTargetException e) {
			throw new IllegalStateException("La finestra delle opzioni di avvio non si è aperta", e.getCause());
		}
		if (!avvia[0]) {
			return Optional.empty();
		}
		if (scelte.getCartellaDiLog() != null) {
			System.setProperty(Logger.PROPRIETA_CARTELLA_DI_LOG, scelte.getCartellaDiLog());
		} else {
			System.clearProperty(Logger.PROPRIETA_CARTELLA_DI_LOG);
		}
		return Optional.of(scelte.comeArgomenti());
	}

	private void costruisci() {
		schermo.addItem(DESKTOP);
		for (SchermoDiProva profilo : SchermoDiProva.values()) {
			schermo.addItem(profilo);
		}
		schermo.addItem(A_MISURA);
		schermo.setMaximumRowCount(SchermoDiProva.values().length + 2);
		schermo.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				return super.getListCellRendererComponent(list, testoDelloSchermo(value), index, isSelected, cellHasFocus);
			}
		});
		orientamento.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Object testo = value == Orientamento.ORIZZONTALE ? "Orizzontale (barra in basso)"
						: value == Orientamento.VERTICALE ? "Verticale (barra a destra)" : value;
				return super.getListCellRendererComponent(list, testo, index, isSelected, cellHasFocus);
			}
		});
		lingua.addItem(LINGUA_DA_SCEGLIERE);
		ScelteDiAvvio.LINGUE.forEach(lingua::addItem);
		lingua.setEditable(true);

		JPanel campi = new JPanel(new GridBagLayout());
		int riga = 0;
		riga = aggiungi(campi, riga, null, modalitaDiProva);
		riga = aggiungi(campi, riga, "Schermo", schermo);
		JPanel misura = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		misura.add(larghezza);
		misura.add(new JLabel("x"));
		misura.add(altezza);
		misura.add(new JLabel("pixel, densità"));
		misura.add(densita);
		riga = aggiungi(campi, riga, "Misura", misura);
		riga = aggiungi(campi, riga, "Orientamento", orientamento);
		riga = aggiungi(campi, riga, "Lingua", lingua);
		riga = aggiungi(campi, riga, "Zoom delle immagini", zoom);
		riga = aggiungi(campi, riga, null, tuttoSchermo);
		riga = aggiungi(campi, riga, null, saltaLogo);
		riga = aggiungi(campi, riga, null, barraClassica);
		riga = aggiungi(campi, riga, null, joystickVirtuale);
		JPanel log = new JPanel(new BorderLayout(4, 0));
		log.add(cartellaDiLog, BorderLayout.CENTER);
		JButton sfoglia = new JButton("Sfoglia…");
		sfoglia.addActionListener(e -> scegliLaCartellaDiLog());
		log.add(sfoglia, BorderLayout.EAST);
		riga = aggiungi(campi, riga, "Cartella del log", log);
		riga = aggiungi(campi, riga, "Altri argomenti", altri);
		rigaDiComando.setEditable(false);
		rigaDiComando.setLineWrap(true);
		rigaDiComando.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		aggiungi(campi, riga, "Riga di comando", new JScrollPane(rigaDiComando));

		JButton avviaIlGioco = new JButton("Avvia");
		avviaIlGioco.addActionListener(e -> {
			raccogliLeScelte();
			avvia = true;
			finestra.dispose();
		});
		JButton esci = new JButton("Esci");
		esci.addActionListener(e -> finestra.dispose());
		JPanel pulsanti = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pulsanti.add(esci);
		pulsanti.add(avviaIlGioco);

		JPanel contenuto = new JPanel(new BorderLayout());
		contenuto.setBorder(BorderFactory.createEmptyBorder(MARGINE, MARGINE, MARGINE, MARGINE));
		contenuto.add(campi, BorderLayout.CENTER);
		contenuto.add(pulsanti, BorderLayout.SOUTH);
		finestra.setContentPane(contenuto);
		finestra.getRootPane().setDefaultButton(avviaIlGioco);
		finestra.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		// Ogni cambiamento si vede subito nella riga di comando
		schermo.addActionListener(e -> {
			abilitaLaMisura();
			aggiornaLaRigaDiComando();
		});
		for (JComponent componente : new JComponent[]{modalitaDiProva, orientamento, lingua, tuttoSchermo, saltaLogo, barraClassica, joystickVirtuale}) {
			if (componente instanceof AbstractButton) {
				((AbstractButton) componente).addActionListener(e -> aggiornaLaRigaDiComando());
			} else {
				((JComboBox<?>) componente).addActionListener(e -> aggiornaLaRigaDiComando());
			}
		}
		for (JSpinner spinner : new JSpinner[]{larghezza, altezza, densita, zoom}) {
			spinner.addChangeListener(e -> aggiornaLaRigaDiComando());
		}
		DocumentListener testo = new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				aggiornaLaRigaDiComando();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				aggiornaLaRigaDiComando();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				aggiornaLaRigaDiComando();
			}
		};
		cartellaDiLog.getDocument().addDocumentListener(testo);
		altri.getDocument().addDocumentListener(testo);

		finestra.pack();
		finestra.setLocationRelativeTo(null);
	}

	private static int aggiungi(JPanel pannello, int riga, String etichetta, JComponent componente) {
		GridBagConstraints vincoli = new GridBagConstraints();
		vincoli.gridy = riga;
		vincoli.insets = new Insets(3, 3, 3, 3);
		vincoli.anchor = GridBagConstraints.WEST;
		if (etichetta != null) {
			vincoli.gridx = 0;
			pannello.add(new JLabel(etichetta), vincoli);
		}
		vincoli.gridx = 1;
		vincoli.fill = GridBagConstraints.HORIZONTAL;
		vincoli.weightx = 1;
		pannello.add(componente, vincoli);
		return riga + 1;
	}

	private static Object testoDelloSchermo(Object voce) {
		if (voce instanceof SchermoDiProva) {
			SchermoDiProva profilo = (SchermoDiProva) voce;
			MisuraDelloSchermo misura = profilo.getMisura();
			return String.format(Locale.ROOT, "%s - %s (%dx%d dp)", profilo.name(), profilo.getDescrizione(), Math.round(misura.getLarghezzaDp()),
					Math.round(misura.getAltezzaDp()));
		}
		return voce;
	}

	private void mostraLeScelte() {
		aggiornando = true;
		try {
			modalitaDiProva.setSelected(scelte.isModalitaDiProva());
			if (scelte.getProfilo() != null) {
				schermo.setSelectedItem(scelte.getProfilo());
			} else if (scelte.getSchermoAMisura() != null) {
				schermo.setSelectedItem(A_MISURA);
				larghezza.setValue(scelte.getSchermoAMisura().getLarghezza());
				altezza.setValue(scelte.getSchermoAMisura().getAltezza());
				densita.setValue(scelte.getSchermoAMisura().getDensita());
			} else {
				schermo.setSelectedItem(DESKTOP);
			}
			orientamento.setSelectedItem(scelte.getOrientamento() != null ? scelte.getOrientamento() : ORIENTAMENTO_AUTOMATICO);
			lingua.setSelectedItem(scelte.getLingua() != null ? scelte.getLingua() : LINGUA_DA_SCEGLIERE);
			zoom.setValue(scelte.getZoom());
			tuttoSchermo.setSelected(scelte.isTuttoSchermo());
			saltaLogo.setSelected(scelte.isSaltaLogo());
			barraClassica.setSelected(scelte.isBarraClassica());
			joystickVirtuale.setSelected(scelte.isJoystickVirtuale());
			cartellaDiLog.setText(scelte.getCartellaDiLog() != null ? scelte.getCartellaDiLog() : "");
			altri.setText(String.join(" ", scelte.getAltri()));
			abilitaLaMisura();
		} finally {
			aggiornando = false;
		}
	}

	private void abilitaLaMisura() {
		boolean aMisura = A_MISURA.equals(schermo.getSelectedItem());
		larghezza.setEnabled(aMisura);
		altezza.setEnabled(aMisura);
		densita.setEnabled(aMisura);
	}

	/**
	 * Porta nelle scelte quel che dice la finestra
	 */
	private void raccogliLeScelte() {
		scelte.setModalitaDiProva(modalitaDiProva.isSelected());
		Object voce = schermo.getSelectedItem();
		if (voce instanceof SchermoDiProva) {
			scelte.impostaProfilo((SchermoDiProva) voce);
		} else if (A_MISURA.equals(voce)) {
			scelte.impostaSchermoAMisura(new MisuraDelloSchermo(((Number) larghezza.getValue()).intValue(), ((Number) altezza.getValue()).intValue(),
					((Number) densita.getValue()).doubleValue()));
		} else {
			scelte.impostaProfilo(null);
		}
		Object direzione = orientamento.getSelectedItem();
		scelte.setOrientamento(direzione instanceof Orientamento ? (Orientamento) direzione : null);
		Object codice = lingua.getSelectedItem();
		scelte.setLingua(codice == null || LINGUA_DA_SCEGLIERE.equals(codice) ? null : codice.toString());
		scelte.setZoom(((Number) zoom.getValue()).doubleValue());
		scelte.setTuttoSchermo(tuttoSchermo.isSelected());
		scelte.setSaltaLogo(saltaLogo.isSelected());
		scelte.setBarraClassica(barraClassica.isSelected());
		scelte.setJoystickVirtuale(joystickVirtuale.isSelected());
		scelte.setCartellaDiLog(cartellaDiLog.getText());
		String liberi = altri.getText().trim();
		scelte.setAltri(liberi.isEmpty() ? java.util.Collections.emptyList() : Arrays.asList(liberi.split("\\s+")));
	}

	private void aggiornaLaRigaDiComando() {
		if (aggiornando) {
			return;
		}
		raccogliLeScelte();
		rigaDiComando.setText(scelte.comeRigaDiComando());
	}

	private void scegliLaCartellaDiLog() {
		JFileChooser scelta = new JFileChooser(cartellaDiLog.getText().trim().isEmpty() ? null : cartellaDiLog.getText().trim());
		scelta.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		scelta.setDialogTitle("Cartella del log");
		if (scelta.showOpenDialog(finestra) == JFileChooser.APPROVE_OPTION) {
			cartellaDiLog.setText(scelta.getSelectedFile().getAbsolutePath());
		}
	}
}
