import {Artist} from './artist';

export interface Artwork {
  id?: number;
  title?: string;
  artist?: Artist;
  gallery?: string;
  price?: string;
  description?: string;
  image?: string;
}
