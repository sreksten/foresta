package com.threeamigos.foresta.trofei;

import com.threeamigos.foresta.eventi.interni.InternoAmiciziaStretta;
import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.eventi.interni.InternoCorruzioneRiuscita;
import com.threeamigos.foresta.eventi.interni.InternoMissioneCompletata;
import com.threeamigos.foresta.eventi.interni.InternoOggettoRaccolto;
import com.threeamigos.foresta.eventi.interni.InternoPastoConsumatoInLocanda;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneAcquistoArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneAcquistoConsumabile;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneIncantatura;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.tipi.TipoConsumabile;
import com.threeamigos.foresta.tipi.TipoOggetto;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoTrofeo;

import java.util.function.IntPredicate;
import java.util.function.Supplier;

/**
 * I trofei esistenti, ognuno con il suo trigger.
 */
public enum ClasseTrofeo {

	PERDIGIORNO(TrofeoPerdigiorno::new),
	// Per ora contano tutte le missioni completate, anche le secondarie
	CACCIATORE_DI_TAGLIE(() -> new TrofeoAContatore<>(TipoTrofeo.CACCIATORE_DI_TAGLIE,
			InternoMissioneCompletata.class, evento -> 1, 50)),
	// Chi viene respinto dall'oste per mancanza di monete non mangia, e non conta
	SBEVAZZONE(() -> new TrofeoAContatore<>(TipoTrofeo.SBEVAZZONE,
			InternoPastoConsumatoInLocanda.class, evento -> 1, 100)),
	AMMAZZAGOBLIN(() -> new TrofeoAContatore<>(TipoTrofeo.AMMAZZAGOBLIN,
			InternoAvversarioSconfitto.class, evento -> evento.getClasse() == TipoPersonaggio.GOBLIN ? 1 : 0, 100)),
	AMICO_DI_TUTTI(() -> new TrofeoAContatore<>(TipoTrofeo.AMICO_DI_TUTTI,
			InternoAmiciziaStretta.class, evento -> 1, 100)),
	// Conta le corruzioni riuscite, una per volta, comunque sia composto il gruppo avversario
	CORRUTTORE(() -> new TrofeoAContatore<>(TipoTrofeo.CORRUTTORE,
			InternoCorruzioneRiuscita.class, evento -> 1, 100)),
	// I boss: basta sconfiggerli una volta
	UCCIDI_IL_DRAGO(() -> boss(TipoTrofeo.UCCIDI_IL_DRAGO, TipoPersonaggio.DRAGO)),
	UCCIDI_LA_STREGA(() -> boss(TipoTrofeo.UCCIDI_LA_STREGA, TipoPersonaggio.STREGA)),
	UCCIDI_IL_LICH(() -> boss(TipoTrofeo.UCCIDI_IL_LICH, TipoPersonaggio.LICH)),
	UCCIDI_L_IDRA(() -> boss(TipoTrofeo.UCCIDI_L_IDRA, TipoPersonaggio.IDRA)),
	UCCIDI_IL_MINOTAURO_GIGANTE(() -> boss(TipoTrofeo.UCCIDI_IL_MINOTAURO_GIGANTE, TipoPersonaggio.MINOTAURO_GIGANTE)),
	// I tesori degli avversari: quanto si trova in una locazione incustodita non conta
	RAPINATORE(() -> refurtiva(TipoTrofeo.RAPINATORE, TipoOggetto.MONETA)),
	LADRO_DI_PREZIOSI(() -> refurtiva(TipoTrofeo.LADRO_DI_PREZIOSI, TipoOggetto.PIETRA_PREZIOSA)),
	ARSENIO_LUPIN(() -> refurtiva(TipoTrofeo.ARSENIO_LUPIN, TipoOggetto.CORONA)),
	// I cofani contano anche se incustoditi, come nelle grotte
	ESPERTO_SCASSINATORE(() -> new TrofeoAContatore<>(TipoTrofeo.ESPERTO_SCASSINATORE,
			InternoOggettoRaccolto.class, evento -> evento.getClasse() == TipoOggetto.COFANO ? evento.getQuantita() : 0, 100)),
	// Gli artefatti in città si comprano solo da armaiolo (tutto tranne gli ingredienti magici) e venditore di pergamene
	RIGATTIERE(() -> acquistoDallArmaiolo(TipoTrofeo.RIGATTIERE, livello -> livello <= 2)),
	COLLEZIONISTA(() -> acquistoDallArmaiolo(TipoTrofeo.COLLEZIONISTA, livello -> livello >= 5)),
	STUDIOSO(() -> new TrofeoAContatore<>(TipoTrofeo.STUDIOSO, NotificaApprovazioneAcquistoArtefatto.class,
			evento -> artefattoComprato(evento).getTipo().isIngrediente() ? 1 : 0, 100)),
	// Gli artefatti degli avversari: l'oggetto della locazione, se custodito
	CACCIATORE_DI_TESORI(() -> bottino(TipoTrofeo.CACCIATORE_DI_TESORI, livello -> true)),
	ESPERTO_CACCIATORE_DI_TESORI(() -> bottino(TipoTrofeo.ESPERTO_CACCIATORE_DI_TESORI, livello -> livello >= 5)),
	BOMBAROLO(() -> new TrofeoAContatore<>(TipoTrofeo.BOMBAROLO, NotificaApprovazioneAcquistoConsumabile.class,
			evento -> consumabileComprato(evento) == TipoConsumabile.INCANTESIMO ? 1 : 0, 100)),
	CARTOGRAFO(() -> new TrofeoAContatore<>(TipoTrofeo.CARTOGRAFO, NotificaApprovazioneAcquistoConsumabile.class,
			evento -> consumabileComprato(evento) == TipoConsumabile.MAPPA_COMPLETA_FORESTA ? 1 : 0, 1)),
	TRAFFICONE(() -> new TrofeoAContatore<>(TipoTrofeo.TRAFFICONE, NotificaApprovazioneIncantatura.class,
			evento -> 1, 50));

