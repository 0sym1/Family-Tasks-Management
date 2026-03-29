# Family Task App - Project Context for AI & Vibe Coding

## 1. Tổng quan dự án (Project Overview)
- **Tên dự án:** Family Task App
- **Mục tiêu:** Quản lý công việc gia đình (tạo, giao, theo dõi task).
- **Ngôn ngữ:** Java.
- **Backend:** Firebase (Authentication, Firestore, Storage).
- **Tính năng đặc biệt:** Hỗ trợ đa ngôn ngữ (Vietnamese & English).

## 2. Kiến trúc & Tech Stack (Architecture)
- **Kiến trúc:** MVVM (Model-View-ViewModel).
- **UI:** Material Design 3, ConstraintLayout, Navigation Component.
- **Data:** ViewModel & LiveData để quản lý State và UI updates.
- **Dependencies:** Firebase SDK, Glide (ảnh), Navigation Component (Safe Args).

## 3. Quy tắc cốt yếu (Crucial Rules)

### A. Tuyệt đối không Hardcode Text (Localization)
- **Quy tắc:** Mọi chuỗi ký tự hiển thị lên UI (Label, Hint, Toast, Dialog, Error Message) **KHÔNG** được viết trực tiếp trong code Java hay XML.
- **Thực hiện:** 
  - Sử dụng `@string/name` trong XML.
  - Sử dụng `context.getString(R.string.name)` trong Fragment/Activity.
  - Các file Resource: `res/values/strings.xml` (Tiếng Việt) và `res/values-en/strings.xml` (Tiếng Anh).

### B. Luồng dữ liệu MVVM
1. **Repository:** Nơi duy nhất tương tác với Firebase. 
   - Không chứa `Context`.
   - Trả về kết quả qua `MutableLiveData`.
   - Đối với thông báo lỗi: Trả về `Exception` hoặc một mã lỗi/Resource ID để View tự xử lý ngôn ngữ.
2. **ViewModel:** Gọi Repository, xử lý logic nghiệp vụ, giữ dữ liệu cho UI.
3. **View (Fragment/Activity):** Chỉ làm nhiệm vụ hiển thị và lắng nghe (Observe) LiveData. Không viết logic xử lý dữ liệu tại đây.

### C. Cấu trúc thư mục (Package Structure)
- `data.model`: Các thực thể dữ liệu (POJO).
- `data.repository`: Xử lý Firebase (Ví dụ: `AuthRepository`, `TaskRepository`).
- `ui.[feature]`: Chia theo tính năng (auth, task, history, notification). Mỗi thư mục chứa Activity/Fragment và ViewModel.
- `utils`: Các helper class (`Constants`, `DateTimeUtils`).

### D. Xử lý lỗi (Error Handling)
- Khi Firebase trả về lỗi, hãy map lỗi đó sang các Key String định nghĩa sẵn trong `strings.xml`.
- Ví dụ: Thay vì trả về "Email đã tồn tại", hãy đảm bảo code có thể gọi đến `R.string.error_email_exists`.

## 4. Hướng dẫn cho AI khi code
- Khi tạo UI mới: Luôn tạo kèm các entry tương ứng trong `strings.xml`.
- Khi viết Repository: Tham khảo `AuthRepository.java` nhưng hãy cải tiến để không fix cứng String tiếng Việt trong hàm `mapError`.
- Khi viết XML: Luôn dùng `style` và `dimen` để đảm bảo tính nhất quán.
- Luôn ưu tiên dùng View Binding.

## 5. Firestore Schema
- `users`: { uid, name, email, avatar_url, language, family_id }
- `tasks`: { task_id, title, description, assigned_to, created_by, status, deadline, created_at }
- `families`: { family_id, family_name, owner_id, members[] }
