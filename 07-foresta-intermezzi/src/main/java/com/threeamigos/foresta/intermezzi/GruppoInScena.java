package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.Verso;

import java.util.ArrayList;
import java.util.List;

/**
 * Il gruppo fermo in una scena, come lo mostrano le scene dei negozi e delle città (vedi {@link ScenaNegozio}): da destra verso
 * sinistra il capo, gli ospiti vivi (un ostaggio liberato, un bardo da riportare a casa...) e il resto del gruppo, tutti rivolti verso
 * destra. Il capo ha la bocca, per le battute.
 */
public final class GruppoInScena {

	/**
	 * Quanto sta a sinistra del precedente ogni personaggio, in frazioni di schermo (la stessa di ScenaNegozio).
	 */
	public static final double DISTANZA_FRA_PERSONAGGI = 0.05;

	private GruppoInScena() {
	}

	/**
	 * Aggiunge alla pagina tutto il gruppo, col capo in {@code xCapo}. Il capo ha l'identificativo dato, gli altri lo stesso con il posto
	 * in fila in fondo ("capo1", "capo2"...).
	 *
	 * @param classeCapo la classe con cui si disegna il capo
	 */
	public static PaginaIntermezzo aggiungi(PaginaIntermezzo pagina, String idCapo, TipoPersonaggio classeCapo, double xCapo, double y) {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		List<TipoPersonaggio> fila = new ArrayList<>();
		fila.add(classeCapo);
		for (Personaggio ospite : gruppo.getOspiti()) {
			if (ospite.isVivo()) {
				fila.add(ospite.getClasse());
			}
		}
		for (Personaggio personaggio : gruppo.getPersonaggiVivi()) {
			if (personaggio != gruppo.getCapo()) {
				fila.add(personaggio.getClasse());
			}
		}
		for (int posto = 0; posto < fila.size(); posto++) {
			ElementoIntermezzo elemento = ElementoIntermezzo
					.personaggio(posto == 0 ? idCapo : idCapo + posto, fila.get(posto), xCapo - posto * DISTANZA_FRA_PERSONAGGI, y)
					.guarda(Verso.DESTRA);
			pagina.conElemento(posto == 0 ? elemento.conBocca(0.5, -0.15) : elemento);
		}
		return pagina;
	}
}
