// frontend/src/app/admin/Dashboard/admin.ts
import { Component, OnInit } from '@angular/core';
import { RouterModule, Router } from '@angular/router'; // Import Router
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { NavSidebarComponent } from '../nav-sidebar/nav-sidebar';
import { NavbarComponent } from '../navbar/navbar';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    NavSidebarComponent, // Vẫn giữ lại vì chúng được dùng trong template
    NavbarComponent,     // Vẫn giữ lại vì chúng được dùng trong template
  ],
  templateUrl: './admin.html',
  styleUrl: './admin.scss',
})
export class Admin implements OnInit {
  dashboardData = {
    totalUsers: 0,
    totalTests: 0,
    activeUsers: 0,
    totalQuestions: 0,
    totalSubmissions: 0,
  };

  constructor(public router: Router) {} // Inject Router

  ngOnInit() {
    this.loadDashboardData();
  }

  private loadDashboardData() {
    console.log('Loading dashboard data for Dashboard Overview...');
    this.dashboardData = {
      totalUsers: 124,
      totalTests: 11,
      activeUsers: 0,
      totalQuestions: 104,
      totalSubmissions: 113,
    };
  }
}