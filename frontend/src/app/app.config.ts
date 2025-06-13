// frontend/src/app/app.config.ts

import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZonelessChangeDetection, PLATFORM_ID, inject } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { initializeApp, provideFirebaseApp, FirebaseApp } from '@angular/fire/app'; // Thêm FirebaseApp để có thể inject instance
import { getAuth, provideAuth } from '@angular/fire/auth';
import { getFirestore, provideFirestore } from '@angular/fire/firestore';
import { getAnalytics, provideAnalytics, isSupported } from '@angular/fire/analytics';
import { isPlatformBrowser } from '@angular/common';

import { routes } from './app.routes'; // Import 'routes' từ './app.routes'
import { firebaseConfig } from './shared/firebase-config'; // Đảm bảo đường dẫn này đúng


export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    provideRouter(routes), // 'routes' đã được import
    provideClientHydration(withEventReplay()),

    // Bước 1: Khởi tạo Firebase App một lần duy nhất tại đây.
    // Đây là nơi `FirebaseApp` instance được tạo ra và có sẵn trong DI.
    provideFirebaseApp(() => initializeApp(firebaseConfig)),

    // Bước 2: Cung cấp Firebase Authentication.
    // getAuth() sẽ nhận FirebaseApp instance từ DI.
    provideAuth(() => {
      const app = inject(FirebaseApp); // Inject FirebaseApp instance đã được khởi tạo
      return getAuth(app);
    }),

    // Bước 3: Cung cấp Firebase Firestore.
    // getFirestore() sẽ nhận FirebaseApp instance từ DI.
    provideFirestore(() => {
      const app = inject(FirebaseApp); // Inject FirebaseApp instance đã được khởi tạo
      return getFirestore(app);
    }),

    // Bước 4: Cung cấp Firebase Analytics có điều kiện.
    // Factory function này có thể là async và trả về Promise<Analytics | null>.
    provideAnalytics(async () => {
      const platformId = inject(PLATFORM_ID);
      const app = inject(FirebaseApp); // Inject FirebaseApp instance đã có sẵn

      if (isPlatformBrowser(platformId)) {
        try {
          const supported = await isSupported(); // Đợi kết quả kiểm tra hỗ trợ Analytics
          if (supported) {
            console.log('Firebase Analytics: Supported and initialized.');
            return getAnalytics(app); // TRUYỀN app instance đã inject vào getAnalytics()
          } else {
            console.warn('Firebase Analytics: Not supported in this browser environment.');
            return null; // Trả về null nếu không được hỗ trợ
          }
        } catch (error) {
          console.error('Error initializing Firebase Analytics:', error);
          return null; // Xử lý lỗi trong quá trình khởi tạo
        }
      } else {
        console.log('Firebase Analytics: Skipping initialization on server (SSR).');
        return null; // Trả về null trực tiếp cho SSR
      }
    })
  ]
};