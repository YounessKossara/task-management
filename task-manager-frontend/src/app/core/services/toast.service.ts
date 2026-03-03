import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface ToastMessage {
    message: string;
    type: 'success' | 'error' | 'info';
    id?: number;
}

@Injectable({
    providedIn: 'root'
})
export class ToastService {
    private toastsSubject = new BehaviorSubject<ToastMessage[]>([]);
    toasts$ = this.toastsSubject.asObservable();
    private idCounter = 0;

    show(message: string, type: 'success' | 'error' | 'info' = 'info') {
        const newToast: ToastMessage = { message, type, id: this.idCounter++ };
        const currentToasts = this.toastsSubject.value;

        this.toastsSubject.next([...currentToasts, newToast]);

        // Auto remove after 3 seconds
        setTimeout(() => {
            this.remove(newToast.id!);
        }, 3000);
    }

    showSuccess(message: string) {
        this.show(message, 'success');
    }

    showError(message: string) {
        this.show(message, 'error');
    }

    remove(id: number) {
        const currentToasts = this.toastsSubject.value;
        this.toastsSubject.next(currentToasts.filter(t => t.id !== id));
    }
}
