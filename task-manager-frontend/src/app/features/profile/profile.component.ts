import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService } from '../../core/services/user.service';
import { User } from '../../core/models/user.model';
import Keycloak from 'keycloak-js';

@Component({
    selector: 'app-profile',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
    user: User | null = null;
    isLoading = true;
    errorMsg = '';

    private keycloak = inject(Keycloak);

    constructor(private userService: UserService) { }

    ngOnInit(): void {
        const userId = this.keycloak.tokenParsed?.sub;
        if (userId) {
            this.userService.getUserById(userId).subscribe({
                next: (data: User) => {
                    this.user = data;
                    this.isLoading = false;
                },
                error: (err: any) => {
                    console.error('Erreur lors du chargement du profil', err);
                    this.errorMsg = 'Impossible de charger vos informations de profil.';
                    this.isLoading = false;
                }
            });
        } else {
            this.errorMsg = "Utilisateur non identifié.";
            this.isLoading = false;
        }
    }
}
