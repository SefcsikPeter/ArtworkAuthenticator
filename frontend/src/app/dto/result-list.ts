import {Artist} from './artist';

export interface ResultList {
  resultId: number;
  artworkId: number;
  artist: Artist;
  artworkTitle: string;
  neuralNetResult: string;
  gptResult: string;
}
