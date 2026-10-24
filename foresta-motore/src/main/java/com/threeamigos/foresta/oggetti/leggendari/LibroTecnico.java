package com.threeamigos.foresta.oggetti.leggendari;

import com.threeamigos.foresta.interfacce.VistaArtefatto;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.GeneratoreArtefatti;
import com.threeamigos.foresta.testi.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;
import com.threeamigos.foresta.tipi.TipoRaritaArtefatto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Un libro tecnico, letto da una riga di leggendari.txt (che ne descrive il formato): un libro leggendario di cui nessuno
 * racconta la leggenda. Si trova solo nei cofani, molto di rado e al più una volta per partita (vedi Cofano e
 * IntermezzoLibroTecnico, che ricorda quelli già trovati). Ha l'aspetto di {@link TipoOggetto#LIBRO_TECNICO}.
 * <p>
 * Di solito è un libro magico (TIPO=LIBRO_MAGICO) del livello del mondo, con i modificatori e gli incantamenti raddoppiati, come il
 * prezzo, e può raddoppiare i danni magici di chi lo porta (DANNI_MAGICI=2: il Manuale di Unix System V). Può anche essere un
 * ingrediente che riprogramma gli artefatti (TIPO=RIPROGRAMMAZIONE: The C Programming Language, vedi
 * TipoArtefatto.RIPROGRAMMAZIONE), senza effetti suoi.
 */
public final class LibroTecnico {

	private static final String SEPARATORE = ";";
	private static final String UGUALE = "=";
	private static final int MOLTIPLICATORE = 2;
	private static final TipoArtefatto TIPO_DI_DEFAULT = TipoArtefatto.LIBRO_MAGICO;

	private static Map<String, LibroTecnico> libri;

	private final String chiave;
	private final String nome;
	private final String descrizione;
	private final List<String> battute;
	private final TipoArtefatto tipo;
	private final int moltiplicatoreDanniMagici;

	private LibroTecnico(String riga) {
		Map<String, String> campi = new HashMap<>();
		List<String> battuteLette = new ArrayList<>();
		for (String campo : riga.split(SEPARATORE)) {
			if (campo.trim().isEmpty()) {
				continue;
			}
			int uguale = campo.indexOf(UGUALE);
			if (uguale < 0) {
				throw new IllegalArgumentException("Un campo è CHIAVE=valore: " + campo + " in " + riga);
			}
			String chiaveCampo = campo.substring(0, uguale).trim();
			String valore = campo.substring(uguale + 1).trim();
			switch (chiaveCampo) {
				case "BATTUTA":
					battuteLette.add(valore);
					break;
				case "CHIAVE":
				case "NOME":
				case "DESCRIZIONE":
				case "TIPO":
				case "DANNI_MAGICI":
					if (campi.put(chiaveCampo, valore) != null) {
						throw new IllegalArgumentException("Campo ripetuto: " + chiaveCampo + " in " + riga);
					}
					break;
				default:
					throw new IllegalArgumentException("Campo sconosciuto: " + chiaveCampo + " in " + riga);
			}
		}
		chiave = obbligatorio(campi, "CHIAVE");
		nome = obbligatorio(campi, "NOME");
		descrizione = obbligatorio(campi, "DESCRIZIONE");
		tipo = campi.containsKey("TIPO") ? TipoArtefatto.valueOf(campi.get("TIPO")) : TIPO_DI_DEFAULT;
		if (tipo != TipoArtefatto.LIBRO_MAGICO && !tipo.isRiprogrammazione()) {
			throw new IllegalArgumentException("Un libro tecnico è un libro magico o una riprogrammazione: " + riga);
		}
		moltiplicatoreDanniMagici = campi.containsKey("DANNI_MAGICI") ? Integer.parseInt(campi.get("DANNI_MAGICI")) : 1;
		if (moltiplicatoreDanniMagici < 1) {
			throw new IllegalArgumentException("DANNI_MAGICI è almeno 1: " + riga);
		}
		if (battuteLette.isEmpty()) {
			throw new IllegalArgumentException("Un libro tecnico ha almeno una BATTUTA: " + riga);
		}
		battute = Collections.unmodifiableList(battuteLette);
	}

	private static String obbligatorio(Map<String, String> campi, String chiave) {
		String valore = campi.get(chiave);
		if (valore == null || valore.isEmpty()) {
			throw new IllegalArgumentException("Manca il campo " + chiave);
		}
		return valore;
	}

	/**
	 * Il libro tecnico di una riga di leggendari.txt.
	 */
	static LibroTecnico da(String riga) {
		return new LibroTecnico(Objects.requireNonNull(riga));
	}

