import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Auth, user, signOut, User } from '@angular/fire/auth';
import { switchMap, of, take } from 'rxjs';
import { collection, doc, getDoc, Firestore } from '@angular/fire/firestore';

interface UserProfile {
  username: string;
  role: 'user' | 'admin';
}

@Component({
  selector: 'app-home',
  standalone: true,
  templateUrl: './home.html',
  styleUrls: ['./home.scss'],
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
})
export class HomeComponent implements OnInit {
  rfEmail: FormGroup;
  now = new Date().getFullYear();
  isLoggedIn = false;
  username: string | null = null;
  defaultUrl = '/dashboard'; // Default URL for logged-in users, will be updated based on role
  userRole: 'user' | 'admin' | null = null;

  constructor(
    private fb: FormBuilder,
    private auth: Auth,
    private router: Router,
    private firestore: Firestore // Inject Firestore
  ) {
    this.rfEmail = this.fb.group({
      subject: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      contentBody: ['', Validators.required],
    });
  }

  ngOnInit() {
    user(this.auth).pipe(
      switchMap(async (firebaseUser: User | null) => {
        if (firebaseUser) {
          this.isLoggedIn = true;
          this.username = firebaseUser.displayName || firebaseUser.email || 'Người dùng';

          // Load user role from Firestore
          const userDocRef = doc(this.firestore, 'users', firebaseUser.uid);
          const userDocSnap = await getDoc(userDocRef);

          if (userDocSnap.exists()) {
            const userProfile = userDocSnap.data() as UserProfile;
            this.userRole = userProfile.role;
            if (this.userRole === 'admin') {
              this.defaultUrl = '/admin'; // Redirect admin to admin dashboard
            } else {
              this.defaultUrl = '/user-dashboard'; // Example for user dashboard
            }
          } else {
            // Default role if not found in Firestore, or handle as needed
            this.userRole = 'user';
            this.defaultUrl = '/user-dashboard';
          }
        } else {
          this.isLoggedIn = false;
          this.username = null;
          this.userRole = null;
          this.defaultUrl = '/login'; // If not logged in, explore leads to login
        }
        return firebaseUser;
      }),
      take(1) // Take only the first emission to avoid re-running on subsequent auth state changes
    ).subscribe();
  }

  async logout() {
    try {
      await signOut(this.auth);
      this.isLoggedIn = false;
      this.username = null;
      this.userRole = null;
      this.defaultUrl = '/login';
      this.router.navigate(['/']); // Redirect to home after logout
    } catch (error) {
      console.error('Lỗi khi đăng xuất:', error);
    }
  }

  sendEmail() {
    if (this.rfEmail.valid) {
      const data = this.rfEmail.value;
      console.log('Gửi:', data);
      // TODO: Implement API call to send email or use Firebase Cloud Functions
      alert('Phản hồi của bạn đã được gửi!');
      this.rfEmail.reset();
    }
  }

  get subject() {
    return this.rfEmail.get('subject');
  }

  get email() {
    return this.rfEmail.get('email');
  }

  get contentBody() {
    return this.rfEmail.get('contentBody');
  }
}