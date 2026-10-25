package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.abilita.AbilitaInnateDiClasse;
import com.threeamigos.foresta.bestiario.RegistroBestiario;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.interfacce.*;
import com.threeamigos.foresta.locazioni.Bosco;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.mappa.CoordinateMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.NotiziaMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.MoltiplicatoriDiClasse;
import com.threeamigos.foresta.tipi.*;
import com.threeamigos.foresta.trofei.ClasseTrofeo;
import com.threeamigos.foresta.trofei.RegistroTrofei;

import java.util.List;
import java.util.Set;

/**
 * La vista in sola lettura della partita che il motore dà alla UI (vedi VistaPartita). Non tiene stato: a ogni
 * chiamata delega ai gruppi correnti e alle classi statiche del motore, così vale anche dopo una partita nuova o
 * riletta.
 */
public class VistaPartitaMotore implements VistaPartita {

	private final VistaMappa mappa = new Mappa();

	@Override
	public VistaGruppoGiocatore getGruppoGiocatore() {
		return GruppoGiocatore.getIstanza();
	}

	@Override
	public VistaGruppo getGruppoAvversario() {
		return GruppoAvversario.getIstanza();
	}

	@Override
	public List<? extends VistaPersonaggio> getOrdineDiCombattimento() {
		return StatoDelCombattimento.getIstanza().getOrdineDelRound();
	}

	@Override
	public VistaMappa getMappa() {
		return mappa;
	}

	@Override
	public List<Missione> getMissioniAttive() {
		return RegistroMissioni.getMissioniAttive();
	}

	@Override
	public List<Missione> getMissioniCompletate() {
		return RegistroMissioni.getMissioniCompletate();
	}

	@Override
	public List<Missione> getMissioniFallite() {
		return RegistroMissioni.getMissioniFallite();
	}

	@Override
	public List<NotiziaMD> getUltimeNotizie() {
		return Notizie.getUltimeNotizie();
	}

	@Override
	public int getGiorno() {
		return LineaTemporale.getGiorno();
	}

	@Override
	public int getPuntiEsperienza() {
		return Statistiche.getPuntiEsperienza();
	}

	@Override
	public int getPuntiEsperienzaPerProssimoLivello() {
		return Statistiche.getPuntiEsperienzaPerProssimoLivello();
	}

	@Override
	public int getMostriUccisi(TipoPersonaggio tipoPersonaggio) {
		return Statistiche.getMostriUccisi(tipoPersonaggio);
	}

	@Override
	public boolean isTrofeoVinto(TipoTrofeo trofeo) {
		return RegistroTrofei.isVinto(trofeo);
	}

	@Override
	public int getObiettivoTrofeo(TipoTrofeo trofeo) {
		return ClasseTrofeo.di(trofeo).getObiettivo();
	}

	@Override
	public int getProgressoTrofeo(TipoTrofeo trofeo) {
		return ClasseTrofeo.di(trofeo).getProgresso();
	}

	@Override
	public boolean isVoceDelBestiarioSbloccata(TipoPersonaggio classe) {
		return RegistroBestiario.isSbloccata(classe);
	}

	@Override
	public Set<String> getCaratteristicheScoperte(TipoPersonaggio classe) {
		return RegistroBestiario.getCaratteristicheScoperte(classe);
	}

	@Override
	public List<MoltiplicatoreDiClasse> getMoltiplicatoriDiClasse(TipoPersonaggio classe) {
		return MoltiplicatoriDiClasse.di(classe);
	}

	@Override
	public List<AbilitaInnataDiClasse> getAbilitaInnate(TipoPersonaggio classe) {
		return AbilitaInnateDiClasse.di(classe);
	}

	@Override
	public String getNomeDelPersonaggioDiCitta(TipoPersonaggio mestiere) {
		return RegistroPersonaggi.getNomeDelPersonaggioDiCitta(mestiere);
	}

	@Override
	public String getNomeSingolare(TipoPersonaggio tipoPersonaggio) {
		return FabbricaPersonaggi.nomeSingolare(tipoPersonaggio);
	}

	@Override
	public String getNomePlurale(TipoPersonaggio tipoPersonaggio) {
		return FabbricaPersonaggi.nomePlurale(tipoPersonaggio);
	}

	@Override
	public boolean isAiutoAbilitato() {
		return ModelloDati.getIstanza().isAiutoAbilitato();
	}

	private static final class Mappa implements VistaMappa {

		@Override
		public int getDimensioneX() {
			return Foresta.getDimensioneX();
		}

		@Override
		public int getDimensioneY() {
			return Foresta.getDimensioneY();
		}

		@Override
		public TipoLocazione getLocazione(CoordinateMD coordinate) {
			return Foresta.getLocazione(coordinate);
		}

		@Override
		public int getVarianteBosco(CoordinateMD coordinate) {
			return Bosco.getVarianteMappa(Foresta.getLocazioneMD(coordinate));
		}

		@Override
		public boolean isLocazioneConosciuta(CoordinateMD coordinate) {
			return Foresta.isLocazioneConosciuta(coordinate);
		}

		@Override
		public boolean isLocazioneVisitata(CoordinateMD coordinate) {
			return Foresta.isLocazioneVisitata(coordinate);
		}

		@Override
		public List<CoordinateMD> getCoordinateDaSegnalare() {
			return CostruttoreDellaForesta.getCoordinateDaSegnalare();
		}

		@Override
		public int getVersioneMappa() {
			return Foresta.getVersioneMappa();
		}

		@Override
		public int getMinXConosciuta() {
			return Foresta.getMinXConosciuta();
		}

		@Override
		public int getMaxXConosciuta() {
			return Foresta.getMaxXConosciuta();
		}

		@Override
		public int getMinYConosciuta() {
			return Foresta.getMinYConosciuta();
		}

		@Override
		public int getMaxYConosciuta() {
			return Foresta.getMaxYConosciuta();
		}

		@Override
		public String getNomeDaMostrare(CoordinateMD coordinate) {
			return Foresta.getNomeDaMostrare(coordinate);
		}

		@Override
		public List<String> getNomiMissioniDaMostrare(CoordinateMD coordinate) {
			return CostruttoreDellaForesta.getNomiMissioniDaMostrare(coordinate);
		}
	}
}
