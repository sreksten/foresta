package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.tipi.Orientamento;
import com.threeamigos.foresta.tipi.SchermoDiProva;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Le opzioni di avvio come le sceglie la finestra delle opzioni (in modalità di prova, sul desktop): si leggono dagli argomenti della riga di comando,
 * si cambiano, e si riscrivono come argomenti, che poi {@link OpzioniDiAvvio} legge come sempre. A differenza di {@link OpzioniDiAvvio} non imposta
 * niente mentre legge (né la lingua, né lo zoom, né la modalità di prova) e non scrive nel log: la finestra si apre prima del primo messaggio, perché
 * la cartella del log si decide lì.
 * <p>
 * Uno {@code SCHERMO=} o uno {@code ZOOM=} che non si capiscono restano fra gli altri argomenti, e {@link OpzioniDiAvvio} li segnalerà nel log.
 */
public final class ScelteDiAvvio {

	/** L'argomento che accende la modalità di prova, e con lei la finestra delle opzioni */
	public static final String MODALITA_DI_PROVA = "MODALITA_DI_PROVA";

	/** I codici delle lingue del gioco, per {@code LINGUA=} */
	public static final List<String> LINGUE = Collections.unmodifiableList(Arrays.asList("it", "en", "en-GB"));

	private boolean modalitaDiProva;
	// Il profilo dello schermo da simulare, o null
	private SchermoDiProva profilo;
	// Uno schermo da simulare scritto a misura, o null (con un profilo è sempre null)
	private MisuraDelloSchermo schermoAMisura;
	// L'orientamento scritto, o null per lasciarlo decidere (orizzontale, verticale con uno schermo da simulare)
	private Orientamento orientamento;
	// Il codice della lingua, o null perché la chieda il gioco
	private String lingua;
	private double zoom = LivelloDiZoom.PREDEFINITO;
	private boolean tuttoSchermo;
	private boolean saltaLogo;
	private boolean barraClassica;
	private boolean joystickVirtuale;
	private boolean tocco;
	// La cartella del file di log (la proprietà foresta.log), o null per la sola console
	private String cartellaDiLog;
	private final List<String> altri = new ArrayList<>();

	/**
	 * Se negli argomenti c'è {@code MODALITA_DI_PROVA} (senza distinzione fra maiuscole e minuscole): è quando si apre la finestra delle opzioni
	 */
	public static boolean isModalitaDiProva(String[] argomenti) {
		return Arrays.stream(argomenti).anyMatch(MODALITA_DI_PROVA::equalsIgnoreCase);
	}

	/**
	 * Le scelte scritte negli argomenti, con la cartella del log della proprietà {@link Logger#PROPRIETA_CARTELLA_DI_LOG}
	 */
	public static ScelteDiAvvio da(String[] argomenti) {
		ScelteDiAvvio scelte = new ScelteDiAvvio();
		for (String argomento : argomenti) {
			scelte.leggiUno(argomento);
		}
		scelte.cartellaDiLog = vuotoComeNull(System.getProperty(Logger.PROPRIETA_CARTELLA_DI_LOG));
		return scelte;
	}

	private void leggiUno(String argomento) {
		String maiuscolo = argomento.toUpperCase(Locale.ROOT);
		if (maiuscolo.equals(MODALITA_DI_PROVA)) {
			modalitaDiProva = true;
		} else if (maiuscolo.equals("ORIZZONTALE")) {
			orientamento = Orientamento.ORIZZONTALE;
		} else if (maiuscolo.equals("VERTICALE")) {
			orientamento = Orientamento.VERTICALE;
		} else if (maiuscolo.equals("TUTTOSCHERMO")) {
			tuttoSchermo = true;
		} else if (maiuscolo.equals("SALTALOGO")) {
			saltaLogo = true;
		} else if (maiuscolo.equals("BARRACLASSICA")) {
			barraClassica = true;
		} else if (maiuscolo.equals("JOYSTICK_VIRTUALE")) {
			joystickVirtuale = true;
		} else if (maiuscolo.equals("TOCCO")) {
			tocco = true;
		} else if (maiuscolo.startsWith("LINGUA=")) {
			lingua = vuotoComeNull(argomento.substring("LINGUA=".length()).trim());
		} else if (maiuscolo.startsWith("ZOOM=") && leggiLoZoom(argomento.substring("ZOOM=".length()))) {
			return;
		} else if (maiuscolo.startsWith("SCHERMO=") && leggiLoSchermo(maiuscolo.substring("SCHERMO=".length()))) {
			return;
		} else {
			// Anche uno ZOOM= o uno SCHERMO= che non si capiscono
			altri.add(argomento);
		}
	}

	private boolean leggiLoZoom(String valore) {
		try {
			double letto = Double.parseDouble(valore);
			if (letto > 0) {
				zoom = letto;
				return true;
			}
		} catch (NumberFormatException e) {
			// Resta fra gli altri argomenti
		}
		return false;
	}

	private boolean leggiLoSchermo(String valore) {
		for (SchermoDiProva candidato : SchermoDiProva.values()) {
			if (candidato.name().equals(valore)) {
				impostaProfilo(candidato);
				return true;
			}
		}
		try {
			impostaSchermoAMisura(MisuraDelloSchermo.leggi(valore));
			return true;
		} catch (IllegalArgumentException e) {
			// Anche NumberFormatException: resta fra gli altri argomenti
			return false;
		}
	}

