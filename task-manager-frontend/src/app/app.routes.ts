import { Routes } from '@angular/router';
import { MainLayoutComponent } from './core/layout/main-layout.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { ProjectListComponent } from './features/projects/project-list/project-list.component';
import { TaskBoardComponent } from './features/tasks/task-board/task-board.component';
import { UserManagementComponent } from './features/users/user-management/user-management.component';
import { ProfileComponent } from './features/profile/profile.component';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
    {
        path: '',
        component: MainLayoutComponent,
        children: [
            { path: 'dashboard', component: DashboardComponent },
            { path: 'projects', component: ProjectListComponent },
            { path: 'projects/:id/tasks', component: TaskBoardComponent },
            { path: 'users', component: UserManagementComponent, canActivate: [adminGuard] },
            { path: 'profile', component: ProfileComponent },
            { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
        ]
    },
    { path: '**', redirectTo: '' }
];
