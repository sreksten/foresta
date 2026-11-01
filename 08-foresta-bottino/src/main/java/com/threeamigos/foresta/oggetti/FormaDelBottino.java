package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Nome;
import com.threeamigos.foresta.tipi.TipoBottino;

import java.util.Optional;

/**
 * Le forme di artefatto che il gruppo trova per terra come bottino: una spada, uno scudo, un elmo... Per ciascuna dice di quale
 * {@link TipoArtefatto} è l'artefatto, quale {@link TipoBottino} è come bottino (e quindi quale sprite ha nella locazione) e
 * come si chiama, al singolare e al plurale e con i suoi articoli (dai testi di {@code TipoBottino}), quando la locazione dice ciò che il gruppo vede ("una spada",
 * "alcuni scudi").
 *
 * @author Stefano Reksten
 */
public enum FormaDelBottino {

	SPADA(TipoArtefatto.SPADA, TipoBottino.SPADA),
	SPADONE(TipoArtefatto.SPADONE, TipoBottino.SPADONE),
	FALCE(TipoArtefatto.FALCE, TipoBottino.FALCE),
	MAZZA(TipoArtefatto.MAZZA, TipoBottino.MAZZA),
	ASCIA(TipoArtefatto.ASCIA, TipoBottino.ASCIA),
	LANCIA(TipoArtefatto.LANCIA, TipoBottino.LANCIA),
	BASTONE_MAGICO(TipoArtefatto.BASTONE_MAGICO, TipoBottino.BASTONE_MAGICO),
	LIBRO_MAGICO(TipoArtefatto.LIBRO_MAGICO, TipoBottino.LIBRO_MAGICO),
	SCUDO(TipoArtefatto.SCUDO, TipoBottino.SCUDO),
	ELMO(TipoArtefatto.ELMO, TipoBottino.ELMO),
	MASCHERA(TipoArtefatto.MASCHERA, TipoBottino.MASCHERA),
	ARMATURA(TipoArtefatto.ARMATURA, TipoBottino.ARMATURA),
	SCHINIERI(TipoArtefatto.SCHINIERI, TipoBottino.SCHINIERI);

	private final TipoArtefatto tipoArtefatto;
	private final TipoBottino tipoOggetto;

	FormaDelBottino(TipoArtefatto tipoArtefatto, TipoBottino tipoOggetto) {
		this.tipoArtefatto = tipoArtefatto;
		this.tipoOggetto = tipoOggetto;
	}

	/**
	 * Di quale tipo è l'artefatto di questa forma.
	 */
	public TipoArtefatto getTipoArtefatto() {
		return tipoArtefatto;
	}

	/**
	 * Quale tipo di bottino è (la stessa costante di {@code TipoBottino}).
	 */
	public TipoBottino getTipoBottino() {
		return tipoOggetto;
	}

	// Il nome e gli articoli vengono dai testi del tipo di bottino, nella lingua in uso (tipo_bottino_<lingua>.properties)
	private Nome nome() {
		return Nome.di(tipoOggetto);
	}

	public String getNomeSingolare() {
		return nome().getSingolare();
	}

	public String getNomePlurale() {
		return nome().getPlurale();
	}

	/**
	 * L'articolo indeterminativo singolare, già con lo spazio o l'apostrofo finale ("una ", "un'").
	 */
	public String getAIS() {
		return Lingue.corrente().articoloIndeterminativo(nome(), false);
	}

	public String getAIP() {
		return Lingue.corrente().articoloIndeterminativo(nome(), true);
	}

	public String getADS() {
		return Lingue.corrente().articoloDeterminativo(nome(), false);
	}

	public String getADP() {
		return Lingue.corrente().articoloDeterminativo(nome(), true);
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
	public static Optional<FormaDelBottino> di(TipoBottino tipo) {
		for (FormaDelBottino forma : values()) {
			if (forma.tipoOggetto == tipo) {
				return Optional.of(forma);
			}
		}
		return Optional.empty();
	}
}
