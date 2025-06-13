// frontend/src/app/user/user-dashboard/user-dashboard.component.ts

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router'; // Import Router
import { Auth, signOut, user, User } from '@angular/fire/auth'; // Import Auth, signOut, user
import { Firestore, doc, getDoc } from '@angular/fire/firestore'; // Import Firestore, doc, getDoc
import { take, switchMap } from 'rxjs/operators'; // Import operators


// Định nghĩa giao diện cho dữ liệu hồ sơ người dùng
interface UserProfile {
  username: string;
  email: string;
  role: 'user' | 'admin';
  // Thêm các trường khác nếu có trong tài liệu người dùng của bạn
}

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './user-dashboard.component.html',
  styleUrl: './user-dashboard.component.scss',
})
export class UserDashboardComponent implements OnInit {
  // Biến để lưu trữ thông tin người dùng hiển thị trên dashboard
  userDisplayName: string | null = null;
  userEmail: string | null = null;
  userRole: 'user' | 'admin' | null = null;

  // Dữ liệu giả định cho dashboard (thay thế bằng dữ liệu thực từ Firestore/API)
  progressPercentage: number = 75;
  testsCompleted: number = 15;
  totalTests: number = 20;
  averageScore: number = 8.5;

  testHistory = [
    { name: 'Cơ sở dữ liệu SQL', score: 9.0, date: new Date('2025-06-01'), status: 'Hoàn thành' },
    { name: 'Giới thiệu về AI', score: 7.5, date: new Date('2025-05-28'), status: 'Cần cải thiện' },
    { name: 'Lập trình Web cơ bản', score: 8.8, date: new Date('2025-05-20'), status: 'Hoàn thành' },
    { name: 'Kỹ năng giao tiếp', score: 6.2, date: new Date('2025-05-15'), status: 'Cần cải thiện' },
  ];

  constructor(
    private auth: Auth, // Inject Auth để xử lý đăng xuất và lấy thông tin người dùng
    private firestore: Firestore, // Inject Firestore để lấy dữ liệu profile
    private router: Router // Inject Router để điều hướng
  ) {}

  ngOnInit(): void {
    // Lắng nghe trạng thái đăng nhập của người dùng và tải dữ liệu profile
    user(this.auth).pipe(
      switchMap(async (firebaseUser: User | null) => {
        if (firebaseUser) {
          this.userDisplayName = firebaseUser.displayName || firebaseUser.email;
          this.userEmail = firebaseUser.email;

          // Tải vai trò người dùng từ Firestore
          const userDocRef = doc(this.firestore, 'users', firebaseUser.uid);
          const userDocSnap = await getDoc(userDocRef);

          if (userDocSnap.exists()) {
            const userProfile = userDocSnap.data() as UserProfile;
            this.userRole = userProfile.role;
            // Cập nhật tên hiển thị nếu có trong Firestore và không có trong Firebase Auth
            this.userDisplayName = userProfile.username || this.userDisplayName;
          } else {
            console.warn('User profile not found in Firestore for UID:', firebaseUser.uid);
            this.userRole = 'user'; // Mặc định là user nếu không tìm thấy profile
          }
        } else {
          // Nếu không đăng nhập, chuyển hướng về trang đăng nhập
          this.router.navigate(['/login']);
        }
        return firebaseUser;
      }),
      take(1) // Chỉ lấy lần phát ra đầu tiên để tránh tải lại liên tục
    ).subscribe();
  }

  // Hàm xử lý đăng xuất người dùng
  async logout() {
    try {
      await signOut(this.auth);
      console.log('User logged out successfully.');
      this.router.navigate(['/login']); // Điều hướng về trang đăng nhập sau khi đăng xuất
    } catch (error) {
      console.error('Error logging out:', error);
      alert('Đăng xuất thất bại. Vui lòng thử lại.');
    }
  }

  // Phương thức để định dạng ngày tháng hiển thị
  formatDate(date: Date): string {
    return date.toLocaleDateString('vi-VN'); // Định dạng theo tiếng Việt
  }
}
