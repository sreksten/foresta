package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.modellodati.NotiziaMD;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoTrofeo;

import java.util.List;

/**
 * Lo stato della partita come lo vede la UI: in sola lettura, al posto di GruppoGiocatore.getIstanza(), Foresta,
 * RegistroMissioni e simili. Main passa alla UI l'implementazione del motore (VistaPartitaMotore); verso il motore
 * la UI parla solo con gli eventi.
 * <p>
 * I gruppi vanno chiesti ogni volta e non tenuti da parte: una partita nuova o riletta li sostituisce.
 */
public interface VistaPartita {

	VistaGruppoGiocatore getGruppoGiocatore();

	VistaGruppo getGruppoAvversario();

	VistaMappa getMappa();

	List<? extends VistaMissione> getMissioniAttive();

	List<? extends VistaMissione> getMissioniCompletate();

	List<? extends VistaMissione> getMissioniFallite();

	/**
	 * Le ultime notizie delle locande, dalla più recente: la lista è viva, chi la tiene ne faccia una copia
	 */
	List<NotiziaMD> getUltimeNotizie();

	int getGiorno();

	int getPuntiEsperienza();

	int getPuntiEsperienzaPerProssimoLivello();

	int getMostriUccisi(TipoPersonaggio tipoPersonaggio);

	boolean isTrofeoVinto(TipoTrofeo trofeo);

	/**
	 * Quanto serve per vincere il trofeo: 100 goblin, 50 missioni, 1 per un boss
	 */
	int getObiettivoTrofeo(TipoTrofeo trofeo);

	/**
	 * A che punto è il trofeo, da 0 all'obiettivo, accumulato partita dopo partita
	 */
	int getProgressoTrofeo(TipoTrofeo trofeo);

	/**
	 * Il nome di un personaggio di quel tipo ("goblin"), per le statistiche
	 */
	String getNomeSingolare(TipoPersonaggio tipoPersonaggio);

	/**
	 * Il nome di più personaggi di quel tipo ("goblin"), per le statistiche
	 */
	String getNomePlurale(TipoPersonaggio tipoPersonaggio);

	/**
	 * Se sono accesi i cartigli dell'aiuto (vedi DisplayableCanvasBarraIcone)
	 */
	boolean isAiutoAbilitato();

}
