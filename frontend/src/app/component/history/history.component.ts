import { Component, OnInit } from '@angular/core';
import {ArtworkResultService} from '../../service/artwork-result.service';
import {ResultList} from '../../dto/result-list';

@Component({
  selector: 'app-history',
  templateUrl: './history.component.html',
  styleUrls: ['./history.component.scss']
})
export class HistoryComponent implements OnInit {
  results: ResultList[] = [];
  constructor(
    private service: ArtworkResultService
  ) { }

  ngOnInit(): void {
    this.service.getAll().subscribe({
      next: data => {
        this.results = data;
        console.log(data);
      }
    });
  }

}
