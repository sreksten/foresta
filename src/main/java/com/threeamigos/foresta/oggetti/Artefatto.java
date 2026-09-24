package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.OggettoConCosto;
import com.threeamigos.foresta.motore.OggettoConPeso;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.modellodati.RaritaArtefatto;
import com.threeamigos.foresta.motore.modellodati.SupertipoArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;
import com.threeamigos.foresta.personaggi.MotivoRifiutoEquipaggiamento;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Artefatto implements Oggetto, OggettoConCosto, OggettoConPeso {

	protected final ArtefattoMD md;

	public Artefatto(ArtefattoMD artefattoMD) {
		this.md = artefattoMD;
	}

	/**
	 * Ricostruisce la sottoclasse corretta (es. ArmaFisica) in base al supertipo,
	 * così da non perdere il tipo concreto quando un Artefatto viene rimaterializzato dal suo ArtefattoMD.
	 */
	public static Artefatto di(ArtefattoMD md) {
		if (md.getTipo().getSupertipo() == SupertipoArtefatto.ARMA) {
			return new ArmaFisica(md);
		}
		return new Artefatto(md);
	}

	public final TipoArtefatto getTipo() {
		return md.getTipo();
	}

	public final String getNome() {
		return md.getNome();
	}

	public final String getDescrizione() {
		return md.getDescrizione();
	}

	public final Optional<String> getNomeProprio() {
		return Optional.ofNullable(md.getNomeProprio());
	}

	/**
	 * Forma completa da usare nei testi, es. "Diavolina, la spada di fuoco, che brucia i nemici".
	 */
	public final String getNomeCompleto() {
		return md.getNomeCompleto();
	}

	/**
	 * Armi, scudi, elmi e armature. Accessori, libro magico e pergamene non si incantano.
	 */
	public boolean isIncantabile() {
		switch (getTipo().getSupertipo()) {
			case ARMA:
			case SCUDO:
			case ELMO:
			case ARMATURA:
				return true;
			default:
				return false;
		}
	}

	public final RaritaArtefatto getRarita() {
		return md.getRarita();
	}

	/**
	 * Numero massimo di effetti in totale (incantamenti più modificatori di attributo), secondo la rarità:
	 * livello − 1 per un comune (quindi nessuno a livello 1), livello per un raro, livello + 1 per un
	 * leggendario, fino a un tetto di 3, 4 o 5.
	 */
	public int getEffettiMassimi() {
		if (!isIncantabile()) {
			return 0;
		}
		return getRarita().getEffettiMassimi(getLivello());
	}

	public final int getLivello() {
		return md.getLivello();
	}

	public final int getCostoAcquisto() {
		return md.getCostoAcquisto();
	}

	public double getPeso() {
		return md.getPeso();
	}

	/**
	 * Modificatori permanenti agli attributi dei personaggi
	 */
	public Collection<ModificatoreAttributo> getModificatori() {
		return md.getModificatori();
	}

	/**
	 * Incantamenti fatti sull'artefatto
	 */
	public Collection<Incantamento> getIncantamenti() {
		return md.getIncantamenti();
	}

	public boolean isFigliVisibili() {
		return md.isFigliVisibili();
	}

	public void mostraFigli() {
		md.setFigliVisibili(true);
	}

	public void nascondiFigli() {
		md.setFigliVisibili(false);
	}

	/**
	 * Interfaccia Oggetto
	 */
	@Override
	public boolean prendi(GruppoGiocatore gruppo, Comando comando) {
		return raccogli(gruppo, comando, this);
	}

	@Override
	public Optional<Artefatto> getArtefatto() {
		return Optional.of(this);
	}

	/**
	 * Chi raccoglie un artefatto (vedi artefatti_e_incantamenti.md, §2 "Loot"). Senza comando si guardano
	 * i candidati, cioè i personaggi vivi che possono equipaggiarlo ({@link #candidati}):
	 * <ul>
	 * <li>nessuno (anche perché chi c'è ha già occupato quello slot, o è una pergamena): va nel gruppo;</li>
	 * <li>uno solo: va direttamente a lui;</li>
	 * <li>più di uno: sceglie il giocatore (Stato.SCELTA_DESTINATARIO_OGGETTO) e si restituisce false.</li>
	 * </ul>
	 * Con GRUPPO va nell'inventario del gruppo, con un personaggio a quel personaggio.
	 *
	 * @return true se l'artefatto è stato raccolto
	 */
	public static boolean raccogli(GruppoGiocatore gruppo, Comando comando, Artefatto artefatto) {
		if (comando == null || comando == Comando.TIMER) {
			List<Comando> candidati = candidati(gruppo, artefatto);
			if (candidati.isEmpty()) {
				gruppo.addArtefatto(artefatto);
				BusEventi.pubblica(new NotificaTestoFrase(comeSoggetto(artefatto)
						+ " viene messo nell'inventario del gruppo: nessuno può prenderlo."));
				return true;
			}
			if (candidati.size() > 1) {
				BusEventi.pubblica(new NotificaTestoFrase("Chi prende " + artefatto.getNomeCompleto() + "?"));
				return false;
			}
			comando = candidati.get(0);
		}
		if (comando == Comando.GRUPPO) {
			riponiNelGruppo(gruppo, artefatto);
			return true;
		}
		Personaggio p = gruppo.getPersonaggio(comando);
		if (consegna(gruppo, p, artefatto)) {
			BusEventi.pubblica(new NotificaTestoFrase(p.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA)
					+ ' ' + artefatto.getTipo().getUtilizzo() + ' ' + artefatto.getNomeCompleto() + '.'));
		}
		return true;
	}

	/**
	 * I comandi dei personaggi vivi che possono equipaggiare l'artefatto: slot libero, livello
	 * sufficiente, mani libere, carico. Le pergamene non hanno mai candidati.
	 */
	public static List<Comando> candidati(GruppoGiocatore gruppo, Artefatto artefatto) {
		List<Comando> candidati = new ArrayList<>();
		List<Personaggio> personaggi = gruppo.getPersonaggi();
		for (int i = 0; i < personaggi.size(); i++) {
			Personaggio personaggio = personaggi.get(i);
			if (personaggio.isVivo() && !personaggio.puoEquipaggiare(artefatto).isPresent()) {
				candidati.add(Comando.ofPersonaggio(i));
			}
		}
		return candidati;
	}

	/**
	 * Mette l'artefatto raccolto nell'inventario del gruppo, per scelta del giocatore.
	 */
	public static void riponiNelGruppo(GruppoGiocatore gruppo, Artefatto artefatto) {
		gruppo.addArtefatto(artefatto);
		BusEventi.pubblica(new NotificaTestoFrase(comeSoggetto(artefatto)
				+ " viene messo nell'inventario del gruppo."));
	}

	/**
	 * Dà l'artefatto al personaggio se lo può equipaggiare (peso, slot, livello...), altrimenti lo mette
	 * nell'inventario del gruppo spiegando il perché. Restituisce true se l'ha preso il personaggio.
	 */
	public static boolean consegna(GruppoGiocatore gruppo, Personaggio personaggio, Artefatto artefatto) {
		Optional<MotivoRifiutoEquipaggiamento> motivoRifiuto = personaggio.puoEquipaggiare(artefatto);
		if (!motivoRifiuto.isPresent()) {
			personaggio.addArtefatto(artefatto);
			return true;
		}
		gruppo.addArtefatto(artefatto);
		// TODO oggi i personaggi del gruppo non si vedono a video: quando ci saranno, il
		// rifiuto andrà mostrato anche lì (es. un fumetto sul personaggio).
		BusEventi.pubblica(new NotificaTestoParagrafo(comeSoggetto(artefatto)
				+ " resta nell'inventario del gruppo: "
				+ personaggio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE)
				+ ' ' + motivoRifiuto.get().getSpiegazione() + '.'));
		return false;
	}

	/**
	 * Forma completa con l'iniziale maiuscola, da usare come soggetto a inizio frase: se contiene
	 * un inciso ("Diavolina, la spada di fuoco, che brucia i nemici") lo chiude con una virgola.
	 */
	private static String comeSoggetto(Artefatto artefatto) {
		String nome = artefatto.getNomeCompleto();
		nome = nome.substring(0, 1).toUpperCase() + nome.substring(1);
		return nome.contains(", ") ? nome + ',' : nome;
	}

	public int getQuantita() {
		return 1;
	}

	public String getNomePlurale() {
		return "artefatti";
	}

	public String getNomeSingolare() {
		return "artefatto";
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

	public ClassiOggetto getClasse() {
		return ClassiOggetto.ARTEFATTO;
	}

	public ArtefattoMD getModelloDati() {
		return md;
	}

}
