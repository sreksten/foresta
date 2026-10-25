package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

import java.util.List;

/**
 * Quello che la finestra di prova (solo in modalità di prova) può fare alla partita oltre a guardarla: cambiare i
 * personaggi del gruppo, far partire le missioni che si offrono in locanda o in città, scegliere gli oggetti speciali dei
 * cofani. I trucchi sui lanci dei dadi non passano di qui: vedi {@code strumenti.TrucchiDiSviluppo}.
 * <p>
 * Tutti i metodi vanno chiamati dal thread principale, dove gira la logica di gioco. Li implementa il motore
 * ({@code StrumentiDiSviluppoMotore}).
 *
 * @author Stefano Reksten
 */
public interface StrumentiDiSviluppo {

    /**
     * Dove si offre una missione.
     */
    enum DovePartono {
        LOCANDA("In locanda"),
        CITTA("In città"),
        BOSCO("Nel bosco");

        private final String descrizione;

        DovePartono(String descrizione) {
            this.descrizione = descrizione;
        }

        public String getDescrizione() {
            return descrizione;
        }
    }

    /**
     * Una missione fra quelle che possono partire in un certo posto, con il suo stato di adesso.
     */
    final class VoceMissione {

        private final MissionePredefinita classe;
        private final String nome;
        private final String descrizione;
        private final String stato;
        private final boolean forzabile;

        public VoceMissione(MissionePredefinita classe, String nome, String descrizione, String stato, boolean forzabile) {
            this.classe = classe;
            this.nome = nome;
            this.descrizione = descrizione;
            this.stato = stato;
            this.forzabile = forzabile;
        }

        public MissionePredefinita getClasse() {
            return classe;
        }

        public String getNome() {
            return nome;
        }

        /**
         * La descrizione della missione, o null se non ne ha (o è vuota).
         */
        public String getDescrizione() {
            return descrizione;
        }

        /**
         * Per esempio "da cominciare", "partenza forzata", "in corso", "completata", "fallita".
         */
        public String getStato() {
            return stato;
        }

        /**
         * Se si può forzarne la partenza adesso: non è partita e non c'è già una partenza forzata.
         */
        public boolean isForzabile() {
            return forzabile;
        }
    }

    /**
     * Un oggetto speciale che un cofano può essere costretto a dare: il codice da passare al trucco
     * {@code COFANO_OGGETTO} e la descrizione per chi sceglie.
     */
    final class OggettoSpeciale {

        private final String codice;
        private final String descrizione;

        public OggettoSpeciale(String codice, String descrizione) {
            this.codice = codice;
            this.descrizione = descrizione;
        }

        public String getCodice() {
            return codice;
        }

        @Override
        public String toString() {
            return descrizione;
        }

        @Override
        public boolean equals(Object altro) {
            return altro instanceof OggettoSpeciale && ((OggettoSpeciale) altro).codice.equals(codice);
        }

        @Override
        public int hashCode() {
            return codice.hashCode();
        }
    }

    /**
     * Le scorte del gruppo del giocatore, a parte gli incantesimi (che sono per classe).
     */
    enum Scorta {
        MONETE("Monete"),
        PREZIOSI("Pietre preziose"),
        POZIONI_SALUTE("Pozioni della salute"),
        POZIONI_SALUTE_GRANDI("Pozioni della salute grandi"),
        POZIONI_MAGIA("Pozioni della magia"),
        POZIONI_MAGIA_GRANDI("Pozioni della magia grandi"),
        POZIONI_AUMENTO_LIVELLO("Pozioni di aumento di livello");

        private final String descrizione;

        Scorta(String descrizione) {
            this.descrizione = descrizione;
        }

        public String getDescrizione() {
            return descrizione;
        }
    }

    /**
     * Il valore di un attributo del personaggio così com'è (senza i modificatori), per mostrarlo in una casella.
     */
    int getValoreBase(String uuidPersonaggio, TipoAttributo attributo);

    /**
     * Gli attributi che si possono impostare: i primari, la salute, la magia e la stanchezza.
     */
    List<TipoAttributo> getAttributiModificabili();

    /**
     * Imposta un attributo di un personaggio del gruppo (la salute e la magia restano entro i massimi). Un personaggio
     * che non c'è, o un attributo che non si può impostare, è un errore.
     */
    void impostaAttributo(String uuidPersonaggio, TipoAttributo attributo, int valore);

    /**
     * Fa morire o resuscitare un personaggio del gruppo.
     */
    void impostaVivo(String uuidPersonaggio, boolean vivo);

    /**
     * Porta un personaggio del gruppo a quel livello (anche più basso: i punti abilità restano come sono).
     */
    void impostaLivello(String uuidPersonaggio, int livello);

    /**
     * Aggiunge punti abilità disponibili a un personaggio del gruppo.
     */
    void aggiungiPuntiAbilita(String uuidPersonaggio, int punti);

    /**
     * Le missioni (predefinite) che possono partire in locanda o in città, con lo stato di adesso.
     */
    List<VoceMissione> getMissioni(DovePartono dove);

    /**
     * Fa partire la missione al prossimo controllo che il gruppo fa in una locanda o in una città, a seconda di dove
     * parte, senza aspettarne le condizioni (la visita tranquilla, la pausa, le visite precedenti). Una missione già partita,
     * o che non si può forzare, è un errore.
     */
    void forzaPartenza(MissionePredefinita classe);

    /**
     * Gli oggetti che un cofano può essere costretto a dare, con i leggendari del catalogo.
     */
    List<OggettoSpeciale> getOggettiSpeciali();

    /**
     * Quanto ha il gruppo di quella scorta.
     */
    int getScorta(Scorta scorta);

    /**
     * Porta la scorta del gruppo a quel valore (almeno zero).
     */
    void impostaScorta(Scorta scorta, int valore);

    /**
     * Quante pergamene di quella classe ha il gruppo.
     */
    int getIncantesimi(TipoIncantesimo classe);

    /**
     * Porta le pergamene di quella classe a quel valore (almeno zero).
     */
    void impostaIncantesimi(TipoIncantesimo classe, int valore);

    /**
     * Dà quei punti esperienza a tutti i personaggi vivi del gruppo e al mondo, come dopo un combattimento.
     */
    void aggiungiEsperienzaAlGruppo(int punti);

    /**
     * Il livello del mondo, da cui dipendono mostri, loot e leggendari.
     */
    int getLivelloDelMondo();

    /**
     * I punti esperienza del mondo.
     */
    int getEsperienzaDelMondo();

    /**
     * Porta il livello del mondo a quel livello (anche più basso) e i punti esperienza a quelli che servono per arrivarci. Salendo
     * è come se il mondo fosse salito di livello (artefatti, annuncio); scendendo cambia solo da lì in poi quello che si genera.
     */
    void impostaLivelloDelMondo(int livello);

}
