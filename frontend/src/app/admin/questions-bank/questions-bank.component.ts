// frontend/src/app/admin/questions-bank/questions-bank.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormArray } from '@angular/forms';
import { Firestore, collection, getDocs, addDoc, doc, updateDoc, deleteDoc } from '@angular/fire/firestore';
import { Auth, user } from '@angular/fire/auth';
import { take } from 'rxjs/operators';
import { SpinnerComponent } from '../../shared/spinner/spinner.component';

// Định nghĩa giao diện cho một câu hỏi
interface Question {
  id?: string; // ID của document trong Firestore - không nên lưu vào document
  text: string;
  options: string[];
  correctAnswerIndex: number;
  subjectId: string;
  level: 'easy' | 'medium' | 'hard';
  createdAt?: Date;
  createdBy?: string;
}

@Component({
  selector: 'app-questions-bank',
  templateUrl: './questions-bank.component.html',
  // styleUrls: ['./questions-bank.component.css'], // Bỏ dòng này nếu không có file css
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    SpinnerComponent
  ]
})
export class QuestionsBankComponent implements OnInit {
  questions: Question[] = [];
  questionForm: FormGroup;
  isEditing = false;
  currentQuestionId: string | null = null;
  loading = true;
  currentUserUid: string | null = null;

  constructor(
    private fb: FormBuilder,
    private firestore: Firestore,
    private auth: Auth
  ) {
    this.questionForm = this.fb.group({
      text: ['', Validators.required],
      options: this.fb.array([
        this.fb.control('', Validators.required),
        this.fb.control('', Validators.required),
        this.fb.control('', Validators.required),
        this.fb.control('', Validators.required)
      ]),
      correctAnswerIndex: [null, Validators.required],
      subjectId: ['', Validators.required],
      level: ['easy', Validators.required]
    });
  }

  ngOnInit() {
    user(this.auth).pipe(take(1)).subscribe(firebaseUser => {
      if (firebaseUser) {
        this.currentUserUid = firebaseUser.uid;
        this.loadQuestions();
      } else {
        this.loading = false;
        console.warn('User not logged in. Cannot load questions.');
      }
    });
  }

  get options(): FormArray {
    return this.questionForm.get('options') as FormArray;
  }

  addOption() {
    this.options.push(this.fb.control('', Validators.required));
  }

  removeOption(index: number) {
    if (this.options.length > 2) {
      this.options.removeAt(index);
      if (this.questionForm.get('correctAnswerIndex')?.value === index) {
        this.questionForm.get('correctAnswerIndex')?.setValue(null);
      } else if (this.questionForm.get('correctAnswerIndex')?.value > index) {
        this.questionForm.get('correctAnswerIndex')?.setValue(this.questionForm.get('correctAnswerIndex')?.value - 1);
      }
    } else {
      alert('Phải có ít nhất 2 lựa chọn.');
    }
  }

  async loadQuestions() {
    this.loading = true;
    try {
      const querySnapshot = await getDocs(collection(this.firestore, 'questions'));
      this.questions = querySnapshot.docs.map(doc => ({
        id: doc.id,
        // Dữ liệu từ Firestore có thể có các trường không có trong interface Question
        // Hoặc các trường Date cần được chuyển đổi.
        // Cân nhắc sử dụng doc.data() as Question nếu bạn chắc chắn cấu trúc khớp 100%,
        // nếu không, hãy map các trường thủ công hoặc sử dụng kiểu 'any' khi đọc.
        ...doc.data() as Question // Ép kiểu khi đọc
      }));
      console.log('Questions loaded:', this.questions);
    } catch (error) {
      console.error('Error loading questions:', error);
      alert('Có lỗi khi tải câu hỏi. Vui lòng thử lại.');
    } finally {
      this.loading = false;
    }
  }

  async onSubmit() {
    if (this.questionForm.invalid) {
      alert('Vui lòng điền đầy đủ và chính xác thông tin câu hỏi.');
      this.questionForm.markAllAsTouched();
      return;
    }

    this.loading = true;

    try {
      const questionData = { // Không cần khai báo kiểu Question ở đây để tránh lỗi
        ...this.questionForm.value,
        correctAnswerIndex: parseInt(this.questionForm.value.correctAnswerIndex, 10),
        options: this.questionForm.value.options.filter((opt: string) => opt.trim() !== '')
      };

      // Đảm bảo không có trường 'id' khi gửi lên Firestore
      const { id, ...dataToSave } = questionData; // Tách 'id' ra nếu có

      if (this.isEditing && this.currentQuestionId) {
        const questionDocRef = doc(this.firestore, 'questions', this.currentQuestionId);
        await updateDoc(questionDocRef, dataToSave); // Truyền đối tượng đã tách id
        alert('Câu hỏi đã được cập nhật thành công!');
      } else {
        const newQuestionRef = collection(this.firestore, 'questions');
        await addDoc(newQuestionRef, {
          ...dataToSave, // Truyền đối tượng đã tách id
          createdAt: new Date(),
          createdBy: this.currentUserUid
        });
        alert('Câu hỏi đã được thêm thành công!');
      }

      this.questionForm.reset();
      this.questionForm.setControl('options', this.fb.array([
        this.fb.control('', Validators.required),
        this.fb.control('', Validators.required),
        this.fb.control('', Validators.required),
        this.fb.control('', Validators.required)
      ]));
      this.isEditing = false;
      this.currentQuestionId = null;
      this.loadQuestions();

    } catch (error) {
      console.error('Error saving question:', error);
      alert('Có lỗi khi lưu câu hỏi. Vui lòng thử lại.');
    } finally {
      this.loading = false;
    }
  }

  editQuestion(question: Question) {
    this.isEditing = true;
    this.currentQuestionId = question.id || null;

    const optionsArray = this.fb.array([]);
    question.options.forEach(option => {
      optionsArray.push(this.fb.control(option, Validators.required));
    });
    this.questionForm.setControl('options', optionsArray);

    this.questionForm.patchValue({
      text: question.text,
      correctAnswerIndex: question.correctAnswerIndex.toString(),
      subjectId: question.subjectId,
      level: question.level
    });
  }

  cancelEdit() {
    this.isEditing = false;
    this.currentQuestionId = null;
    this.questionForm.reset();
    this.questionForm.setControl('options', this.fb.array([
      this.fb.control('', Validators.required),
      this.fb.control('', Validators.required),
      this.fb.control('', Validators.required),
      this.fb.control('', Validators.required)
    ]));
  }

  async deleteQuestion(questionId: string | undefined) {
    if (!questionId) {
      alert('Không tìm thấy ID câu hỏi để xóa.');
      return;
    }

    if (confirm('Bạn có chắc chắn muốn xóa câu hỏi này?')) {
      this.loading = true;
      try {
        const questionDocRef = doc(this.firestore, 'questions', questionId);
        await deleteDoc(questionDocRef);
        alert('Câu hỏi đã được xóa thành công!');
        this.loadQuestions();
      } catch (error) {
        console.error('Error deleting question:', error);
        alert('Có lỗi khi xóa câu hỏi. Vui lòng thử lại.');
      } finally {
        this.loading = false;
      }
    }
  }
}