import {Component, OnInit} from '@angular/core';
import {Artist} from '../../dto/artist';
import {FormGroup, FormControl} from '@angular/forms';

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

  constructor() { }

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
    console.log(this.artworkForm?.value);
  }

}
