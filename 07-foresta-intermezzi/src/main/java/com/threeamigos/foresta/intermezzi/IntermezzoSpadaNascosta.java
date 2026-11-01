package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.tipi.NegozioInScena;
import com.threeamigos.foresta.artefatti.GeneratoreArtefatti;
import com.threeamigos.foresta.artefatti.SpadaNormalmenteNascosta;
import com.threeamigos.foresta.artefatti.leggendari.LibroTecnico;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.tipi.TipoIntermezzo;

import java.util.ArrayList;
import java.util.List;
import com.threeamigos.foresta.testi.Testi;

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
				.parlaIlCapo(Testi.formatta("intermezzi", "IntermezzoSpadaNascosta.1"))
				.parlaIlNegoziante(Testi.formatta("intermezzi", "IntermezzoSpadaNascosta.2"))
				.parlaIlCapo(Testi.formatta("intermezzi", "IntermezzoSpadaNascosta.3"))
				.parlaIlNegoziante(Testi.formatta("intermezzi", "IntermezzoSpadaNascosta.4"))
				.getPagine());

		SpadaNormalmenteNascosta spada = SpadaNormalmenteNascosta.costruisci(GeneratoreArtefatti.istanza(), Statistiche.getLivello());
		// Niente NotificaArtefattoTrovato: la sua rivelazione si vedrebbe solo nella schermata di gioco, cioè dopo il negozio.
		// La rivelazione è quella della pagina, che vola verso il capo perché l'inventario del gruppo qui non si vede
		gruppo.addArtefatto(spada);

		String idNegoziante = alchimista.getIdNegoziante();
		pagine.add(alchimista.paginaConCapo(CAPO, gruppo.getCapo().getClasse())
				.conRivelazioneDiArtefatto(spada, Statistiche.getLivello(), CAPO)
				.conBattuta(BattutaIntermezzo.di(CAPO, Testi.formatta("intermezzi", "IntermezzoSpadaNascosta.5")).daSecondo(PaginaIntermezzo.SECONDI_RIVELAZIONE_ARTEFATTO + PAUSA_DOPO_LA_RIVELAZIONE))
				.conBattuta(BattutaIntermezzo.di(idNegoziante, Testi.formatta("intermezzi", "IntermezzoSpadaNascosta.6")))
				.conBattuta(BattutaIntermezzo.di(CAPO, Testi.formatta("intermezzi", "IntermezzoSpadaNascosta.7")))
				.conBattuta(BattutaIntermezzo.di(idNegoziante, Testi.formatta("intermezzi", "IntermezzoSpadaNascosta.8", saluto))));
		return pagine;
	}
}
