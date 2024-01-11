import { Injectable } from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';

const baseUri = environment.backendUrl + '/results';
@Injectable({
  providedIn: 'root'
})
export class ArtworkResultService {

  constructor(
    private http: HttpClient
  ) { }

  getById(id: number) {
    return this.http.get(baseUri + '/' + id);
  }
}
