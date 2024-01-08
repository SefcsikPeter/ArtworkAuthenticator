import {AfterViewInit, Component, OnInit} from '@angular/core';
import {Artist} from '../../dto/artist';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit, AfterViewInit {
  isImageSelected = false;
  imageSrc: string | ArrayBuffer | null = null;
  artists = Object.values(Artist);

  constructor() { }

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
  }

  onFileSelect(event: Event): void {
    const eventTarget = event.target as HTMLInputElement;
    const file = eventTarget.files?.[0];
    if (!file) {
      this.isImageSelected = false;
      return;
    }
    this.isImageSelected = true;
    const reader = new FileReader();
    reader.onload = e => {
      this.imageSrc = reader.result;
    };
    reader.readAsDataURL(file);
  }

}
