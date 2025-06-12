import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule],
  template: `
    <nav class="bg-white shadow-lg">
      <div class="max-w-7xl mx-auto px-4">
        <div class="flex justify-between h-16">
          <div class="flex">
            <div class="flex-shrink-0 flex items-center">
              <h2 class="text-xl font-bold text-gray-800">Admin Dashboard</h2>
            </div>
          </div>
          <div class="flex items-center">
            <div class="ml-3 relative">
              <div class="flex items-center">
                <span class="text-gray-700 mr-4">Admin User</span>
                <img
                  class="h-8 w-8 rounded-full"
                  src="https://ui-avatars.com/api/?name=Admin&background=random"
                  alt="User avatar"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </nav>
  `,
  styles: [],
})
export class NavbarComponent {}
