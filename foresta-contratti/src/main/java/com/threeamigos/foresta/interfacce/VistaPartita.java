package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.tipi.TipoBottino;

import com.threeamigos.foresta.mappa.VistaMappa;

import com.threeamigos.foresta.modellodati.NotiziaMD;
import com.threeamigos.foresta.tipi.AbilitaInnataDiClasse;
import com.threeamigos.foresta.tipi.MoltiplicatoreDiClasse;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoTrofeo;

import java.util.List;
import java.util.Set;

/**
 * Lo stato della partita come lo vede la UI: in sola lettura, al posto di GruppoGiocatore.getIstanza(), Foresta,
 * RegistroMissioni e simili. Main passa alla UI l'implementazione del motore (VistaPartitaMotore); verso il motore
 * la UI parla solo con gli eventi.
 * <p>
 * I gruppi vanno chiesti ogni volta e non tenuti da parte: una partita nuova o riletta li sostituisce.
 */
public interface VistaPartita {

	VistaGruppoGiocatore getGruppoGiocatore();

	/**
	 * Quanto paga il gruppo per un oggetto che costa {@code costo}, con lo sconto della contrattazione.
	 */
	int prezzoAcquisto(int costo);

	/**
	 * Quanto incassa il gruppo vendendo un oggetto che costa {@code costo}.
	 */
	int prezzoVendita(int costo);

	/**
	 * L'oggetto che si vede a terra nella locazione in cui si trova il gruppo, o null se non ce n'è.
	 */
	TipoBottino getTipoBottinoInLocazione();

	VistaGruppo getGruppoAvversario();

	/**
	 * Tutti i personaggi del combattimento, dei due gruppi, nell'ordine in cui agiscono nel round (vedi
	 * StatoDelCombattimento.getOrdineDelRound): prima chi può agire, poi i caduti e chi è in panchina.
	 */
	List<? extends VistaPersonaggio> getOrdineDiCombattimento();

	VistaMappa getMappa();

	/**
	 * L'introduzione della storia, un paragrafo per elemento: la racconta il motore, non la UI.
	 */
	List<String> getStoria();

	/**
	 * Il finale della partita persa, una pagina per elemento.
	 */
	List<String> getFinaleDellaSconfitta();

	/**
	 * Il finale della partita vinta, una pagina per elemento.
	 */
	List<String> getFinaleDellaVittoria();

	/**
	 * La cassa del gruppo: monete e preziosi.
	 */
	VistaCassa getCassa();

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
	 * Se la voce del bestiario di quella classe (o di quella che ne condivide il ruolo) è sbloccata: chiusa, il bestiario ne mostra solo la
	 * silhouette e "???"
	 */
	boolean isVoceDelBestiarioSbloccata(TipoPersonaggio classe);

	/**
	 * Le chiavi (vedi CaratteristicaDiClasse.getChiave) delle caratteristiche già scoperte della voce di quella classe: di quelle che la
	 * classe ha davvero, il bestiario scrive solo queste
	 */
	Set<String> getCaratteristicheScoperte(TipoPersonaggio classe);

	/**
	 * I moltiplicatori di quella classe (carico, furtività, danni...) con la nota che spiega perché ha quel valore, nell'ordine in cui il
	 * bestiario li mostra. Sono della classe e non del ruolo: il Mago e la Maga possono averli diversi, e chi mostra la voce li chiede a tutte e due
	 */
	List<MoltiplicatoreDiClasse> getMoltiplicatoriDiClasse(TipoPersonaggio classe);

	/**
	 * Le capacità innate di quella classe (le abilità del Chierico, i canti del Bardo, il dardo arcano...), per il bestiario; vuoto per chi non ne ha
	 */
	List<AbilitaInnataDiClasse> getAbilitaInnate(TipoPersonaggio classe);

	/**
	 * Il nome, con la provenienza, del personaggio di quel mestiere (armaiolo, alchimista, venditore di pergamene,
	 * incantatore...) della città in cui è il gruppo: "Mastro Ferruccio da Ruuna". Null se il gruppo non è in una città.
	 */
	String getNomeDelPersonaggioDiCitta(TipoPersonaggio mestiere);

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
