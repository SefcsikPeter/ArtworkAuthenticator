import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { Artist } from '../../dto/artist';
import { FormGroup, FormControl } from '@angular/forms';
import { ArtworkService } from '../../service/artwork.service';
import { Artwork } from '../../dto/artwork';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
const { ipcRenderer } = require('electron');

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  isImageSelected = false;
  imageSrc: string | null = null;
  imageName: string | null = null;
  artworkForm: FormGroup | undefined;
  artists = Object.values(Artist);
  artwork: Artwork = {};

  constructor(
    private service: ArtworkService,
    private router: Router,
    private notification: ToastrService,
    private changeDetectorRef: ChangeDetectorRef
  ) {
    ipcRenderer.on('selected-file', (event: any, path: string) => {
      console.log('Selected file:', path);
      this.processSelectedFile(path);
    });
  }

  ngOnInit(): void {
    this.artworkForm = new FormGroup({
      title: new FormControl(),
      artist: new FormControl(),
      gallery: new FormControl(),
      price: new FormControl(),
      description: new FormControl()
    });
  }

  openFileDialog() {
    ipcRenderer.send('open-file-dialog');
  }

  processSelectedFile(filePath: string) {
    this.isImageSelected = true;
    this.imageSrc = `http://localhost:3000/image/${filePath}`;
    //this.imageName = decodeURIComponent(filePath).split('\\').pop()?.split('/').pop() || '';
    try {
      // Decode the Base64 string to UTF-8
      const decodedPath = atob(filePath);

      // Extract the file name from the full path
      this.imageName = decodedPath.split('\\').pop()?.split('/').pop() || '';
    } catch (e) {
      console.error('Failed to decode file path', e);
      this.imageName = 'Invalid file';
    }
    this.changeDetectorRef.detectChanges();
  }


  onSubmit(): void {
    console.log('submitted form', this.artworkForm?.value);
    if (this.artworkForm?.value.title) {
      this.artwork.title = this.artworkForm?.value.title;
    }
    if (this.artworkForm?.value.artist) {
      this.artwork.artist = this.artworkForm?.value.artist;
    } else {
      this.notification.success('', 'Please select an artist name!', {
        toastClass: 'user-info',
        positionClass: 'custom-toast-center'
      });
      return;
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
    this.notification.success('', 'The entered information has been submitted, please wait for the results', {
      toastClass: 'user-info',
      positionClass: 'custom-toast-center'
    });
    this.service.analyse(this.artwork).subscribe({
      next: data => {
        console.log('passed to service', data);
        this.router.navigate(['/results', data]);
      },
      error: error => {
        console.error('Error analysing artwork', error);
        this.notification.success('', 'There has been an error processing your request', {
          toastClass: 'user-info',
          positionClass: 'custom-toast-center'
        });
      }
    });
  }

  formatName(artist: string): string {
    return artist.replace('_', ' ');
  }
}
