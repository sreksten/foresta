package com.threeamigos.foresta.ui.immagini;

import com.threeamigos.foresta.tipi.AspettoArtefatto;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.MossaCartaForbiciSasso;
import com.threeamigos.foresta.tipi.TipoBottino;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.EnumMap;
import java.util.Map;

/**
 * Il nome della risorsa (relativo a <code>img/</code>, per esempio <code>personaggi/Goblin.png</code>) dell'immagine di ogni classe di personaggio,
 * di ogni oggetto che si trova nelle locazioni e di ogni locazione: le tabelle che Swing e libGDX hanno in comune, così il motore non carica immagini
 * (si può usare senza schermo) e nessuna delle due interfacce le copia. Le immagini le carica ciascuna col suo toolkit.
 */
public final class NomiDelleImmagini {

	private static final Map<TipoPersonaggio, String> PERSONAGGI = new EnumMap<>(TipoPersonaggio.class);
	// Solo per chi si può reclutare (e per qualche altro): l'icona per il riquadro del gruppo
	private static final Map<TipoPersonaggio, String> ICONE = new EnumMap<>(TipoPersonaggio.class);
	private static final Map<TipoBottino, String> OGGETTI = new EnumMap<>(TipoBottino.class);
	private static final Map<AspettoArtefatto, String> ASPETTI = new EnumMap<>(AspettoArtefatto.class);

