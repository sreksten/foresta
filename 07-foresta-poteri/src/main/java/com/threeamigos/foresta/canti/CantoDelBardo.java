package com.threeamigos.foresta.canti;

import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoCanto;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.ArrayList;
import java.util.List;

/**
 * Chi sa cantare e quali canti può cantare adesso. Le capacità dipendono dal {@link TipoPersonaggio}, non dalla classe
 * Java: nessun altro personaggio, nemmeno il Viandante, canta.
 */
public final class CantoDelBardo {

	private CantoDelBardo() {
	}

	/**
	 * @return true se la classe sa cantare (Bardo, Cantastorie e il bardo della locanda, che è un bardo a tutti gli effetti)
	 */
	public static boolean conosciutoDa(TipoPersonaggio classe) {
		return classe == TipoPersonaggio.BARDO || classe == TipoPersonaggio.CANTASTORIE
				|| classe == TipoPersonaggio.BARDO_DELLE_LOCANDE;
	}

	/**
	 * Se il gruppo ha imparato l'Urlo della Banshee: dal giorno in cui il bardo della locanda glielo insegna, con
	 * l'intermezzo che segue il viaggio della moglie (vedi IntermezzoUrloDellaBanshee), il gruppo lo ricorda.
	 */
	public static boolean isUrloDellaBansheeAppreso() {
		return CantiImparati.haImparato(TipoCanto.URLO_DELLA_BANSHEE);
	}

	/**
	 * Se la classe conosce quel canto: i canti di base li conosce chi sa cantare; l'Urlo della Banshee il bardo della
	 * locanda sempre, il Bardo e la Cantastorie solo da quando il gruppo l'ha imparato.
	 */
	public static boolean conosciutoDa(TipoPersonaggio classe, TipoCanto tipo) {
		if (!conosciutoDa(classe)) {
			return false;
		}
		return tipo != TipoCanto.URLO_DELLA_BANSHEE || classe == TipoPersonaggio.BARDO_DELLE_LOCANDE || isUrloDellaBansheeAppreso();
	}

	/**
	 * @return true se il personaggio è vivo, sa cantare quel canto, non è {@code SILENZIATO}, ha la MAGIA per cantarlo e non è in ricarica
	 */
	public static boolean puoCantare(Personaggio personaggio, TipoCanto tipo) {
		return personaggio.isVivo() && conosciutoDa(personaggio.getClasse(), tipo)
				&& !personaggio.hasEffettoDiStato(TipoEffettoDiStato.SILENZIATO)
				&& personaggio.getMagia() >= tipo.getCostoLancio()
				&& !isInRicarica(personaggio, tipo);
	}

	/**
	 * Se il personaggio ha cantato da poco quel canto e deve aspettare ancora qualche turno per cantarlo di nuovo.
	 */
	public static boolean isInRicarica(Personaggio personaggio, TipoCanto tipo) {
		return personaggio.getRicarica(tipo.name()) > 0;
	}

	/**
	 * Mette il canto in ricarica per i turni del suo tipo: la chiama chi l'ha appena fatto partire.
	 */
	public static void avviaRicarica(Personaggio personaggio, TipoCanto tipo) {
		personaggio.setRicarica(tipo.name(), tipo.getRicarica());
	}

	/**
	 * I canti del personaggio vivo che conosce ma che, essendo in ricarica, non può ancora cantare, e che servirebbero adesso (vedi
	 * {@link #cantiUtili}): la UI li mostra spenti, con i turni che mancano.
	 */
	public static List<TipoCanto> cantiInRicarica(Personaggio personaggio, Gruppo gruppo, Gruppo gruppoAvversario) {
		List<TipoCanto> canti = new ArrayList<>();
		if (!personaggio.isVivo() || gruppoAvversario.getPersonaggiVivi().isEmpty()) {
			return canti;
		}
		for (TipoCanto tipo : TipoCanto.values()) {
			if (conosciutoDa(personaggio.getClasse(), tipo) && isInRicarica(personaggio, tipo)
					&& (tipo != TipoCanto.CURATIVO || gruppo.piuFerito().isPresent())) {
				canti.add(tipo);
			}
		}
		return canti;
	}

	/**
	 * I canti che il personaggio può cantare adesso <b>nello scontro</b>: quelli che può cantare (vedi
	 * {@link #cantiPossibili}) e che servono a qualcosa. I canti si cantano solo contro avversari vivi, quindi senza non
	 * ce n'è nessuno; il canto curativo serve se qualcuno del gruppo è ferito.
	 */
	public static List<TipoCanto> cantiUtili(Personaggio personaggio, Gruppo gruppo, Gruppo gruppoAvversario) {
		List<TipoCanto> canti = new ArrayList<>();
		if (gruppoAvversario.getPersonaggiVivi().isEmpty()) {
			return canti;
		}
		for (TipoCanto tipo : cantiPossibili(personaggio)) {
			if (tipo != TipoCanto.CURATIVO || gruppo.piuFerito().isPresent()) {
				canti.add(tipo);
			}
		}
		return canti;
	}

	/**
	 * I canti che il personaggio può cantare adesso, nell'ordine di {@link TipoCanto}
	 */
	public static List<TipoCanto> cantiPossibili(Personaggio personaggio) {
		List<TipoCanto> canti = new ArrayList<>();
		for (TipoCanto tipo : TipoCanto.values()) {
			if (puoCantare(personaggio, tipo)) {
				canti.add(tipo);
			}
		}
		return canti;
	}
}
