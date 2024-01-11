import { Component, OnInit } from '@angular/core';
import {ArtworkResult} from '../../dto/artwork-result';
import {ArtworkResultService} from '../../service/artwork-result.service';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-result-page',
  templateUrl: './result-page.component.html',
  styleUrls: ['./result-page.component.scss']
})
export class ResultPageComponent implements OnInit {
  result: ArtworkResult = {};
  constructor(
    private service: ArtworkResultService,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.loadResult();
  }

  loadResult(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.service.getById(+id).subscribe({
        next: data => {
          console.log('loaded result', data);
          this.result = data;
        },
        error: err => {
          console.error(err);
        }
      });
    }
  }
}
