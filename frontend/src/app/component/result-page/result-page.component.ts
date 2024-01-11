import { Component, OnInit } from '@angular/core';
import {ArtworkResult} from '../../dto/artwork-result';
import {ArtworkResultService} from '../../service/artwork-result.service';
import {ActivatedRoute} from '@angular/router';
import {ArtworkService} from '../../service/artwork.service';
import {Artwork} from '../../dto/artwork';

@Component({
  selector: 'app-result-page',
  templateUrl: './result-page.component.html',
  styleUrls: ['./result-page.component.scss']
})
export class ResultPageComponent implements OnInit {
  result: ArtworkResult = {};
  artwork: Artwork = {};
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
          console.log('loaded artwork data', this.artwork);
        }
      });
    }
  }
}
