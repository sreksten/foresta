package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Un personaggio visto dalla UI, in sola lettura (vedi VistaPartita e VistaGruppo). Lo implementa Personaggio: chi
 * lo cambia è il motore, a cui la UI lo chiede con i comandi (per esempio ComandoSpesaPuntoAbilita).
 */
public interface VistaPersonaggio {

	enum OpzioniGetNome {
		/**
		 * Es. il, la
		 */
		INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
		/**
		 * Es. un, una
		 */
		INCLUDI_ARTICOLO_INDETERMINATIVO_SINGOLARE,
		/**
		 * Es. del, della
		 */
		INCLUDI_PREPOSIZIONE_ARTICOLATA,
		/**
		 * Riporta in maiuscolo la prima lettera del risultato (non necessariamente del nome se preceduto da
		 * preposizione o articolo)
		 */
		INIZIALE_MAIUSCOLA
	}

	/**
	 * L'identità del personaggio: resta la stessa anche se l'oggetto viene ricreato dal suo modello dati
	 */
	String getUuid();

	TipoPersonaggio getClasse();

	String getNome(OpzioniGetNome... opzioni);

	/**
	 * Il nome proprio del personaggio, se ne ha uno
	 */
	Optional<String> getNomeProprio();

	String getNomeSingolare();

	boolean isPNG();

	boolean isVivo();

	/**
	 * Come è morto, per i testi; null se è vivo
	 */
	String getCausaTrapasso();

	int getLivello();

	/**
	 * L'ordinale del personaggio all'interno del gruppo, per distinguere i mostri dello stesso tipo
	 */
	int getOrdinale();

	int getPuntiEsperienza();

	int getPuntiAbilitaDisponibili();

	int getSalute();

	int getSaluteMassima();

	int getRigenerazioneSalute();

	int getMagia();

	int getMagiaMassima();

	int getRigenerazioneMagia();

	int getStanchezza();

	int getCaricoMassimo();

	int getForza();

	int getDestrezza();

	int getCostituzione();

	int getIntelligenza();

	int getSaggezza();

	int getCarisma();

	int getFortuna();

	int getCritico();

	int getPrecisione();

	int getVelocita();

	int getFurtivita();

	int getParata();

	int getResistenzaMagica();

	int getPercezione();

	int getSoggezione();

	int getFuria();

	int getCoraggio();

	int getValore();

	int getContrattazione();

	double getPotereMagico();

	int getBersagli();

	/**
	 * Gli artefatti che il personaggio porta
	 */
	Collection<? extends VistaArtefatto> getInventario();

	/**
	 * Se adesso il personaggio può prendere l'artefatto: livello, classe, peso, forza e posto libero permettendo
	 */
	boolean isEquipaggiabile(VistaArtefatto artefatto);

	/**
	 * Per i testi, i set leggendari che il personaggio ha completato: "Set completo: Corredo di RomyJona (bonus x1,5)"
	 */
	List<String> getDescrizioniSetCompleti();

}
