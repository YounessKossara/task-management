import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import Keycloak from 'keycloak-js';

export const adminGuard: CanActivateFn = () => {
    const keycloak = inject(Keycloak);
    const router = inject(Router);

    if (keycloak.hasRealmRole('ADMIN')) {
        return true;
    }

    router.navigate(['/dashboard']);
    return false;
};
