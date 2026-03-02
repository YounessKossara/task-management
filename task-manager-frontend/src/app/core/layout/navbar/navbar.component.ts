import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import Keycloak from 'keycloak-js';

@Component({
    selector: 'app-navbar',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
    username = '';
    private keycloak = inject(Keycloak);

    async ngOnInit() {
        if (this.keycloak.authenticated) {
            const profile = await this.keycloak.loadUserProfile();
            this.username = profile.username || 'Utilisateur';
        }
    }

    logout() {
        this.keycloak.logout({ redirectUri: window.location.origin });
    }
}
