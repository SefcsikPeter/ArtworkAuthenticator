import { Component, OnInit } from '@angular/core';
import {ArtworkResultService} from '../../service/artwork-result.service';
import {ResultList} from '../../dto/result-list';
import {Artist} from "../../dto/artist";

@Component({
  selector: 'app-history',
  templateUrl: './history.component.html',
  styleUrls: ['./history.component.scss']
})
export class HistoryComponent implements OnInit {
  results: ResultList[] = [];
  constructor(
    private service: ArtworkResultService
  ) { }

  ngOnInit(): void {
    this.service.getAll().subscribe({
      next: data => {
        this.results = data;
        this.results.reverse();
        console.log(data);
      }
    });
  }

  extractRating(sequence: string, inputString: string): number {
    const escapedSequence = sequence.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
    const pattern = new RegExp(escapedSequence + '([+-]?\\d*(\\.\\d+)?)(?!\\d)', 'g');
    const match = pattern.exec(inputString);
    if (match && match[1]) {
      return parseFloat(match[1]);
    } else {
      return NaN;
    }
  }

  splitRating(rating: string): number {
    if (rating) {
      const probs = rating.split(', ');
      return +probs[2];
    } else {
      return NaN;
    }
  }

  formatName(name: Artist): string {
    const nameString = '' + name;
    return nameString.replace('_', ' ');
  }

}
