package com.threeamigos.foresta.trofei;

import com.threeamigos.foresta.eventi.interni.InternoAmiciziaStretta;
import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.eventi.interni.InternoCorruzioneRiuscita;
import com.threeamigos.foresta.eventi.interni.InternoOggettoRaccolto;
import com.threeamigos.foresta.eventi.interni.InternoPastoConsumatoInLocanda;
import com.threeamigos.foresta.motore.tipi.TipoTrofeo;
import com.threeamigos.foresta.oggetti.ClassiOggetto;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;

import java.util.function.Supplier;

/**
 * I trofei esistenti, ognuno con il suo trigger.
 */
public enum ClasseTrofeo {

	// Chi viene respinto dall'oste per mancanza di monete non mangia, e non conta
	SBEVAZZONE(() -> new TrofeoAContatore<>(TipoTrofeo.SBEVAZZONE,
			InternoPastoConsumatoInLocanda.class, evento -> 1, 100)),
	AMMAZZAGOBLIN(() -> new TrofeoAContatore<>(TipoTrofeo.AMMAZZAGOBLIN,
			InternoAvversarioSconfitto.class, evento -> evento.getClasse() == ClassePersonaggio.GOBLIN ? 1 : 0, 100)),
	AMICO_DI_TUTTI(() -> new TrofeoAContatore<>(TipoTrofeo.AMICO_DI_TUTTI,
			InternoAmiciziaStretta.class, evento -> 1, 100)),
	// Conta le corruzioni riuscite, una per volta, comunque sia composto il gruppo avversario
	CORRUTTORE(() -> new TrofeoAContatore<>(TipoTrofeo.CORRUTTORE,
			InternoCorruzioneRiuscita.class, evento -> 1, 100)),
	// I boss: basta sconfiggerli una volta
	UCCIDI_IL_DRAGO(() -> boss(TipoTrofeo.UCCIDI_IL_DRAGO, ClassePersonaggio.DRAGO)),
	UCCIDI_LA_STREGA(() -> boss(TipoTrofeo.UCCIDI_LA_STREGA, ClassePersonaggio.STREGA)),
	UCCIDI_IL_LICH(() -> boss(TipoTrofeo.UCCIDI_IL_LICH, ClassePersonaggio.LICH)),
	UCCIDI_L_IDRA(() -> boss(TipoTrofeo.UCCIDI_L_IDRA, ClassePersonaggio.IDRA)),
	UCCIDI_IL_MINOTAURO_GIGANTE(() -> boss(TipoTrofeo.UCCIDI_IL_MINOTAURO_GIGANTE, ClassePersonaggio.MINOTAURO_GIGANTE)),
	// I tesori degli avversari: quanto si trova in una locazione incustodita non conta
	RAPINATORE(() -> refurtiva(TipoTrofeo.RAPINATORE, ClassiOggetto.MONETA)),
	LADRO_DI_PREZIOSI(() -> refurtiva(TipoTrofeo.LADRO_DI_PREZIOSI, ClassiOggetto.GEMMA)),
	ARSENIO_LUPIN(() -> refurtiva(TipoTrofeo.ARSENIO_LUPIN, ClassiOggetto.CORONA)),
	// I cofani contano anche se incustoditi, come nelle grotte
	ESPERTO_SCASSINATORE(() -> new TrofeoAContatore<>(TipoTrofeo.ESPERTO_SCASSINATORE,
			InternoOggettoRaccolto.class, evento -> evento.getClasse() == ClassiOggetto.COFANO ? evento.getQuantita() : 0, 100));

	private final Supplier<Trofeo> supplier;

	ClasseTrofeo(Supplier<Trofeo> supplier) {
		this.supplier = supplier;
	}

	public Trofeo getIstanza() {
		return supplier.get();
	}

	private static Trofeo refurtiva(TipoTrofeo tipo, ClassiOggetto classe) {
		return new TrofeoAContatore<>(tipo, InternoOggettoRaccolto.class,
				evento -> evento.isCustodito() && evento.getClasse() == classe ? evento.getQuantita() : 0, 100);
	}

	private static Trofeo boss(TipoTrofeo tipo, ClassePersonaggio classe) {
		return new TrofeoAContatore<>(tipo, InternoAvversarioSconfitto.class, evento -> evento.getClasse() == classe ? 1 : 0, 1);
	}
}