	private final Supplier<Trofeo> supplier;

	ClasseTrofeo(Supplier<Trofeo> supplier) {
		this.supplier = supplier;
	}

	public Trofeo getIstanza() {
		return supplier.get();
	}

	/**
	 * Il trofeo di quel tipo (non iscritto agli eventi: per leggerne obiettivo e progresso).
	 */
	public static Trofeo di(TipoTrofeo tipo) {
		return valueOf(tipo.name()).getIstanza();
	}

	private static Trofeo refurtiva(TipoTrofeo tipo, TipoOggetto tipoOggetto) {
		return new TrofeoAContatore<>(tipo, InternoOggettoRaccolto.class,
				evento -> evento.isCustodito() && evento.getClasse() == tipoOggetto ? evento.getQuantita() : 0, 100);
	}

	private static Trofeo acquistoDallArmaiolo(TipoTrofeo tipo, IntPredicate livello) {
		return new TrofeoAContatore<>(tipo, NotificaApprovazioneAcquistoArtefatto.class, evento -> {
			Artefatto artefatto = artefattoComprato(evento);
			return !artefatto.getTipo().isIngrediente() && livello.test(artefatto.getLivello()) ? 1 : 0;
		}, 100);
	}

	private static Trofeo bottino(TipoTrofeo tipo, IntPredicate livello) {
		return new TrofeoAContatore<>(tipo, InternoOggettoRaccolto.class,
				evento -> evento.isCustodito() && evento.getArtefatto().filter(a -> livello.test(a.getLivello())).isPresent() ? 1 : 0, 100);
	}

	private static Artefatto artefattoComprato(NotificaApprovazioneAcquistoArtefatto evento) {
		return (Artefatto) evento.getOggettoSpostato();
	}

	private static TipoConsumabile consumabileComprato(NotificaApprovazioneAcquistoConsumabile evento) {
		return evento.getEventoRichiestaAcquistoConsumabile().getTipoConsumabile();
	}

	private static Trofeo boss(TipoTrofeo tipo, TipoPersonaggio classe) {
		return new TrofeoAContatore<>(tipo, InternoAvversarioSconfitto.class, evento -> evento.getClasse() == classe ? 1 : 0, 1);
	}
}