	static {
		PERSONAGGI.put(TipoPersonaggio.ARPIA, "personaggi/Arpia.png");
		PERSONAGGI.put(TipoPersonaggio.BARDO, "personaggi/Bardo.png");
		ICONE.put(TipoPersonaggio.BARDO, "icone/Bardo.png");
		PERSONAGGI.put(TipoPersonaggio.CANTASTORIE, "personaggi/Cantastorie.png");
		ICONE.put(TipoPersonaggio.CANTASTORIE, "icone/Cantastorie.png");
		PERSONAGGI.put(TipoPersonaggio.CENTAURO, "personaggi/Centauro.png");
		ICONE.put(TipoPersonaggio.CENTAURO, "icone/Centauro.png");
		PERSONAGGI.put(TipoPersonaggio.CHIMERA, "personaggi/Chimera.png");
		PERSONAGGI.put(TipoPersonaggio.CHIMERA_DRAGO, "personaggi/ChimeraDrago.png");
		PERSONAGGI.put(TipoPersonaggio.DRAGO, "personaggi/Drago.png");
		PERSONAGGI.put(TipoPersonaggio.ELFA, "personaggi/Elfa.png");
		ICONE.put(TipoPersonaggio.ELFA, "icone/Elfa.png");
		PERSONAGGI.put(TipoPersonaggio.ELFO, "personaggi/Elfo.png");
		ICONE.put(TipoPersonaggio.ELFO, "icone/Elfo.png");
		PERSONAGGI.put(TipoPersonaggio.EREMITA, "personaggi/Eremita.png");
		ICONE.put(TipoPersonaggio.EREMITA, "icone/Eremita.png");
		PERSONAGGI.put(TipoPersonaggio.FANTASMA, "personaggi/Fantasma.png");
		PERSONAGGI.put(TipoPersonaggio.FOLLETTO, "personaggi/Folletto.png");
		PERSONAGGI.put(TipoPersonaggio.GARGOYLE, "personaggi/Gargoyle.png");
		PERSONAGGI.put(TipoPersonaggio.GIGANTE, "personaggi/Gigante.png");
		ICONE.put(TipoPersonaggio.GIGANTE, "icone/Gigante.png");
		PERSONAGGI.put(TipoPersonaggio.GOBLIN, "personaggi/Goblin.png");
		ICONE.put(TipoPersonaggio.GOBLIN, "icone/Goblin.png");
		PERSONAGGI.put(TipoPersonaggio.GUERRIERA, "personaggi/Guerriera.png");
		ICONE.put(TipoPersonaggio.GUERRIERA, "icone/Guerriera.png");
		PERSONAGGI.put(TipoPersonaggio.GUERRIERO, "personaggi/Guerriero.png");
		ICONE.put(TipoPersonaggio.GUERRIERO, "icone/Guerriero.png");
		PERSONAGGI.put(TipoPersonaggio.HOBGOBLIN, "personaggi/Hobgoblin.png");
		ICONE.put(TipoPersonaggio.HOBGOBLIN, "icone/Hobgoblin.png");
		PERSONAGGI.put(TipoPersonaggio.IDRA, "personaggi/Idra.png");
		PERSONAGGI.put(TipoPersonaggio.LADRA, "personaggi/Ladra.png");
		ICONE.put(TipoPersonaggio.LADRA, "icone/Ladra.png");
		PERSONAGGI.put(TipoPersonaggio.LADRO, "personaggi/Ladro.png");
		ICONE.put(TipoPersonaggio.LADRO, "icone/Ladro.png");
		PERSONAGGI.put(TipoPersonaggio.LICH, "personaggi/Lich.png");
		PERSONAGGI.put(TipoPersonaggio.MAGA, "personaggi/Maga.png");
		ICONE.put(TipoPersonaggio.MAGA, "icone/Maga.png");
		PERSONAGGI.put(TipoPersonaggio.MAGO, "personaggi/Mago.png");
		ICONE.put(TipoPersonaggio.MAGO, "icone/Mago.png");
		PERSONAGGI.put(TipoPersonaggio.SACERDOTESSA, "personaggi/Sacerdotessa.png");
		ICONE.put(TipoPersonaggio.SACERDOTESSA, "icone/Sacerdotessa.png");
		PERSONAGGI.put(TipoPersonaggio.SACERDOTE, "personaggi/Sacerdote.png");
		ICONE.put(TipoPersonaggio.SACERDOTE, "icone/Sacerdote.png");
		PERSONAGGI.put(TipoPersonaggio.CHIERICA, "personaggi/Chierica.png");
		ICONE.put(TipoPersonaggio.CHIERICA, "icone/Chierica.png");
		PERSONAGGI.put(TipoPersonaggio.CHIERICO, "personaggi/Chierico.png");
		ICONE.put(TipoPersonaggio.CHIERICO, "icone/Chierico.png");
		PERSONAGGI.put(TipoPersonaggio.DRUIDA, "personaggi/Druida.png");
		ICONE.put(TipoPersonaggio.DRUIDA, "icone/Druida.png");
		PERSONAGGI.put(TipoPersonaggio.DRUIDO, "personaggi/Druido.png");
		ICONE.put(TipoPersonaggio.DRUIDO, "icone/Druido.png");
		PERSONAGGI.put(TipoPersonaggio.MINOTAURO, "personaggi/Minotauro.png");
		ICONE.put(TipoPersonaggio.MINOTAURO, "icone/Minotauro.png");
		PERSONAGGI.put(TipoPersonaggio.MINOTAURO_GIGANTE, "personaggi/MinotauroGigante.png");
		PERSONAGGI.put(TipoPersonaggio.NECROMANTE, "personaggi/Necromante.png");
		PERSONAGGI.put(TipoPersonaggio.OMBRAFIAMMA, "personaggi/OmbraFiamma.png");
		ICONE.put(TipoPersonaggio.OMBRAFIAMMA, "icone/OmbraFiamma.png");
		PERSONAGGI.put(TipoPersonaggio.OMBRA_NERA, "personaggi/OmbraNera.png");
		PERSONAGGI.put(TipoPersonaggio.SCHELETRO, "personaggi/Scheletro.png");
		PERSONAGGI.put(TipoPersonaggio.SPETTRO, "personaggi/Spettro.png");
		PERSONAGGI.put(TipoPersonaggio.SPIRITO, "personaggi/Spirito.png");
		PERSONAGGI.put(TipoPersonaggio.STREGA, "personaggi/Strega.png");
		PERSONAGGI.put(TipoPersonaggio.TITANO, "personaggi/Titano.png");
		ICONE.put(TipoPersonaggio.TITANO, "icone/Titano.png");
		PERSONAGGI.put(TipoPersonaggio.TROLL, "personaggi/Troll.png");
		PERSONAGGI.put(TipoPersonaggio.VIVERNA, "personaggi/Viverna.png");
		PERSONAGGI.put(TipoPersonaggio.LOCANDIERE, "personaggi/Locandiere.png");
		ICONE.put(TipoPersonaggio.LOCANDIERE, "icone/Locandiere.png");
		PERSONAGGI.put(TipoPersonaggio.ARMAIOLO, "personaggi/Armaiolo.png");
		ICONE.put(TipoPersonaggio.ARMAIOLO, "icone/Armaiolo.png");
		PERSONAGGI.put(TipoPersonaggio.ALCHIMISTA, "personaggi/Alchimista.png");
		ICONE.put(TipoPersonaggio.ALCHIMISTA, "icone/Alchimista.png");
		PERSONAGGI.put(TipoPersonaggio.VENDITORE_DI_PERGAMENE, "personaggi/VenditoreDiPergamene.png");
		ICONE.put(TipoPersonaggio.VENDITORE_DI_PERGAMENE, "icone/VenditoreDiPergamene.png");
		PERSONAGGI.put(TipoPersonaggio.INCANTATORE, "personaggi/Incantatore.png");
		ICONE.put(TipoPersonaggio.INCANTATORE, "icone/Incantatore.png");
		PERSONAGGI.put(TipoPersonaggio.MOGLIE_DEL_BARDO, "personaggi/MoglieDelBardo.png");
		ICONE.put(TipoPersonaggio.MOGLIE_DEL_BARDO, "icone/MoglieDelBardo.png");
		PERSONAGGI.put(TipoPersonaggio.VIANDANTE, "personaggi/Viandante.png");
		ICONE.put(TipoPersonaggio.VIANDANTE, "icone/Viandante.png");
		PERSONAGGI.put(TipoPersonaggio.BARDO_DELLE_LOCANDE, "personaggi/BardoLocanda.png");
		ICONE.put(TipoPersonaggio.BARDO_DELLE_LOCANDE, "icone/BardoLocanda.png");
		PERSONAGGI.put(TipoPersonaggio.VICHINGO, "personaggi/Vichingo.png");
		ICONE.put(TipoPersonaggio.VICHINGO, "icone/Vichingo.png");
		PERSONAGGI.put(TipoPersonaggio.CAPITANO_DELLE_GUARDIE, "personaggi/CapitanoDelleGuardie.png");
		ICONE.put(TipoPersonaggio.CAPITANO_DELLE_GUARDIE, "icone/CapitanoDelleGuardie.png");
		PERSONAGGI.put(TipoPersonaggio.BORGOMASTRO, "personaggi/Borgomastro.png");
		PERSONAGGI.put(TipoPersonaggio.UOMO, "personaggi/Uomo.png");
		ICONE.put(TipoPersonaggio.UOMO, "icone/Uomo.png");
		PERSONAGGI.put(TipoPersonaggio.DONNA, "personaggi/Donna.png");
		ICONE.put(TipoPersonaggio.DONNA, "icone/Donna.png");

		OGGETTI.put(TipoBottino.ANELLO, "oggetti/Anello.png");
		OGGETTI.put(TipoBottino.COFANO, "oggetti/Cofano.png");
		OGGETTI.put(TipoBottino.CORONA, "oggetti/Corona.png");
		OGGETTI.put(TipoBottino.PIETRA_PREZIOSA, "oggetti/PietraPreziosa.png");
		OGGETTI.put(TipoBottino.MONETA, "oggetti/Moneta.png");
		OGGETTI.put(TipoBottino.SCUDO, "oggetti/Scudo.png");
		OGGETTI.put(TipoBottino.SPADA, "oggetti/Spada.png");
		OGGETTI.put(TipoBottino.SPADONE, "oggetti/Spadone.png");
		OGGETTI.put(TipoBottino.FALCE, "oggetti/FalceDelTristoMietitore.png");
		OGGETTI.put(TipoBottino.MAZZA, "oggetti/Mazza.png");
		OGGETTI.put(TipoBottino.ASCIA, "oggetti/Ascia.png");
		OGGETTI.put(TipoBottino.LANCIA, "oggetti/Lancia.png");
		OGGETTI.put(TipoBottino.BASTONE_MAGICO, "oggetti/BastoneMagico.png");
		OGGETTI.put(TipoBottino.LIBRO_MAGICO, "oggetti/LibroMagico.png");
		OGGETTI.put(TipoBottino.ELMO, "oggetti/Elmo.png");
		OGGETTI.put(TipoBottino.MASCHERA, "oggetti/Maschera.png");
		OGGETTI.put(TipoBottino.ARMATURA, "oggetti/Armatura.png");
		OGGETTI.put(TipoBottino.SCHINIERI, "oggetti/Schinieri.png");
		OGGETTI.put(TipoBottino.OGGETTO_MISSIONE, "oggetti/OggettoMissione.png");
		// Gli artefatti non si mostrano nelle locazioni; il sacchetto generico vale per tutti gli oggetti delle missioni
		ASPETTI.put(AspettoArtefatto.LIBRO_TECNICO, "oggetti/LibroTecnico.png");
		ASPETTI.put(AspettoArtefatto.SPADA_NORMALMENTE_NASCOSTA, "oggetti/LaSpadaNormalmenteNascosta.png");
	}

