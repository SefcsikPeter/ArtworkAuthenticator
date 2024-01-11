import {Artist} from './artist';

export interface ArtworkResult {
  artworkId?: number;
  resultId?: number;
  title?: string;
  artist?: Artist;
  gallery?: string;
  price?: string;
  description?: string;
  image?: string;
  neuralNetResult?: string;
  gptResult?: string;
}
