package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.RegistroMissioni;

import java.util.Objects;

/**
 * Un oggetto che serve a una missione, per esempio le radici di mandragola che chiede l'alchimista: non ha una
 * classe sua, si ricorda il nome, la missione e la chiave sotto cui la missione lo conta. Lo mette nelle locazioni
 * la missione stessa (vedi {@link Missione#getOggettoInLocazione}); raccoglierlo lo conta per la missione (vedi
 * {@link MissioneAPassi#oggettoDiMissioneRaccolto}). Come ogni oggetto delle locazioni non si salva: se il gruppo
 * non lo prende, alla prossima visita la missione decide di nuovo se c'è.
 */
public class OggettoMissione extends OggettoBase implements Oggetto {

	private final String idMissione;
	private final String chiave;
	private final NomeOggetto nome;

	public OggettoMissione(String idMissione, String chiave, NomeOggetto nome, int quantita) {
		super(quantita);
		this.idMissione = Objects.requireNonNull(idMissione);
		this.chiave = Objects.requireNonNull(chiave);
		this.nome = Objects.requireNonNull(nome);
	}

	public String getIdMissione() {
		return idMissione;
	}

	public String getChiave() {
		return chiave;
	}

	@Override
	public ClassiOggetto getClasse() {
		return ClassiOggetto.OGGETTO_MISSIONE;
	}

	@Override
	public String getNomeSingolare() {
		return nome.getSingolare();
	}

	@Override
	public String getNomePlurale() {
		return nome.getPlurale();
	}

	@Override
	public String getAIS() {
		return nome.getAIS();
	}

	@Override
	public String getAIP() {
		return nome.getAIP();
	}

	@Override
	public String getADS() {
		return nome.getADS();
	}

	@Override
	public String getADP() {
		return nome.getADP();
	}

	@Override
	public boolean prendi(GruppoGiocatore gruppo, Comando azione) {
		RegistroMissioni.getMissione(idMissione)
				.filter(MissioneAPassi.class::isInstance)
				.ifPresent(m -> ((MissioneAPassi) m).oggettoDiMissioneRaccolto(chiave, quantita));
		return super.prendi(gruppo, azione);
	}
}
