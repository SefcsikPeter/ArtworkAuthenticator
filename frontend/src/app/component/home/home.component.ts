import {Component, OnInit} from '@angular/core';
import {Artist} from '../../dto/artist';
import {FormGroup, FormControl} from '@angular/forms';
import {ArtworkService} from '../../service/artwork.service';
import {Artwork} from '../../dto/artwork';
import {Router} from '@angular/router';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  isImageSelected = false;
  imageSrc: string | ArrayBuffer | null = null;
  imageName: string | null = null;
  artworkForm: FormGroup | undefined;
  artists = Object.values(Artist);
  artwork: Artwork = {};

  constructor(
    private service: ArtworkService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.artworkForm = new FormGroup({
      title: new FormControl(),
      artist: new FormControl(),
      gallery: new FormControl(),
      price: new FormControl(),
      description: new FormControl()
    });
  }

  onFileSelect(event: Event): void {
    const eventTarget = event.target as HTMLInputElement;
    const file = eventTarget.files?.[0];

    if (!file) {
      this.isImageSelected = false;
      return;
    }

    this.isImageSelected = true;
    this.imageName = file.name; // Save the file name

    const reader = new FileReader();
    reader.onload = e => {
      this.imageSrc = reader.result as string;
    };
    reader.readAsDataURL(file);
  }

  onSubmit(): void {
    console.log('submitted form', this.artworkForm?.value);
    if (this.artworkForm?.value.title) {
      this.artwork.title = this.artworkForm?.value.title;
    }
    if (this.artworkForm?.value.artist) {
      this.artwork.artist = this.artworkForm?.value.artist;
    }
    if (this.artworkForm?.value.gallery) {
      this.artwork.gallery = this.artworkForm?.value.gallery;
    }
    if (this.artworkForm?.value.price) {
      this.artwork.price = this.artworkForm?.value.price;
    }
    if (this.artworkForm?.value.description) {
      this.artwork.description = this.artworkForm?.value.description.replace(/\n/g, ' ');
    }
    if (typeof this.imageSrc === 'string') {
      this.artwork.image = this.imageSrc;
    }
    this.service.analyse(this.artwork).subscribe({
      next: data => {
        console.log('passed to service', data);
        this.router.navigate(['/results', data]);
      },
      error: error => {
        console.error('Error analysing artwork', error);
      }
    });
  }

  formatName(artist: string): string {
    return artist.replace('_', ' ');
  }
}
