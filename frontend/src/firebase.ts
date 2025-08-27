import { initializeApp } from 'firebase/app'
import { getAuth, GoogleAuthProvider, setPersistence, browserLocalPersistence } from 'firebase/auth'

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY as string,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN as string,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID as string,
  appId: import.meta.env.VITE_FIREBASE_APP_ID as string,
}

export const app = initializeApp(firebaseConfig)
export const auth = getAuth(app)
// Ensure auth persists across tabs/sessions so re-login flow is consistent
setPersistence(auth, browserLocalPersistence).catch(() => { /* ignore */ })
export const googleProvider = new GoogleAuthProvider()
