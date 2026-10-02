package com.threeamigos.foresta.motore.tipi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * I trofei che il giocatore può vincere. A differenza delle missioni non scadono mai e
 * valgono da una partita all'altra: una volta vinto, un trofeo non si perde più.
 */
public enum TipoTrofeo {

	PERDIGIORNO("Perdigiorno", "Vinci tutti gli altri trofei", SupertipoTrofeo.SPECIALE),
	CACCIATORE_DI_TAGLIE("Cacciatore di taglie", "Completa 50 missioni", SupertipoTrofeo.MISSIONE),
	AMMAZZAGOBLIN("Ammazzagoblin", "Uccidi 100 goblin", SupertipoTrofeo.UCCISIONE),
	UCCIDI_IL_DRAGO("Salvatore della Foresta", "Sconfiggi il Drago", SupertipoTrofeo.UCCISIONE),
	UCCIDI_LA_STREGA("Il calderone è vuoto", "Sconfiggi la Strega", SupertipoTrofeo.UCCISIONE),
	UCCIDI_IL_LICH("Riposa in pace, stavolta sul serio", "Sconfiggi il Lich", SupertipoTrofeo.UCCISIONE),
	UCCIDI_L_IDRA("Una testa alla volta", "Sconfiggi l'Idra", SupertipoTrofeo.UCCISIONE),
	UCCIDI_IL_MINOTAURO_GIGANTE("Più grosso è, più rumore fa", "Sconfiggi il Minotauro Gigante", SupertipoTrofeo.UCCISIONE),
	RAPINATORE("Rapinatore", "Impossessati di 100 monete appartenenti agli avversari", SupertipoTrofeo.BOTTINO),
	LADRO_DI_PREZIOSI("Ladro di preziosi", "Impossessati di 100 gemme appartenenti agli avversari", SupertipoTrofeo.BOTTINO),
	ARSENIO_LUPIN("Arsenio Lupin", "Impossessati di 100 corone appartenenti agli avversari", SupertipoTrofeo.BOTTINO),
	ESPERTO_SCASSINATORE("Esperto scassinatore", "Apri 100 cofani", SupertipoTrofeo.BOTTINO),
	CACCIATORE_DI_TESORI("Cacciatore di tesori", "Impossessati di 100 artefatti in possesso di avversari", SupertipoTrofeo.BOTTINO),
	ESPERTO_CACCIATORE_DI_TESORI("Esperto cacciatore di tesori", "Impossessati di 100 artefatti di livello 5 o superiore in possesso degli avversari", SupertipoTrofeo.BOTTINO),
	RIGATTIERE("Rigattiere", "Compra 100 artefatti di livello 2 o inferiore dall'armaiolo", SupertipoTrofeo.ACQUISTO),
	COLLEZIONISTA("Collezionista", "Compra 100 artefatti di livello 5 o superiore dall'armaiolo", SupertipoTrofeo.ACQUISTO),
	STUDIOSO("Studioso", "Compra 100 pergamene dal venditore di pergamene", SupertipoTrofeo.ACQUISTO),
	BOMBAROLO("Bombarolo", "Compra 100 incantesimi dall'alchimista", SupertipoTrofeo.ACQUISTO),
	CARTOGRAFO("Cartografo", "Compra la mappa completa della foresta dall'alchimista", SupertipoTrofeo.ACQUISTO),
	TRAFFICONE("Trafficone", "Incanta 50 artefatti dall'incantatore", SupertipoTrofeo.ACQUISTO),
	AMICO_DI_TUTTI("Amico di tutti", "Fai amicizia 100 volte", SupertipoTrofeo.DIPLOMAZIA),
	CORRUTTORE("Corruttore", "Porta a termine 100 tentativi di corruzione", SupertipoTrofeo.DIPLOMAZIA),
	SBEVAZZONE("Sbevazzone", "Visita 100 locande nella foresta o in città", SupertipoTrofeo.VARIE);

	private final String nome;
	private final String descrizione;
	private final SupertipoTrofeo supertipo;

	TipoTrofeo(String nome, String descrizione, SupertipoTrofeo supertipo) {
		this.nome = nome;
		this.descrizione = descrizione;
		this.supertipo = supertipo;
	}

	public String getNome() {
		return nome;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public SupertipoTrofeo getSupertipo() {
		return supertipo;
	}

	/**
	 * Tutti i trofei raggruppati per tipologia, nell'ordine di {@link SupertipoTrofeo}.
	 */
	public static List<TipoTrofeo> perTipologia() {
		List<TipoTrofeo> trofei = new ArrayList<>(Arrays.asList(values()));
		trofei.sort(Comparator.comparing(TipoTrofeo::getSupertipo));
		return trofei;
	}
}
