// frontend/src/app/admin/subjects-management/subjects-management.component.ts

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Firestore, collection, getDocs, addDoc, doc, updateDoc, deleteDoc } from '@angular/fire/firestore';
import { Auth, user } from '@angular/fire/auth';
import { take } from 'rxjs/operators';
import { SpinnerComponent } from '../../shared/spinner/spinner.component';

interface Subject {
  id?: string; // ID của document trong Firestore - không nên lưu vào document
  name: string;
  description: string;
  createdAt?: Date;
  createdBy?: string;
}

@Component({
  selector: 'app-subjects-management',
  templateUrl: './subjects-management.component.html',
  // styleUrls: ['./subjects-management.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    SpinnerComponent
  ]
})
export class SubjectsManagementComponent implements OnInit {
  subjects: Subject[] = [];
  subjectForm: FormGroup;
  isEditing = false;
  currentSubjectId: string | null = null;
  loading = true;
  currentUserUid: string | null = null;

  constructor(
    private fb: FormBuilder,
    private firestore: Firestore,
    private auth: Auth
  ) {
    this.subjectForm = this.fb.group({
      name: ['', Validators.required],
      description: ['', Validators.required],
    });
  }

  ngOnInit() {
    user(this.auth).pipe(take(1)).subscribe(firebaseUser => {
      if (firebaseUser) {
        this.currentUserUid = firebaseUser.uid;
        this.loadSubjects();
      } else {
        this.loading = false;
        console.warn('User not logged in. Cannot load subjects.');
      }
    });
  }

  async loadSubjects() {
    this.loading = true;
    try {
      const querySnapshot = await getDocs(collection(this.firestore, 'subjects'));
      this.subjects = querySnapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data() as Subject // Ép kiểu khi đọc để đảm bảo khớp interface
      }));
      console.log('Subjects loaded:', this.subjects);
    } catch (error) {
      console.error('Error loading subjects:', error);
      alert('Có lỗi khi tải môn học. Vui lòng thử lại.');
    } finally {
      this.loading = false;
    }
  }

  async onSubmit() {
    if (this.subjectForm.invalid) {
      alert('Vui lòng điền đầy đủ và chính xác thông tin môn học.');
      this.subjectForm.markAllAsTouched();
      return;
    }

    this.loading = true;

    try {
      const subjectData = this.subjectForm.value; // KHÔNG GÁN KIỂU Subject ở đây

      // Tách trường 'id' ra khỏi đối tượng nếu có, vì id là của Firestore
      const { id, ...dataToSave } = subjectData;

      if (this.isEditing && this.currentSubjectId) {
        const subjectDocRef = doc(this.firestore, 'subjects', this.currentSubjectId);
        await updateDoc(subjectDocRef, dataToSave); // Truyền đối tượng đã tách id
        alert('Môn học đã được cập nhật thành công!');
      } else {
        const newSubjectRef = collection(this.firestore, 'subjects');
        await addDoc(newSubjectRef, {
          ...dataToSave, // Truyền đối tượng đã tách id
          createdAt: new Date(),
          createdBy: this.currentUserUid
        });
        alert('Môn học đã được thêm thành công!');
      }

      this.subjectForm.reset();
      this.isEditing = false;
      this.currentSubjectId = null;
      this.loadSubjects();

    } catch (error) {
      console.error('Error saving subject:', error);
      alert('Có lỗi khi lưu môn học. Vui lòng thử lại.');
    } finally {
      this.loading = false;
    }
  }

  editSubject(subject: Subject) {
    this.isEditing = true;
    this.currentSubjectId = subject.id || null;
    this.subjectForm.patchValue({
      name: subject.name,
      description: subject.description
    });
  }

  cancelEdit() {
    this.isEditing = false;
    this.currentSubjectId = null;
    this.subjectForm.reset();
  }

  async deleteSubject(subjectId: string | undefined) {
    if (!subjectId) {
      alert('Không tìm thấy ID môn học để xóa.');
      return;
    }

    if (confirm('Bạn có chắc chắn muốn xóa môn học này?')) {
      this.loading = true;
      try {
        const subjectDocRef = doc(this.firestore, 'subjects', subjectId);
        await deleteDoc(subjectDocRef);
        alert('Môn học đã được xóa thành công!');
        this.loadSubjects();
      } catch (error) {
        console.error('Error deleting subject:', error);
        alert('Có lỗi khi xóa môn học. Vui lòng thử lại.');
      } finally {
        this.loading = false;
      }
    }
  }
}