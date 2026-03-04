import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../../core/services/user.service';
import { User } from '../../../core/models/user.model';
import Keycloak from 'keycloak-js';
import { Component, OnInit, inject, ViewChild, ElementRef } from '@angular/core';
import { ToastService } from '../../../core/services/toast.service';

@Component({
    selector: 'app-user-management',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './user-management.component.html',
    styleUrls: ['./user-management.component.css']
})
export class UserManagementComponent implements OnInit {
    users: User[] = [];
    isAdmin = false;

    showModal = false;
    isEditing = false;
    currentUser: Partial<User> = {};
    password = '';

    selectedUserIdForUpload: string | null = null;
    selectedFileForCreation: File | null = null;
    @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

    private keycloak = inject(Keycloak);
    private toastService = inject(ToastService);

    constructor(
        private userService: UserService
    ) { }

    ngOnInit() {
        this.isAdmin = this.keycloak.hasRealmRole('ADMIN');
        this.loadUsers();
    }

    loadUsers() {
        this.userService.getAllUsers().subscribe(data => {
            // Option 2: Hide everyone with 'ADMIN' role from the team view
            this.users = data.filter(user => user.role !== 'ADMIN');
        });
    }

    openModal(user?: User) {
        if (user) {
            this.isEditing = true;
            this.currentUser = { ...user };
        } else {
            this.isEditing = false;
            this.currentUser = { nom: '', prenom: '', email: '', role: 'USER', dateNaissance: '' };
            this.password = '';
            this.selectedFileForCreation = null;
        }
        this.showModal = true;
    }

    closeModal() {
        this.showModal = false;
    }

    saveUser() {
        if (this.isEditing && this.currentUser.keycloakId) {
            this.userService.updateUser(this.currentUser.keycloakId, this.currentUser as User).subscribe(() => {
                this.loadUsers();
                this.closeModal();
            });
        } else {
            if (!this.password) {
                this.toastService.showError("Mot de passe obligatoire pour la création.");
                return;
            }
            this.userService.createUser(this.currentUser as User, this.password).subscribe({
                next: (createdUser) => {
                    this.toastService.showSuccess(`Utilisateur ${createdUser.prenom} créé avec succès !`);
                    if (this.selectedFileForCreation && createdUser.keycloakId) {
                        this.userService.uploadIdentityDoc(createdUser.keycloakId, this.selectedFileForCreation).subscribe({
                            next: () => {
                                this.toastService.showSuccess("Document d'identité uploadé avec succès !");
                                this.loadUsers();
                                this.closeModal();
                            },
                            error: (err) => {
                                this.toastService.showError("L'utilisateur a été créé, mais l'upload du document a échoué.");
                                console.error(err);
                                this.loadUsers();
                                this.closeModal();
                            }
                        });
                    } else {
                        this.loadUsers();
                        this.closeModal();
                    }
                },
                error: (err) => {
                    this.toastService.showError("Erreur lors de la création de l'utilisateur. Conflit d'email/username ?");
                    console.error(err);
                }
            });
        }
    }

    deleteUser(keycloakId: string) {
        if (confirm('Voulez-vous vraiment supprimer cet utilisateur ?')) {
            this.userService.deleteUser(keycloakId).subscribe(() => this.loadUsers());
        }
    }

    triggerFileInput(keycloakId: string) {
        this.selectedUserIdForUpload = keycloakId;
        this.fileInput.nativeElement.click();
    }

    onFileSelected(event: Event) {
        // [Existing file selection logic...]
        const input = event.target as HTMLInputElement;
        if (input.files && input.files.length > 0) {
            const file = input.files[0];
            if (this.showModal && !this.isEditing) {
                // Handling file select during new user creation
                this.selectedFileForCreation = file;
            } else if (this.selectedUserIdForUpload) {
                // Handling file select for existing user from table
                this.userService.uploadIdentityDoc(this.selectedUserIdForUpload, file).subscribe({
                    next: () => {
                        this.toastService.showSuccess("Document d'identité uploadé avec succès !");
                        this.loadUsers();
                        input.value = ''; // Reset file input
                    },
                    error: (err) => {
                        this.toastService.showError("Erreur lors de l'upload du document d'identité.");
                        console.error(err);
                    }
                });
            }
        }
    }

    getTodayDate(): string {
        return new Date().toISOString().split('T')[0];
    }

    getMinBirthDate(): string {
        const d = new Date();
        d.setFullYear(d.getFullYear() - 100);
        return d.toISOString().split('T')[0];
    }

    isBirthDateInvalid(): boolean {
        if (!this.currentUser.dateNaissance) return false;
        const birthDate = new Date(this.currentUser.dateNaissance);
        const today = new Date();
        const minDate = new Date();
        minDate.setFullYear(minDate.getFullYear() - 100);
        return birthDate > today || birthDate < minDate;
    }
}
