import { Component, OnInit } from '@angular/core';
import {ArtworkResult} from '../../dto/artwork-result';
import {ArtworkResultService} from '../../service/artwork-result.service';
import {ActivatedRoute} from '@angular/router';
import {ArtworkService} from '../../service/artwork.service';
import {Artwork} from '../../dto/artwork';
import {Artist} from '../../dto/artist';

@Component({
  selector: 'app-result-page',
  templateUrl: './result-page.component.html',
  styleUrls: ['./result-page.component.scss']
})
export class ResultPageComponent implements OnInit {
  result: ArtworkResult = {};
  artwork: Artwork = {};
  artists = Object.values(Artist);
  selectedArtist = '';
  selectedArtistProb = '';
  top1Artist = '';
  top1ArtistProb = '';
  constructor(
    private resultService: ArtworkResultService,
    private artworkService: ArtworkService,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.resultService.getById(+id).subscribe({
        next: data => {
          console.log('loaded result', data);
          this.result = data;
          this.sortArtistNames();
          this.setNNResult();
          this.loadArtwork();
        },
        error: err => {
          console.error(err);
        }
      });
    }
  }

  loadArtwork(): void {
    if (this.result.artworkId) {
      this.artworkService.getById(this.result.artworkId).subscribe({
        next: data => {
          this.artwork = data;
          if (this.artwork.artist) {
            this.selectedArtist = this.artwork.artist;
          }
          console.log('loaded artwork data', this.artwork);
        }
      });
    }
  }

  setNNResult() {
    if (this.result.neuralNetResult) {
      const artistProbabilities = this.result.neuralNetResult.split(',');
      this.top1Artist = this.artists[+artistProbabilities[0]];
      this.top1ArtistProb = this.truncateString(artistProbabilities[1], 6);
      console.log(artistProbabilities[1], artistProbabilities[2]);
      this.selectedArtistProb = this.truncateString(artistProbabilities[2], 6);
    }
  }

  sortArtistNames() {
    this.artists = this.artists.filter(item => item !== 'Alfons_Walde');
    this.artists.sort((a, b) => a.localeCompare(b));
    this.artists.push(Artist.alfonsWalde);
  }

  truncateString(str: string, maxLen: number) {
    if (str.includes('e')) {
      str = ' 0.00000';
    }
    if (str.length > maxLen) {
      return str.substring(0, maxLen);
    } else {
      return str;
    }
  }

  formatName(artist: Artist): string {
    return ('' + artist).replace('_', ' ');
  }
}
