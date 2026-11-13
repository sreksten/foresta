package com.threeamigos.foresta.artefatti.leggendari;

import com.threeamigos.foresta.strumenti.Maiuscole;
import com.threeamigos.foresta.artefatti.*;
import com.threeamigos.foresta.modellodati.*;
import com.threeamigos.foresta.tipi.AspettoArtefatto;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoRaritaArtefatto;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;

import java.util.*;

/**
 * Un libro tecnico, letto da una riga di leggendari.txt (che ne descrive il formato): un libro leggendario di cui nessuno
 * racconta la leggenda. Si trova solo nei cofani, molto di rado e al più una volta per partita (vedi Cofano e
 * IntermezzoLibroTecnico, che ricorda quelli già trovati). Ha l'aspetto di {@link AspettoArtefatto#LIBRO_TECNICO}.
 * <p>
 * Di solito è un libro magico (TIPO=LIBRO_MAGICO) del livello del mondo, con i modificatori e gli incantamenti raddoppiati, come il
 * prezzo, e può raddoppiare i danni magici di chi lo porta (DANNI_MAGICI=2: il Manuale di Unix System V). Può anche essere un
 * ingrediente che riprogramma gli artefatti (TIPO=RIPROGRAMMAZIONE: The C Programming Language, vedi
 * TipoArtefatto.RIPROGRAMMAZIONE), senza effetti suoi.
 */
public final class LibroTecnico {

	// I campi della riga di un libro tecnico (vedi RigaDiCampi): quelli che compaiono una volta e quelli che si ripetono
	private static final List<String> CAMPI_SINGOLI = Arrays.asList("CHIAVE", "NOME", "DESCRIZIONE", "TIPO", "DANNI_MAGICI");
	private static final List<String> CAMPI_RIPETUTI = Collections.singletonList("BATTUTA");
	private static final int MOLTIPLICATORE = 2;
	private static final TipoArtefatto TIPO_DI_DEFAULT = TipoArtefatto.LIBRO_MAGICO;

	private static Map<String, LibroTecnico> libri;

	static {
		// I libri sono quelli della grammatica della lingua in uso
		com.threeamigos.foresta.testi.Testi.alCambioDiLingua(LibroTecnico::reimposta);
	}

	private final String chiave;
	private final String nome;
	private final String descrizione;
	private final List<String> battute;
	private final TipoArtefatto tipo;
	private final int moltiplicatoreDanniMagici;

	private LibroTecnico(String riga) {
		RigaDiCampi campi = new RigaDiCampi(riga, CAMPI_SINGOLI, CAMPI_RIPETUTI);
		chiave = campi.obbligatorio("CHIAVE");
		nome = campi.obbligatorio("NOME");
		descrizione = campi.obbligatorio("DESCRIZIONE");
		tipo = campi.ha("TIPO") ? campi.enumerato(TipoArtefatto.class, "TIPO", campi.facoltativo("TIPO")) : TIPO_DI_DEFAULT;
		if (tipo != TipoArtefatto.LIBRO_MAGICO && !tipo.isRiprogrammazione()) {
			throw new IllegalArgumentException(campi.conLaRiga("Un libro tecnico è un libro magico o una riprogrammazione"));
		}
		moltiplicatoreDanniMagici = campi.ha("DANNI_MAGICI") ? campi.intero("DANNI_MAGICI", campi.facoltativo("DANNI_MAGICI")) : 1;
		if (moltiplicatoreDanniMagici < 1) {
			throw new IllegalArgumentException(campi.conLaRiga("DANNI_MAGICI è almeno 1"));
		}
		battute = campi.ripetuti("BATTUTA");
		if (battute.isEmpty()) {
			throw new IllegalArgumentException(campi.conLaRiga("Un libro tecnico ha almeno una BATTUTA"));
		}
	}

	/**
	 * Il libro tecnico di una riga di leggendari.txt.
	 */
	static LibroTecnico da(String riga) {
		return new LibroTecnico(riga);
	}

	/**
	 * Tutti i libri tecnici, nell'ordine in cui li dà la grammatica (quello del file). Si leggono una volta sola, controllando che
	 * le chiavi siano tutte diverse.
	 */
	public static synchronized Collection<LibroTecnico> tutti() {
		if (libri == null) {
			libri = leggi(ProduttoreDiArtefattiCasuale.tuttiILibriTecnici());
		}
		return libri.values();
	}

	/**
	 * Torna a leggere i libri dalla grammatica al prossimo uso, per i test che li hanno sostituiti con righe loro.
	 */
	static synchronized void reimposta() {
		libri = null;
	}

	/**
	 * Legge le righe date, controllando che le chiavi siano tutte diverse.
	 */
	static Map<String, LibroTecnico> leggi(List<String> righe) {
		Map<String, LibroTecnico> letti = new LinkedHashMap<>();
		for (String riga : righe) {
			LibroTecnico libro = da(riga);
			if (letti.put(libro.getChiave(), libro) != null) {
				throw new IllegalStateException("Libro tecnico ripetuto: " + libro.getChiave());
			}
		}
		return Collections.unmodifiableMap(letti);
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
		if (artefatto.getAspetto() != AspettoArtefatto.LIBRO_TECNICO) {
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
	 * Il titolo come lo legge chi ha il libro in mano, fra virgolette e con la descrizione: "“Il Manuale di Unix System
	 * V”, che spiega tutto, tranne come si esce dall'editor".
	 */
	public String getTitoloDaLeggere() {
		return '“' + Maiuscole.inizialeMaiuscola(nome) + "”, " + descrizione;
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
		md.setAspetto(AspettoArtefatto.LIBRO_TECNICO);
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
