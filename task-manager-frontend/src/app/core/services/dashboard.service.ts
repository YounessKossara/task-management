import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface DashboardConfiguration {
    id?: number;
    userId: string;
    theme: string;
    layoutPreferences: string;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
    private apiUrl = `${environment.apiUrl}/dashboard`;

    constructor(private http: HttpClient) { }

    getUserDashboardConfig(userId: string): Observable<DashboardConfiguration> {
        return this.http.get<DashboardConfiguration>(`${this.apiUrl}/${userId}`);
    }

    saveUserDashboardConfig(config: DashboardConfiguration): Observable<DashboardConfiguration> {
        return this.http.post<DashboardConfiguration>(this.apiUrl, config);
    }
}
