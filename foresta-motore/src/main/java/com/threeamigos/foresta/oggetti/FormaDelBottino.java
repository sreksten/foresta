package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;

import java.util.Optional;

/**
 * Le forme di artefatto che il gruppo trova per terra come bottino: una spada, uno scudo, un elmo... Per ciascuna dice di quale
 * {@link TipoArtefatto} è l'artefatto, quale {@link TipoOggetto} è come bottino (e quindi quale sprite ha nella locazione) e
 * come si chiama, al singolare e al plurale e con i suoi articoli, quando la locazione dice ciò che il gruppo vede ("una spada",
 * "alcuni scudi").
 *
 * @author Stefano Reksten
 */
public enum FormaDelBottino {

	SPADA(TipoArtefatto.SPADA, TipoOggetto.SPADA, "spada", "spade",
			Articoli.UNA, Articoli.ALCUNE, Articoli.LA, Articoli.LE),
	SPADONE(TipoArtefatto.SPADONE, TipoOggetto.SPADONE, "spadone", "spadoni",
			Articoli.UNO, Articoli.ALCUNI, Articoli.LO, Articoli.GLI),
	FALCE(TipoArtefatto.FALCE, TipoOggetto.FALCE, "falce", "falci",
			Articoli.UNA, Articoli.ALCUNE, Articoli.LA, Articoli.LE),
	MAZZA(TipoArtefatto.MAZZA, TipoOggetto.MAZZA, "mazza", "mazze",
			Articoli.UNA, Articoli.ALCUNE, Articoli.LA, Articoli.LE),
	ASCIA(TipoArtefatto.ASCIA, TipoOggetto.ASCIA, "ascia", "asce",
			Articoli.UN_APOSTROFO, Articoli.ALCUNE, Articoli.L_APOSTROFO, Articoli.LE),
	LANCIA(TipoArtefatto.LANCIA, TipoOggetto.LANCIA, "lancia", "lance",
			Articoli.UNA, Articoli.ALCUNE, Articoli.LA, Articoli.LE),
	BASTONE_MAGICO(TipoArtefatto.BASTONE_MAGICO, TipoOggetto.BASTONE_MAGICO, "bastone magico", "bastoni magici",
			Articoli.UN, Articoli.ALCUNI, Articoli.IL, Articoli.I),
	LIBRO_MAGICO(TipoArtefatto.LIBRO_MAGICO, TipoOggetto.LIBRO_MAGICO, "libro magico", "libri magici",
			Articoli.UN, Articoli.ALCUNI, Articoli.IL, Articoli.I),
	SCUDO(TipoArtefatto.SCUDO, TipoOggetto.SCUDO, "scudo", "scudi",
			Articoli.UNO, Articoli.ALCUNI, Articoli.LO, Articoli.GLI),
	ELMO(TipoArtefatto.ELMO, TipoOggetto.ELMO, "elmo", "elmi",
			Articoli.UN, Articoli.ALCUNI, Articoli.L_APOSTROFO, Articoli.GLI),
	MASCHERA(TipoArtefatto.MASCHERA, TipoOggetto.MASCHERA, "maschera", "maschere",
			Articoli.UNA, Articoli.ALCUNE, Articoli.LA, Articoli.LE),
	ARMATURA(TipoArtefatto.ARMATURA, TipoOggetto.ARMATURA, "armatura", "armature",
			Articoli.UNA, Articoli.ALCUNE, Articoli.L_APOSTROFO, Articoli.LE),
	SCHINIERI(TipoArtefatto.SCHINIERI, TipoOggetto.SCHINIERI, "schinieri", "schinieri",
			Articoli.UN_PAIO_DI, Articoli.ALCUNE_PAIA_DI, Articoli.GLI, Articoli.GLI);

	private final TipoArtefatto tipoArtefatto;
	private final TipoOggetto tipoOggetto;
	private final String nomeSingolare;
	private final String nomePlurale;
	private final String articoloIndeterminativoSingolare;
	private final String articoloIndeterminativoPlurale;
	private final String articoloDeterminativoSingolare;
	private final String articoloDeterminativoPlurale;

	FormaDelBottino(TipoArtefatto tipoArtefatto, TipoOggetto tipoOggetto, String nomeSingolare, String nomePlurale,
			String articoloIndeterminativoSingolare, String articoloIndeterminativoPlurale,
			String articoloDeterminativoSingolare, String articoloDeterminativoPlurale) {
		this.tipoArtefatto = tipoArtefatto;
		this.tipoOggetto = tipoOggetto;
		this.nomeSingolare = nomeSingolare;
		this.nomePlurale = nomePlurale;
		this.articoloIndeterminativoSingolare = articoloIndeterminativoSingolare;
		this.articoloIndeterminativoPlurale = articoloIndeterminativoPlurale;
		this.articoloDeterminativoSingolare = articoloDeterminativoSingolare;
		this.articoloDeterminativoPlurale = articoloDeterminativoPlurale;
	}

	/**
	 * Di quale tipo è l'artefatto di questa forma.
	 */
	public TipoArtefatto getTipoArtefatto() {
		return tipoArtefatto;
	}

	/**
	 * Quale tipo di bottino è (la stessa costante di {@code TipoOggetto}).
	 */
	public TipoOggetto getTipoOggetto() {
		return tipoOggetto;
	}

	public String getNomeSingolare() {
		return nomeSingolare;
	}

	public String getNomePlurale() {
		return nomePlurale;
	}

	/**
	 * L'articolo indeterminativo singolare, già con lo spazio o l'apostrofo finale ("una ", "un'").
	 */
	public String getAIS() {
		return articoloIndeterminativoSingolare;
	}

	public String getAIP() {
		return articoloIndeterminativoPlurale;
	}

	public String getADS() {
		return articoloDeterminativoSingolare;
	}

	public String getADP() {
		return articoloDeterminativoPlurale;
	}

	/**
	 * La forma degli artefatti di quel tipo, se ne hanno una: gli ingredienti magici, i talismani e gli anelli, per
	 * esempio, non ne hanno.
	 */
	public static Optional<FormaDelBottino> di(TipoArtefatto tipo) {
		for (FormaDelBottino forma : values()) {
			if (forma.tipoArtefatto == tipo) {
				return Optional.of(forma);
			}
		}
		return Optional.empty();
	}

	/**
	 * La forma del bottino di quel tipo, se ne ha una: un cofano o delle monete non ne hanno.
	 */
	public static Optional<FormaDelBottino> di(TipoOggetto tipo) {
		for (FormaDelBottino forma : values()) {
			if (forma.tipoOggetto == tipo) {
				return Optional.of(forma);
			}
		}
		return Optional.empty();
	}
}
