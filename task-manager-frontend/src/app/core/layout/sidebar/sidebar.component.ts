import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import Keycloak from 'keycloak-js';

@Component({
    selector: 'app-sidebar',
    standalone: true,
    imports: [RouterLink, RouterLinkActive, CommonModule],
    templateUrl: './sidebar.component.html',
    styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
    private keycloak = inject(Keycloak);

    isUserOnly(): boolean {
        // Un simple utilisateur n'a ni le rôle ADMIN ni le rôle RESPONSABLE.
        return !this.keycloak.hasRealmRole('ADMIN') && !this.keycloak.hasRealmRole('RESPONSABLE');
    }

    isAdmin(): boolean {
        return this.keycloak.hasRealmRole('ADMIN');
    }

    logout() {
        this.keycloak.logout({ redirectUri: window.location.origin });
    }
}
