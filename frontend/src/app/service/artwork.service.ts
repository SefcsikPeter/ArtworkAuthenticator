import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Artwork} from '../dto/artwork';
import {environment} from '../../environments/environment';
import {ArtworkResult} from '../dto/artwork-result';

const baseUrl = environment.backendUrl + '/artwork';
@Injectable({
  providedIn: 'root'
})
export class ArtworkService {

  constructor(
    private http: HttpClient
  ) { }

  analyse(artwork: Artwork): Observable<ArtworkResult> {
    console.log(artwork);
    return this.http.post<Artwork>(
      baseUrl,
      artwork
    );
  }

  getById(id: number) {
    return this.http.get(baseUrl + '/' + id);
  }
}
