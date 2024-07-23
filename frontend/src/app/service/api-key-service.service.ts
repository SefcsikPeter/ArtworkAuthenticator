import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { from, Observable, of, switchMap } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

const openaiUrl = 'https://api.openai.com/v1/models';
const backendUrl = environment.backendUrl + '/apikey';

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

  sendApiKey(apiKey: string): Observable<any> {
    return this.encryptApiKey(apiKey).pipe(
      switchMap(encryptedApiKey => {
        console.log('encrypted API key being sent to backend');
        return this.http.post(environment.backendUrl + '/apikey', { encryptedApiKey }, {
          headers: new HttpHeaders({
            // eslint-disable-next-line @typescript-eslint/naming-convention
            'Content-Type': 'application/json'
          })
        });
      })
    );
  }

  private encryptApiKey(apiKey: string): Observable<string> {
    return this.getPublicKey().pipe(
      switchMap(publicKey => {
        const encoder = new TextEncoder();
        const encodedMessage = encoder.encode(apiKey);
        return from(window.crypto.subtle.encrypt(
          {
            name: 'RSA-OAEP'
          },
          publicKey,
          encodedMessage
        )).pipe(
          map((encrypted: ArrayBuffer) => {
            // Convert ArrayBuffer to base64 string using Array.from
            const byteArray = new Uint8Array(encrypted);
            const charArray = Array.from(byteArray, byte => String.fromCharCode(byte));
            return window.btoa(charArray.join(''));
          })
        );
      })
    );
  }

  private getPublicKey(): Observable<CryptoKey> {
    return this.http.get(backendUrl, { responseType: 'text' }).pipe(
      switchMap((pem: string) => this.importPublicKey(pem))
    );
  }

  private importPublicKey(pem: string): Observable<CryptoKey> {
    const binaryDerString = window.atob(pem.replace(/-----\w+ PUBLIC KEY-----/g, '').replace(/[\r\n]/g, ''));
    const binaryDer = this.str2ab(binaryDerString);

    return from(window.crypto.subtle.importKey(
      'spki',
      binaryDer,
      {
        name: 'RSA-OAEP',
        hash: 'SHA-1'
      },
      true,
      ['encrypt']
    ));
  }

  private str2ab(str: string): ArrayBuffer {
    const buf = new ArrayBuffer(str.length);
    const bufView = new Uint8Array(buf);
    for (let i = 0, strLen = str.length; i < strLen; i++) {
      bufView[i] = str.charCodeAt(i);
    }
    return buf;
  }
}
