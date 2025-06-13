// frontend/src/app/admin/tests-management/tests-management.component.ts

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Firestore, collection, getDocs, addDoc, doc, updateDoc, deleteDoc } from '@angular/fire/firestore';
import { Auth, user } from '@angular/fire/auth';
import { take } from 'rxjs/operators';
import { SpinnerComponent } from '../../shared/spinner/spinner.component';

interface Test {
  id?: string; // ID của document trong Firestore - không nên lưu vào document
  title: string;
  description: string;
  subjectId: string;
  questionIds: string[];
  durationMinutes: number;
  passScore: number;
  createdAt?: Date;
  createdBy?: string;
}

interface SubjectOption {
  id: string;
  name: string;
}

@Component({
  selector: 'app-tests-management',
  templateUrl: './tests-management.component.html',
  // styleUrls: ['./tests-management.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    SpinnerComponent
  ]
})
export class TestsManagementComponent implements OnInit {
  tests: Test[] = [];
  testForm: FormGroup;
  isEditing = false;
  currentTestId: string | null = null;
  loading = true;
  currentUserUid: string | null = null;
  subjects: SubjectOption[] = [];

  constructor(
    private fb: FormBuilder,
    private firestore: Firestore,
    private auth: Auth
  ) {
    this.testForm = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      subjectId: ['', Validators.required],
      questionIds: [[], Validators.required],
      durationMinutes: [30, [Validators.required, Validators.min(1)]],
      passScore: [50, [Validators.required, Validators.min(0), Validators.max(100)]],
    });
  }

  getSubjectName(subjectId: string): string {
    const subject = this.subjects?.find(s => s.id === subjectId);
    return subject ? subject.name : subjectId;
  }

  ngOnInit() {
    user(this.auth).pipe(take(1)).subscribe(firebaseUser => {
      if (firebaseUser) {
        this.currentUserUid = firebaseUser.uid;
        this.loadSubjectsForDropdown();
        this.loadTests();
      } else {
        this.loading = false;
        console.warn('User not logged in. Cannot load tests.');
      }
    });
  }

  async loadSubjectsForDropdown() {
    try {
      const querySnapshot = await getDocs(collection(this.firestore, 'subjects'));
      this.subjects = querySnapshot.docs.map(doc => ({
        id: doc.id,
        name: doc.data()['name']
      })) as SubjectOption[];
    } catch (error) {
      console.error('Error loading subjects for dropdown:', error);
    }
  }

  async loadTests() {
    this.loading = true;
    try {
      const querySnapshot = await getDocs(collection(this.firestore, 'tests'));
      this.tests = querySnapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data() as Test // Ép kiểu khi đọc để đảm bảo khớp interface
      }));
      console.log('Tests loaded:', this.tests);
    } catch (error) {
      console.error('Error loading tests:', error);
      alert('Có lỗi khi tải bài test. Vui lòng thử lại.');
    } finally {
      this.loading = false;
    }
  }

  async onSubmit() {
    if (this.testForm.invalid) {
      alert('Vui lòng điền đầy đủ và chính xác thông tin bài test.');
      this.testForm.markAllAsTouched();
      return;
    }

    this.loading = true;

    try {
      const testData = { // KHÔNG GÁN KIỂU Test ở đây
        ...this.testForm.value,
        questionIds: this.testForm.value.questionIds ? this.testForm.value.questionIds.split(',').map((id: string) => id.trim()) : []
      };

      // Tách trường 'id' ra khỏi đối tượng nếu có, vì id là của Firestore
      const { id, ...dataToSave } = testData;

      if (this.isEditing && this.currentTestId) {
        const testDocRef = doc(this.firestore, 'tests', this.currentTestId);
        await updateDoc(testDocRef, dataToSave); // Truyền đối tượng đã tách id
        alert('Bài test đã được cập nhật thành công!');
      } else {
        const newTestRef = collection(this.firestore, 'tests');
        await addDoc(newTestRef, {
          ...dataToSave, // Truyền đối tượng đã tách id
          createdAt: new Date(),
          createdBy: this.currentUserUid
        });
        alert('Bài test đã được thêm thành công!');
      }

      this.testForm.reset();
      this.isEditing = false;
      this.currentTestId = null;
      this.loadTests();

    } catch (error) {
      console.error('Error saving test:', error);
      alert('Có lỗi khi lưu bài test. Vui lòng thử lại.');
    } finally {
      this.loading = false;
    }
  }

  editTest(test: Test) {
    this.isEditing = true;
    this.currentTestId = test.id || null;
    this.testForm.patchValue({
      title: test.title,
      description: test.description,
      subjectId: test.subjectId,
      questionIds: test.questionIds.join(', '),
      durationMinutes: test.durationMinutes,
      passScore: test.passScore
    });
  }

  cancelEdit() {
    this.isEditing = false;
    this.currentTestId = null;
    this.testForm.reset();
  }

  async deleteTest(testId: string | undefined) {
    if (!testId) {
      alert('Không tìm thấy ID bài test để xóa.');
      return;
    }

    if (confirm('Bạn có chắc chắn muốn xóa bài test này?')) {
      this.loading = true;
      try {
        const testDocRef = doc(this.firestore, 'tests', testId);
        await deleteDoc(testDocRef);
        alert('Bài test đã được xóa thành công!');
        this.loadTests();
      } catch (error) {
        console.error('Error deleting test:', error);
        alert('Có lỗi khi xóa bài test. Vui lòng thử lại.');
      } finally {
        this.loading = false;
      }
    }
  }
}