	/**
	 * Le icone delle pozioni e di quel che le varia: salute (piccola e grande), magia (piccola e grande), panacea, aumento di livello
	 */
	public static final String POZIONE_SALUTE = "icone/PozioneSalute.png";
	public static final String POZIONE_SALUTE_GRANDE = "icone/PozioneSaluteGrande.png";
	public static final String POZIONE_MAGIA = "icone/PozioneMagia.png";
	public static final String POZIONE_MAGIA_GRANDE = "icone/PozioneMagiaGrande.png";
	public static final String POZIONE_PANACEA = "icone/PozionePanacea.png";
	public static final String POZIONE_AUMENTO_LIVELLO = "icone/AumentoLivello.png";
	/** L'icona della pergamena, che fa da immagine agli ingredienti magici senza una loro */
	public static final String ICONA_PERGAMENA = "icone/Pergamena.png";
	/** Le icone della schermata delle tattiche: su, giù, togli, aggiungi, sposta a sinistra e a destra, conferma e annulla */
	public static final String ICONA_SU = "icone/Su.png";
	public static final String ICONA_GIU = "icone/Giu.png";
	public static final String ICONA_ELIMINA = "icone/Rimuovi.png";
	public static final String ICONA_AGGIUNGI = "icone/Aggiungi.png";
	public static final String ICONA_SINISTRA = "icone/Sinistra.png";
	public static final String ICONA_DESTRA = "icone/Destra.png";
	public static final String ICONA_SI = "icone/Si.png";
	public static final String ICONA_NO = "icone/No.png";
	public static final String FRECCIA_SU = "icone/ComponenteScorrevole-FrecciaSu.png";
	public static final String FRECCIA_GIU = "icone/ComponenteScorrevole-FrecciaGiu.png";
	public static final String FRECCIA_SINISTRA = "icone/ComponenteScorrevole-FrecciaSinistra.png";
	public static final String FRECCIA_DESTRA = "icone/ComponenteScorrevole-FrecciaDestra.png";
	public static final String CARTIGLIO_SINISTRO = "fondinon2x2/Cartiglio-sinistro.png";
	public static final String CARTIGLIO_DESTRO = "fondinon2x2/Cartiglio-destro.png";
	public static final String SFONDO_ICONA_TRASPARENTE = "icone/Sfondo-icona-trasparente.png";
	public static final String MISSIONE_BIRRA = "icone/Missione-birra-grande.png";
	public static final String MISSIONE_GALLO = "icone/Missione-gallo.png";
	public static final String SEPARATORE = "fondinon2x2/Separatore.png";
	public static final String SEPARATORE_ARMI = "fondinon2x2/Separatore-Armi.png";
	public static final String SEPARATORE_ELMI = "fondinon2x2/Separatore-Elmi.png";
	public static final String SEPARATORE_MASCHERE = "fondi/Separatore-Maschere.png";
	public static final String SEPARATORE_ARMATURE = "fondinon2x2/Separatore-Armature.png";
	public static final String SEPARATORE_SCHINIERI = "fondinon2x2/Separatore-Schinieri.png";
	public static final String SEPARATORE_SCUDI = "fondinon2x2/Separatore-Scudi.png";
	public static final String SEPARATORE_INCANTESIMI = "fondi/Separatore-Incantesimi.png";
	public static final String SEPARATORE_POZIONI = "fondi/Separatore-Pozioni.png";
	public static final String SEPARATORE_LIBRI_MAGICI = "fondinon2x2/Separatore-LibriMagici.png";
	public static final String SEPARATORE_INCANTAMENTI = "fondi/Separatore-Incantamenti.png";
	public static final String SEPARATORE_NINNOLI = "fondi/Separatore-Ninnoli.png";
	/**
	 * Le icone degli sprite a tempo (le variazioni di statistiche, monete, tempo…)
	 */
	public static final String SPRITE_AMICIZIA = "icone/Amicizia.png";
	public static final String SPRITE_COMBATTIMENTO = "icone/Combattimento.png";
	public static final String SPRITE_MAGIA = "icone/Incantesimo.png";
	public static final String SPRITE_GRUPPO = "icone/Gruppo.png";
	public static final String SPRITE_MAPPA = "icone/Mappa.png";
	public static final String SPRITE_MONETA = "icone/Moneta.png";
	public static final String SPRITE_PIETRA_PREZIOSA = "icone/PietraPreziosa.png";
	public static final String SPRITE_TEMPO = "icone/Tempo.png";

