import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import Keycloak from 'keycloak-js';
import { environment } from '../../environments/environment';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const keycloak = inject(Keycloak);

    // Only add token for requests to our API
    if (req.url.startsWith(environment.apiUrl)) {
        const token = keycloak.token;
        if (token) {
            const cloned = req.clone({
                setHeaders: {
                    Authorization: `Bearer ${token}`
                }
            });
            return next(cloned);
        }
    }

    return next(req);
};
