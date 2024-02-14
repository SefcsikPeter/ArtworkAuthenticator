import { Injectable } from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ResultList} from '../dto/result-list';
import {MessageList} from '../dto/message-list';

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

  getAll(): Observable<ResultList[]> {
    return this.http.get<ResultList[]>(baseUri);
  }

  getAllMessagesByResultId(id: number): Observable<MessageList[]> {
    return this.http.get<MessageList[]>(baseUri + '/' + id + '/' + 'messages');
  }
}
