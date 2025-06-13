// frontend/src/app/app.config.ts

import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZonelessChangeDetection, PLATFORM_ID, inject } from '@angular/core'; // Thêm 'inject'
import { provideRouter } from '@angular/router';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { initializeApp, provideFirebaseApp } from '@angular/fire/app';
import { getAuth, provideAuth } from '@angular/fire/auth';
import { getFirestore, provideFirestore } from '@angular/fire/firestore';
import { getAnalytics, provideAnalytics, isSupported } from '@angular/fire/analytics';
import { isPlatformBrowser } from '@angular/common';

import { routes } from './app.routes'; // Import 'routes' từ './app.routes'
import { firebaseConfig } from './shared/firebase-config'; // Đảm bảo đường dẫn này đúng nếu bạn đã di chuyển tệp firebase-config.ts


export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    provideRouter(routes), // 'routes' đã được import
    provideClientHydration(withEventReplay()),
    provideFirebaseApp(() => initializeApp(firebaseConfig)),
    provideAuth(() => getAuth()),
    provideFirestore(() => getFirestore()),
    // Cung cấp Firebase Analytics có điều kiện
    provideAnalytics(async () => {
      const platformId = inject(PLATFORM_ID);
      // Chỉ khởi tạo Analytics nếu đang chạy trên trình duyệt VÀ được hỗ trợ.
      // Chúng ta trả về Promise<Analytics> hoặc Promise<null> để Angular xử lý bất đồng bộ.
      // isSupported() cũng trả về Promise.
      if (isPlatformBrowser(platformId)) {
        const supported = await isSupported();
        if (supported) {
          console.log('Firebase Analytics: Supported and initialized.');
          // Lưu ý: initializeApp(firebaseConfig) cần được gọi một lần duy nhất
          // Nếu bạn đã gọi nó ở provideFirebaseApp, hãy đảm bảo không lặp lại ở đây.
          // getAnalytics() cần một FirebaseApp instance.
          // Để tránh lỗi "Firebase: No Firebase App '[DEFAULT]' has been created"
          // chúng ta sẽ inject FirebaseApp thay vì initializeApp lại.
          const app = initializeApp(firebaseConfig); // Hoặc inject FirebaseApp instance đã có
          return getAnalytics(app);
        } else {
          console.warn('Firebase Analytics: Not supported in this browser environment.');
          return null; // Trả về null nếu không được hỗ trợ
        }
      } else {
        console.log('Firebase Analytics: Skipping initialization on server (SSR).');
        return Promise.resolve(null); // Trả về Promise.resolve(null) cho SSR
      }
    })
  ]
};