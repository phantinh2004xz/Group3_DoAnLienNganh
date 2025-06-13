import { Routes } from '@angular/router';
import { HomeComponent } from './home/home';
import { Login } from './login/login';
import { Admin } from './admin/admin';
import { canActivate, redirectLoggedInTo, redirectUnauthorizedTo } from '@angular/fire/auth-guard';

// Constants for redirection
const redirectToLogin = () => redirectUnauthorizedTo(['login']);
const redirectToHome = () => redirectLoggedInTo(['']);
const redirectToAdmin = () => redirectUnauthorizedTo(['login']); // Admin specific, could be another page

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: Login, ...canActivate(redirectToHome) }, // Redirect logged-in users from login page
  { path: 'admin', component: Admin, ...canActivate(redirectToAdmin) }, // Protect admin route
  { path: '**', redirectTo: '' }, // Wildcard route for a 404 page or redirect to home
];