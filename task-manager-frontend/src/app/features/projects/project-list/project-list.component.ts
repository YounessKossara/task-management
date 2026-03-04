import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProjectService } from '../../../core/services/project.service';
import { Project } from '../../../core/models/project.model';
import { UserService } from '../../../core/services/user.service';
import { User } from '../../../core/models/user.model';
import Keycloak from 'keycloak-js';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { ToastService } from '../../../core/services/toast.service';

@Component({
    selector: 'app-project-list',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './project-list.component.html',
    styleUrls: ['./project-list.component.css']
})
export class ProjectListComponent implements OnInit {
    projects: Project[] = [];
    users: User[] = [];
    isAdmin = false;
    isResponsable = false;
    currentUserId: string | undefined;

    showModal = false;
    currentProject: Project = { nom: '', description: '', dateDebut: '' };
    isEditing = false;

    private keycloak = inject(Keycloak);
    private toastService = inject(ToastService);

    constructor(
        private projectService: ProjectService,
        private userService: UserService,
        private router: Router
    ) { }

    ngOnInit() {
        this.isAdmin = this.keycloak.hasRealmRole('ADMIN');
        this.isResponsable = this.keycloak.hasRealmRole('RESPONSABLE');
        this.currentUserId = this.keycloak.tokenParsed?.sub;
        this.loadProjects();
        if (this.isAdmin || this.isResponsable) {
            this.loadUsers();
        }
    }

    loadUsers() {
        this.userService.getAllUsers().subscribe((data: User[]) => {
            this.users = data.filter(u => u.role === 'RESPONSABLE');
        });
    }

    loadProjects() {
        this.projectService.getAllProjects().subscribe((data) => {
            this.projects = data;
        });
    }

    viewTasks(projectId: number) {
        this.router.navigate(['/projects', projectId, 'tasks']);
    }

    openModal(project?: Project) {
        if (project) {
            this.isEditing = true;
            this.currentProject = { ...project };
        } else {
            this.isEditing = false;
            this.currentProject = { nom: '', description: '', dateDebut: new Date().toISOString().split('T')[0], responsableKeycloakId: undefined };
        }
        this.showModal = true;
    }

    closeModal() {
        this.showModal = false;
    }

    saveProject() {
        if (this.isEditing && this.currentProject.id) {
            this.projectService.updateProject(this.currentProject.id, this.currentProject).subscribe(() => {
                this.toastService.showSuccess(`Projet modifié avec succès`);
                this.loadProjects();
                this.closeModal();
            });
        } else {
            this.projectService.createProject(this.currentProject).subscribe(() => {
                this.toastService.showSuccess(`Projet créé avec succès`);
                this.loadProjects();
                this.closeModal();
            });
        }
    }

    deleteProject(id: number) {
        if (confirm('Voulez-vous vraiment supprimer ce projet ?')) {
            this.projectService.deleteProject(id).subscribe(() => {
                this.toastService.showSuccess(`Projet supprimé`);
                this.loadProjects();
            });
        }
    }

    getResponsableName(keycloakId?: string): string {
        if (!keycloakId) return 'Non défini';
        const user = this.users.find(u => u.keycloakId === keycloakId);
        return user ? `${user.prenom} ${user.nom}` : 'Inconnu';
    }

    isProjectDatesInvalid(): boolean {
        if (!this.currentProject.dateDebut || !this.currentProject.dateFin) return false;
        return new Date(this.currentProject.dateFin) < new Date(this.currentProject.dateDebut);
    }
}
