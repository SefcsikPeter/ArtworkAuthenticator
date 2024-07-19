import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import { catchError, map } from 'rxjs/operators';

const openaiUrl = 'https://api.openai.com/v1/models';

@Injectable({
  providedIn: 'root'
})
export class ApiKeyServiceService {

  constructor(private http: HttpClient) { }

  checkApiKey(apiKey: string): Observable<boolean> {
    const headers = new HttpHeaders({
      // eslint-disable-next-line @typescript-eslint/naming-convention
      Authorization: `Bearer ${apiKey}`
    });
    return this.http.get(openaiUrl, { headers }).pipe(
      map(() => true),
      catchError((error) => {
        if (error.status === 401) {
          return of(false);
        } else {
          throw error;
        }
      })
    );
  }
}
