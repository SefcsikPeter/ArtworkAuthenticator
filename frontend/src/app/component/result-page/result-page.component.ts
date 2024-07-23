import { Component, OnInit } from '@angular/core';
import {ArtworkResult} from '../../dto/artwork-result';
import {ArtworkResultService} from '../../service/artwork-result.service';
import {ActivatedRoute} from '@angular/router';
import {ArtworkService} from '../../service/artwork.service';
import {Artwork} from '../../dto/artwork';
import {Artist} from '../../dto/artist';
import {FormControl, FormGroup} from '@angular/forms';
import {MessageService} from '../../service/message.service';
import {UserMessage} from '../../dto/user-message';
import {MessageList} from '../../dto/message-list';

@Component({
  selector: 'app-result-page',
  templateUrl: './result-page.component.html',
  styleUrls: ['./result-page.component.scss']
})
export class ResultPageComponent implements OnInit {
  result: ArtworkResult = {};
  resultId = -1;
  artwork: Artwork = {};
  artists = Object.values(Artist);
  selectedArtist = '';
  selectedArtistProb = '';
  top1Artist = '';
  top1ArtistProb = '';
  feedback: FormGroup | undefined;
  messagePairs: MessageList[] = [];
  constructor(
    private resultService: ArtworkResultService,
    private artworkService: ArtworkService,
    private messageService: MessageService,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.loadData();
    this.feedback = new FormGroup({
      text: new FormControl('')
    });
  }

  loadData(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.resultId = +id;
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
      this.resultService.getAllMessagesByResultId(+id).subscribe({
        next: messages => {
          console.log(messages);
          this.messagePairs = messages;
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
      this.top1ArtistProb = this.truncateString('' + +this.truncateString(artistProbabilities[1], 6)*100, 4);
      console.log(artistProbabilities[1], artistProbabilities[2]);
      this.selectedArtistProb = this.truncateString('' + +this.truncateString(artistProbabilities[2], 6)*100, 4);
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

  sendFeedback() {
    if (this.feedback?.value.text) {
      console.log(this.messagePairs);
      const userMessage: UserMessage = {resultId: this.resultId, userMessage: this.sanitizeForJSON(this.feedback.value.text)};
      this.messageService.create(userMessage).subscribe({
        next: response => {
          const messagePair: MessageList = {userMessage: this.feedback?.value.text, gptResponse: response.response};
          this.messagePairs.push(messagePair);
          if(this.feedback) {
            this.feedback.reset();
          }
          console.log(response);
        },
        error: err => {
          console.log(err);
        }
      });
    }
  }

  sanitizeForJSON(text: string) {
    return text.replace(/\\/g, '\\\\')
      .replace(/"/g, '\\"')
      .replace(/\n/g, '\\n')
      .replace(/\r/g, '\\r')
      .replace(/\t/g, '\\t');
  }
}
