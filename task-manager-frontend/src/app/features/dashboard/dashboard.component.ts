import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectService } from '../../core/services/project.service';
import { Project } from '../../core/models/project.model';

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
    stats = {
        totalProjects: 0
    };

    recentProjects: Project[] = [];

    constructor(private projectService: ProjectService) { }

    ngOnInit() {
        this.loadDashboardData();
    }

    loadDashboardData() {
        this.projectService.getAllProjects().subscribe((projects) => {
            this.recentProjects = projects.slice(0, 5);
            this.stats.totalProjects = projects.length;
        });
    }
}