	private NomiDelleImmagini() {
	}

	/**
	 * Le immagini del bosco sulla mappa: la prima è quella di sempre, le altre sono varianti (vedi <code>Foresta.VARIANTE_BOSCO</code>: la variante di una casella
	 * va da 1 a NUMERO_VARIANTI_BOSCO)
	 */
	public static final String[] VARIANTI_DEL_BOSCO_SULLA_MAPPA = {
			"mappa/Foresta.png", "mappa/Foresta2.png", "mappa/Foresta3.png", "mappa/Foresta4.png"
	};
	public static final String SEGNALINO = "mappa/Segnalino.png";
	/** La mano-puntatore del joystick virtuale sulla mappa a tutto schermo */
	public static final String MANO_PUNTATORE = "icone/ManoPuntatore.png";
	/** La sfera magica in basso a sinistra, che copre metà del notiziario */
	public static final String SFERA_MAGICA = "fondinon2x2/SferaMagica.png";
	public static final String INDICATORE = "mappa/Indicatore.png";

	/**
	 * La tessera di una locazione sulla mappa (per il bosco, la prima variante: vedi {@link #tesseraDelBosco}), o null se quel tipo non ne ha una
	 */
	public static String tessera(TipoLocazione tipo) {
		if (tipo == TipoLocazione.BOSCO) {
			return VARIANTI_DEL_BOSCO_SULLA_MAPPA[0];
		}
		if (tipo.getCategoriaLocazione() == CategoriaLocazione.CASTELLO) {
			return "mappa/Castello.png";
		}
		if (tipo.getCategoriaLocazione() == CategoriaLocazione.CITTA) {
			return "mappa/Citta.png";
		}
		switch (tipo) {
			case GROTTA:
				return "mappa/Grotta.png";
			case LOCANDA:
				return "mappa/Locanda.png";
			case PALUDE:
				return "mappa/Palude.png";
			case RADURA:
				return "mappa/Radura.png";
			case ROVINE:
				return "mappa/Rovine.png";
			case TEMPIO:
				return "mappa/Tempio.png";
			case ALCHIMISTA:
				return "mappa/Alchimista.png";
			case ARMAIOLO:
				return "mappa/Armaiolo.png";
			case VENDITORE_DI_PERGAMENE:
				return "mappa/VenditoreDiPergamene.png";
			case INCANTATORE:
				return "mappa/Incantatore.png";
			default:
				return null;
		}
	}

