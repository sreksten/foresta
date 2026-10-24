package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.artefatti.GeneratoreArtefatti;
import com.threeamigos.foresta.artefatti.SpadaNormalmenteNascosta;
import com.threeamigos.foresta.artefatti.leggendari.LibroTecnico;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoIntermezzo;

import java.util.ArrayList;
import java.util.List;

/**
 * La prima volta che il gruppo entra dall'alchimista dopo aver trovato in un cofano il Manuale di Unix System V (vedi
 * LibroTecnico): il capo dice di averlo letto e chiede dell'"incantesimo" ls, l'alchimista spiega che la versione più potente è
 * "ls -la", che fa vedere le cose nascoste, e a quel punto la .spada normalmente nascosta si rivela e passa al gruppo (vedi
 * {@link SpadaNormalmenteNascosta}). Poi l'alchimista dà il benvenuto: per questo l'oroscopo, quella volta, non scatta.
 * <p>
 * Scatta una sola volta per partita, e sta prima dell'oroscopo nell'ordine di TipoIntermezzo.
 */
public class IntermezzoSpadaNascosta implements Intermezzo {

	private static final String CHIAVE_DEL_MANUALE = "MANUALE_UNIX_SYSTEM_V";
	private static final String CAPO = "capo";
	private static final double PAUSA_DOPO_LA_RIVELAZIONE = 0.3;

	private final TipoIntermezzo tipo;
	private final String saluto;

	IntermezzoSpadaNascosta(TipoIntermezzo tipo, String saluto) {
		this.tipo = tipo;
		this.saluto = saluto;
	}

	@Override
	public String getId() {
		return tipo.name();
	}

	@Override
	public boolean deveScattare(MomentoIntermezzo momento) {
		return NegozioInScena.ALCHIMISTA.isIngresso(momento) && LibroTecnico.getLibro(CHIAVE_DEL_MANUALE)
				.filter(IntermezzoLibroTecnico::isTrovato).isPresent();
	}

	/**
	 * Le pagine; la spada passa al gruppo qui, perché l'automa le chiede una volta sola, subito dopo aver segnato l'intermezzo come
	 * scattato.
	 */
	@Override
	public List<PaginaIntermezzo> getPagine() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		NegozioInScena alchimista = NegozioInScena.ALCHIMISTA;

		List<PaginaIntermezzo> pagine = new ArrayList<>(alchimista.entraIlGruppo()
				.parlaIlCapo("Abbiamo letto il Manuale di Unix System V. Parla di un incantesimo di due lettere, ls. Potete spiegarcelo?")
				.parlaIlNegoziante("Ls? Elenca ciò che si trova in un luogo. Ma provate ad usare le sue opzioni: \"ls -a\".")
				.parlaIlCapo("E che cosa fa, \"ls -a\"?")
				.parlaIlNegoziante("Permette di vedere le cose nascoste.")
				.getPagine());

		SpadaNormalmenteNascosta spada = SpadaNormalmenteNascosta.costruisci(GeneratoreArtefatti.istanza(), Statistiche.getLivello());
		// Niente NotificaArtefattoTrovato: la sua rivelazione si vedrebbe solo nella schermata di gioco, cioè dopo il negozio.
		// La rivelazione è quella della pagina, che vola verso il capo perché l'inventario del gruppo qui non si vede
		gruppo.addArtefatto(spada);

		String idNegoziante = alchimista.getIdNegoziante();
		pagine.add(alchimista.paginaConCapo(CAPO, gruppo.getCapo().getClasse())
				.conRivelazioneDiArtefatto(spada, Statistiche.getLivello(), CAPO)
				.conBattuta(BattutaIntermezzo.di(CAPO, "Ma... è comparsa una spada dal nulla!").daSecondo(Costanti.SECONDI_RIVELAZIONE_ARTEFATTO + PAUSA_DOPO_LA_RIVELAZIONE))
				.conBattuta(BattutaIntermezzo.di(idNegoziante, "Ecco la .spada normalmente nascosta! Era nel vostro inventario da sempre, ma nessuno poteva vederla."))
				.conBattuta(BattutaIntermezzo.di(CAPO, "E ora possiamo usarla?"))
				.conBattuta(BattutaIntermezzo.di(idNegoziante, "Certamente. Ma veniamo a noi. " + saluto)));
		return pagine;
	}
}
