import { Component } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  standalone: true,
  templateUrl: './home.html',
  styleUrls: ['./home.scss'],
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
})
export class HomeComponent {
  rfEmail: FormGroup;
  now = new Date().getFullYear();
  isLoggedIn = false; // 👈 Thêm biến này
  username = 'Người dùng'; // 👈 Thêm biến này
  defaultUrl = '/dashboard'; // 👈 Thêm biến này

  constructor(private fb: FormBuilder) {
    this.rfEmail = this.fb.group({
      subject: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      contentBody: ['', Validators.required],
    });
  }

  sendEmail() {
    if (this.rfEmail.valid) {
      const data = this.rfEmail.value;
      console.log('Gửi:', data);
      // TODO: gọi API nếu có
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
