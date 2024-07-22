import { Component, OnInit } from '@angular/core';
import {ApiKeyServiceService} from '../../service/api-key-service.service';
import {FormControl, FormGroup} from '@angular/forms';

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
  apiKeyForm: FormGroup | undefined;

  constructor(
    private apiKeyService: ApiKeyServiceService
  ) { }

  ngOnInit(): void {
    this.apiKeyForm = new FormGroup({
      apiKey: new FormControl()
    });
  }

  validateApiKey(): void {
    if (this.apiKeyForm?.value.apiKey) {
      this.apiKey = this.apiKeyForm?.value.apiKey;
      this.submitted = true;
      this.apiKeyService.checkApiKey(this.apiKey).subscribe(
        {
          next: isValid => {
            this.validationResult = isValid;
            this.errorMessage = '';
            if (isValid) {
              this.apiKeyService.sendApiKey(this.apiKey).subscribe(
                {
                  next: () => {
                    console.log('Encrypted API key sent to backend');
                  },
                  error: err => {
                    console.log('Failed to send API key to backend', err);
                  }
                }
              );
            }
          },
          error: error => {
            this.validationResult = false;
            this.errorMessage = 'An error occurred while validating the API key.';
            console.error('An error occurred:', error);
          }
        }
      );
    }
  }
}
