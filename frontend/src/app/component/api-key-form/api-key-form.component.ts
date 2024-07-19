import { Component, OnInit } from '@angular/core';
import {ApiKeyServiceService} from '../../service/api-key-service.service';

@Component({
  selector: 'app-api-key-form',
  templateUrl: './api-key-form.component.html',
  styleUrls: ['./api-key-form.component.scss']
})
export class ApiKeyFormComponent implements OnInit {
  apiKey = '';
  validationResult: boolean | undefined;
  errorMessage = '';
  submitted = false;

  constructor(
    private apiKeyService: ApiKeyServiceService
  ) { }

  ngOnInit(): void {
  }

  validateApiKey(): void {
    this.submitted = true;
    this.apiKeyService.checkApiKey(this.apiKey).subscribe(
      isValid => {
        this.validationResult = isValid;
        this.errorMessage = '';
      },
      error => {
        this.validationResult = false;
        this.errorMessage = 'An error occurred while validating the API key.';
        console.error('An error occurred:', error);
      }
    );
  }
}
