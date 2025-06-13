import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; // Cần cho ngModel trong select role
import { Firestore, collection, getDocs, doc, updateDoc, deleteDoc } from '@angular/fire/firestore';
import { SpinnerComponent } from '../../shared/spinner/spinner.component';
// Lưu ý: Xóa người dùng khỏi Firebase Auth cần quyền admin và thường được thực hiện ở backend (Cloud Functions)
// import { Auth, deleteUser } from '@angular/fire/auth'; // Nếu muốn xóa user từ frontend (không khuyến nghị)

interface AppUser {
  uid: string;
  email: string;
  username: string;
  role: 'user' | 'admin';
  // Thêm các trường khác nếu có, ví dụ: createdAt, lastLoginAt
}

@Component({
  selector: 'app-users-management',
  templateUrl: './users-management.component.html',
  // styleUrls: ['./users-management.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    SpinnerComponent
  ]
})
export class UsersManagementComponent implements OnInit {
  users: AppUser[] = [];
  loading = true;

  constructor(private firestore: Firestore) {} // Không cần Auth nếu không xóa user

  ngOnInit() {
    this.loadUsers();
  }

  async loadUsers() {
    this.loading = true;
    try {
      const querySnapshot = await getDocs(collection(this.firestore, 'users'));
      this.users = querySnapshot.docs.map(doc => ({
        uid: doc.id,
        ...doc.data()
      })) as AppUser[];
      console.log('Users loaded in UsersManagementComponent:', this.users);
    } catch (error) {
      console.error('Error loading users in UsersManagementComponent:', error);
      alert('Có lỗi khi tải danh sách người dùng. Vui lòng thử lại.');
    } finally {
      this.loading = false;
    }
  }

  async updateUserRole(userToUpdate: AppUser, newRole: 'user' | 'admin') {
    // Ngăn admin tự hạ cấp chính mình (tùy chọn)
    // if (userToUpdate.uid === this.currentUserUid && newRole === 'user') {
    //   alert('Bạn không thể tự hạ cấp vai trò của chính mình.');
    //   return;
    // }

    if (confirm(`Bạn có chắc chắn muốn thay đổi vai trò của ${userToUpdate.username} thành ${newRole}?`)) {
      this.loading = true;
      try {
        const userDocRef = doc(this.firestore, 'users', userToUpdate.uid);
        await updateDoc(userDocRef, { role: newRole });
        userToUpdate.role = newRole; // Cập nhật trạng thái cục bộ
        alert(`Đã cập nhật vai trò của ${userToUpdate.username} thành ${newRole}.`);
      } catch (error) {
        console.error('Error updating user role:', error);
        alert('Không thể cập nhật vai trò. Vui lòng kiểm tra quyền hạn và kết nối.');
      } finally {
        this.loading = false;
      }
    }
  }

  // Chức năng xóa người dùng. RẤT CẨN THẬN khi triển khai trên frontend.
  // Thường nên xóa từ backend (Firebase Cloud Functions) để đảm bảo xóa cả Firebase Auth và Firestore.
  // async deleteUser(userToDelete: AppUser) {
  //   if (confirm(`Bạn có chắc chắn muốn xóa người dùng ${userToDelete.username} không?`)) {
  //     this.loading = true;
  //     try {
  //       // Bước 1: Xóa tài liệu Firestore
  //       const userDocRef = doc(this.firestore, 'users', userToDelete.uid);
  //       await deleteDoc(userDocRef);

  //       // Bước 2: Xóa người dùng khỏi Firebase Authentication (CHỈ KHI ĐANG ĐĂNG NHẬP VỚI USER ĐÓ)
  //       // Hoặc sử dụng Firebase Cloud Function để xóa từ Admin SDK.
  //       // const authUser = this.auth.currentUser;
  //       // if (authUser && authUser.uid === userToDelete.uid) {
  //       //   await deleteUser(authUser); // Chỉ có thể xóa chính tài khoản đang đăng nhập
  //       // } else {
  //       //   // Đây là kịch bản cần Cloud Function
  //       //   alert('Để xóa người dùng khác, vui lòng sử dụng Admin SDK qua Cloud Functions.');
  //       //   this.loading = false;
  //       //   return;
  //       // }

  //       alert(`Người dùng ${userToDelete.username} đã được xóa.`);
  //       this.loadUsers();
  //     } catch (error) {
  //       console.error('Error deleting user:', error);
  //       alert('Lỗi khi xóa người dùng. Vui lòng kiểm tra quyền hạn hoặc thử lại sau.');
  //     } finally {
  //       this.loading = false;
  //     }
  //   }
  // }
}