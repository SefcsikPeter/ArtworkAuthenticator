package artwork.authenticator.entity;

import artwork.authenticator.type.Artist;

public class Artwork {
  private Long id;
  private String title;
  private Artist artist;
  private String gallery;
  private String price;
  private String description;
  private String image;

  public Long getId() {
    return id;
  }

  public Artwork setId(Long id) {
    this.id = id;
    return this;
  }

  public String getTitle() {
    return title;
  }

  public Artwork setTitle(String title) {
    this.title = title;
    return this;
  }

  public Artist getArtist() {
    return artist;
  }

  public Artwork setArtist(Artist artist) {
    this.artist = artist;
    return this;
  }

  public String getGallery() {
    return gallery;
  }

  public Artwork setGallery(String gallery) {
    this.gallery = gallery;
    return this;
  }

  public String getPrice() {
    return price;
  }

  public Artwork setPrice(String price) {
    this.price = price;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public Artwork setDescription(String description) {
    this.description = description;
    return this;
  }

  public String getImage() {
    return image;
  }

  public Artwork setImage(String image) {
    this.image = image;
    return this;
  }
}
