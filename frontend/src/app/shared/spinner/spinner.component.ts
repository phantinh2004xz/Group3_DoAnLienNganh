// frontend/src/app/shared/spinner/spinner.component.ts

import { Component } from '@angular/core';
import { CommonModule } from '@angular/common'; // Cần CommonModule cho các directive Angular cơ bản

@Component({
  selector: 'app-spinner', // Selector để sử dụng component này trong HTML
  standalone: true,        // Khai báo là một standalone component
  imports: [CommonModule], // Import CommonModule cho các tính năng như *ngIf, *ngFor
  template: `
    <div class="flex items-center justify-center">
      <div class="loader ease-linear rounded-full border-4 border-t-4 border-gray-200 h-10 w-10"></div>
    </div>
  `, // Template HTML inline cho spinner
  styles: `
    .loader {
      border-top-color: #3498db; /* Màu của vòng quay spinner */
      -webkit-animation: spin 1s linear infinite; /* Animation cho WebKit browsers */
      animation: spin 1s linear infinite; /* Animation chuẩn */
    }

    @-webkit-keyframes spin {
      0% { -webkit-transform: rotate(0deg); }
      100% { -webkit-transform: rotate(360deg); }
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
  `, // Styles CSS inline cho spinner
})
export class SpinnerComponent {
  // Không cần logic đặc biệt cho spinner này, chỉ là một biểu tượng tải
}