	public static final String CORNICE_LARGA = "fondi/CorniceLarga.png";

	/**
	 * La mano di una mossa della sfida a carta, forbici e sasso: la sinistra è quella del giocatore, la destra quella dell'avversario
	 */
	public static String manoDellaSfida(MossaCartaForbiciSasso mossa, boolean sinistra) {
		String nome;
		switch (mossa) {
			case CARTA:
				nome = "Carta";
				break;
			case FORBICE:
				nome = "Forbici";
				break;
			default:
				nome = "Sasso";
				break;
		}
		return "fondi/" + nome + (sinistra ? "-sx.png" : "-dx.png");
	}

	/**
	 * La tessera del bosco con quella variante (da 1)
	 */
	public static String tesseraDelBosco(int variante) {
		return VARIANTI_DEL_BOSCO_SULLA_MAPPA[variante - 1];
	}

	/**
	 * L'icona di un incantesimo
	 */
	public static String iconaDelIncantesimo(TipoIncantesimo tipo) {
		switch (tipo) {
			case ARIA:
				return "icone/Aria.png";
			case ACQUA:
				return "icone/Acqua.png";
			case TERRA:
				return "icone/Terra.png";
			case FUOCO:
				return "icone/Fuoco.png";
			case FULMINE:
				return "icone/Fulmine.png";
			case GELO:
				return "icone/Gelo.png";
			case VELENO:
				return "icone/Veleno.png";
			case MORTE:
				return "icone/Morte.png";
			case RESURREZIONE:
				return "icone/Resurrezione.png";
			case SILENZIO:
				return "icone/Silenzio.png";
			case NEBBIA:
				return "icone/Nebbia.png";
			case NEGA_MAGIA:
				return "icone/NegaMagia.png";
			case GRIDO_DI_BATTAGLIA:
				return "icone/GridoDiBattaglia.png";
			case TERREMOTO:
				return "icone/Terremoto.png";
			case TEMPESTA:
				return "icone/Tempesta.png";
			case TERRORE:
				return "icone/Terrore.png";
			default:
				throw new IllegalArgumentException("Nessuna icona per " + tipo);
		}
	}

