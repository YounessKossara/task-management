import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TaskService } from '../../../core/services/task.service';
import { Task, TaskStatus, TaskPriority } from '../../../core/models/task.model';
import { User } from '../../../core/models/user.model';
import { Project } from '../../../core/models/project.model';
import { UserService } from '../../../core/services/user.service';
import { ProjectService } from '../../../core/services/project.service';
import Keycloak from 'keycloak-js';
import { inject } from '@angular/core';
import { ToastService } from '../../../core/services/toast.service';

@Component({
    selector: 'app-task-board',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink],
    templateUrl: './task-board.component.html',
    styleUrls: ['./task-board.component.css']
})
export class TaskBoardComponent implements OnInit {
    projectId!: number;
    tasks: Task[] = [];
    users: User[] = [];
    assignableUsers: User[] = [];
    project!: Project;
    isAdmin = false;
    isResponsable = false;
    currentUserId: string | undefined;

    todoTasks: Task[] = [];
    inProgressTasks: Task[] = [];
    doneTasks: Task[] = [];

    showModal = false;
    currentTask: Partial<Task> = {};
    isEditing = false;

    TaskStatus = TaskStatus;
    TaskPriority = TaskPriority;

    private keycloak = inject(Keycloak);
    private toastService = inject(ToastService);

    constructor(
        private route: ActivatedRoute,
        private taskService: TaskService,
        private userService: UserService,
        private projectService: ProjectService
    ) { }

    ngOnInit() {
        this.isAdmin = this.keycloak.hasRealmRole('ADMIN');
        this.isResponsable = this.keycloak.hasRealmRole('RESPONSABLE');
        this.currentUserId = this.keycloak.tokenParsed?.sub;
        console.log("TaskBoard Auth Check:", {
            isAdmin: this.isAdmin,
            isResponsable: this.isResponsable,
            currentUserId: this.currentUserId
        });
        this.route.params.subscribe(params => {
            this.projectId = +params['id'];
            this.loadProject();
            this.loadTasks();
            this.loadUsers();
        });
    }

    loadProject() {
        this.projectService.getProjectById(this.projectId).subscribe((data: Project) => {
            this.project = data;
            console.log("Project loaded:", {
                projectId: this.project.id,
                responsableKeycloakId: this.project.responsableKeycloakId
            });
            this.filterAssignableUsers();
        });
    }

    loadUsers() {
        this.userService.getAllUsers().subscribe((data: User[]) => {
            this.users = data;
            this.filterAssignableUsers();
        });
    }

    filterAssignableUsers() {
        if (this.project && this.users.length > 0) {
            this.assignableUsers = this.users.filter(u => u.role === 'USER' && u.keycloakId !== this.project.responsableKeycloakId);
        }
    }

    loadTasks() {
        this.taskService.getTasksByProject(this.projectId).subscribe(data => {
            this.tasks = data;
            this.distributeTasks();
        });
    }

    distributeTasks() {
        this.todoTasks = this.tasks.filter(t => t.statut === TaskStatus.TODO);
        this.inProgressTasks = this.tasks.filter(t => t.statut === TaskStatus.IN_PROGRESS);
        this.doneTasks = this.tasks.filter(t => t.statut === TaskStatus.DONE);
    }

    openModal(task?: Task) {
        if (task) {
            this.isEditing = true;
            this.currentTask = { ...task };
        } else {
            this.isEditing = false;
            this.currentTask = {
                titre: '',
                description: '',
                statut: TaskStatus.TODO,
                priorite: TaskPriority.MEDIUM,
                projectId: this.projectId
            };
        }
        this.showModal = true;
    }

    closeModal() {
        this.showModal = false;
    }

    saveTask() {
        if (this.isEditing && this.currentTask.id) {
            this.taskService.updateTask(this.currentTask.id, this.currentTask as Task).subscribe(() => {
                this.toastService.showSuccess('Tâche modifiée avec succès');
                this.loadTasks();
                this.closeModal();
            });
        } else {
            this.taskService.createTask(this.projectId, this.currentTask as Task).subscribe(() => {
                this.toastService.showSuccess('Tâche ajoutée avec succès');
                this.loadTasks();
                this.closeModal();
            });
        }
    }

    deleteTask(id: number) {
        if (confirm('Supprimer cette tâche ?')) {
            this.taskService.deleteTask(id).subscribe(() => {
                this.toastService.showSuccess('Tâche supprimée');
                this.loadTasks();
            });
        }
    }

    changeStatus(task: Task, newStatus: TaskStatus) {
        this.taskService.updateTaskStatus(task.id!, newStatus).subscribe(() => {
            if (newStatus === TaskStatus.DONE) {
                this.toastService.showSuccess(`Tâche '${task.titre}' terminée !`);
            }
            this.loadTasks();
        });
    }
}
