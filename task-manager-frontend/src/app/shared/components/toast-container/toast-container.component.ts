import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService, ToastMessage } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container position-fixed bottom-0 end-0 p-3" style="z-index: 1055;">
      <div *ngFor="let toast of toasts" 
           class="toast align-items-center text-white border-0 show mb-2 shadow" 
           [class.bg-success]="toast.type === 'success'"
           [class.bg-danger]="toast.type === 'error'"
           [class.bg-info]="toast.type === 'info'"
           role="alert" aria-live="assertive" aria-atomic="true">
        <div class="d-flex">
          <div class="toast-body fw-medium d-flex align-items-center">
            <i class="bi me-2 fs-5" 
               [ngClass]="{
                 'bi-check-circle-fill': toast.type === 'success',
                 'bi-exclamation-triangle-fill': toast.type === 'error',
                 'bi-info-circle-fill': toast.type === 'info'
               }"></i>
            {{ toast.message }}
          </div>
          <button type="button" class="btn-close btn-close-white me-2 m-auto" (click)="remove(toast.id!)" aria-label="Close"></button>
        </div>
      </div>
    </div>
  `
})
export class ToastContainerComponent {
  private toastService = inject(ToastService);
  toasts: ToastMessage[] = [];

  ngOnInit() {
    this.toastService.toasts$.subscribe((toasts: ToastMessage[]) => {
      this.toasts = toasts;
    });
  }

  remove(id: number) {
    this.toastService.remove(id);
  }
}