	/**
	 * Tutti i libri tecnici, nell'ordine in cui li dà la grammatica (casuale). Si leggono una volta sola, controllando che
	 * le chiavi siano tutte diverse.
	 */
	public static synchronized Collection<LibroTecnico> tutti() {
		if (libri == null) {
			Map<String, LibroTecnico> letti = new LinkedHashMap<>();
			for (String riga : ProduttoreDiTestiCasuale.tuttiILibriTecnici()) {
				LibroTecnico libro = da(riga);
				if (letti.put(libro.getChiave(), libro) != null) {
					throw new IllegalStateException("Libro tecnico ripetuto: " + libro.getChiave());
				}
			}
			libri = Collections.unmodifiableMap(letti);
		}
		return libri.values();
	}

	/**
	 * Il libro tecnico con quella chiave.
	 */
	public static Optional<LibroTecnico> getLibro(String chiave) {
		tutti();
		return Optional.ofNullable(libri.get(chiave));
	}

	/**
	 * Il libro tecnico che l'artefatto è, se lo è: ne ha l'aspetto e il titolo.
	 */
	public static Optional<LibroTecnico> di(VistaArtefatto artefatto) {
		if (artefatto.getAspetto() != TipoOggetto.LIBRO_TECNICO) {
			return Optional.empty();
		}
		return tutti().stream().filter(libro -> libro.nome.equals(artefatto.getNome())).findFirst();
	}

	/**
	 * L'identificativo del libro: "MANUALE_UNIX_SYSTEM_V". Non cambia.
	 */
	public String getChiave() {
		return chiave;
	}

	/**
	 * Il titolo come sta sulla copertina, con l'articolo se ce l'ha: "il Manuale di Unix System V".
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * La descrizione o il sottotitolo: "di B. Kernighan e D. Ritchie".
	 */
	public String getDescrizione() {
		return descrizione;
	}

	/**
	 * Il tipo di artefatto che il libro è: un libro magico, o una riprogrammazione.
	 */
	public TipoArtefatto getTipo() {
		return tipo;
	}

	/**
	 * Per quanto il libro, equipaggiato, moltiplica i danni magici di chi lo porta: 1 se non li cambia.
	 */
	public int getMoltiplicatoreDanniMagici() {
		return moltiplicatoreDanniMagici;
	}

	/**
	 * Quel che il capo può dire sfogliandolo, una battuta per elemento.
	 */
	public List<String> getBattute() {
		return battute;
	}

	/**
	 * Il titolo come lo legge chi ha il libro in mano, fra virgolette e con la descrizione: "\"Il Manuale di Unix System
	 * V\", che aiuta a far gli spregi ar CDA".
	 */
	public String getTitoloDaLeggere() {
		return '"' + nome.substring(0, 1).toUpperCase(Locale.ROOT) + nome.substring(1) + "\", " + descrizione;
	}

	/**
	 * Un artefatto nuovo: del tipo del libro (un libro magico normale di quel livello, o una riprogrammazione), con il titolo e la
	 * descrizione del libro, i modificatori e gli incantamenti raddoppiati, il prezzo raddoppiato, leggendario e con l'aspetto di un
	 * libro tecnico.
	 */
	public Artefatto costruisci(GeneratoreArtefatti generatore, int livello) {
		Artefatto artefatto = generatore.generaArtefatto(tipo, livello);
		ArtefattoMD md = artefatto.getModelloDati();
		md.setNomeProprio(null);
		md.setNome(nome);
		md.setDescrizione(descrizione);
		for (ModificatoreAttributoMD modificatore : new ArrayList<>(md.getModificatori())) {
			md.removeModificatore(modificatore);
			md.addModificatore(modificatore.conQuantita(modificatore.getQuantita() * MOLTIPLICATORE));
		}
		for (IncantamentoMD incantamento : new ArrayList<>(md.getIncantamenti())) {
			md.removeIncantamento(incantamento);
			md.addIncantamento(new IncantamentoMD(incantamento.getNomeIncantamento(), incantamento.getTipoDannoElementale(),
					incantamento.getDannoBonusFisso() * MOLTIPLICATORE, incantamento.getCoefficienteScala() * MOLTIPLICATORE));
		}
		md.setCostoAcquisto(md.getCostoAcquisto() * MOLTIPLICATORE);
		md.setRarita(TipoRaritaArtefatto.LEGGENDARIO);
		md.setAspetto(TipoOggetto.LIBRO_TECNICO);
		return artefatto;
	}

	@Override
	public boolean equals(Object altro) {
		return altro instanceof LibroTecnico && chiave.equals(((LibroTecnico) altro).chiave);
	}

	@Override
	public int hashCode() {
		return chiave.hashCode();
	}
}
