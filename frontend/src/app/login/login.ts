import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import {
  FormsModule,
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { SpinnerComponent } from '../shared/spinner/spinner';

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
  openTab = 1;
  isLoggedIn = false;
  isLoginFailed = false;
  errorMessage = '';
  preLoading = false;

  loginForm: FormGroup;

  constructor(private fb: FormBuilder) {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
    });
  }

  get email() {
    return this.loginForm.get('email');
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      console.log('Login form submitted', this.loginForm.value);
    }
  }
}
