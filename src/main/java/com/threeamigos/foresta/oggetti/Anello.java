package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.motore.modellodati.TipoModificatore;
import com.threeamigos.foresta.tools.CostruttoreArtefatto;
import com.threeamigos.foresta.tools.Misc;

import java.util.Optional;

public class Anello extends OggettoBase implements Oggetto {

	private final boolean anelloMagico;
	private final int tipo;
	// Se è magico, l'artefatto nasce con l'anello: serve già per scegliere chi lo prende
	private final Artefatto artefatto;
	private boolean notificato;

	public Anello() {
		super();
		int tiroDado = Dado.tira(6);
		Logger.log("Tiro del dado per anello magico: " + tiroDado);
		if (tiroDado == 6) {
			anelloMagico = true;
			tipo = Dado.tira(3);
			Logger.log("Anello magico di tipo " + tipo);
			artefatto = creaAnelloMagico(tipo);
		} else {
			// Gli anelli non magici restano senza effetto. TODO più avanti si potranno vendere come gemme
			anelloMagico = false;
			tipo = -1;
			artefatto = null;
		}
	}

	private static Artefatto creaAnelloMagico(int tipo) {
		String nome;
		ModificatoreAttributo modificatore;
		String descrizione;
		if (tipo == 1) {
			String[] appartenenzePossibili = new String[]{ "della Valchiria", "del Grifone", "del Paladino", "del Centurione", "della Manticora", "del Minotauro", "della Viverna" };
			int indiceAppartenenza = Dado.tiraAncheAUnaFaccia(appartenenzePossibili.length) - 1;
			nome = "un Anello magico " + appartenenzePossibili[indiceAppartenenza];
			modificatore = new ModificatoreAttributo(TipoAttributo.VALORE, TipoModificatore.AUMENTO_FISSO, Costanti.ANELLO_MAGICO_AGGIUNTA_VALORE);
			descrizione = "che aumenta il Valore";
		} else if (tipo == 2) {
			String[] appartenenzePossibili = new String[]{ "del Berserker", "della Fenice", "del Pegaso", "della Salamandra", "del Gladiatore", "dell'Ippogrifo", "dell'Esploratore" };
			int indiceAppartenenza = Dado.tiraAncheAUnaFaccia(appartenenzePossibili.length) - 1;
			nome = "un Anello magico " + appartenenzePossibili[indiceAppartenenza];
			modificatore = new ModificatoreAttributo(TipoAttributo.CORAGGIO, TipoModificatore.AUMENTO_PERCENTUALE, Costanti.ANELLO_MAGICO_AGGIUNTA_CORAGGIO);
			descrizione = "che aumenta il Coraggio";
		} else {
			String[] appartenenzePossibili = new String[]{ "della Sirena", "della Sfinge", "dell'Arcangelo", "della Gorgone", "del Dullahan", "della Lamia", "del Basilisco" };
			int indiceAppartenenza = Dado.tiraAncheAUnaFaccia(appartenenzePossibili.length) - 1;
			nome = "un Anello magico " + appartenenzePossibili[indiceAppartenenza];
			modificatore = new ModificatoreAttributo(TipoAttributo.CARISMA, TipoModificatore.AUMENTO_PERCENTUALE, Costanti.ANELLO_MAGICO_AGGIUNTA_CARISMA);
			descrizione = "che aumenta il Carisma";
		}
		return CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.ANELLO)
				.setNome(nome)
				.setDescrizione(descrizione)
				.setLivello(1)
				.setDanniBase(0)
				.setCostoAcquisto(20)
				.setPeso(0.1)
				.setModificatore(modificatore)
				.costruisci();
	}

	@Override
	public Optional<Artefatto> getArtefatto() {
		return Optional.ofNullable(artefatto);
	}

	public String getAIS() {
		return Misc.UN;
	}

	public String getAIP() {
		return Misc.ALCUNI;
	}

	public String getADS() {
		return Misc.L_APOSTROFO;
	}

	public String getADP() {
		return Misc.GLI;
	}

	public String getNomeSingolare() {
		return "anello";
	}

	public String getNomePlurale() {
		return "anelli";
	}

	public ClassiOggetto getClasse() {
		return ClassiOggetto.ANELLO;
	}

	@Override
	public boolean prendi(GruppoGiocatore gruppo, Comando azione) {
		if (anelloMagico) {
			if (!notificato) {
				StringBuilder sb = new StringBuilder("Questo è un anello magico, che fa aumentare ");
				if (tipo == 1)
					sb.append("il valore in combattimento");
				else if (tipo == 2)
					sb.append("il coraggio");
				else
					sb.append("il carisma");
				sb.append("!");
				BusEventi.pubblica(new NotificaTestoFrase(sb.toString()));
				notificato = true;
			}
			if (!Artefatto.raccogli(gruppo, azione, artefatto)) {
				return false;
			}
			Statistiche.addPunti(Costanti.ANELLO_MAGICO_PUNTEGGIO);
			GestoreProgressione.acquisisciArtefattoMinore();
		}
		return super.prendi(gruppo, azione);
	}
}
