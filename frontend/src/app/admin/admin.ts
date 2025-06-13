import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { NavSidebarComponent } from './nav-sidebar/nav-sidebar'; // Đảm bảo đường dẫn đúng
import { NavbarComponent } from './navbar/navbar'; // Đảm bảo đường dẫn đúng
import { Firestore, collection, getDocs, doc, updateDoc } from '@angular/fire/firestore'; // Thêm updateDoc

interface AppUser {
  uid: string;
  email: string;
  username: string;
  role: 'user' | 'admin';
}

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
export class Admin implements OnInit {
  dashboardData = {
    totalUsers: 0,
    totalTests: 0,
    activeUsers: 0,
  };
  users: AppUser[] = []; // New property to store users

  constructor(private firestore: Firestore) {}

  ngOnInit() {
    this.loadDashboardData();
    this.loadUsers(); // Load users when component initializes
  }

  private loadDashboardData() {
    // TODO: Implement actual API call or Firestore query to get dashboard data
    console.log('Loading dashboard data...');
    // Example dummy data
    this.dashboardData = {
      totalUsers: 100,
      totalTests: 50,
      activeUsers: 20,
    };
  }

  async loadUsers() {
    try {
      const querySnapshot = await getDocs(collection(this.firestore, 'users'));
      this.users = querySnapshot.docs.map(doc => ({
        uid: doc.id,
        ...doc.data()
      })) as AppUser[];
      console.log('Users loaded:', this.users);
    } catch (error) {
      console.error('Error loading users:', error);
    }
  }

  async updateUserRole(user: AppUser, newRole: 'user' | 'admin') {
    try {
      const userDocRef = doc(this.firestore, 'users', user.uid);
      await updateDoc(userDocRef, { role: newRole });
      user.role = newRole; // Update local state immediately for UI refresh
      console.log(`User ${user.username} role updated to ${newRole}`);
      alert(`Đã cập nhật vai trò của ${user.username} thành ${newRole}.`);
    } catch (error) {
      console.error('Error updating user role:', error);
      alert('Không thể cập nhật vai trò. Vui lòng kiểm tra quyền hạn của bạn.');
    }
  }
}