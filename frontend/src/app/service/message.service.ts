import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment';
import {UserMessage} from '../dto/user-message';
import {Observable} from 'rxjs';
import {GptResponseDto} from '../dto/gpt-response-dto';

const baseUri = environment.backendUrl + '/messages';
@Injectable({
  providedIn: 'root'
})
export class MessageService {

  constructor(
    private http: HttpClient
  ) { }

  create(userMessage: UserMessage): Observable<GptResponseDto> {
    console.log('user message:', userMessage);

    return this.http.post<GptResponseDto>(
      baseUri,
      userMessage
    );
  }
}
