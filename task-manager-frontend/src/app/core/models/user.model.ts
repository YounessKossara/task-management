export interface User {
    keycloakId: string;
    nom: string;
    prenom: string;
    dateNaissance?: string;
    email: string;
    telephone?: string;
    identityDocUrl?: string;
    role?: string;
}
