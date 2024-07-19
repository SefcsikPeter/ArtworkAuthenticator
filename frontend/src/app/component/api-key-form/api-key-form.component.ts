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
}
