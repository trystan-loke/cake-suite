import { auth } from '../firebase';
import { 
  updatePassword, 
  reauthenticateWithCredential, 
  EmailAuthProvider,
  User 
} from 'firebase/auth';

export async function changePassword(
  currentPassword: string,
  newPassword: string
): Promise<void> {
  const user = auth.currentUser;
  
  if (!user || !user.email) {
    throw new Error('User not authenticated');
  }
  
  const credential = EmailAuthProvider.credential(user.email, currentPassword);
  await reauthenticateWithCredential(user, credential);
  
  await updatePassword(user, newPassword);
}