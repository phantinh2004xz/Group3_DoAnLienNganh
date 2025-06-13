import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import {
  FormsModule,
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { SpinnerComponent } from '../shared/spinner/spinner'; // Đảm bảo đường dẫn đúng

import {
  Auth,
  createUserWithEmailAndPassword,
  signInWithEmailAndPassword,
  updateProfile,
  sendPasswordResetEmail, // Thêm để xử lý quên mật khẩu
} from '@angular/fire/auth';
import { doc, setDoc, getDoc, Firestore } from '@angular/fire/firestore'; // Thêm getDoc

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    ReactiveFormsModule,
    SpinnerComponent,
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  openTab = 1; // 1 for login, 2 for register, 3 for forgot password
  isLoggedIn = false; // Firebase auth state will manage this
  isLoginFailed = false;
  errorMessage = '';
  preLoading = false;

  loginForm: FormGroup;
  registerForm: FormGroup;
  forgotPasswordForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private auth: Auth,
    private firestore: Firestore,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
    });

    this.registerForm = this.fb.group({
      username: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
    }, { validators: this.passwordMatchValidator });

    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
    });
  }

  passwordMatchValidator(form: FormGroup) {
    return form.get('password')?.value === form.get('confirmPassword')?.value
      ? null : { 'mismatch': true };
  }

  get loginEmail() { return this.loginForm.get('email'); }
  get loginPassword() { return this.loginForm.get('password'); }
  get registerUsername() { return this.registerForm.get('username'); }
  get registerEmail() { return this.registerForm.get('email'); }
  get registerPassword() { return this.registerForm.get('password'); }
  get registerConfirmPassword() { return this.registerForm.get('confirmPassword'); }
  get forgotPasswordEmail() { return this.forgotPasswordForm.get('email'); }

  async onLoginSubmit(): Promise<void> {
    this.preLoading = true;
    this.isLoginFailed = false;
    this.errorMessage = '';

    if (this.loginForm.invalid) {
      this.preLoading = false;
      return;
    }

    try {
      const { email, password } = this.loginForm.value;
      const userCredential = await signInWithEmailAndPassword(this.auth, email, password);
      const user = userCredential.user;

      // Get user role from Firestore
      const userDocRef = doc(this.firestore, 'users', user.uid);
      const userDocSnap = await getDoc(userDocRef);

      let role: 'user' | 'admin' = 'user'; // Default role if not found (shouldn't happen with proper registration)
      if (userDocSnap.exists()) {
        role = userDocSnap.data()['role'] || 'user';
      } else {
        // If user document doesn't exist, create one with default role
        await setDoc(userDocRef, { role: 'user', username: user.displayName || user.email });
      }

      this.isLoggedIn = true;
      console.log('Đăng nhập thành công! Vai trò:', role);

      // Redirect based on role
      if (role === 'admin') {
        this.router.navigate(['/admin']);
      } else {
        this.router.navigate(['/']); // Redirect to home or user dashboard
      }

    } catch (error: any) {
      this.isLoginFailed = true;
      this.errorMessage = this.getFirebaseErrorMessage(error.code);
      console.error('Lỗi đăng nhập:', error);
    } finally {
      this.preLoading = false;
    }
  }

  async onRegisterSubmit(): Promise<void> {
    this.preLoading = true;
    this.isLoginFailed = false;
    this.errorMessage = '';

    if (this.registerForm.invalid) {
      this.preLoading = false;
      return;
    }

    try {
      const { email, password, username } = this.registerForm.value;
      const userCredential = await createUserWithEmailAndPassword(this.auth, email, password);
      const user = userCredential.user;

      // Update user profile with display name in Firebase Auth
      await updateProfile(user, { displayName: username });

      // Store user role in Firestore (default to 'user' for new registrations)
      await setDoc(doc(this.firestore, 'users', user.uid), {
        email: user.email,
        username: username,
        role: 'user', // Default role for new users
        createdAt: new Date() // Add creation timestamp (optional)
      });

      this.isLoggedIn = true;
      console.log('Đăng ký thành công!');
      this.router.navigate(['/']); // Redirect to home or user dashboard after registration
    } catch (error: any) {
      this.isLoginFailed = true;
      this.errorMessage = this.getFirebaseErrorMessage(error.code);
      console.error('Lỗi đăng ký:', error);
    } finally {
      this.preLoading = false;
    }
  }

  async onForgotPasswordSubmit(): Promise<void> {
    this.preLoading = true;
    this.isLoginFailed = false;
    this.errorMessage = '';

    if (this.forgotPasswordForm.invalid) {
      this.preLoading = false;
      return;
    }

    try {
      const { email } = this.forgotPasswordForm.value;
      await sendPasswordResetEmail(this.auth, email); // Firebase method to send reset email
      console.log('Gửi yêu cầu khôi phục mật khẩu đến:', email);
      alert('Nếu email tồn tại, một liên kết đặt lại mật khẩu đã được gửi đến hộp thư của bạn. Vui lòng kiểm tra email của bạn.');
      this.openTab = 1; // Go back to login tab
    } catch (error: any) {
      this.isLoginFailed = true;
      this.errorMessage = this.getFirebaseErrorMessage(error.code);
      console.error('Lỗi khôi phục mật khẩu:', error);
    } finally {
      this.preLoading = false;
    }
  }

  private getFirebaseErrorMessage(code: string): string {
    switch (code) {
      case 'auth/invalid-email': return 'Địa chỉ email không hợp lệ.';
      case 'auth/user-disabled': return 'Tài khoản này đã bị vô hiệu hóa.';
      case 'auth/user-not-found': return 'Không tìm thấy người dùng với email này.';
      case 'auth/wrong-password': return 'Mật khẩu không đúng.';
      case 'auth/email-already-in-use': return 'Địa chỉ email đã được sử dụng bởi một tài khoản khác.';
      case 'auth/weak-password': return 'Mật khẩu quá yếu (cần ít nhất 6 ký tự).';
      case 'auth/invalid-credential': return 'Thông tin đăng nhập không hợp lệ.';
      case 'auth/missing-email': return 'Vui lòng nhập địa chỉ email.';
      case 'auth/operation-not-allowed': return 'Thao tác không được phép. Vui lòng liên hệ quản trị viên.';
      default: return 'Đã xảy ra lỗi. Vui lòng thử lại sau.';
    }
  }
}