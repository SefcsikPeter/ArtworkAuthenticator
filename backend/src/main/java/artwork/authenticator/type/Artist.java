package artwork.authenticator.type;

public enum Artist {
  Albrecht_Durer,
  Boris_Kustodiev,
  Camille_Pissarro,
  Childe_Hassam,
  Claude_Monet,
  Edgar_Degas,
  Eugene_Boudin,
  Gustave_Dore,
  Ilya_Repin,
  Ivan_Aivazovsky,
  Ivan_Shishkin,
  John_Singer_Sargent,
  Marc_Chagall,
  Martiros_Saryan,
  Nicholas_Roerich,
  Pablo_Picasso,
  Paul_Cezanne,
  Pierre_Auguste_Renoir,
  Pyotr_Konchalovsky,
  Raphael_Kirchner,
  Rembrandt,
  Salvador_Dali,
  Vincent_van_Gogh,
  Alfons_Walde;

  public static int getArtistIndex(Artist artist) {
    Artist[] artists = Artist.values();
    for (int i = 0; i < artists.length; i++) {
      if (artists[i] == artist) {
        return i;
      }
    }
    return -1; // Return -1 if the artist is not found (optional)
  }
}
