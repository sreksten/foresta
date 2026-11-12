#!/usr/bin/env python3
"""
Genera l'icona dell'app Android dalla bitmap di partenza (LogoForesta.png, la F del titolo: pixel art 39x62) in
app/src/main/res: l'icona adattiva (sfondo a tinta unita e primo piano trasparente) per le densita' mdpi, xhdpi, xxhdpi e xxxhdpi.
Il primo piano e' la bitmap ingrandita SENZA interpolare, per tenere nitidi i pixel, con un fattore intero: x1 (mdpi), x2 (xhdpi), x3 (xxhdpi),
x4 (xxxhdpi), sempre dentro l'area di sicurezza dell'icona adattiva (66 su 108 punti). Le densita' che mancano (hdpi) le copre il sistema
ridimensionando le vicine.
L'icona adattiva c'e' solo da Android 8 (API 26): per le versioni prima (il minSdk e' 24) servono anche le icone tradizionali, ic_launcher.png
(la F su un quadrato pergamena con gli angoli arrotondati) e ic_launcher_round.png (su un cerchio), da 48 punti: anche qui la F ingrandita con un
fattore intero, il piu' grande con cui ci sta con un po' di margine (la mdpi, dove nemmeno x1 ci sta, la ricava il sistema dalla xhdpi).

Uso, dalla cartella foresta-android:  python3 icone-android/genera_icone.py   (serve Pillow)
"""
import os
from PIL import Image, ImageDraw

SFONDO = "#DEBD94"  # la pergamena
RADICE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
SORGENTE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "LogoForesta.png")
RES = os.path.join(RADICE, "app", "src", "main", "res")
# densita': (lato del livello in pixel = 108 punti x scala, fattore intero di ingrandimento)
DENSITA = {"mdpi": (108, 1), "xhdpi": (216, 2), "xxhdpi": (324, 3), "xxxhdpi": (432, 4)}
# le icone tradizionali (prima di Android 8): densita': (lato in pixel = 48 punti x scala, fattore intero di ingrandimento)
TRADIZIONALI = {"xhdpi": (96, 1), "xxhdpi": (144, 2), "xxxhdpi": (192, 2)}
# il raggio degli angoli del quadrato, in parte del lato
RAGGIO_DEGLI_ANGOLI = 0.18

ICONA_ADATTIVA = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@mipmap/ic_launcher_foreground" />
</adaptive-icon>
"""

COLORE = """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ic_launcher_background">%s</color>
</resources>
"""


def scrivi(percorso, testo):
    os.makedirs(os.path.dirname(percorso), exist_ok=True)
    with open(percorso, "w", encoding="utf-8") as f:
        f.write(testo)


def main():
    bitmap = Image.open(SORGENTE).convert("RGBA")
    for nome, (lato, fattore) in DENSITA.items():
        ingrandita = bitmap.resize((bitmap.width * fattore, bitmap.height * fattore), Image.NEAREST)
        livello = Image.new("RGBA", (lato, lato), (0, 0, 0, 0))
        livello.alpha_composite(ingrandita, ((lato - ingrandita.width) // 2, (lato - ingrandita.height) // 2))
        cartella = os.path.join(RES, "mipmap-" + nome)
        os.makedirs(cartella, exist_ok=True)
        livello.save(os.path.join(cartella, "ic_launcher_foreground.png"))
        print(nome, lato, "x", lato, "fattore", fattore)
    for nome, (lato, fattore) in TRADIZIONALI.items():
        ingrandita = bitmap.resize((bitmap.width * fattore, bitmap.height * fattore), Image.NEAREST)
        posto = ((lato - ingrandita.width) // 2, (lato - ingrandita.height) // 2)
        cartella = os.path.join(RES, "mipmap-" + nome)
        for file, forma in (("ic_launcher.png", "quadrato"), ("ic_launcher_round.png", "cerchio")):
            icona = Image.new("RGBA", (lato, lato), (0, 0, 0, 0))
            disegno = ImageDraw.Draw(icona)
            if forma == "quadrato":
                disegno.rounded_rectangle((0, 0, lato - 1, lato - 1), radius=int(lato * RAGGIO_DEGLI_ANGOLI), fill=SFONDO)
            else:
                disegno.ellipse((0, 0, lato - 1, lato - 1), fill=SFONDO)
            icona.alpha_composite(ingrandita, posto)
            icona.save(os.path.join(cartella, file))
        print(nome, "tradizionale", lato, "x", lato, "fattore", fattore)
    scrivi(os.path.join(RES, "mipmap-anydpi-v26", "ic_launcher.xml"), ICONA_ADATTIVA)
    scrivi(os.path.join(RES, "mipmap-anydpi-v26", "ic_launcher_round.xml"), ICONA_ADATTIVA)
    scrivi(os.path.join(RES, "values", "ic_launcher_background.xml"), COLORE % SFONDO)


if __name__ == "__main__":
    main()
