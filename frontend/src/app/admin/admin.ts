import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { NavSidebarComponent } from './nav-sidebar/nav-sidebar';
import { NavbarComponent } from './navbar/navbar';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    NavSidebarComponent,
    NavbarComponent,
  ],
  templateUrl: './admin.html',
  styleUrl: './admin.scss',
})
export class Admin {
  // Admin dashboard data
  dashboardData = {
    totalUsers: 0,
    totalTests: 0,
    activeUsers: 0,
  };

  constructor() {
    // TODO: Load dashboard data
    this.loadDashboardData();
  }

  private loadDashboardData() {
    // TODO: Implement API call to get dashboard data
    console.log('Loading dashboard data...');
  }
}
