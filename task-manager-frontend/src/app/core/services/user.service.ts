import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class UserService {
    private apiUrl = `${environment.apiUrl}/users`;

    constructor(private http: HttpClient) { }

    getAllUsers(): Observable<User[]> {
        return this.http.get<User[]>(this.apiUrl);
    }

    getUserById(keycloakId: string): Observable<User> {
        return this.http.get<User>(`${this.apiUrl}/${keycloakId}`);
    }

    createUser(user: User, password?: string): Observable<User> {
        let url = this.apiUrl;
        if (password) {
            url += `?password=${password}`;
        }
        return this.http.post<User>(url, user);
    }

    updateUser(keycloakId: string, user: User): Observable<User> {
        return this.http.put<User>(`${this.apiUrl}/${keycloakId}`, user);
    }

    deleteUser(keycloakId: string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${keycloakId}`);
    }

    uploadIdentityDoc(keycloakId: string, file: File): Observable<string> {
        const formData = new FormData();
        formData.append('file', file);
        return this.http.post(`${this.apiUrl}/${keycloakId}/identity-doc`, formData, { responseType: 'text' });
    }
}
