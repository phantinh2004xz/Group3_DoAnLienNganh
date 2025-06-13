import { Routes } from '@angular/router';
import { HomeComponent } from './home/home';
import { Login } from './login/login';
// Admin Dashboard (layout)
import { Admin } from './admin/Dashboard/admin';
// Các component quản trị khác
import { UsersManagementComponent } from './admin/users-management/users-management.component';
import { QuestionsBankComponent } from './admin/questions-bank/questions-bank.component';
import { SubjectsManagementComponent } from './admin/subjects-management/subjects-management.component';
import { TestsManagementComponent } from './admin/tests-management/tests-management.component';
// User
import { UserDashboardComponent } from './user/user-dashboard/user-dashboard.component';

// Auth guard từ AngularFire
import { canActivate, redirectLoggedInTo, redirectUnauthorizedTo } from '@angular/fire/auth-guard';

// Nếu đã đăng nhập -> về home; chưa đăng nhập -> về login
const redirectToLogin = () => redirectUnauthorizedTo(['login']);
const redirectToHome = () => redirectLoggedInTo(['']);

// Định nghĩa routes
export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: Login, ...canActivate(redirectToHome) },

  {
    path: 'admin',
    component: Admin,
    ...canActivate(redirectToLogin), // Chỉ cần kiểm tra đăng nhập
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: Admin },
      { path: 'users', component: UsersManagementComponent },
      { path: 'questions', component: QuestionsBankComponent },
      { path: 'subjects', component: SubjectsManagementComponent },
      { path: 'tests', component: TestsManagementComponent },
      // Có thể thêm các route con khác ở đây
    ]
  },

  { path: 'user-dashboard', component: UserDashboardComponent, ...canActivate(redirectToLogin) },

  { path: '**', redirectTo: '' }
];
