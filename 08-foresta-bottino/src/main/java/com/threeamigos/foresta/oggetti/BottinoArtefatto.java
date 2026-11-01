package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.GeneratoreArtefatti;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Nome;
import com.threeamigos.foresta.tipi.TipoBottino;

import java.util.Objects;
import java.util.Optional;

/**
 * Un artefatto che il gruppo trova in una locazione. L'artefatto resta il pezzo di equipaggiamento vero, che si conserva e si
 * salva con il suo {@code ArtefattoMD}; il bottino lo avvolge e dice cos'è per chi lo trova.
 * <p>
 * Ne esistono due casi:
 * <ul>
 * <li>una spada, uno scudo, un elmo... per terra ({@link #nuovo(FormaDelBottino)}): nasce con il suo artefatto, al livello del
 * mondo, e la locazione lo descrive con il nome della forma ("una spada");</li>
 * <li>un artefatto già fatto, come quello custodito in un tempio ({@link #giaFatto(Artefatto)}): la locazione lo descrive con il
 * suo nome completo e, quando lo si prende, lo si mostra con la rivelazione ({@link #isDaRivelare()}).</li>
 * </ul>
 * In tutti e due i casi, se l'artefatto ha una {@link FormaDelBottino} il bottino ha la sua classe e il suo sprite; altrimenti
 * (un ingrediente magico, un talismano...) è un generico {@code ARTEFATTO}. Prenderlo dà i punti del suo valore (vedi
 * {@link RaccoltaBottino}).
 *
 * @author Stefano Reksten
 */
public class BottinoArtefatto implements Bottino {

	private final Artefatto artefatto;
	// Null se l'artefatto non ha una forma da bottino
	private final FormaDelBottino forma;
	private final boolean daRivelare;

	private BottinoArtefatto(Artefatto artefatto, FormaDelBottino forma, boolean daRivelare) {
		this.artefatto = Objects.requireNonNull(artefatto);
		this.forma = forma;
		this.daRivelare = daRivelare;
	}

	/**
	 * Una spada, uno scudo... per terra: con un artefatto casuale di quella forma, al livello del mondo.
	 */
	public static BottinoArtefatto nuovo(FormaDelBottino forma) {
		Artefatto artefatto = GeneratoreArtefatti.istanza().generaArtefatto(forma.getTipoArtefatto(), Statistiche.getLivello());
		return new BottinoArtefatto(artefatto, forma, false);
	}

	/**
	 * Un artefatto già fatto che il gruppo trova in una locazione (quello custodito in un tempio): la forma è quella del suo tipo,
	 * se ne ha una.
	 */
	public static BottinoArtefatto giaFatto(Artefatto artefatto) {
		return new BottinoArtefatto(artefatto, FormaDelBottino.di(artefatto.getTipo()).orElse(null), true);
	}

	@Override
	public Optional<Artefatto> getArtefatto() {
		return Optional.of(artefatto);
	}

	/**
	 * Vero per un artefatto già fatto, che la locazione descrive con il suo nome completo e che alla raccolta si mostra con la
	 * rivelazione.
	 */
	@Override
	public boolean isDaRivelare() {
		return daRivelare;
	}

	@Override
	public int getQuantita() {
		return 1;
	}

	@Override
	public String getNomePlurale() {
		return nome().getPlurale();
	}

	@Override
	public String getNomeSingolare() {
		return nome().getSingolare();
	}

	@Override
	public String getAIS() {
		return Lingue.corrente().articoloIndeterminativo(nome(), false);
	}

	@Override
	public String getAIP() {
		return Lingue.corrente().articoloIndeterminativo(nome(), true);
	}

	@Override
	public String getADS() {
		return Lingue.corrente().articoloDeterminativo(nome(), false);
	}

	@Override
	public String getADP() {
		return Lingue.corrente().articoloDeterminativo(nome(), true);
	}

	// La forma dell'artefatto (spada, scudo...) o, senza, "artefatto": nomi e articoli dai testi di TipoBottino
	private Nome nome() {
		return Nome.di(getClasse());
	}

	@Override
	public TipoBottino getClasse() {
		return forma != null ? forma.getTipoBottino() : TipoBottino.ARTEFATTO;
	}
}
