package com.threeamigos.foresta.modificatori;

import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.modificatori.tipi.BersaglioModificatore;
import com.threeamigos.foresta.modificatori.tipi.TipoModificatore;
import com.threeamigos.foresta.modificatori.tipi.UnitaDurataModificatore;
import com.threeamigos.foresta.serializzazione.LettoreCampi;
import com.threeamigos.foresta.serializzazione.MappaProprieta;
import com.threeamigos.foresta.serializzazione.Serializzabile;

import java.io.IOException;
import java.util.Objects;

import static com.threeamigos.foresta.serializzazione.Serializzabile.composto;
import static com.threeamigos.foresta.serializzazione.Serializzabile.facoltativo;

/**
 * Modifica di un certo scostamento (positivo o negativo) il valore base di un attributo di un personaggio.
 * Può durare un certo tempo (in turni di combattimento o in locazioni) e, se scritto su un'arma, colpire il
 * bersaglio invece di chi la porta. È immutabile: chi lo fa scalare ne sostituisce la copia (vedi conDurata).
 * Su un artefatto la durata è quella con cui il modificatore sarà applicato; su un personaggio è il tempo che resta.
 * <p>
 * Qui ci sono solo i dati: i testi con cui lo si racconta al giocatore ("Coraggio -20% per 3 turni") li scrive
 * {@code TestoDeiModificatori}, in foresta-contratti.
 *
 * @author Stefano Reksten
 */
public class ModificatoreAttributoMD {

    // Quale attributo cambia
    private final TipoAttributo tipoAttributo;
    // Come lo cambia (somma, percentuale...)
    private final TipoModificatore tipoModificatore;
    // Di quanto, nell'unità del tipo di modificatore
    private final double quantita;
    // Da dove viene, in parole, per chi lo legge; mai null
    private final String note;
    // In che cosa si misura la durata, o permanente
    private final UnitaDurataModificatore unitaDurata;
    // Quante unità dura; 0 se è permanente
    private final int durata;
    // Su chi agisce: chi lo porta o il bersaglio del colpo
    private final BersaglioModificatore bersaglio;
    // Se si può togliere (per esempio con una purificazione)
    private final boolean rimovibile;

    /**
     * Un modificatore che l'Alba Sacra può togliere (vedi l'altro costruttore).
     *
     * @param note una nota libera, spesso vuota (null vale come vuota); il carattere "|" si toglie
     * @param durata 0 per un modificatore permanente, almeno 1 per uno a tempo (nell'unità indicata)
     * @throws IllegalArgumentException se la durata non è coerente con l'unità: un permanente deve avere durata 0 e uno a
     * tempo almeno 1
     */
    public ModificatoreAttributoMD(TipoAttributo tipoAttributo, TipoModificatore tipoModificatore,
                                 double quantita, String note, UnitaDurataModificatore unitaDurata, int durata,
                                 BersaglioModificatore bersaglio) {
        this(tipoAttributo, tipoModificatore, quantita, note, unitaDurata, durata, bersaglio, true);
    }

    /**
     * @param rimovibile false se nemmeno l'Alba Sacra può toglierlo prima della scadenza (l'ignominia della fuga)
     * @see #ModificatoreAttributoMD(TipoAttributo, TipoModificatore, double, String, UnitaDurataModificatore, int, BersaglioModificatore)
     */
    public ModificatoreAttributoMD(TipoAttributo tipoAttributo, TipoModificatore tipoModificatore,
                                 double quantita, String note, UnitaDurataModificatore unitaDurata, int durata,
                                 BersaglioModificatore bersaglio, boolean rimovibile) {
        if (unitaDurata == UnitaDurataModificatore.PERMANENTE ? durata != 0 : durata < 1) {
            throw new IllegalArgumentException("Un modificatore permanente ha durata 0, uno a tempo almeno 1: "
                    + unitaDurata + " " + durata);
        }
        this.tipoAttributo = tipoAttributo;
        this.tipoModificatore = tipoModificatore;
        this.quantita = quantita;
        // Mai null: una nota assente è vuota, come dopo un salvataggio
        String pulita = Serializzabile.pulisciCampo(note);
        this.note = pulita == null ? "" : pulita;
        this.unitaDurata = unitaDurata;
        this.durata = durata;
        this.bersaglio = bersaglio;
        this.rimovibile = rimovibile;
    }

    /**
     * Un modificatore permanente, con una nota, che agisce su chi porta l'artefatto.
     */
    public ModificatoreAttributoMD(TipoAttributo tipoAttributo, TipoModificatore tipoModificatore,
                                 double quantita, String note) {
        this(tipoAttributo, tipoModificatore, quantita, note, UnitaDurataModificatore.PERMANENTE, 0,
                BersaglioModificatore.PORTATORE);
    }

    /**
     * Un modificatore permanente, senza note, che agisce su chi porta l'artefatto.
     */
    public ModificatoreAttributoMD(TipoAttributo tipoAttributo, TipoModificatore tipoModificatore,
                                 double quantita) {
        this(tipoAttributo, tipoModificatore, quantita, "");
    }

    /**
     * L'attributo del personaggio che il modificatore cambia.
     */
    public TipoAttributo getTipoAttributo() {
        return tipoAttributo;
    }

