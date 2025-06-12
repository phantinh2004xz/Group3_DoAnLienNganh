import { Routes } from '@angular/router';
import { HomeComponent } from './home/home';
import { Login } from './login/login';
import { Admin } from './admin/admin';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: Login },
  { path: 'admin', component: Admin },
  { path: '**', redirectTo: '' },
];
