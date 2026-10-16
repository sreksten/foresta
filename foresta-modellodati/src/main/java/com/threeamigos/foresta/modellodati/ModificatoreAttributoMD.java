package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.BersaglioModificatore;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.UnitaDurataModificatore;

import java.util.Objects;
import java.util.Locale;

/**
 * Modifica di un certo scostamento (positivo o negativo) il valore base di un attributo di un personaggio.
 * Può durare un certo tempo (in turni di combattimento o in locazioni) e, se scritto su un'arma, colpire il
 * bersaglio invece di chi la porta. È immutabile: chi lo fa scalare ne sostituisce la copia (vedi conDurata).
 * Su un artefatto la durata è quella con cui il modificatore sarà applicato; su un personaggio è il tempo che resta.
 * @author Stefano Reksten
 */
public class ModificatoreAttributoMD {

    private final TipoAttributo tipoAttributo;
    private final TipoModificatore tipoModificatore;
    private final double quantita;
    private final String note;
    private final UnitaDurataModificatore unitaDurata;
    private final int durata;
    private final BersaglioModificatore bersaglio;

    /**
     * @param note una nota libera, spesso vuota; il carattere "|" si toglie
     * @param durata 0 per un modificatore permanente, almeno 1 per uno a tempo (nell'unità indicata)
     * @throws IllegalArgumentException se la durata non è coerente con l'unità: un permanente deve avere durata 0 e uno a
     * tempo almeno 1
     */
    public ModificatoreAttributoMD(TipoAttributo tipoAttributo, TipoModificatore tipoModificatore,
                                 double quantita, String note, UnitaDurataModificatore unitaDurata, int durata,
                                 BersaglioModificatore bersaglio) {
        if (unitaDurata == UnitaDurataModificatore.PERMANENTE ? durata != 0 : durata < 1) {
            throw new IllegalArgumentException("Un modificatore permanente ha durata 0, uno a tempo almeno 1: "
                    + unitaDurata + " " + durata);
        }
        this.tipoAttributo = tipoAttributo;
        this.tipoModificatore = tipoModificatore;
        this.quantita = quantita;
        this.note = Serializzabile.pulisciCampo(note);
        this.unitaDurata = unitaDurata;
        this.durata = durata;
        this.bersaglio = bersaglio;
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
     * Come si applica la quantità: per esempio come variazione fissa, in percentuale o come valore a cui portare l'attributo.
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
                bersaglio);
    }

    /**
     * Lo stesso modificatore, ma per chi porta l'artefatto: la copia che un'arma mette sul bersaglio colpito.
     */
    public ModificatoreAttributoMD suPortatore() {
        return new ModificatoreAttributoMD(tipoAttributo, tipoModificatore, quantita, note, unitaDurata, durata,
                BersaglioModificatore.PORTATORE);
    }

    /**
     * Gli stessi dati con un'altra quantità (per i bonus moltiplicati dei set)
     */
    public ModificatoreAttributoMD conQuantita(double nuovaQuantita) {
        return new ModificatoreAttributoMD(tipoAttributo, tipoModificatore, nuovaQuantita, note, unitaDurata, durata,
                bersaglio);
    }

    /**
     * Se è lo stesso modificatore a parte la durata: così un doppione rinfresca quello che c'è invece di sommarsi
     */
    public boolean stessoEffetto(ModificatoreAttributoMD altro) {
        return tipoAttributo == altro.tipoAttributo && tipoModificatore == altro.tipoModificatore
                && quantita == altro.quantita && Objects.equals(note, altro.note)
                && unitaDurata == altro.unitaDurata && bersaglio == altro.bersaglio;
    }

    /**
     * Per i testi, l'effetto: per esempio "Fortuna porta a 0" o "Coraggio -20%"
     */
    public String descrizioneEffetto() {
        int valore = (int) quantita;
        String testo;
        switch (tipoModificatore) {
            case VARIAZIONE_FISSA:
                testo = String.format(Locale.ROOT, "%+d", valore);
                break;
            case VARIAZIONE_PERCENTUALE:
                testo = String.format(Locale.ROOT, "%+d%%", valore);
                break;
            default:
                testo = "porta a " + valore;
                break;
        }
        return tipoAttributo.getNome() + " " + testo;
    }

    /**
     * Per gli sprite, breve: "Furtività +5", "Coraggio -3", "Coraggio -20%", "Intelligenza a 0"
     */
    public String descrizioneBreve() {
        int valore = (int) quantita;
        switch (tipoModificatore) {
            case VARIAZIONE_FISSA:
                return String.format(Locale.ROOT, "%s %+d", tipoAttributo.getNome(), valore);
            case VARIAZIONE_PERCENTUALE:
                return String.format(Locale.ROOT, "%s %+d%%", tipoAttributo.getNome(), valore);
            default:
                return tipoAttributo.getNome() + " a " + valore;
        }
    }

    /**
     * Per i testi, la durata: per esempio "per 3 turni" o "per 1 locazione"; vuota se è permanente
     */
    public String descrizioneDurata() {
        switch (unitaDurata) {
            case TURNI:
                return "per " + durata + (durata == 1 ? " turno" : " turni");
            case LOCAZIONI:
                return "per " + durata + (durata == 1 ? " locazione" : " locazioni");
            default:
                return "";
        }
    }

    /**
     * Per i testi, tutto: per esempio "Fortuna porta a 0 sul bersaglio per 5 turni"
     */
    public String descrizione() {
        StringBuilder sb = new StringBuilder(descrizioneEffetto());
        if (bersaglio == BersaglioModificatore.BERSAGLIO) {
            sb.append(" sul bersaglio");
        }
        if (isTemporaneo()) {
            sb.append(' ').append(descrizioneDurata());
        }
        return sb.toString();
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
        return Objects.hash(tipoAttributo, tipoModificatore, quantita, note, unitaDurata, durata, bersaglio);
    }
}
