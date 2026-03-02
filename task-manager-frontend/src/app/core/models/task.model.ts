export enum TaskStatus {
    TODO = 'TODO',
    IN_PROGRESS = 'IN_PROGRESS',
    DONE = 'DONE'
}

export enum TaskPriority {
    LOW = 'LOW',
    MEDIUM = 'MEDIUM',
    HIGH = 'HIGH'
}

export interface Task {
    id?: number;
    titre: string;
    description?: string;
    statut: TaskStatus;
    priorite?: TaskPriority;
    projectId: number;
    assigneeKeycloakId?: string;
}
