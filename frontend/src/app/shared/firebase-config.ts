// frontend/src/app/firebase-config.ts (sau khi chỉnh sửa)

// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
// import { getAnalytics, isSupported } from "firebase/analytics"; // <-- Loại bỏ import này nếu bạn xử lý Analytics ở app.config.ts
import { getAuth } from "firebase/auth";
import { getFirestore } from "firebase/firestore";
// import { PLATFORM_ID } from '@angular/core'; // <-- Loại bỏ import này
// import { isPlatformBrowser } from '@angular/common'; // <-- Loại bỏ import này
// import { inject } from '@angular/core'; // <-- Loại bỏ import này

// Your web app's Firebase configuration
export const firebaseConfig = {
  apiKey: "AIzaSyAYKIk3sqUjuo4BKiTiXHWxpNC-HU6sOt0",
  authDomain: "liennganh-e70a0.firebaseapp.com",
  projectId: "liennganh-e70a0",
  storageBucket: "liennganh-e70a0.firebasestorage.app",
  messagingSenderId: "911347553688",
  appId: "1:911347553688:web:14412cb4651f5e6b9fc7ed",
  measurementId: "G-3VTDZF257G"
};

// Initialize Firebase App
const app = initializeApp(firebaseConfig);

// Loại bỏ hoàn toàn phần khởi tạo Analytics ở đây
// let analytics;
// if (typeof window !== 'undefined') {
//   isSupported().then(supported => {
//     if (supported) {
//       analytics = getAnalytics(app);
//     }
//   });
// }


export const auth = getAuth(app);
export const db = getFirestore(app);