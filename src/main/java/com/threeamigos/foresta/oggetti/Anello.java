package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.motore.modellodati.TipoModificatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.CostruttoreArtefatto;
import com.threeamigos.foresta.tools.Misc;

public class Anello extends OggettoBase implements Oggetto {

	private final boolean anelloMagico;
	private final int tipo;
	private boolean notificato;

	public Anello() {
		super();
		int tiroDado = Dado.tira(6);
		Logger.log("Tiro del dado per anello magico: " + tiroDado);
		if (tiroDado == 6) {
			anelloMagico = true;
			tipo = Dado.tira(3);
			Logger.log("Anello magico di tipo " + tipo);
		} else {
			anelloMagico = false;
			tipo = -1;
		}
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
			if (gruppo.getNumeroPersonaggiVivi() > 1 && azione == null) {
				BusEventi.pubblica(new NotificaTestoFrase("Chi lo vuole indossare?"));
				return false;
			} else {
				if (azione == null || azione == Comando.TIMER) {
					return false;
				}

				String nome;
				ModificatoreAttributo modificatore;
				String descrizione;
				if (tipo == 1) {
					String[] appartenenzePossibili = new String[]{ "della Valchiria", "del Grifone", "del Paladino", "del Centurione", "della Manticora", "del Minotauro", "della Viverna" };
					int indiceAppartenenza = (int) (Math.random() * appartenenzePossibili.length);
					nome = "un Anello magico " + appartenenzePossibili[indiceAppartenenza];
					modificatore = new ModificatoreAttributo(TipoAttributo.VALORE, TipoModificatore.AUMENTO_FISSO, Costanti.ANELLO_MAGICO_AGGIUNTA_VALORE);
					descrizione = "che aumenta il Valore";
				} else if (tipo == 2) {
					String[] appartenenzePossibili = new String[]{ "del Berserker", "della Fenice", "del Pegaso", "della Salamandra", "del Gladiatore", "dell'Ippogrifo", "dell'Esploratore" };
					int indiceAppartenenza = (int) (Math.random() * appartenenzePossibili.length);
					nome = "un Anello magico " + appartenenzePossibili[indiceAppartenenza];
					modificatore = new ModificatoreAttributo(TipoAttributo.CORAGGIO, TipoModificatore.AUMENTO_PERCENTUALE, Costanti.ANELLO_MAGICO_AGGIUNTA_CORAGGIO);
					descrizione = "che aumenta il Coraggio";
				} else {
					String[] appartenenzePossibili = new String[]{ "della Sirena", "della Sfinge", "dell'Arcangelo", "della Gorgone", "del Dullahan", "della Lamia", "del Basilisco" };
					int indiceAppartenenza = (int) (Math.random() * appartenenzePossibili.length);
					nome = "un Anello magico " + appartenenzePossibili[indiceAppartenenza];
					modificatore = new ModificatoreAttributo(TipoAttributo.CARISMA, TipoModificatore.AUMENTO_PERCENTUALE, Costanti.ANELLO_MAGICO_AGGIUNTA_CARISMA);
					descrizione = "che aumenta il Carisma";
				}

				Artefatto anelloMagico = CostruttoreArtefatto.istanza()
						.setTipo(TipoArtefatto.ANELLO)
						.setNome(nome)
						.setDescrizione(descrizione)
						.setLivello(1)
						.setDanniBase(0)
						.setCostoAcquisto(20)
						.setPeso(0.1)
						.setModificatore(modificatore)
						.costruisci();
				if (azione == Comando.GRUPPO) {
					Artefatto.riponiNelGruppo(gruppo, anelloMagico);
				} else {
					Personaggio p = gruppo.getPersonaggio(azione);
					if (Artefatto.consegna(gruppo, p, anelloMagico)) {
						BusEventi.pubblica(new NotificaTestoFrase(p.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " indossa l'anello."));
					}
				}

				Statistiche.addPunti(Costanti.ANELLO_MAGICO_PUNTEGGIO);
				GestoreProgressione.acquisisciArtefattoMinore();
			}
		}
		return super.prendi(gruppo, azione);
	}
}