    /**
     * Come si applica la quantità: per esempio come variazione fissa, in percentuale
     * o come valore a cui portare l'attributo.
     */
    public TipoModificatore getTipoModificatoreAttributo() {
        return tipoModificatore;
    }

    /**
     * La quantità del modificatore (vedi {@link #getTipoModificatoreAttributo()}).
     */
    public double getQuantita() {
        return quantita;
    }

    /**
     * Da dove viene il modificatore, in parole (vuoto se non lo dice).
     */
    public String getNote() {
        return note;
    }

    /**
     * L'unità in cui si misura la durata, o permanente.
     */
    public UnitaDurataModificatore getUnitaDurata() {
        return unitaDurata;
    }

    /**
     * @return la durata (0 se è permanente)
     */
    public int getDurata() {
        return durata;
    }

    /**
     * Chi subisce il modificatore: chi porta l'artefatto o, per un'arma, il bersaglio che colpisce.
     */
    public BersaglioModificatore getBersaglio() {
        return bersaglio;
    }

    /**
     * Dice se l'Alba Sacra può toglierlo prima della scadenza.
     */
    public boolean isRimovibile() {
        return rimovibile;
    }

    /**
     * Dice se il modificatore ha una scadenza.
     */
    public boolean isTemporaneo() {
        return unitaDurata != UnitaDurataModificatore.PERMANENTE;
    }

    /**
     * Lo stesso modificatore con un'altra durata, nella stessa unità. Non vale per i permanenti.
     */
    public ModificatoreAttributoMD conDurata(int nuovaDurata) {
        return new ModificatoreAttributoMD(tipoAttributo, tipoModificatore, quantita, note, unitaDurata, nuovaDurata,
                bersaglio, rimovibile);
    }

    /**
     * Lo stesso modificatore, ma per chi porta l'artefatto: la copia che un'arma mette sul bersaglio colpito.
     */
    public ModificatoreAttributoMD suPortatore() {
        return new ModificatoreAttributoMD(tipoAttributo, tipoModificatore, quantita, note, unitaDurata, durata,
                BersaglioModificatore.PORTATORE, rimovibile);
    }

    /**
     * Gli stessi dati con un'altra quantità (per i bonus moltiplicati dei set)
     */
    public ModificatoreAttributoMD conQuantita(double nuovaQuantita) {
        return new ModificatoreAttributoMD(tipoAttributo, tipoModificatore, nuovaQuantita, note, unitaDurata, durata,
                bersaglio, rimovibile);
    }

    /**
     * Se è lo stesso modificatore a parte la durata: così un doppione rinfresca quello che c'è invece di sommarsi
     */
    public boolean stessoEffetto(ModificatoreAttributoMD altro) {
        return tipoAttributo == altro.tipoAttributo && tipoModificatore == altro.tipoModificatore
                && Double.compare(quantita, altro.quantita) == 0 && Objects.equals(note, altro.note)
                && unitaDurata == altro.unitaDurata && bersaglio == altro.bersaglio && rimovibile == altro.rimovibile;
    }

    /**
     * Il modificatore come campo di un salvataggio: attributo, tipo, quantità, unità, durata, bersaglio, se è rimovibile e nota, separati da
     * {@value MappaProprieta#SEPARATORE}. La nota, testo libero, è l'ultima: se contiene il separatore non sposta gli altri.
     * Lo rilegge {@link #daCampo(LettoreCampi)}.
     */
    public String comeCampo() {
        return composto(
                tipoAttributo,
                tipoModificatore,
                quantita,
                unitaDurata,
                durata,
                bersaglio,
                rimovibile,
                facoltativo(note));
    }

    /**
     * Rilegge il prossimo campo scritto da {@link #comeCampo()}. Una nota vuota si rilegge come "", come la mette il
     * costruttore senza nota, così il modificatore riletto è equals all'originale.
     */
    public static ModificatoreAttributoMD daCampo(LettoreCampi campi) throws IOException {
        LettoreCampi sottocampi = campi.sottocampi(7, 8);
        TipoAttributo tipoAttributo = sottocampi.enumerato(TipoAttributo.class);
        TipoModificatore tipoModificatore = sottocampi.enumerato(TipoModificatore.class);
        double quantita = sottocampi.decimale();
        UnitaDurataModificatore unitaDurata = sottocampi.enumerato(UnitaDurataModificatore.class);
        int durata = sottocampi.intero();
        BersaglioModificatore bersaglio = sottocampi.enumerato(BersaglioModificatore.class);
        boolean rimovibile = sottocampi.booleano();
        String note = sottocampi.haAltriCampi() ? sottocampi.testo() : "";
        return new ModificatoreAttributoMD(tipoAttributo, tipoModificatore, quantita, note, unitaDurata, durata, bersaglio, rimovibile);
    }

    /**
     * Due modificatori sono uguali se hanno tutti gli stessi dati, durata compresa.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ModificatoreAttributoMD)) {
            return false;
        }
        ModificatoreAttributoMD other = (ModificatoreAttributoMD) obj;
        return stessoEffetto(other) && durata == other.durata;
    }

    /**
     * Coerente con {@link #equals(Object)}.
     */
    @Override
    public int hashCode() {
        return Objects.hash(tipoAttributo, tipoModificatore, quantita, note, unitaDurata, durata, bersaglio, rimovibile);
    }

}
