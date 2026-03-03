import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { ProjectService } from '../../core/services/project.service';
import { Project } from '../../core/models/project.model';
import Keycloak from 'keycloak-js';
import { inject } from '@angular/core';

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
    stats = {
        totalProjects: 0
    };

    recentProjects: Project[] = [];

    private keycloak = inject(Keycloak);

    constructor(private projectService: ProjectService, private router: Router) { }

    ngOnInit() {
        const isUserOnly = !this.keycloak.hasRealmRole('ADMIN') && !this.keycloak.hasRealmRole('RESPONSABLE');
        if (isUserOnly) {
            this.router.navigate(['/projects']);
            return;
        }

        this.loadDashboardData();
    }

    loadDashboardData() {
        this.projectService.getAllProjects().subscribe((projects) => {
            this.recentProjects = projects.slice(0, 5);
            this.stats.totalProjects = projects.length;
        });
    }
}