	/**
	 * L'immagine della classe di personaggio, per la schermata di gioco
	 *
	 * @throws IllegalArgumentException se la classe non ha un'immagine
	 */
	public static String personaggio(TipoPersonaggio classe) {
		String nome = PERSONAGGI.get(classe);
		if (nome == null) {
			throw new IllegalArgumentException("Nessuna immagine per " + classe);
		}
		return nome;
	}

	/**
	 * L'icona della classe per il riquadro del gruppo, o null se non ne ha una
	 */
	public static String iconaDelPersonaggio(TipoPersonaggio classe) {
		return ICONE.get(classe);
	}

	/**
	 * L'immagine di un oggetto che si trova nelle locazioni, o null per gli artefatti, che non si mostrano
	 */
	public static String oggetto(TipoBottino tipo) {
		return OGGETTI.get(tipo);
	}

	/**
	 * L'immagine di un aspetto speciale di artefatto (i libri tecnici, la spada normalmente nascosta)
	 *
	 * @throws IllegalArgumentException se l'aspetto non ha un'immagine
	 */
	public static String oggetto(AspettoArtefatto aspetto) {
		String nome = ASPETTI.get(aspetto);
		if (nome == null) {
			throw new IllegalArgumentException("Nessuna immagine per l'aspetto " + aspetto);
		}
		return nome;
	}

	/**
	 * Lo sfondo di una locazione, o null se quel tipo non ne ha uno
	 */
	public static String locazione(TipoLocazione tipo) {
		if (tipo == TipoLocazione.BOSCO) {
			return "locazioni/Foresta.png";
		}
		if (tipo.getCategoriaLocazione() == CategoriaLocazione.CASTELLO) {
			return "locazioni/Castello.png";
		}
		if (tipo.getCategoriaLocazione() == CategoriaLocazione.CITTA) {
			return "locazioni/Citta.png";
		}
		switch (tipo) {
			case GROTTA:
				return "locazioni/Grotta.png";
			case LOCANDA:
				return "locazioni/Locanda.png";
			case PALUDE:
				return "locazioni/Palude.png";
			case RADURA:
				return "locazioni/Radura.png";
			case ROVINE:
				return "locazioni/Rovine.png";
			case TEMPIO:
				return "locazioni/Tempio.png";
			case ALCHIMISTA:
				return "locazioni/Alchimista.png";
			case ARMAIOLO:
				return "locazioni/Armaiolo.png";
			case VENDITORE_DI_PERGAMENE:
				return "locazioni/VenditoreDiPergamene.png";
			case INCANTATORE:
				return "locazioni/Incantatore.png";
			default:
				return null;
		}
	}

	/**
	 * Se l'immagine è a metà risoluzione e va ingrandita dello zoom: lo sono quelle di <code>personaggi/</code>, di <code>fondi/</code> e dei file nella radice
	 * (i loghi); le altre (<code>fondinon2x2/</code>, le icone, le mappe) si usano com'è
	 */
	public static boolean conZoom(String risorsa) {
		return risorsa.startsWith("personaggi/") || risorsa.startsWith("fondi/") || !risorsa.contains("/");
	}
}