	/**
	 * Gli argomenti per {@link OpzioniDiAvvio}, nell'ordine in cui si scriverebbero a mano; le opzioni al loro valore predefinito non si scrivono
	 */
	public String[] comeArgomenti() {
		List<String> argomenti = new ArrayList<>();
		if (modalitaDiProva) {
			argomenti.add(MODALITA_DI_PROVA);
		}
		if (profilo != null) {
			argomenti.add("SCHERMO=" + profilo.name());
		} else if (schermoAMisura != null) {
			argomenti.add("SCHERMO=" + schermoAMisura.getLarghezza() + "x" + schermoAMisura.getAltezza() + "@" + numero(schermoAMisura.getDensita()));
		}
		if (orientamento != null) {
			argomenti.add(orientamento.name());
		}
		if (lingua != null) {
			argomenti.add("LINGUA=" + lingua);
		}
		if (zoom != LivelloDiZoom.PREDEFINITO) {
			argomenti.add("ZOOM=" + numero(zoom));
		}
		if (tuttoSchermo) {
			argomenti.add("TUTTOSCHERMO");
		}
		if (saltaLogo) {
			argomenti.add("SALTALOGO");
		}
		if (barraClassica) {
			argomenti.add("BARRACLASSICA");
		}
		if (joystickVirtuale) {
			argomenti.add("JOYSTICK_VIRTUALE");
		}
		if (tocco) {
			argomenti.add("TOCCO");
		}
		argomenti.addAll(altri);
		return argomenti.toArray(new String[0]);
	}

	/**
	 * La riga di comando equivalente, con la cartella del log come proprietà davanti: per ricopiarla la prossima volta
	 */
	public String comeRigaDiComando() {
		StringBuilder riga = new StringBuilder();
		if (cartellaDiLog != null) {
			riga.append("-D").append(Logger.PROPRIETA_CARTELLA_DI_LOG).append('=').append(cartellaDiLog);
		}
		for (String argomento : comeArgomenti()) {
			if (riga.length() > 0) {
				riga.append(' ');
			}
			riga.append(argomento);
		}
		return riga.toString();
	}

	/**
	 * Un numero senza zeri inutili: 2 e non 2.0, 2.625 così com'è
	 */
	private static String numero(double valore) {
		if (valore == Math.rint(valore)) {
			return String.valueOf((long) valore);
		}
		return String.valueOf(valore);
	}

	private static String vuotoComeNull(String testo) {
		return testo == null || testo.trim().isEmpty() ? null : testo.trim();
	}

	public boolean isModalitaDiProva() {
		return modalitaDiProva;
	}

	public void setModalitaDiProva(boolean modalitaDiProva) {
		this.modalitaDiProva = modalitaDiProva;
	}

	/** Il profilo dello schermo da simulare, o null (lo schermo del desktop, o uno a misura) */
	public SchermoDiProva getProfilo() {
		return profilo;
	}

	/** Simula lo schermo di quel profilo (null: lo schermo del desktop) */
	public void impostaProfilo(SchermoDiProva profilo) {
		this.profilo = profilo;
		this.schermoAMisura = null;
	}

	/** Lo schermo da simulare scritto a misura, o null */
	public MisuraDelloSchermo getSchermoAMisura() {
		return schermoAMisura;
	}

	/** Simula uno schermo di quella misura (null: lo schermo del desktop) */
	public void impostaSchermoAMisura(MisuraDelloSchermo misura) {
		this.schermoAMisura = misura;
		this.profilo = null;
	}

	/** L'orientamento scritto, o null se lo decide il gioco */
	public Orientamento getOrientamento() {
		return orientamento;
	}

	public void setOrientamento(Orientamento orientamento) {
		this.orientamento = orientamento;
	}

	/** Il codice della lingua (vedi {@link #LINGUE}), o null se la chiede il gioco */
	public String getLingua() {
		return lingua;
	}

	public void setLingua(String lingua) {
		this.lingua = vuotoComeNull(lingua);
	}

	public double getZoom() {
		return zoom;
	}

	/**
	 * @throws IllegalArgumentException se non è positivo
	 */
	public void setZoom(double zoom) {
		if (!(zoom > 0)) {
			throw new IllegalArgumentException("Livello di zoom non valido: " + zoom);
		}
		this.zoom = zoom;
	}

	public boolean isTuttoSchermo() {
		return tuttoSchermo;
	}

	public void setTuttoSchermo(boolean tuttoSchermo) {
		this.tuttoSchermo = tuttoSchermo;
	}

	public boolean isSaltaLogo() {
		return saltaLogo;
	}

	public void setSaltaLogo(boolean saltaLogo) {
		this.saltaLogo = saltaLogo;
	}

	public boolean isBarraClassica() {
		return barraClassica;
	}

	public void setBarraClassica(boolean barraClassica) {
		this.barraClassica = barraClassica;
	}

	public boolean isJoystickVirtuale() {
		return joystickVirtuale;
	}

	public void setJoystickVirtuale(boolean joystickVirtuale) {
		this.joystickVirtuale = joystickVirtuale;
	}

	public boolean isTocco() {
		return tocco;
	}

	public void setTocco(boolean tocco) {
		this.tocco = tocco;
	}

	/** La cartella del file di log, o null per la sola console */
	public String getCartellaDiLog() {
		return cartellaDiLog;
	}

	public void setCartellaDiLog(String cartellaDiLog) {
		this.cartellaDiLog = vuotoComeNull(cartellaDiLog);
	}

	/** Gli argomenti che non sono opzioni comuni, nell'ordine */
	public List<String> getAltri() {
		return Collections.unmodifiableList(altri);
	}

	/** Sostituisce gli altri argomenti (quelli che la finestra mostra come testo libero) */
	public void setAltri(List<String> nuovi) {
		altri.clear();
		for (String argomento : nuovi) {
			if (!argomento.trim().isEmpty()) {
				altri.add(argomento.trim());
			}
		}
	}
}
