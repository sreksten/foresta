package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.modellodati.Notizia;
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

	List<Missione> getMissioniAttive();

	List<Missione> getMissioniCompletate();

	List<Missione> getMissioniFallite();

	/**
	 * Le ultime notizie delle locande, dalla più recente: la lista è viva, chi la tiene ne faccia una copia
	 */
	List<Notizia> getUltimeNotizie();

	int getGiorno();

	int getPuntiEsperienza();

	int getPuntiEsperienzaPerProssimoLivello();

	int getMostriUccisi(TipoPersonaggio tipoPersonaggio);

	boolean isTrofeoVinto(TipoTrofeo trofeo);

	/**
	 * Se sono accesi i cartigli dell'aiuto (vedi DisplayableCanvasBarraIcone)
	 */
	boolean isAiutoAbilitato();

}
